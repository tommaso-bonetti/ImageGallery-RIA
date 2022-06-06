package it.polimi.tiw.imagegallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.imagegallery.dao.AlbumDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/ReorderAlbums")
@MultipartConfig
public class ReorderAlbums extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public ReorderAlbums() {
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
		String order = request.getParameter("order");
		List<Integer> albumOrder = new Gson().fromJson(order, new TypeToken<List<Integer>>(){}.getType());
		
		Integer userId = (Integer) request.getSession().getAttribute("userId");
		if (userId == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Invalid user session, please log out");
			return;
		}
		
		for (Integer index : albumOrder) {
			if (index == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Invalid album order");
				return;
			}
		}
		
		AlbumDAO albumDAO = new AlbumDAO(connection);
		try {
			albumDAO.reorderAlbums(userId, albumOrder);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to reorder albums");
			return;
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		response.setStatus(200);
	}
}
