package ss;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Ignore;
import org.junit.Test;

import stackserver.data.StreamReader;

public class StreamReaderTest {
	
	@Ignore
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
	
	@Ignore
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
	
	@Ignore
	@Test
	public void testClosedSocket() throws IOException, InterruptedException {
		final List<byte[]> ops = new LinkedList<>();
		ServerSocket stackServerSocket = new ServerSocket(4505);
		Thread t = new Thread(() -> {
			while (true) {
				try {
					Socket socket = stackServerSocket.accept();
					byte[] input = StreamReader.toByteArray(socket.getInputStream());
					ops.add(input);
					byte[] op = new byte[] { 0x08, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
					byte[] arr = StreamReader.toByteArray(socket.getInputStream());
					if (arr == null)
						break;
					socket.getOutputStream().write(op);
					socket.getOutputStream().flush();
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (ops.size() == 0)
					break;
			}
		}, "SockRunner");
		t.start();

		byte[] arr = new byte[] { (byte) 0x80 };
		System.out.println("Sending push request to Socket Server");
		// write to socket using ObjectOutputStream
		InetAddress host = InetAddress.getLocalHost();
		Socket mainServerSocket = new Socket(host.getHostName(), 4505);
		mainServerSocket.getOutputStream().write(arr);
		mainServerSocket.getOutputStream().flush();
		mainServerSocket.close();

		Thread.sleep(1000L);
		ops.clear();
		stackServerSocket.close();
		t.join();
	}
	
	@Test
	public void testLinkedBlockingStreamReader() throws InterruptedException {
		final byte[] op = new byte[] { 0x08, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
		final LinkedBlockingQueue<Byte> lbq = new LinkedBlockingQueue<>();
		Thread one = new Thread(() -> {
			for(byte b : op) {
				lbq.offer(b);
			}
		}, "Runner");
		one.start();
		
		byte[] barr = StreamReader.toByteArray(lbq);
		assertArrayEquals(op, barr);
		one.join();
	}

}
