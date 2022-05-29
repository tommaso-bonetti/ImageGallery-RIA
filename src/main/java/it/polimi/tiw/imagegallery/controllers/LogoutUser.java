package it.polimi.tiw.imagegallery.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LogoutUser")
public class LogoutUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public LogoutUser() {
		super();
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		if (session != null)
			session.invalidate();
		
		response.sendRedirect(getServletContext().getContextPath() + "/GoToLoginPage");
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}
