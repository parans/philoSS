package stackserver.data;

public class PopResponse implements Response {

	byte[] payload;

	public PopResponse(byte[] payload) {
		this.payload = payload;
	}

	@Override
	public byte[] payload() {
		return payload;
	}
}
