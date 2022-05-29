package it.polimi.tiw.imagegallery.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;

import it.polimi.tiw.imagegallery.beans.User;
import it.polimi.tiw.imagegallery.dao.UserDAO;
import it.polimi.tiw.imagegallery.utils.ConnectionManager;

@WebServlet("/RegisterUser")
@MultipartConfig
public class RegisterUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public RegisterUser() {
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
		String email = null;
		String username = null;
		String password = null;
		String repeatPassword = null;
		
		Pattern emailPattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]+[a-zA-Z0-9]@[a-zA-Z][a-zA-Z0-9.-]+[a-zA-Z]$");
		Pattern usernamePattern = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.-]*$");
		
		List<String> errorMessages = new ArrayList<>();
		Gson gson = new Gson();
		
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		password = StringEscapeUtils.escapeJava(request.getParameter("password"));
		repeatPassword = StringEscapeUtils.escapeJava(request.getParameter("repeatPassword"));
		if (email == null || email.isEmpty())
			errorMessages.add("Missing or empty email value");
		if (username == null || username.isEmpty())
			errorMessages.add("Missing or empty username value");
		if (password == null || password.isEmpty())
			errorMessages.add("Missing or empty password value");
		if (repeatPassword == null || repeatPassword.isEmpty())
			errorMessages.add("Missing or empty repeat password value");
		
		if (errorMessages.size() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String json = gson.toJson(errorMessages);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
			return;
		}
		
		if (!emailPattern.matcher(email).matches()) {
			System.out.println("Invalid email format");
			errorMessages.add("Invalid email format");
		}
		if (!usernamePattern.matcher(username).matches()) {
			System.out.println("Username can only contain letters, numbers, underscores, dots and hyphens, must start with a letter");
			errorMessages.add("Username can only contain letters, numbers, underscores, dots and hyphens, must start with a letter");
		}
		if (password.length() < 6) {
			System.out.println("Password needs to be at least 6 characters long");
			errorMessages.add("Password needs to be at least 6 characters long");
		}
		if (!password.equals(repeatPassword)) {
			System.out.println("Password and repeat password do not match");
			errorMessages.add("Password and repeat password do not match");
		}
		
		if (errorMessages.size() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String json = gson.toJson(errorMessages);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(json);
		} else {
			UserDAO userDAO = new UserDAO(connection);
			User user = null;
			
			try {
				user = userDAO.createUser(email, username, password);
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Unable to register new user");
				return;
			}
			
			if (user == null) {
				response.setStatus(HttpServletResponse.SC_CONFLICT);
				response.getWriter().println("Username already exists");
			} else {
				request.getSession().setAttribute("userId", user.getId());
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(user.getUsername());
			}
		}
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
