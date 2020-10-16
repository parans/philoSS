package ss;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import stackserver.StreamReader;

public class StreamReaderTest {
	
	@Test
	public void testPushPacket() throws IOException, InterruptedException {
		final List<byte[]> ops = new LinkedList<>();
		ServerSocket stackServerSocket = new ServerSocket(4505);
		Thread t = new Thread(() -> {
			while(true) {
				try {
					Socket socket = stackServerSocket.accept();
					byte[] input = StreamReader.toByteArray(socket.getInputStream());
					ops.add(input);
					byte[] op = new byte[] {0};
					socket.getOutputStream().write(op);
					socket.getOutputStream().flush();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(ops.size() == 0) break;
			}
		}, "SockRunner");
		t.start();
		
        byte[] arr = new byte[] {0x08, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        for(int i=0; i<10; i++) {
        	System.out.println("Sending push request to Socket Server");
    		// write to socket using ObjectOutputStream
        	InetAddress host = InetAddress.getLocalHost();
    		Socket mainServerSocket = new Socket(host.getHostName(), 4505);
    		mainServerSocket.getOutputStream().write(arr);
    		mainServerSocket.getOutputStream().flush();
    		
    		byte[] res = StreamReader.toByteArray(mainServerSocket.getInputStream());
			System.out.println("Reading done");
			String message = new String(res, StandardCharsets.UTF_8.name());
			System.out.println("Message: " + message);
			Thread.sleep(1000L);
			mainServerSocket.close();
        }
        ops.clear();
		stackServerSocket.close();	
	}
	
	@Test
	public void testPopPacket() throws IOException, InterruptedException {
		final List<byte[]> ops = new LinkedList<>();
		ServerSocket stackServerSocket = new ServerSocket(4505);
		Thread t = new Thread(() -> {
			while(true) {
				try {
					Socket socket = stackServerSocket.accept();
					byte[] input = StreamReader.toByteArray(socket.getInputStream());
					ops.add(input);
					byte[] op = new byte[] {0x08, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
					socket.getOutputStream().write(op);
					socket.getOutputStream().flush();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(ops.size() == 0) break;
			}
		}, "SockRunner");
		t.start();
		
        byte[] arr = new byte[] {(byte) 0x80};
        for(int i=0; i<10; i++) {
        	System.out.println("Sending push request to Socket Server");
    		// write to socket using ObjectOutputStream
        	InetAddress host = InetAddress.getLocalHost();
    		Socket mainServerSocket = new Socket(host.getHostName(), 4505);
    		mainServerSocket.getOutputStream().write(arr);
    		mainServerSocket.getOutputStream().flush();
    		
    		byte[] res = StreamReader.toByteArray(mainServerSocket.getInputStream());
			System.out.println("Reading done");
			String message = new String(res, StandardCharsets.UTF_8.name());
			System.out.println("Message: " + message);
			Thread.sleep(1000L);
			mainServerSocket.close();
        }
        ops.clear();
		stackServerSocket.close();
	}

}
