package io.github.icodegarden.wing.distribution.sync;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.wing.common.EnvException;
import io.github.icodegarden.wing.common.SyncFailedException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SuppressWarnings("rawtypes")
public class KafkaBroadcast extends AbstractDistributionSyncStrategy {

	private static final Logger log = LoggerFactory.getLogger(KafkaBroadcast.class);

	private static final String TOPIC = "io.cahce.sync";

	private static final KryoSerializer KRYO_SERIALIZER = new KryoSerializer();
	private static final KryoDeserializer KRYO_DESERIALIZER = new KryoDeserializer();

	private KafkaProducer<String, byte[]> producer;
	private KafkaConsumer<String, byte[]> consumer;

	private boolean closed = false;
	private final String bootstrapServers;

	public KafkaBroadcast(String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}
	
	@Override
	public boolean injectCacher(DistributionSyncCacher distributionSyncCacher) {
		boolean inject = super.injectCacher(distributionSyncCacher);
		if (inject) {
			Properties properties = new Properties();
			properties.put("bootstrap.servers", bootstrapServers);

			producer = buildProducer(properties);
			consumer = buildConsumer(properties);

			subBroadcast();
		}
		return inject;
	}

	private void subBroadcast() throws EnvException {
		consumer.subscribe(Arrays.asList(TOPIC));

		new Thread(this.getClass().getSimpleName() + "-subscribe") {
			public void run() {
				try {
					while (!closed) {
						ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));
						for (ConsumerRecord<String, byte[]> record : records) {
							try {
								byte[] message = record.value();
								DistributionSyncDTO distributionSyncDTO = (DistributionSyncDTO) KRYO_DESERIALIZER
										.deserialize(message);
								receiveSync(distributionSyncDTO);
							} catch (Exception e) {
								log.error("ex on handle cache sync from kafka", e);
							}
						}
					}
				} finally {
					consumer.close(Duration.ofMillis(3000));
				}
			};
		}.start();
	}

	@Override
	protected void broadcast(DistributionSyncDTO message) throws SyncFailedException {
		byte[] bytes = KRYO_SERIALIZER.serialize(message);

		ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(TOPIC, bytes);
		producer.send(record);
	}

	@Override
	public void close() throws IOException {
		closed = true;
		producer.close(Duration.ofMillis(3000));
	}

	private KafkaProducer<String, byte[]> buildProducer(Properties properties) {
		Properties props = new Properties();

		props.put("acks", "1");// Type: stringDefault: 1Valid Values: [all, -1, 0, 1]Importance: high
		props.put("retries", 2);// 重试
		props.put("max.request.size", 1000012);
		props.put("delivery.timeout.ms", 3000);
		props.put("linger.ms", 5);
		props.put("request.timeout.ms", 2500);
		props.put("compression.type", "none");

		props.putAll(properties);

		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

		return new KafkaProducer<String, byte[]>(props);
	}

	private KafkaConsumer<String, byte[]> buildConsumer(Properties properties) {
		Properties props = new Properties();

		props.put("fetch.min.bytes", 1);// 实时
		props.put("fetch.max.bytes", 52428800);// 默认
		props.put("fetch.max.wait.ms", 500);
		props.put("max.poll.interval.ms", 600000);// 加倍
		props.put("max.poll.records", 100);
		props.put("heartbeat.interval.ms", 3000);//
		props.put("session.timeout.ms", 10000);//
		props.put("max.partition.fetch.bytes", 1048576);// 默认，broker限制单个大小
		props.put("partition.assignment.strategy", "org.apache.kafka.clients.consumer.RoundRobinAssignor");
		props.put("auto.offset.reset", "latest");
		props.put("connections.max.idle.ms", Integer.MAX_VALUE);// 加大

		props.putAll(properties);

		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		props.put("group.id", distributionSyncCacher.getApplicationName() + "-" + UUID.randomUUID().toString().hashCode());
		props.put("enable.auto.commit", true);

		return new KafkaConsumer<String, byte[]>(props);
	}
}
