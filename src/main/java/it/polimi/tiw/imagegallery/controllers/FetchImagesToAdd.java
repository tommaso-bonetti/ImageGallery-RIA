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

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.polimi.tiw.imagegallery.beans.Album;
import it.polimi.tiw.imagegallery.beans.Image;
import it.polimi.tiw.imagegallery.dao.AlbumDAO;
import it.polimi.tiw.imagegallery.dao.ImageDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/FetchImagesToAdd")
public class FetchImagesToAdd extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public FetchImagesToAdd() {
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
		String albumIdString = StringEscapeUtils.escapeJava(request.getParameter("albumId"));
		int albumId;
		
		Integer userId = (Integer) request.getSession().getAttribute("userId");		
		if (userId == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Missing user session");
			return;
		}
		
		try {
			albumId = Integer.parseInt(albumIdString);
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Invalid album id");
			return;
		}
		
		List<Image> images = null;
		ImageDAO imageDAO = new ImageDAO(connection);
		Album album = null;
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		try {
			images = imageDAO.fetchImagesNotInAlbum(albumId, userId);
			album = albumDAO.fetchAlbumById(albumId);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in retrieving images from the database");
			return;
		}
		
		if (album == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("Invalid album selection");
			return;
		}
		
		Gson gson = new Gson();
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("album", gson.toJsonTree(album));
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
