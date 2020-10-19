package stackserver.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import stackserver.connection.Connection;
import stackserver.connection.ConnectionHandler;
import stackserver.data.LifoServerConfig;
import stackserver.service.Service;

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

	/**
	 * Interrupt the threads handling the connections
	 * @param socket
	 */
	public void abortHandler(SocketChannel socket) {
		if (socketMap.containsKey(socket)) {
			Thread handler = socketMap.get(socket);
			if (handler != null) {
				handler.interrupt();
			}
		}
	}

	/**
	 * Submit connection to internal worker pool
	 * @param connection
	 * @return false if connection not submitted
	 * @throws IOException
	 */
	public boolean submit(Connection connection) throws IOException {
		synchronizer.lock();
		try {
			if (LifoServerConfig.verbose) {
				logger.info("Connection queue size:" + connectionQueue.size());
			}
			if (connectionQueue.size() == poolSize) {
				SocketChannel key = connectionQueue.keySet().iterator().next();
				/**
				 * Check if connection is older than 10s
				 */
				if (connectionQueue.get(key) + 10000L < System
						.currentTimeMillis()) {
					logger.info("Aborting slow client");
					abortHandler(key);
					key.close();
				} else {
					return false;
				}
			}
			connectionQueue.put(connection.channel, System.currentTimeMillis());
			es.submit(new ConnectionHandler(connection, service, synchronizer,
					connectionQueue, socketMap));
			return true;
		} finally {
			synchronizer.unlock();
		}
	}

	/**
	 * Drain connections and stop connection handlers
	 */
	public void drain() {
		synchronizer.lock();
		Iterator<SocketChannel> it = connectionQueue.keySet().iterator();
		while (it.hasNext()) {
			abortHandler(it.next());
		}
		connectionQueue.clear();
		synchronizer.unlock();
	}
}
