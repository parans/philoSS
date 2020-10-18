package stackserver.nio;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
 
public class SocketServerExample {
    private Selector selector;
    private Map<SocketChannel,PipedOutputStream> dataMapper;
    private InetSocketAddress listenAddress;
     
    public static void main(String[] args) throws Exception {
        Runnable server = new Runnable() {
            @Override
            public void run() {
                 try {
                    new SocketServerExample("localhost", 8090).startServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 
            }
        };
         
        Runnable client = new Runnable() {
            @Override
            public void run() {
                 try {
                     new SocketClientExample().startClient();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                 
            }
        };
       Thread s = new Thread(server);
       Thread c1 = new Thread(client, "client-A");
       //Thread c2 = new Thread(client, "client-B");
       s.start();
       Thread.sleep(5000L);
       c1.start();
       //c2.start();
       c1.join();
       //c2.join();
       s.join();
    }
 
    public SocketServerExample(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
        dataMapper = new HashMap<SocketChannel, PipedOutputStream>();
    }
 
    // create server channel    
    private void startServer() throws IOException {
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
        }
    }
 
    
    
    //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);
 
        // register channel with selector for further IO
        dataMapper.put(channel, new PipedOutputStream());
        new Thread(() -> {
        	try {
        		int count = 0;
        		byte[] arr = new byte[256];
				PipedInputStream is = new PipedInputStream(dataMapper.get(channel));
				while ((count = is.read(arr)) != -1) {
					System.out.println("Got: " + new String(arr));
				}
				System.out.println("Done processing stream exiting now");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }, "Worker").start();
        channel.register(this.selector, SelectionKey.OP_READ);
    }
     
    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int numRead = -1;
        numRead = channel.read(buffer);
        PipedOutputStream pos = this.dataMapper.get(channel);
        
        if (numRead == -1) {
            this.dataMapper.remove(channel);
            pos.close();
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }
 
        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        pos.write(data);
    }
}
