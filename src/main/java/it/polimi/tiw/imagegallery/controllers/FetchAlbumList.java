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

import it.polimi.tiw.imagegallery.beans.Album;
import it.polimi.tiw.imagegallery.dao.AlbumDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/FetchAlbumList")
public class FetchAlbumList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public FetchAlbumList() {
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
		String ownAlbums = StringEscapeUtils.escapeJava(request.getParameter("ownAlbums"));
		String albumIdString = StringEscapeUtils.escapeJava(request.getParameter("albumId"));
		
		Integer currentUserId = (Integer) request.getSession().getAttribute("userId");		
		if (currentUserId == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Missing user session");
			return;
		}
		
		List<Album> albums = null;
		AlbumDAO albumDAO = new AlbumDAO(connection);
		
		if (albumIdString != null) {
			if (ownAlbums == null || !ownAlbums.equals("true")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid request parameters");
				return;
			}
			
			int albumId;
			try {
				albumId = Integer.parseInt(albumIdString);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Invalid album id");
				return;
			}
			
			try {
				albums = albumDAO.fetchOtherAlbumsByUser(currentUserId, albumId);
			} catch (SQLException e) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Error in retrieving albums from the database");
				return;
			}
		} else {
			try {
				if (ownAlbums != null && ownAlbums.equals("true"))
					albums = albumDAO.fetchAlbumsByOwner(currentUserId);
				else
					albums = albumDAO.fetchAlbumsNotByOwner(currentUserId);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Error in retrieving albums from the database");
				return;
			}
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(albums);
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
