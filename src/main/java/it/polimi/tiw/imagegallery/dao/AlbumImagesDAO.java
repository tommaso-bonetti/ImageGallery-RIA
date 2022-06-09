package it.polimi.tiw.imagegallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlbumImagesDAO {
	private final Connection connection;
	
	public AlbumImagesDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void addImageToAlbum(int userId, int imageId, int albumId) throws SQLException {		
		String query = "INSERT INTO AlbumImages (imageId, albumId) VALUES (?, ?)";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, imageId);
			prepStatement.setInt(2, albumId);
			prepStatement.executeUpdate();
		}
	}
	
	public boolean checkAlbumExistence(int albumId) throws SQLException {
		String query = "SELECT * FROM Album WHERE albumId = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, albumId);
			prepStatement.executeQuery();
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst()) return false;
				return true;
			}
		}
	}

	public boolean checkAlbumOwnership(int userId, int albumId) throws SQLException {
		String query = "SELECT * FROM Album WHERE albumId = ? AND ownerId = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, albumId);
			prepStatement.setInt(2, userId);
			prepStatement.executeQuery();
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst()) return false;
				return true;
			}
		}
	}
	
	public boolean checkImageExistence(int imageId) throws SQLException {
		String query = "SELECT * FROM Image WHERE imageId = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, imageId);
			prepStatement.executeQuery();
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst()) return false;
				return true;
			}
		}
	}
	
	public boolean checkImageOwnership(int userId, int imageId) throws SQLException {
		String query = "SELECT * FROM Image WHERE imageId = ? AND ownerId = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, imageId);
			prepStatement.setInt(2, userId);
			prepStatement.executeQuery();
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst()) return false;
				return true;
			}
		}
	}
}
