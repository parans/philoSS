package stackserver;

public class PushResponse implements Response {
	byte[] payload;
	
	PushResponse(byte[] payload) {
		this.payload = payload;
	}
	
	@Override
	public byte[] payload() {
		return payload;
	}
}
