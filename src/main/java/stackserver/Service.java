package stackserver;

public interface Service {
	
	Response handleRequest(Request req);
	
	DataSource dataSource();
}
