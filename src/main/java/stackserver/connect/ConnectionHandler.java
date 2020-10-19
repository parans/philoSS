package stackserver.connect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import stackserver.data.PacketSerializer;
import stackserver.data.Request;
import stackserver.data.Response;
import stackserver.data.StreamReader;
import stackserver.service.Service;

public class ConnectionHandler implements Runnable {

	protected Connection connection;
	protected Service serviceImpl;
	protected ReentrantLock synchronizer;
	protected Map<SocketChannel, Thread> socketMap;
	protected Map<SocketChannel, Long> connectionQueue;
	private static Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

	public ConnectionHandler(Connection connection, Service service,
			ReentrantLock sync, Map<SocketChannel, Long> connectionQueue,
			Map<SocketChannel, Thread> socketMap) {
		this.connection = connection;
		this.serviceImpl = service;
		this.synchronizer = sync;
		this.socketMap = socketMap;
		this.connectionQueue = connectionQueue;
	}

	@Override
	public void run() {
		byte[] output = null;
		socketMap.put(connection.channel, Thread.currentThread());
		try {
			byte[] packet = StreamReader.toByteArray(connection.is);
			Request req = PacketSerializer.deserialize(packet);
			req.service(serviceImpl);
			Response res = serviceImpl.handleRequest(req);
			output = PacketSerializer.serialize(res);
			if (output != null && connection.channel.isOpen()) {
				connection.channel.write(ByteBuffer.wrap(output));
			}
		} catch (IOException e) {
			logger.info("IOException:" + e.getStackTrace());
		} catch (InterruptedException e) {
			logger.info("Thread interrupted while reading:" + e.getMessage());
		} finally {
			socketMap.remove(connection.channel);
			synchronizer.lock();
			connectionQueue.remove(connection.channel);
			synchronizer.unlock();
		}
	}
}
