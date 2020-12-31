package labrador.driver.database.setup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import labrador.search.SearchProvider;
import labrador.search.SearchProviderException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import pandorasbox.filesystemchangebroadcasting.FileSystemChangeBroadcaster;
import pandorasbox.persistence.Dao;

public class DatabaseSetupDriverSpringImpl implements DatabaseSetupDriver,
		ApplicationContextAware {

	private DataSource data_source;
	private String username;
	private ApplicationContext context;

	public void setupUserDatabase() throws IOException, SearchProviderException {
		createUserDatabase();
		cleanDatabase();
	}

	public void createUserDatabase() {
		try {
			// TODO this will only work with ident authentication !!!!!!!11!!!!
			Connection connection = DriverManager.getConnection(getDataSource()
					.getConnection().getMetaData().getURL(), getDataSource()
					.getConnection().getMetaData().getUserName(), "");

			Statement statement = connection.createStatement();
			String create = "CREATE DATABASE " + getUsername();
			statement.executeUpdate(create);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO: this might function?
	@SuppressWarnings("unchecked")
	public void cleanDatabase() throws IOException, SearchProviderException {
		 SearchProvider searchProvider = (SearchProvider) getApplicationContext()
				.getBean("defaultSearchProvider");
		searchProvider.createIndex();

		Dao<FileSystemChangeBroadcaster> fscbDao = (Dao<FileSystemChangeBroadcaster>) getApplicationContext()
				.getBean("defaultFscbDao");
		FileSystemChangeBroadcaster fscb = (FileSystemChangeBroadcaster) getApplicationContext()
				.getBean("defaultFscb");
		fscbDao.setPeristable(fscb);
	}

	public void setDataSource(DataSource dataSource) {
		data_source = dataSource;
	}

	public DataSource getDataSource() {
		return data_source;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	protected ApplicationContext getApplicationContext() {
		return context;
	}

	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;
	}

}
