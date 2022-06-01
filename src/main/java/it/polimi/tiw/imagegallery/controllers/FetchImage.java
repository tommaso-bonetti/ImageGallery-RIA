package it.polimi.tiw.imagegallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.imagegallery.beans.Image;
import it.polimi.tiw.imagegallery.dao.ImageDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/FetchImage")
public class FetchImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public FetchImage() {
		super();
	}

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		connection = ConnectionManager.getConnection(servletContext);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String imageIdString = request.getParameter("imageId");
		String albumIdString = request.getParameter("albumId");
		
		int imageId;
		int albumId;
		try {
			imageId = Integer.parseInt(imageIdString);
			albumId = Integer.parseInt(albumIdString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Invalid image or album id");
			return;
		}
		
		Image image = null;
		ImageDAO imageDAO = new ImageDAO(connection);
		try {
			image = imageDAO.fetchImageById(imageId, albumId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in retrieving images from the database");
			return;
		}
		
		if (image == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Invalid image selection");
			return;
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(image);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(json);
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
