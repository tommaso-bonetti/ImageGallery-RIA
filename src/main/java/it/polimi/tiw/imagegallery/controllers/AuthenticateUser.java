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
import it.polimi.tiw.imagegallery.beans.User;
import it.polimi.tiw.imagegallery.dao.UserDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/AuthenticateUser")
@MultipartConfig
public class AuthenticateUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public AuthenticateUser() {
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
		String username = null;
		String password = null;
		
		try {
			username = StringEscapeUtils.escapeJava(request.getParameter("username"));
			password = StringEscapeUtils.escapeJava(request.getParameter("password"));
			if (username == null || username.isEmpty())
				throw new Exception("Missing or empty username value");
			if (password == null || password.isEmpty())
				throw new Exception("Missing or empty password value");
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		
		try {
			user = userDAO.checkCredentials(username, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Unable to check user credentials");
			return;
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		request.getSession().setAttribute("userId", user.getId());
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().println(user.getUsername());
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
