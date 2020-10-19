package stackserver.datasource;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LifoDataSource implements DataSource {

	private int capacity;
	private ReentrantLock stackLock;
	private LinkedList<byte[]> stack;
	private Condition stackFull;
	private Condition stackEmpty;

	static Logger logger = Logger.getLogger(LifoDataSource.class.getName());

	public LifoDataSource(int cap) {
		this.capacity = cap;
		stack = new LinkedList<>();
		stackLock = new ReentrantLock(true);
		stackFull = stackLock.newCondition();
		stackEmpty = stackLock.newCondition();
	}

	public byte[] addItem(byte[] item) {
		if (item == null || item.length == 0)
			return new byte[]{0};
		stackLock.lock();
		while (stack.size() == capacity) {
			try {
				stackFull.await();
			} catch (InterruptedException e) {
				logger.info(Thread.currentThread() + " interrupted");
				stackLock.unlock();
				return null;
			}
		}
		stack.addLast(item);
		stackEmpty.signal();
		stackLock.unlock();
		logger.info("Adding item to dataSource:"
				+ new String(item, StandardCharsets.US_ASCII));
		byte[] res = new byte[]{0};
		return res;
	}

	public byte[] remove() {
		stackLock.lock();
		while (stack.isEmpty()) {
			try {
				stackEmpty.await();
			} catch (InterruptedException e) {
				logger.info(Thread.currentThread() + " interrupted");
				stackLock.unlock();
				return null;
			}
		}
		byte[] item = stack.removeLast();
		stackFull.signal();
		stackLock.unlock();
		logger.info("Removing item from dataSource:"
				+ new String(item, StandardCharsets.US_ASCII));
		return item;
	}
}
