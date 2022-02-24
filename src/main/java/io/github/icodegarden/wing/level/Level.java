package io.github.icodegarden.wing.level;

import java.util.List;

import io.github.icodegarden.wing.Cacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class Level {

	private Cacher cacher;
	private Level pre;
	private Level next;

	private Level(Cacher cacher) {
		this.cacher = cacher;
	}

	public static Level of(List<Cacher> ordered) {
		Level L1 = null;
		Level pre = null;
		for (int i = 0; i < ordered.size(); i++) {
			Level level = new Level(ordered.get(i));
			level.setPre(pre);

			if (pre != null) {
				pre.setNext(level);
			}
			pre = level;

			if (L1 == null) {
				L1 = level;
			}
		}
		return L1;
	}

	public Cacher getCacher() {
		return cacher;
	}

	public Level getPre() {
		return pre;
	}

	public Level getNext() {
		return next;
	}

	private void setNext(Level next) {
		this.next = next;
	}

	private void setPre(Level pre) {
		this.pre = pre;
	}

	@Override
	public String toString() {
		return "Level [cacher=" + cacher + ", next=" + next + "]";
	}

}
