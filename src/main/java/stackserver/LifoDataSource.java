package stackserver;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LifoDataSource implements DataSource {
	
	int capacity;
	ReentrantLock stackLock;
	LinkedList<byte[]> stack;
	Condition stackFull;
	Condition stackEmpty;
	
	static Logger logger = Logger.getLogger(LifoDataSource.class.getName());
	
	public LifoDataSource(int cap) {
		this.capacity = cap;
		stack = new LinkedList<>();
		stackLock = new ReentrantLock();
		stackFull = stackLock.newCondition();
		stackEmpty = stackLock.newCondition();
	}
	
	public byte[] addItem(byte[] item) {
		stackLock.lock();
		if(stack.size() == capacity) {
			try {
				stackFull.await();
			} catch (InterruptedException e) {
				logger.info(Thread.currentThread() + " interrupted");
				//e.printStackTrace();
				stackLock.unlock();
				return null;
			}
		}
		stack.addLast(item);
		stackEmpty.signal();
		stackLock.unlock();
		System.out.println("Adding item to dataSource:" + new String(item, StandardCharsets.UTF_8));
		byte[] res = new byte[] {0};
		return res;
	}
	
	public byte[] remove() {
		stackLock.lock();
		if(stack.isEmpty()) {
			try {
				stackEmpty.await();
			} catch (InterruptedException e) {
				logger.info(Thread.currentThread() + " interrupted");
				//e.printStackTrace();
				stackLock.unlock();
				return null;
			}
		}
		byte[] item = stack.removeLast();
		stackFull.signal();
		stackLock.unlock();
		System.out.println("Removing item from dataSource:" + new String(item, StandardCharsets.UTF_8));
		return item;
	}
}
