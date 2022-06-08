package it.polimi.tiw.imagegallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.imagegallery.dao.CommentDAO;
import it.polimi.tiw.imagegallery.dao.UserDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/PublishComment")
@MultipartConfig
public class PublishComment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public PublishComment() {
		super();
	}
	
	@Override
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionManager.getConnection(servletContext);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		String imageIdString = StringEscapeUtils.escapeJava(request.getParameter("imageId"));
		String commentBody = StringEscapeUtils.escapeJava(request.getParameter("commentBody"));
		
		try {
			if (username == null || username.isEmpty())
				throw new Exception("Missing user session");
			if (imageIdString == null || imageIdString.isEmpty())
				throw new Exception("Missing image id");
			if (commentBody == null || commentBody.isEmpty())
				throw new Exception("Missing comment body");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		UserDAO userDAO = new UserDAO(connection);
		Integer userId = (Integer) request.getSession().getAttribute("userId");
		try {
			if (userId == null || userDAO.getUser(username).getId() != userId) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Invalid user session, please log out");
				return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to verify user session");
			return;
		}
		
		int imageId;
		
		try {
			imageId = Integer.parseInt(imageIdString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid image selection");
			return;
		}
		
		CommentDAO commentDAO = new CommentDAO(connection);
		
		try {
			commentDAO.createComment(userId, imageId, commentBody);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to publish comment");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(imageId);
	}

	@Override
	public void destroy() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e){
				e.printStackTrace();
			}
		}
	}
}
