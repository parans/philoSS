package stackserver;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class LocalConnectionHandler extends ConnectionHandler  {
	 byte[] item;
	 byte[] output;
	 
	 	public LocalConnectionHandler(Connection connection, Service service, ReentrantLock synchronizer, byte[] item) {
			super(connection, service, synchronizer, null, null);
			this.item = item;
		}
		
		@Override
		public void run() {
			socketMap.put(connection.channel, Thread.currentThread());
			byte[] packet = this.item;
			Request req = PacketSerializer.deserialize(packet);
			req.service(serviceImpl);
			Response res = serviceImpl.handleRequest(req);
			output = PacketSerializer.serialize(res);
			try {
				connection.channel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			socketMap.remove(connection.channel);
		}
}
