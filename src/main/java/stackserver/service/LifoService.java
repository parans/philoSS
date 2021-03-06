package stackserver.service;

import stackserver.data.Request;
import stackserver.data.Response;
import stackserver.datasource.DataSource;

public class LifoService implements Service {

	private DataSource ds;

	public LifoService(DataSource dataSource) {
		this.ds = dataSource;
	}

	public DataSource dataSource() {
		return ds;
	}

	/**
	 * Handle Request, serve from @{DataSource}
	 */
	@Override
	public Response handleRequest(Request req) {
		if (req == null)
			return null;
		return req.execute();
	}
}
