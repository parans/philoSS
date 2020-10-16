package stackserver;

public interface Request {
	
	void service(Service srv);
	
	void payload(byte[] payload);
	
	Response execute();
}
