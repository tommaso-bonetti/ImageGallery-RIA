package it.polimi.tiw.imagegallery.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

public class ConnectionManager {
	public static Connection getConnection(ServletContext servletContext) throws UnavailableException {
		Connection connection;
		
		try {
			ServletContext context = servletContext;
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new UnavailableException("Unable to load database driver");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UnavailableException("Unable to get database connection");
		}
		
		return connection;
	}
}
