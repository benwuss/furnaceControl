package com.jl.hl.furnace;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 此为非线程安全的FIFO队列，故意的，为了效率。
 * 
 * @author benwu
 * 
 * @param <T>
 */
public class FIFO<T> {

	private int limit = 10;

	private Object o = new Object();

	private Queue<T> queue = new LinkedList<T>();

	public FIFO(int limit) {
		this.limit = limit;
	}

	public T addLast(T addLast) {
		T head = null;

		head = null;
		while (queue.size() >= limit) {
			head = queue.poll();
		}
		queue.offer(addLast);

		return head;

	}

	public T addLastSafe(T addLast) {
		T head = null;
		synchronized (o) {
			head = null;
			while (queue.size() >= limit) {
				head = queue.poll();
			}
			queue.offer(addLast);

			return head;
		}
	}

	public Queue<T> getFIFOQueue() {
		return queue;
	}

	public ArrayList<T> getFIFOByASC() {
		ArrayList<T> l = new ArrayList<T>(limit);
		for (T v : queue) {
			l.add(v);
		}
		return l;
	}

	public T getMostRecentOne() {
		if (queue.size() > 0) {
			return getFIFOByDSC().get(0);
		} else {
			return null;
		}
	}

	public ArrayList<T> getFIFOByDSC() {
		ArrayList<T> l = new ArrayList<T>(limit);
		for (T v : queue) {
			l.add(0, v);
		}
		return l;
	}

	public T poll() {
		return queue.poll();
	}

	public T pollSafe() {
		synchronized (o) {
			return queue.poll();
		}
	}
}
