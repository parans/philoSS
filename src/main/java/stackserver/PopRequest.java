package stackserver;

public class PopRequest implements Request {

	byte[] payload;
	Service serviceImpl;

	@Override
	public Response execute() {
		byte[] res = serviceImpl.dataSource().remove();
		if (res == null) {
			return new PopResponse(null);
		}
		byte len = (byte) res.length;
		byte[] output = new byte[res.length + 1];
		output[0] = len;
		System.arraycopy(res, 0, output, 1, res.length);
		return new PopResponse(output);
	}

	@Override
	public void service(Service srv) {
		this.serviceImpl = srv;
	}

	@Override
	public void payload(byte[] payload) {
		this.payload = payload;
	}

}
