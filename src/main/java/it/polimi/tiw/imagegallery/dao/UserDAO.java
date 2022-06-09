package it.polimi.tiw.imagegallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.imagegallery.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkCredentials(String username, String password) throws Exception, SQLException {
		
		String query = "SELECT userId, email, username FROM User WHERE username = ? AND password = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setString(1, username);
			prepStatement.setString(2, password);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();
					User user = new User();
					user.setId(res.getInt("userId"));
					user.setEmail(res.getString("email"));
					user.setUsername(res.getString("username"));
					return user;
				}
			}
		}
	}
	
	public User createUser(String email, String username, String password) throws Exception, SQLException {
		User res = null;
		
		if (getUser(username) != null)
			throw new Exception("Username already exists");
		else {
			String query = "INSERT INTO User (email, username, password) VALUES (?, ?, ?)";
			try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
				prepStatement.setString(1, email);
				prepStatement.setString(2, username);
				prepStatement.setString(3, password);
				
				int affectedRows = prepStatement.executeUpdate();
				if (affectedRows == 1)
					res = getUser(username);
			}
		}
		
		return res;
	}

	public User getUser(int userId) throws SQLException {
		String query = "SELECT userId, email, username FROM User WHERE userId = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, userId);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();
					User user = new User();
					user.setId(res.getInt("userId"));
					user.setEmail(res.getString("email"));
					user.setUsername(res.getString("username"));
					return user;
				}
			}
		}
	}
	
	public User getUser(String username) throws SQLException {
		String query = "SELECT userId, email, username FROM User WHERE username = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setString(1, username);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();
					User user = new User();
					user.setId(res.getInt("userId"));
					user.setEmail(res.getString("email"));
					user.setUsername(res.getString("username"));
					return user;
				}
			}
		}
	}
}
