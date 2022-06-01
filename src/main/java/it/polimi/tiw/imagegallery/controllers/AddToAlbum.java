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

import it.polimi.tiw.imagegallery.dao.AlbumImagesDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/AddToAlbum")
@MultipartConfig
public class AddToAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public AddToAlbum() {
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
		String targetImage = request.getParameter("targetImage");
		String targetAlbum = request.getParameter("targetAlbum");
		
		try {
			if (targetImage == null || targetImage.isEmpty())
				throw new Exception("Missing target image");
			if (targetAlbum == null || targetAlbum.isEmpty())
				throw new Exception("Missing target album");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		int targetImageId;
		int targetAlbumId;
		
		try {
			targetImageId = Integer.parseInt(targetImage);
			targetAlbumId = Integer.parseInt(targetAlbum);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid image or album selection");
			return;
		}
		
		AlbumImagesDAO albumImagesDAO = new AlbumImagesDAO(connection);
		int userId = (int) request.getSession().getAttribute("userId");
		
		try {
			albumImagesDAO.addImageToAlbum(userId, targetImageId, targetAlbumId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to add image to album");
			return;
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(targetAlbumId);
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