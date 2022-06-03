package it.polimi.tiw.imagegallery.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginChecker implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("LoginChecker filter executing...");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession session = req.getSession();
		
		if (session.isNew() || session.getAttribute("userId") == null) {
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.setHeader("Location", req.getServletContext().getContextPath() + "/login.html");
			return;
		}
		
		chain.doFilter(request, response);
	}

}
