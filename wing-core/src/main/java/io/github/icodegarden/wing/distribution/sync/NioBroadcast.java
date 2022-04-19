package io.github.icodegarden.wing.distribution.sync;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.java.ClientNioSelector;
import io.github.icodegarden.commons.nio.java.JavaNioClient;
import io.github.icodegarden.commons.nio.java.JavaNioServer;
import io.github.icodegarden.wing.common.EnvException;
import io.github.icodegarden.wing.common.SyncFailedException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NioBroadcast extends AbstractDistributionSyncStrategy {

	private static final Logger log = LoggerFactory.getLogger(NioBroadcast.class);
	
	private final ScheduledThreadPoolExecutor scheduleClearOfflineThreadPool = ThreadPoolUtils
			.newSingleScheduledThreadPool("Schedule-Clear-Offline");

	private static final String IP = SystemUtils.getIp();/* 对外网络ip */
	private final ClientNioSelector clientNioSelector;
	private final int serverPort;

	private final JavaNioServer javaNioServer;
	private final Supplier<List<Discovery>> instancesSupplier;
	private ScheduledFuture<?> scheduleClearOffline;
	private ConcurrentHashMap<Integer/* hash */, NioClient> nioClients = new ConcurrentHashMap<Integer, NioClient>();

	public NioBroadcast(int serverPort, Supplier<List<Discovery>> discoverysSupplier) {
		clientNioSelector = ClientNioSelector.openNew(NioBroadcast.class.getSimpleName() + "-Client");

		this.instancesSupplier = discoverysSupplier;

		this.serverPort = serverPort;
		InetSocketAddress bind = new InetSocketAddress(IP, this.serverPort);

		this.javaNioServer = new JavaNioServer("NioNotice-Server", bind, new MessageHandler() {
			@Override
			public Object reply(Object obj) {
				return null;
			}

			@Override
			public void receive(Object obj) {
				DistributionSyncDTO distributionSyncDTO = (DistributionSyncDTO) obj;
				receiveSync(distributionSyncDTO);
			}
		});

		try {
			javaNioServer.start();
		} catch (IOException e) {
			throw new EnvException("ex on start Nio Server", e);
		}

		/**
		 * schedule clear NioClient that discovery was offline
		 */
		this.scheduleClearOffline = scheduleClearOfflineThreadPool.scheduleAtFixedRate(() -> {
			try {
				Set<Integer> hashes = this.instancesSupplier.get().stream()
						.filter(discovery -> discovery.getApplicationName().equals(distributionSyncCacher.getApplicationName()))
						.map(Discovery::hashCode).collect(Collectors.toSet());
				for (Entry<Integer, NioClient> entry : nioClients.entrySet()) {
					if (!hashes.contains(entry.getKey())) {
						NioClient remove = nioClients.remove(entry.getKey());
						if (remove != null) {
							try {
								remove.close();
							} catch (IOException e) {
								log.error("ex on close that discovery was offline Nio Client:{}", remove, e);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("ex on schedule clear NioClient that discovery was offline", e);
			}
		}, 60, 60, TimeUnit.SECONDS);
	}

	private boolean isLocal(String address, int port) {
		return IP.equals(address) && port == serverPort;
	}

	@Override
	protected void broadcast(DistributionSyncDTO message) throws SyncFailedException {
		/**
		 * 只发送给相同服务名的，并且不包含本实例
		 */
		List<Discovery> discoverys = instancesSupplier.get();
		discoverys.parallelStream()
				.filter(discovery -> discovery.getApplicationName().equals(message.getApplicationName())
						&& !isLocal(discovery.getAddress(), discovery.getPort()))
				.forEach(discovery -> {
					NioClient nioClient = nioClients.get(discovery.hashCode());
					if (nioClient == null) {
						InetSocketAddress bind = new InetSocketAddress(discovery.getAddress(), discovery.getPort());
						nioClient = new JavaNioClient(bind, clientNioSelector);
						try {
							nioClient.connect();
							NioClient pre = nioClients.put(discovery.hashCode(), nioClient);
							if (pre != null) {
								pre.close();
							}
						} catch (IOException e) {
							log.error("ex on create Nio Client", e);
							return;// 注意continue
						}
					}
					try {
						//TODO 可以考虑加入失败重试机制，使用request，响应boolean
						nioClient.send(message);
					} catch (Exception e) {
						log.error("ex on notice message to Nio Client:{}", nioClient, e);
					}
				});
	}

	@Override
	public void close() throws IOException {
		if (javaNioServer != null) {
			javaNioServer.close();
		}
		if (clientNioSelector != null) {
			clientNioSelector.close();
		}
		if (scheduleClearOffline != null) {
			scheduleClearOffline.cancel(true);
		}
		for (Entry<Integer, NioClient> entry : nioClients.entrySet()) {
			NioClient nioClient = entry.getValue();
			try {
				nioClient.close();
			} catch (IOException e) {
				log.error("ex on close Nio Client:{}", nioClient, e);
			}
		}
		
		scheduleClearOfflineThreadPool.setRemoveOnCancelPolicy(true);
		scheduleClearOfflineThreadPool.shutdown();
	}

}
