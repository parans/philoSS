package ss;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import stackserver.ConnectionHandler;
import stackserver.DataSource;
import stackserver.LifoDataSource;
import stackserver.LifoService;
import stackserver.MainServer;
import stackserver.PacketSerializer;
import stackserver.Request;
import stackserver.Response;
import stackserver.ServerPool;
import stackserver.Service;


@RunWith(JUnit4ClassRunner.class)
public class StackServerBasicTest {
	
	static ReentrantLock synchronizer;
	static Service service;
	static ServerPool sp;
	
	 static class LocalConnectionHandler extends ConnectionHandler  {
		 byte[] item;
		 byte[] output;
		 
		 	public LocalConnectionHandler(Socket soc, ServerPool sp, Service service, ReentrantLock synchronizer, byte[] item) {
				super(soc, sp, service, synchronizer);
				this.item = item;
			}
			
			@Override
			public void run() {
				socketMap.put(socket, Thread.currentThread());
				byte[] packet = this.item;
				Request req = PacketSerializer.deserialize(packet);
				req.service(serviceImpl);
				Response res = serviceImpl.handleRequest(req);
				output = PacketSerializer.serialize(res);
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				socketMap.remove(socket);
			}
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		synchronizer = new ReentrantLock(true);
		sp = new ServerPool(5, synchronizer);
		DataSource ds = new LifoDataSource(5);
    	service = new LifoService(ds);
	}
	
	@After
	public void tearDown() {
		sp.drain();
	}
	
	@Test
	public void testBasic() throws IOException, InterruptedException {
		
		byte[] arr = new byte[] {(byte) 0x80};
		List<LocalConnectionHandler> lchs = new LinkedList<>();
		for(int i=0; i<3; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			LocalConnectionHandler ch = new LocalConnectionHandler(soc, sp, service, synchronizer, arr);
			sp.submit(ch);
			lchs.add(ch);
	        Thread.sleep(1000L);
		}
		
		arr = new byte[] {0x08, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
		for(int i=0; i<5; i++) {
			System.out.println("Sending request to Socket Server");
			Socket soc = new Socket();
			LocalConnectionHandler ch = new LocalConnectionHandler(soc, sp, service, synchronizer, arr);
			while(!sp.submit(ch));
	        Thread.sleep(1000L);
		}
		
		Thread.sleep(10000L);
		for (; !lchs.isEmpty();) {
			LocalConnectionHandler lch = lchs.get(0);
			if (lch.output != null) {
				String message = IOUtils.toString(lch.output, StandardCharsets.UTF_8.toString());
				System.out.println("Message: " + message);
				lchs.remove(0);
			}
			Thread.sleep(1000L);
		}
	}
	
	@Test
	public void testServerBusy() throws IOException, InterruptedException {
		byte[] arr = new byte[] {(byte) 0x80};
		List<LocalConnectionHandler> lchs = new LinkedList<>();
		for(int i=0; i<5; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			LocalConnectionHandler ch = new LocalConnectionHandler(soc, sp, service, synchronizer, arr);
			sp.submit(ch);
			lchs.add(ch);
	        Thread.sleep(1000L);
		}
		
		//Sending two more pop requests, expecting busy state
		for(int i=0; i<3; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			LocalConnectionHandler ch = new LocalConnectionHandler(soc, sp, service, synchronizer, arr);
			boolean success = sp.submit(ch);
			assertFalse("False expected", success);
		}
	}
	
	@Test
	public void testServerStateAfterSleep() throws IOException, InterruptedException {
		
		byte[] arr = new byte[] {(byte) 0x80};
		List<LocalConnectionHandler> lchs = new LinkedList<>();
		for(int i=0; i<5; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			LocalConnectionHandler ch = new LocalConnectionHandler(soc, sp, service, synchronizer, arr);
			sp.submit(ch);
			lchs.add(ch);
	        Thread.sleep(1000L);
		}
		
		Thread.sleep(6000L);
		//Sending two more pop requests, expecting busy state
		arr = new byte[] {(byte) 0x80};
		for(int i=0; i<3; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			LocalConnectionHandler ch = new LocalConnectionHandler(soc, sp, service, synchronizer, arr);
			boolean success = sp.submit(ch);
			assertTrue("True expected", success);
			Thread.sleep(1000L);
		}
	}
}
