package ss;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import stackserver.DataSource;
import stackserver.LifoDataSource;
import stackserver.LifoService;
import stackserver.MainServer;
import stackserver.ServerPool;
import stackserver.Service;
import stackserver.StreamReader;
import stackserver.nio.SocketClientExample;
import stackserver.nio.SocketServerExample;

public class StackServerIntegrationTest {

	static Service service;
	static ServerPool sp;
	static MainServer ms;
	
	@AfterClass
	public static void tearDownAfterClass() throws IOException {
		System.out.println("Stop server");
		ms.stop();
	}
	
	@BeforeClass
	public static void setupBeforeClass() throws UnknownHostException, IOException, InterruptedException {
		DataSource ds = new LifoDataSource(5);
    	service = new LifoService(ds);
    	sp = new ServerPool(5, service);
    	ms = new MainServer(sp, "localhost", 8080);
		
		Runnable server = new Runnable() {
            @Override
            public void run() {
                 try {
                    ms.startServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                 
            }
        };
       Thread s = new Thread(server);
       s.start();
       Thread.sleep(7000L);
	}
	
	private void sendReceive(InetSocketAddress hostAddress, byte[] arr) throws IOException, InterruptedException {
		SocketChannel client = SocketChannel.open(hostAddress);
		//client.socket().bind(hostAddress);
		client.socket().getOutputStream().write(arr);
        System.out.println("Writing done");
		
		byte[] res = StreamReader.toByteArray(client.socket().getInputStream());
		System.out.println("Reading done");
		String message = new String(res, StandardCharsets.UTF_8.name());
		System.out.println("Message: " + message);
		//Thread.sleep(1000L);
		client.close();
	}
	
	private void send(InetSocketAddress hostAddress, byte b) throws IOException, InterruptedException {
		SocketChannel client = SocketChannel.open(hostAddress);
		//client.socket().bind(hostAddress);
		client.socket().getOutputStream().write(b);
        System.out.println("Writing done");
	}
	
	@Test
	public void test() throws IOException, InterruptedException {
		
		InetSocketAddress hostAddress = new InetSocketAddress("localhost", 8080);
		
        byte[] arr = new byte[] {0x08, 'u', '5', '1', '7', 'j', 'X', 'O', 'M'};
		for (int i = 0; i < 5; i++) {
			System.out.println("Sending push request to Socket Server");
			// write to socket using ObjectOutputStream
			sendReceive(hostAddress, arr);
		}
        
		byte[] arr2 = new byte[] {0x02, 'Z', 'S'};
		SocketChannel client = SocketChannel.open(hostAddress);
		//client.socket().bind(hostAddress);
		client.socket().getOutputStream().write(arr2[0]);
		client.socket().getOutputStream().write(arr2[1]);
		
        arr = new byte[] {(byte) 0x80};
		for (int i = 0; i < 3; i++) {
			System.out.println("Sending pop request to Socket Server");
			sendReceive(hostAddress, arr);
		}
		
		client.socket().getOutputStream().write(arr2[2]);
		
		for (int i = 0; i < 2; i++) {
			System.out.println("Sending pop request to Socket Server");
			sendReceive(hostAddress, arr);
		}
		client.close();
	}

}
