package labrador.driver.database.setup;

import java.io.IOException;

import javax.sql.DataSource;

import labrador.search.SearchProviderException;

public interface DatabaseSetupDriver {

	public abstract void setupUserDatabase() throws IOException,
			SearchProviderException;

	public abstract void createUserDatabase();

	public abstract void cleanDatabase() throws IOException,
			SearchProviderException;
	
	public abstract DataSource getDataSource();

	public abstract void setDataSource(DataSource dataSource);
	
	public abstract String getUsername();
	
	public abstract void setUsername(String username);

}