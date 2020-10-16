package stackserver;

public class PushRequest implements Request{

	byte[] payload;
	Service serviceImpl;
	
	@Override
	public Response execute() {
		byte[] res = serviceImpl.dataSource().addItem(payload); 
		if(res == null) {
			return new PushResponse(null);
		}
		return new PushResponse(res);
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
