package xtflix;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginFilter implements Filter {
  public void init(FilterConfig filerConfig){};

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;

      String uri = request.getRequestURI();

      if(uri.endsWith("/login.html") || uri.endsWith("/api/login")){
        chain.doFilter(request, response);
        return;
      }

      if(uri.endsWith(".js") || uri.endsWith(".css")){
        chain.doFilter(request, response);
        return;
      }

      HttpSession session = request.getSession(false);
      if(session != null && session.getAttribute("user") != null){
        chain.doFilter(request, response);
        return;
      }else{
        response.sendRedirect("login.html");
      }
  }
  public void destroy() {}
}
