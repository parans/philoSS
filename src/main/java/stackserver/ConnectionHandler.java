package stackserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {

	protected Socket socket;
	protected ServerPool pool;
	protected Service serviceImpl;
	protected ReentrantLock synchronizer;
	protected static Map<Socket, Thread> socketMap;
	static Logger logger = Logger.getLogger(ConnectionHandler.class.getName());
	static {
		socketMap = new ConcurrentHashMap<Socket, Thread>();
	}
	
	public ConnectionHandler(Socket socket, ServerPool pool, Service service, ReentrantLock sync) {
		this.socket = socket;
		this.pool = pool;
		this.serviceImpl = service;
		this.synchronizer = sync;
	}
	
	public static void abortHandler(Socket socket) {
		if(socketMap.containsKey(socket)) {
			Thread handler = socketMap.get(socket);
			handler.interrupt();
		}
	}
	
	@Override
	public void run() {
		byte[] output = null;
		socketMap.put(socket, Thread.currentThread());
		try(InputStream is = socket.getInputStream()) {
			byte[] packet = StreamReader.toByteArray(is);
			Request req = PacketSerializer.deserialize(packet);
			req.service(serviceImpl);
			Response res = serviceImpl.handleRequest(req);
			output = PacketSerializer.serialize(res);
			if(output != null && !socket.isClosed()) {
				logger.info("Writing out the request to socket");
				socket.getOutputStream().write(output);
				socket.getOutputStream().flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			socketMap.remove(socket);
			synchronizer.lock();
			pool.connectionQueue().remove(socket);
			synchronizer.unlock();
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
