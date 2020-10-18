package stackserver;

import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ServerPool {
	
	protected Service service;
	protected ExecutorService es;
	protected int poolSize;
	protected ReentrantLock synchronizer;
	protected LinkedHashMap<SocketChannel, Long> connectionQueue;
	protected Map<SocketChannel, Thread> socketMap;
	
	private static Logger logger = Logger.getLogger(ServerPool.class.getName());
	
	public ServerPool(int poolSize, Service service) {
		this.service = service;
		this.poolSize = poolSize;
		this.es = Executors.newFixedThreadPool(poolSize);
		this.connectionQueue = new LinkedHashMap<>();
		this.synchronizer = new ReentrantLock(true);
		this.socketMap = new ConcurrentHashMap<>();
	}
	
	public void abortHandler(SocketChannel socket) {
		if(socketMap.containsKey(socket)) {
			Thread handler = socketMap.get(socket);
			if(handler != null) handler.interrupt();
		}
	}
	
	public boolean submit(Connection connection) {
		synchronizer.lock();
		try {
			System.out.println("Connection queue size:" + connectionQueue.size());
			if(connectionQueue.size() == poolSize) {
				SocketChannel key = connectionQueue.keySet().iterator().next();
				if(connectionQueue.get(key) + 10000L < System.currentTimeMillis()) {
					abortHandler(key);
				} else {
					return false;
				}
			}
			connectionQueue.put(connection.channel, System.currentTimeMillis());
			es.submit(new ConnectionHandler(connection, service, synchronizer, connectionQueue, socketMap));
			return true;
		} finally {
			synchronizer.unlock();
		}
	}
	
	public void drain() {
		synchronizer.lock();
		Iterator<SocketChannel> it = connectionQueue.keySet().iterator();
		while(it.hasNext()) {
			abortHandler(it.next());
		}
		connectionQueue.clear();
		synchronizer.unlock();
	}
}
