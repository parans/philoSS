package stackserver.connect;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class Connection {
	public SocketChannel channel;
	public LinkedBlockingQueue<Byte> is;

	public Connection(SocketChannel socket, LinkedBlockingQueue<Byte> is) {
		this.channel = socket;
		this.is = is;
	}
}
