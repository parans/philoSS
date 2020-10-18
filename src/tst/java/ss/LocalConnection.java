package ss;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

import stackserver.Connection;

public class LocalConnection extends Connection {
	public byte[] input;
	
	LocalConnection(SocketChannel socket, LinkedBlockingQueue<Byte> is, byte[] input) {
		super(socket, is);
		this.input = input;
	}

}
