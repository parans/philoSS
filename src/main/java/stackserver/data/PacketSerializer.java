package stackserver.data;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PacketSerializer {

	public static byte[] serialize(Response res) {
		return res.payload();
	}

	public static Request deserialize(byte[] packet) {
		Request req = null;
		if (packet == null)
			return req;
		byte first = packet[0];
		if (first < 0) {
			req = new PopRequest();
		} else if ((first & 0x00) == 0) {
			int len = first & 0xff;
			byte[] payload = Arrays.copyOfRange(packet, 1, packet.length);
			req = new PushRequest();
			req.payload(payload);
			System.out.println("Payload:"
					+ new String(payload, StandardCharsets.US_ASCII));
		}
		return req;
	}
}
