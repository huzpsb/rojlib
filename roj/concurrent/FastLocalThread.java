package roj.concurrent;

import roj.util.EmptyArrays;

/**
 * @author Roj233
 * @since 2021/9/13 12:49
 */
public class FastLocalThread extends Thread {
	public FastLocalThread() {
		super();
	}

	public FastLocalThread(Runnable r) {
		super(r);
	}

	public FastLocalThread(ThreadGroup tg, String name) {
		super(tg, name);
	}

	Object[] localDataArray = EmptyArrays.OBJECTS;
	final Object arrayLock = new Object();
}
