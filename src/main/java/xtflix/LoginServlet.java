package xtflix;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {
      protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws IOException {
        // 处理 logout
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
          HttpSession session = request.getSession(false);
          if (session != null) {
            session.invalidate();
          }
          JsonObject result = new JsonObject();
          result.addProperty("status", "success");
          result.addProperty("message", "Logged out");
          response.setContentType("application/json");
          response.getWriter().write(result.toString());
          return;
        }
        //取参数
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        //设置返回类型
        response.setContentType("application/json");
        JsonObject responseJsonObject = new JsonObject();

        try{
          Class.forName("com.mysql.cj.jdbc.Driver");
          Connection connection = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");

          // 3. 查 customers 表
          String query = "SELECT * FROM customers WHERE email = ? AND password = ?";
          PreparedStatement statement = connection.prepareStatement(query);
          statement.setString(1, email);
          statement.setString(2, password);
          ResultSet rs = statement.executeQuery();

          if (rs.next()) {
            // 4a. 验证成功 → 创建 Session
            HttpSession session = request.getSession();
            session.setAttribute("user", new User(rs.getInt("id"), rs.getString("email")));

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");
          } else {
            // 4b. 验证失败
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Incorrect email or password");
          }

          rs.close();
          statement.close();
          connection.close();
        }catch (Exception e){
          responseJsonObject.addProperty("status", "fail");
          responseJsonObject.addProperty("message", e.getMessage());
        }
        response.getWriter().write(responseJsonObject.toString());
      }
}
