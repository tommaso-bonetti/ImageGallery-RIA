package it.polimi.tiw.imagegallery.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.imagegallery.beans.Album;

public class AlbumDAO {
	private final Connection connection;
	
	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Album> fetchAlbumsByOwner(int ownerId) throws SQLException {
		List<Album> albums = new ArrayList<>();
		
		String query = "SELECT albumId, title, creationDate, ownerId, username FROM Album JOIN User"
				+ " ON Album.ownerId = User.userId WHERE ownerId = ? ORDER BY albumId DESC";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, ownerId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Album album = new Album();
					album.setId(res.getInt("albumId"));
					album.setTitle(res.getString("title"));
					album.setCreationDate(res.getDate("creationDate"));
					album.setOwnerId(res.getInt("ownerId"));
					album.setOwnerUsername(res.getString("username"));
					albums.add(album);
				}
			}
		}
		
		return albums;
	}
	
	public List<Album> fetchAlbumsNotByOwner(int ownerId) throws SQLException {
		List<Album> albums = new ArrayList<>();
		
		String query = "SELECT albumId, title, creationDate, ownerId, username FROM Album JOIN User"
				+ " ON Album.ownerId = User.userId WHERE ownerId <> ? ORDER BY albumId DESC";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, ownerId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Album album = new Album();
					album.setId(res.getInt("albumId"));
					album.setTitle(res.getString("title"));
					album.setCreationDate(res.getDate("creationDate"));
					album.setOwnerId(res.getInt("ownerId"));
					album.setOwnerUsername(res.getString("username"));
					albums.add(album);
				}
			}
		}
		
		return albums;
	}

	public Album fetchAlbumById(int albumId) throws SQLException {
		String query = "SELECT albumId, title, creationDate, ownerId, username FROM Album JOIN User"
				+ " ON Album.ownerId = User.userId WHERE albumId = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, albumId);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst())
					return null;
				else {
					res.next();
					Album album= new Album();
					album.setId(res.getInt("albumId"));
					album.setTitle(res.getString("title"));
					album.setCreationDate(res.getDate("creationDate"));
					album.setOwnerId(res.getInt("ownerId"));
					album.setOwnerUsername(res.getString("username"));
					return album;
				}
			}
		}
	}
	
	public void createAlbum(int userId, String albumTitle) throws SQLException {
		String query = "INSERT INTO Album (title, creationDate, ownerId) VALUES (?, ?, ?)";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setString(1, albumTitle);
			prepStatement.setDate(2, new Date(System.currentTimeMillis()));
			prepStatement.setInt(3, userId);
			prepStatement.executeUpdate();
		}
	}

	public List<Album> fetchOtherAlbumsByUser(int ownerId, int albumId) throws SQLException {
		List<Album> albums = new ArrayList<>();
		
		String query = "SELECT albumId, title, creationDate, ownerId, username FROM Album JOIN User"
				+ " ON Album.ownerId = User.userId WHERE ownerId = ? AND albumId <> ? ORDER BY albumId DESC";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, ownerId);
			prepStatement.setInt(2, albumId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Album album = new Album();
					album.setId(res.getInt("albumId"));
					album.setTitle(res.getString("title"));
					album.setCreationDate(res.getDate("creationDate"));
					album.setOwnerId(res.getInt("ownerId"));
					album.setOwnerUsername(res.getString("username"));
					albums.add(album);
				}
			}
		}
		
		return albums;
	}
}
