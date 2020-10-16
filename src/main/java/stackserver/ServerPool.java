package stackserver;

import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ServerPool {
	
	private ExecutorService es;
	private int poolSize;
	LinkedHashMap<Socket, Long> connectionQueue;
	protected ReentrantLock commonDsLock;
	private ScheduledExecutorService connectionMonitor;
	
	private Runnable connectionMonitorTask = () -> {
		
	};
	
	public ServerPool(int poolSize, ReentrantLock cdsLock) {
		this.poolSize = poolSize;
		this.es = Executors.newFixedThreadPool(poolSize);
		this.connectionQueue = new LinkedHashMap<>();
		this.commonDsLock = cdsLock;
		this.connectionMonitor = Executors.newSingleThreadScheduledExecutor();
		connectionMonitor.scheduleAtFixedRate(connectionMonitorTask, 5, 5, TimeUnit.SECONDS);
	}
	
	public LinkedHashMap<Socket, Long> connectionQueue() {
		return connectionQueue;
	}
	
	public boolean submit(ConnectionHandler ch) {
		commonDsLock.lock();
		try {
			if(connectionQueue.size() == poolSize) {
				Socket key = connectionQueue.keySet().iterator().next();
				if(connectionQueue.get(key) + 10000L < System.currentTimeMillis()) {
					ConnectionHandler.abortHandler(key);
				} else {
					return false;
				}
			}
			connectionQueue.put(ch.socket, System.currentTimeMillis());
			es.submit(ch);
			return true;
		} finally {
			commonDsLock.unlock();
		}
	}
	
	public void drain() {
		commonDsLock.lock();
		Iterator<Socket> it = connectionQueue.keySet().iterator();
		while(it.hasNext()) {
			ConnectionHandler.abortHandler(it.next());
		}
		connectionQueue.clear();
		commonDsLock.unlock();
	}
}
