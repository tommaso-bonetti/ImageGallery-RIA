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

import it.polimi.tiw.imagegallery.dao.AlbumDAO;
import it.polimi.tiw.imagegallery.dao.UserDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/CreateAlbum")
@MultipartConfig
public class CreateAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public CreateAlbum() {
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
		String username = request.getParameter("username");
		String albumTitle = request.getParameter("albumTitle");
		
		try {
			if (username == null || username.isEmpty())
				throw new Exception("Missing user session");
			if (albumTitle == null || albumTitle.isBlank())
				throw new Exception("Missing or empty album title");
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
		
		int albumId;
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		try {
			albumId = albumDAO.createAlbum(userId, albumTitle);
			if (albumId < 0) throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to create album");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(albumId);
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
