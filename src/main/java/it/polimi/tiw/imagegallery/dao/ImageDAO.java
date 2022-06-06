package it.polimi.tiw.imagegallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.imagegallery.beans.Image;

public class ImageDAO {
	private final Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Image fetchImageById(int imageId, int albumId) throws SQLException {
		String query = "SELECT * FROM Image WHERE imageId = ? AND imageId = ANY (SELECT imageId FROM AlbumImages WHERE albumId = ?)";
		
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, imageId);
			prepStatement.setInt(2, albumId);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();
					Image image = new Image();
					image.setId(res.getInt("imageId"));
					image.setTitle(res.getString("title"));
					image.setDescription(res.getString("description"));
					image.setUploadDate(res.getTimestamp("uploadDate"));
					image.setFilePath(res.getString("filePath"));
					image.setOwnerId(res.getInt("ownerId"));
					return image;
				}
			}
		}
	}

	public List<Image> fetchImagesByAlbum(int albumId) throws SQLException {
		List<Image> images = new ArrayList<>();
		
		String query =
				"SELECT * FROM Image WHERE imageId = ANY (SELECT imageId FROM AlbumImages WHERE albumId = ?)"
				+ " ORDER BY uploadDate DESC, imageId DESC";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, albumId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Image image = new Image();
					image.setId(res.getInt("imageId"));
					image.setTitle(res.getString("title"));
					image.setDescription(res.getString("description"));
					image.setUploadDate(res.getTimestamp("uploadDate"));
					image.setFilePath(res.getString("filePath"));
					image.setOwnerId(res.getInt("ownerId"));
					images.add(image);
				}
				return images;
			}
		}
	}
		
	public List<Image> fetchImagesNotInAlbum(int albumId, int userId) throws SQLException {
		List<Image> images = new ArrayList<>();
		
		String query =
				"SELECT * FROM Image WHERE ownerId = ? AND imageId <> ALL (SELECT imageId FROM AlbumImages WHERE albumId = ?)";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1,  userId);
			prepStatement.setInt(2, albumId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Image image = new Image();
					image.setId(res.getInt("imageId"));
					image.setTitle(res.getString("title"));
					image.setDescription(res.getString("description"));
					image.setUploadDate(res.getTimestamp("uploadDate"));
					image.setFilePath(res.getString("filePath"));
					image.setOwnerId(res.getInt("ownerId"));
					images.add(image);
				}
				return images;
			}
		}
	}
}
