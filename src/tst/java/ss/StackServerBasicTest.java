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

import stackserver.MainServer;
import stackserver.ServerPool;
import stackserver.connect.ConnectionHandler;
import stackserver.data.PacketSerializer;
import stackserver.data.Request;
import stackserver.data.Response;
import stackserver.datasource.DataSource;
import stackserver.datasource.LifoDataSource;
import stackserver.service.LifoService;
import stackserver.service.Service;


@RunWith(JUnit4ClassRunner.class)
public class StackServerBasicTest {
	
	static Service service;
	static LocalServerPool sp;
	static MainServer ms;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DataSource ds = new LifoDataSource(5);
    	service = new LifoService(ds);
    	sp = new LocalServerPool(5, service);
    	ms = new MainServer(sp, "localhost", 7007, 5);
    	ms.startServer();
	}
	
	@After
	public void tearDown() {
		sp.drain();
	}
	
	/*@Test
	public void testBasic() throws IOException, InterruptedException {
		InetAddress host = InetAddress.getLocalHost();
		byte[] arr = new byte[] {(byte) 0x80};
		List<LocalConnectionHandler> lchs = new LinkedList<>();
		for(int i=0; i<3; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket(host.getHostName(), 7007);
			sp.submit(soc, arr);
			lchs.add(ch);
	        Thread.sleep(1000L);
		}
		
		arr = new byte[] {0x08, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
		for(int i=0; i<5; i++) {
			System.out.println("Sending request to Socket Server");
			Socket soc = new Socket();
			while(!sp.submit(soc));
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
		for(int i=0; i<5; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			sp.submit(soc, arr);
	        Thread.sleep(1000L);
		}
		
		//Sending two more pop requests, expecting busy state
		for(int i=0; i<3; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			boolean success = sp.submit(soc, arr);
			assertFalse("False expected", success);
		}
	}
	
	@Test
	public void testServerStateAfterSleep() throws IOException, InterruptedException {
		
		byte[] arr = new byte[] {(byte) 0x80};
		for(int i=0; i<5; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			sp.submit(soc, arr);
	        Thread.sleep(1000L);
		}
		
		Thread.sleep(6000L);
		//Sending two more pop requests, expecting busy state
		arr = new byte[] {(byte) 0x80};
		for(int i=0; i<3; i++) {
			System.out.println("Sending pop request to Socket Server");
			Socket soc = new Socket();
			boolean success = sp.submit(soc, arr);
			assertTrue("True expected", success);
			Thread.sleep(1000L);
		}
	}*/
}
