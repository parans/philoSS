package stackserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class MainServer {
	
	private Selector selector;
    private Map<SocketChannel, LinkedBlockingQueue<Byte>> dataMapper;
    private InetSocketAddress listenAddress;
	
	private static ServerSocket eventLoopSocket;
	private ServerPool sp;
	
	private final byte[] SERVER_BUSY = new byte[] {(byte) 0xff};
	static Logger logger = Logger.getLogger(MainServer.class.getName());
 
	public MainServer(ServerPool sp, String address, int port) {
		this.sp = sp;
		listenAddress = new InetSocketAddress(address, port);
        dataMapper = new HashMap<SocketChannel, LinkedBlockingQueue<Byte>>();
	}
	
	/*public void eventLoop(int port) throws IOException {
		Socket sock = null;
		eventLoopSocket = new ServerSocket(port);
        while(true) {
            logger.info("Event Waiting for the client request");
            sock = eventLoopSocket.accept();
            byte[] input = IOUtils.toByteArray(sock.getInputStream());
            if(input.length == 1 && input[0] == 0x88) {
            	sp.drain();
            } else if(input.length == 1 && input[0] == 0x07) {
            	
            }
            System.out.println("Waiting for the client request");
            
            //if(message.equalsIgnoreCase("exit")) break
        }
        //eventLoopSocket.close();
	}*/
	
    /*public void start(int port) throws IOException, ClassNotFoundException {
        stackServerSocket = new ServerSocket(port);
        while(true) {
            logger.info("StackServer waiting for the client request");
            Socket socket = stackServerSocket.accept();
            boolean success = sp.submit(socket);
            if(!success) {
            	//Connection limit excceded
            	socket.getOutputStream().write(SERVER_BUSY);
            	socket.close();
            }
            System.out.println("Waiting for the client request");
        }
    }*/
    
    public void stop() {
    	try {
    		this.selector.close();
			//stackServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
 // create server channel    
    public void startServer() throws IOException, InterruptedException {
        this.selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
 
        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
 
        System.out.println("Server started...");
 
        while (true) {
            // wait for events
            this.selector.select();
 
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
                    this.accept(key);
                }
                else if (key.isReadable()) {
                    this.read(key);
                }
            }
            Thread.sleep(2);
        }
    }
    
  //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
    	System.out.println("Accepting socket");
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        //Socket socket = channel.socket();
        //SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        //System.out.println("Connected to: " + remoteAddr);
 
        // register channel with selector for further IO
        LinkedBlockingQueue<Byte> iods = new LinkedBlockingQueue<>();
        
        boolean accepted = sp.submit(new Connection(channel, iods));
        if(!accepted) {
        	System.out.println("Server busy");
        	channel.write(ByteBuffer.wrap(SERVER_BUSY));
        	channel.close();
            key.cancel();
            return;
        }
        dataMapper.put(channel, iods);
        channel.register(this.selector, SelectionKey.OP_READ);
        //System.out.println("Accepted, registered");
    }
     
    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
    	//System.out.println("Reading request");
        SocketChannel channel = (SocketChannel) key.channel();
        //channel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(256);
        int numRead = -1;
        numRead = channel.read(buffer);
        LinkedBlockingQueue<Byte> iods = this.dataMapper.get(channel);
        
        if (numRead == -1) {
        	System.out.println("Closing connection");
            /*Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);*/
            sp.abortHandler(channel);
            channel.close();
            key.cancel();
            this.dataMapper.remove(channel);
            return;
        }
        
        //ByteArrayOutputStream baos = new Byte
        for(byte b : buffer.array()) {
        	while(!iods.offer(b));
        }
        //System.out.println("Done reading request");
    }
    
    public static void main(String[] args) {
    	
    	DataSource ds = new LifoDataSource(100);
    	Service ls = new LifoService(ds);
    	ServerPool sp = new ServerPool(100, ls);
        MainServer server = new MainServer(sp, "localhost", 8080);
		try {
			/*Thread eventLoop = new Thread(() -> {
				try {
					server.eventLoop(8081);
				} catch (IOException e) {
					logger.info("IOException:" + e.getMessage());
				}
			}, "EventLoop");
			eventLoop.start();
			server.start(8080);*/
			server.startServer();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException:" + e.getLocalizedMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
