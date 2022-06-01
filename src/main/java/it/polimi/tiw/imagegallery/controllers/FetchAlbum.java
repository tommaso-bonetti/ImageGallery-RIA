package it.polimi.tiw.imagegallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.polimi.tiw.imagegallery.beans.Album;
import it.polimi.tiw.imagegallery.beans.Image;
import it.polimi.tiw.imagegallery.dao.AlbumDAO;
import it.polimi.tiw.imagegallery.dao.ImageDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/FetchAlbum")
public class FetchAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public FetchAlbum() {
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
		String albumIdString = request.getParameter("albumId");
		
		int albumId;
		try {
			albumId = Integer.parseInt(albumIdString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Invalid album id");
			return;
		}
		
		Album album = null;
		List<Image> images = null;
		
		AlbumDAO albumDAO = new AlbumDAO(connection);
		ImageDAO imageDAO = new ImageDAO(connection);
		
		try {
			album = albumDAO.fetchAlbumById(albumId);
			images = imageDAO.fetchImagesByAlbum(albumId);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in retrieving images from the database");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Invalid page selection");
			return;
		}
		
		if (album == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Invalid album selection");
			return;
		}
		
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(gson.toJson(album), JsonObject.class);
		jsonObject.add("images", gson.toJsonTree(images));
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(jsonObject);
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
