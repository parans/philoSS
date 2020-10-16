package ss;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ss.StackServerBasicTest.LocalConnectionHandler;
import stackserver.StreamReader;

public class StackServerIntegrationTest {

	//static Socket evenLoopSocket;
	//static Socket mainServerSocket;
	
	@After
	public void tearDown() throws IOException {
		 //byte[] arr = new byte[] {(byte) 0x88};
		 //evenLoopSocket.getOutputStream().write(arr);
	}
	
	@BeforeClass
	public static void setupBeforeClass() throws UnknownHostException, IOException {
		//InetAddress host = InetAddress.getLocalHost();
		//evenLoopSocket = new Socket(host.getHostName(), 8081);
		//mainServerSocket = new Socket(host.getHostName(), 8080);
	}
	
	@Test
	public void test() throws IOException, InterruptedException {
		InetAddress host = InetAddress.getLocalHost();
		Socket mainServerSocket = null;
        byte[] arr = new byte[] {0x08, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
		for (int i = 0; i < 3; i++) {
			System.out.println("Sending push request to Socket Server");
			// write to socket using ObjectOutputStream
			mainServerSocket = new Socket(host.getHostName(), 8080);
			mainServerSocket.getOutputStream().write(arr);
			mainServerSocket.getOutputStream().flush();
			System.out.println("Writing done");
			
			byte[] res = StreamReader.toByteArray(mainServerSocket.getInputStream());
			System.out.println("Reading done");
			String message = new String(res, StandardCharsets.UTF_8.name());
			System.out.println("Message: " + message);
			Thread.sleep(1000L);
			mainServerSocket.close();
		}
        
        arr = new byte[] {(byte) 0x80};
		for (int i = 0; i < 3; i++) {
			System.out.println("Sending pop request to Socket Server");
			// write to socket using ObjectOutputStream
			mainServerSocket = new Socket(host.getHostName(), 8080);
			mainServerSocket.getOutputStream().write(arr);
			mainServerSocket.getOutputStream().flush();
			
			byte[] res = StreamReader.toByteArray(mainServerSocket.getInputStream());
			String message = new String(res, StandardCharsets.UTF_8.name());
			System.out.println("Message: " + message);
			Thread.sleep(1000L);
			mainServerSocket.close();
		}
	}

}
