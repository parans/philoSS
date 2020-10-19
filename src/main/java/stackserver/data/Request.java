package stackserver.data;

import stackserver.service.Service;

public interface Request {

	void service(Service srv);

	void payload(byte[] payload);

	Response execute();
}
