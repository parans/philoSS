package stackserver;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ServerPool {
	
	protected Service service;
	protected ExecutorService es;
	protected int poolSize;
	protected ReentrantLock synchronizer;
	//private ScheduledExecutorService connectionMonitor;
	protected LinkedHashMap<SocketChannel, Long> connectionQueue;
	protected Map<SocketChannel, Thread> socketMap;
	static Logger logger = Logger.getLogger(ServerPool.class.getName());
	
	/*private Runnable connectionMonitorTask = () -> {
		logger.info("Checking stale connections every 20s");
		socketMap.keySet().iterator().forEachRemaining( key -> {
			try {
				byte[] arr = StreamReader.toByteArray(key.getInputStream());
				if(arr == null) {
					abortHandler(key);
				}
			} catch (IOException e) {
				logger.info("Error reading stream continuing");
			}
		});
	};*/
	
	public ServerPool(int poolSize, Service service) {
		this.service = service;
		this.poolSize = poolSize;
		this.es = Executors.newFixedThreadPool(poolSize);
		this.connectionQueue = new LinkedHashMap<>();
		this.synchronizer = new ReentrantLock(true);
		this.socketMap = new ConcurrentHashMap<>();
		//this.connectionMonitor = Executors.newSingleThreadScheduledExecutor();
		//connectionMonitor.scheduleAtFixedRate(connectionMonitorTask, 5, 60, TimeUnit.SECONDS);
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
