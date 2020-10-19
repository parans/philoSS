package stackserver;

public class LifoService implements Service {

	private DataSource ds;

	public LifoService(DataSource dataSource) {
		this.ds = dataSource;
	}

	public DataSource dataSource() {
		return ds;
	}

	@Override
	public Response handleRequest(Request req) {
		if (req == null)
			return null;
		return req.execute();
	}
}
