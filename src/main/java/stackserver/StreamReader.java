package stackserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamReader {
	
	public static byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] arr = new byte[129];
		int total = 0;
		int count = is.read(arr, 0, 1);
		if(count == 0) 
			return null;
		
		byte first = arr[0];
		if(first < 0) 
			return arr;
		
		int len = first & 0xff;
		do {
			total += count;
			baos.write(arr, 0, count);
			if (total == len + 1) break;
		} while ((count = is.read(arr)) != -1);
		return baos.toByteArray();
	}
}
