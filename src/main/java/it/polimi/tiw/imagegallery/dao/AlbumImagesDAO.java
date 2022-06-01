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
	
	public void addImageToAlbum(int userId, int imageId, int albumId) throws Exception, SQLException {
		if (!checkAlbumOwnership(userId, albumId))
			throw new Exception("Cannot add image to album owned by another user");
		
		if (!checkImageOwnership(userId, imageId))
			throw new Exception("Cannot add image owned by another user to your album");
		
		String query = "INSERT INTO AlbumImages (imageId, albumId) VALUES (?, ?)";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, imageId);
			prepStatement.setInt(2, albumId);
			prepStatement.executeUpdate();
		}
	}

	private boolean checkAlbumOwnership(int userId, int albumId) throws Exception {
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
	
	private boolean checkImageOwnership(int userId, int imageId) throws Exception {
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
