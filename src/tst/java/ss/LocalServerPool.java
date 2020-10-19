package ss;

import java.io.ByteArrayOutputStream;
import java.net.Socket;

import stackserver.LocalConnectionHandler;
import stackserver.ServerPool;
import stackserver.connect.Connection;
import stackserver.service.Service;

public class LocalServerPool extends ServerPool {
	
	public LocalServerPool(int poolSize, Service service) {
		super(poolSize, service);
	}
	
	public boolean submit(Connection connection) {
		/*synchronizer.lock();
		try {
			if(connectionQueue.size() == poolSize) {
				Socket key = connectionQueue.keySet().iterator().next();
				if(connectionQueue.get(key) + 10000L < System.currentTimeMillis()) {
					abortHandler(key);
				} else {
					return false;
				}
			}
			connectionQueue.put(connection.socket, System.currentTimeMillis());
			es.submit(new LocalConnectionHandler(connection, service, synchronizer, connectionQueue, 
					socketMap, connection.input));
			return true;
		} finally {
			synchronizer.unlock();
		}*/
		return false;
	}
}
