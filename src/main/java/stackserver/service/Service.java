package stackserver.service;

import stackserver.data.Request;
import stackserver.data.Response;
import stackserver.datasource.DataSource;

public interface Service {

	Response handleRequest(Request req);

	DataSource dataSource();
}
