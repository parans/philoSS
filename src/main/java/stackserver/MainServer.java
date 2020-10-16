package stackserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class MainServer {
	
	private ReentrantLock synchronizer;
	private static ServerSocket stackServerSocket;
	private static ServerSocket eventLoopSocket;
	private ServerPool sp;
	private Service service;
	
	private final byte[] SERVER_BUSY = new byte[] {(byte) 0xff};
	static Logger logger = Logger.getLogger(MainServer.class.getName());
 
	MainServer(ServerPool sp, Service service, ReentrantLock sync) {
		this.sp = sp;
		this.service = service;
		this.synchronizer = sync;
	}
	
	public void eventLoop(int port) throws IOException {
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
	}
	
    public void start(int port) throws IOException, ClassNotFoundException {
        stackServerSocket = new ServerSocket(port);
        while(true) {
            logger.info("StackServer waiting for the client request");
            Socket socket = stackServerSocket.accept();
            boolean success = sp.submit(new ConnectionHandler(socket, sp, service, synchronizer));
            if(!success) {
            	//Connection limit excceded
            	socket.getOutputStream().write(SERVER_BUSY);
            	socket.close();
            }
            
            System.out.println("Waiting for the client request");
        }
    }
    
    public static void stop() {
    	try {
			stackServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args) {
    	ReentrantLock synchronizer = new ReentrantLock(true);
    	ServerPool sp = new ServerPool(100, synchronizer);
    	DataSource ds = new LifoDataSource(100);
    	Service ls = new LifoService(ds);
        MainServer server = new MainServer(sp, ls, synchronizer);
		try {
			Thread eventLoop = new Thread(() -> {
				try {
					server.eventLoop(8081);
				} catch (IOException e) {
					logger.info("IOException:" + e.getMessage());
				}
			}, "EventLoop");
			eventLoop.start();
			server.start(8080);
		} catch (IOException | ClassNotFoundException e) {
			logger.info("IOException:" + e.getMessage());
		}
    }
}
