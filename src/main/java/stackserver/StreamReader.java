package stackserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class StreamReader {
	
	public static byte[] toByteArray(InputStream is) throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] arr = new byte[129];
		int total = 0;
		int count = is.read(arr, 0, 1);
		if(count == -1) 
			return null;
		
		byte first = arr[0];
		if(first < 0) 
			return arr;
		
		int len = first & 0xff;
		do {
			total += count;
			baos.write(arr, 0, count);
			if (total >= len + 1) break;
		} while ((count = is.read(arr)) != -1);
		
		if(total < len+1) return null;
		return baos.toByteArray();
	}
	
	public static byte[] toByteArray(LinkedBlockingQueue<Byte> is) throws InterruptedException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] arr = new byte[1];
		int total = 1;
		byte first = is.take();
		if(first < 0) {
			arr[0] = first;
			return arr;
		}
		int len = first & 0xff;
		baos.write(first);
		while (total < len + 1) {
			first = is.take();
			baos.write(first);
			total++;
		}
		return baos.toByteArray();
	}
}
