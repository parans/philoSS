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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class MainServer {
	
	private Selector selector;
    private Map<SocketChannel, LinkedBlockingQueue<Byte>> dataMapper;
    private InetSocketAddress listenAddress;
	private ServerPool sp;
	
	private final byte[] SERVER_BUSY = new byte[] {(byte) 0xff};
	static Logger logger = Logger.getLogger(MainServer.class.getName());
 
	public MainServer(ServerPool sp, String address, int port) {
		this.sp = sp;
		listenAddress = new InetSocketAddress(address, port);
        dataMapper = new ConcurrentHashMap<SocketChannel, LinkedBlockingQueue<Byte>>();
	}
    
    public void stop() {
    	try {
    		this.selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    
    //create server channel    
	public void startServer() throws IOException {
        selector = Selector.open();
        
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
 
        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        System.out.println("Server started...");
 
        while (true) {
            // wait for events
            try {
            	
				selector.select();
				//System.out.println("Events received" + c);
				
				//work on selected keys
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
	                    System.out.println("Done accepting");
	                } else if(key.isReadable()) {
	                	read(key);
	                }
	            }
			} catch (IOException e1) {
				e1.printStackTrace();
			}
 
            try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
    
  //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
    	System.out.println("Accepting socket");
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        if(dataMapper.containsKey(channel)) {
        	return;
        }
        LinkedBlockingQueue<Byte> iods = new LinkedBlockingQueue<>();
        dataMapper.put(channel, iods);
        boolean accepted = sp.submit(new Connection(channel, iods));
        if(!accepted) {
        	System.out.println("Server busy");
        	channel.write(ByteBuffer.wrap(SERVER_BUSY));
        	channel.close();
            key.cancel();
            return;
        }
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Accepted, registered");
    }
     
    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int numRead = -1;
        numRead = channel.read(buffer);
        LinkedBlockingQueue<Byte> iods = dataMapper.get(channel);
        
        if (numRead == -1) {
        	System.out.println("Closing connection");
            sp.abortHandler(channel);
            channel.close();
            key.cancel();
            dataMapper.remove(channel);
            return;
        }
        
        byte[] data = Arrays.copyOfRange(buffer.array(), 0, numRead);
        for(byte b : data) {
        	while(!iods.offer(b));
        }
    }
    
    public static void main(String[] args) {
    	
    	DataSource ds = new LifoDataSource(100);
    	Service ls = new LifoService(ds);
    	ServerPool sp = new ServerPool(100, ls);
        MainServer server = new MainServer(sp, "localhost", 8080);
		try {
			server.startServer();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException:" + e.getLocalizedMessage());
		}
    }
}
