package stackserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class MainServer {

	private Selector selector;
	private Map<SocketChannel, LinkedBlockingQueue<Byte>> dataMapper;
	private InetSocketAddress listenAddress;
	private ServerPool sp;
	private ExecutorService ioWorker;
	private int size;

	private static final byte[] SERVER_BUSY = new byte[]{(byte) 0xff};
	static Logger logger = Logger.getLogger(MainServer.class.getName());

	public MainServer(ServerPool sp, String address, int port, int size) {
		this.sp = sp;
		this.listenAddress = new InetSocketAddress(address, port);
		this.dataMapper = new ConcurrentHashMap<SocketChannel, LinkedBlockingQueue<Byte>>();
		this.ioWorker = Executors.newSingleThreadExecutor();
		this.size = size;
	}

	public void stop() {
		try {
			this.selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// create server channel
	public void startServer() throws IOException {
		selector = Selector.open();

		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// retrieve server socket and bind to port
		serverChannel.socket().bind(listenAddress, 150);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		logger.info("Server started...");
		while (true) {
			// wait for events
			try {
				selector.select();

				// work on selected keys
				Iterator keys = this.selector.selectedKeys().iterator();
				while (keys.hasNext()) {
					SelectionKey key = (SelectionKey) keys.next();

					// this is necessary to prevent the same key from coming up
					// again the next time around.
					keys.remove();

					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						accept(key);
					} else if (key.isReadable()) {
						read(key);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				logger.info("Interrupted while sleeping");
			}
		}
	}

	// accept a connection made to this channel's socket
	private void accept(SelectionKey key) throws IOException {
		logger.info("Accepting socket");
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		final SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);
		if (dataMapper.containsKey(channel)) {
			return;
		}
		LinkedBlockingQueue<Byte> iods = new LinkedBlockingQueue<>();
		dataMapper.put(channel, iods);
		boolean accepted = sp.submit(new Connection(channel, iods));
		if (!accepted) {
			logger.info("Server busy");
			channel.write(ByteBuffer.wrap(SERVER_BUSY));
		}
		channel.register(selector, SelectionKey.OP_READ);
		logger.info("Accepted, registered");
	}

	// read from the socket channel
	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(130);
		int numRead = -1;
		numRead = channel.read(buffer);
		LinkedBlockingQueue<Byte> iods = dataMapper.get(channel);

		if (numRead == -1) {
			logger.info("Closing connection");
			sp.abortHandler(channel);
			channel.close();
			key.cancel();
			dataMapper.remove(channel);
			return;
		}

		final int bytesRead = numRead;
		ioWorker.submit(() -> {
			byte[] data = Arrays.copyOfRange(buffer.array(), 0, bytesRead);
			for (byte b : data) {
				while (!iods.offer(b));
			}
		});
	}

	public static void main(String[] args) {
		int size = (args != null && args.length >= 1)
				? Integer.parseInt(args[0])
				: 100;
		logger.info("Internal size :" + size);
		DataSource ds = new LifoDataSource(size);
		Service ls = new LifoService(ds);
		ServerPool sp = new ServerPool(size, ls);
		MainServer server = new MainServer(sp, "localhost", 8080, size);
		try {
			server.startServer();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException:" + e.getLocalizedMessage());
		}
	}
}
