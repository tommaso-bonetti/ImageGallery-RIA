package it.polimi.tiw.imagegallery.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.imagegallery.beans.Comment;

public class CommentDAO {
	private Connection connection;
	
	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Comment> fetchCommentsByImage(int imageId) throws SQLException {
		List<Comment> comments = new ArrayList<>();
		
		String query = "SELECT commentId, body, publisherId, imageId, publishedDate, username FROM Comment JOIN User"
				+ " ON Comment.publisherId = User.userId WHERE imageId = ? ORDER BY commentId ASC";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setInt(1, imageId);
			try (ResultSet res = prepStatement.executeQuery()) {
				while (res.next()) {
					Comment comment = new Comment();
					comment.setId(res.getInt("commentId"));
					comment.setBody(res.getString("body"));
					comment.setPublisherId(res.getInt("publisherId"));
					comment.setPublisherUsername(res.getString("username"));
					comment.setImageId(res.getInt("imageId"));
					comment.setPublishedDate(res.getDate("publishedDate"));
					comments.add(comment);
				}
			}
		}
		
		return comments;
	}
	
	public void createComment(int publisherId, int imageId, String body) throws SQLException {		
		String query = "INSERT INTO Comment (body, publisherId, imageId, publishedDate) VALUES (?, ?, ?, ?)";
		try (PreparedStatement prepStatement = connection.prepareStatement(query)) {
			prepStatement.setString(1, body);
			prepStatement.setInt(2, publisherId);
			prepStatement.setInt(3, imageId);
			prepStatement.setDate(4, new Date(System.currentTimeMillis()));
			prepStatement.executeUpdate();
		}
	}
}
