package it.polimi.tiw.imagegallery.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
				+ " ON Album.ownerId = User.userId WHERE ownerId = ?"
				+ " ORDER BY CASE WHEN customIndex IS null THEN 1 ELSE 0 END, customIndex ASC, creationDate DESC, albumId DESC";  // apply custom order
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, ownerId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Album album = new Album();
					album.setId(res.getInt("albumId"));
					album.setTitle(res.getString("title"));
					album.setCreationDate(res.getTimestamp("creationDate"));
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
				+ " ON Album.ownerId = User.userId WHERE ownerId <> ? ORDER BY creationDate DESC, albumId DESC";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, ownerId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Album album = new Album();
					album.setId(res.getInt("albumId"));
					album.setTitle(res.getString("title"));
					album.setCreationDate(res.getTimestamp("creationDate"));
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
					album.setCreationDate(res.getTimestamp("creationDate"));
					album.setOwnerId(res.getInt("ownerId"));
					album.setOwnerUsername(res.getString("username"));
					return album;
				}
			}
		}
	}
	
	public int createAlbum(int userId, String albumTitle) throws SQLException {
		connection.setAutoCommit(false);
		
		String insert = "INSERT INTO Album (title, creationDate, ownerId, customIndex) VALUES (?, ?, ?, ?)";
		String update = "UPDATE Album SET customIndex = (customIndex + 1) WHERE albumId <> ? AND ownerId = ?";
		PreparedStatement insertPrepStatement = null;
		PreparedStatement updatePrepStatement = null;
		
		try {
			insertPrepStatement = connection.prepareStatement(insert);
			
			insertPrepStatement.setString(1, albumTitle);
			insertPrepStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			insertPrepStatement.setInt(3, userId);
			insertPrepStatement.setInt(4, 0);  // set as first in order
			insertPrepStatement.executeUpdate();
			
			updatePrepStatement = connection.prepareStatement(update);
			
			updatePrepStatement.setInt(1, albumIdByTitle(albumTitle));
			updatePrepStatement.setInt(2, userId);
			updatePrepStatement.executeUpdate();
			
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw e;
		}
		
		connection.setAutoCommit(true);
		return albumIdByTitle(albumTitle);
	}

	public List<Album> fetchOtherAlbumsByUser(int ownerId, int albumId) throws SQLException {
		List<Album> albums = new ArrayList<>();
		
		String query = "SELECT albumId, title, creationDate, ownerId, username FROM Album JOIN User"
				+ " ON Album.ownerId = User.userId WHERE ownerId = ? AND albumId <> ? ORDER BY creationDate DESC, albumId DESC";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, ownerId);
			prepStatement.setInt(2, albumId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Album album = new Album();
					album.setId(res.getInt("albumId"));
					album.setTitle(res.getString("title"));
					album.setCreationDate(res.getTimestamp("creationDate"));
					album.setOwnerId(res.getInt("ownerId"));
					album.setOwnerUsername(res.getString("username"));
					albums.add(album);
				}
			}
		}
		
		return albums;
	}
	
	public void reorderAlbums(int userId, List<Integer> order) throws Exception {
		for (int albumId : order)
			if (!checkAlbumOwnership(userId, albumId))
				throw new Exception("Moving nonexistent albums or albums owned by other users is forbidden");
		
		if (order.size() != userAlbumsNumber(userId))
			throw new Exception("Custom order must contain all albums");
		
		connection.setAutoCommit(false);
		
		try {
			for (int i = 0; i < order.size(); i++) {
				String update = "UPDATE Album SET customIndex = ? WHERE albumId = ?";
				PreparedStatement prepStatement = connection.prepareStatement(update);
				
				prepStatement.setInt(1, i);
				prepStatement.setInt(2, order.get(i));
				prepStatement.executeUpdate();
			}
			
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			connection.setAutoCommit(true);
			throw e;
		}
		
		connection.setAutoCommit(true);
	}

	public boolean hasAlbumWithTitle(int userId, String albumTitle) throws SQLException {
		String query = "SELECT * FROM Album WHERE ownerID = ? AND title = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, userId);
			prepStatement.setString(2, albumTitle);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst()) return false;
				res.next();
				return true;
			}
		}
	}
	
	private int albumIdByTitle(String title) throws SQLException {
		String query = "SELECT albumId FROM Album WHERE title = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setString(1, title);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (!res.isBeforeFirst()) return -1;
				res.next();
				return res.getInt("albumId");
			}
		}
	}
	
	private boolean checkAlbumOwnership(int userId, int albumId) throws SQLException {
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
	
	private int userAlbumsNumber(int userId) throws SQLException {
		String query = "SELECT COUNT(*) FROM Album WHERE ownerId = ?";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, userId);
			try (ResultSet res = prepStatement.executeQuery()) {
				if (res.isBeforeFirst()) {
					res.next();
					return res.getInt(1);
				} else
					return -1;
			}
		}
	}
}
