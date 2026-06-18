package xtflix;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SingleStarServlet extends HttpServlet {
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    String starId = request.getParameter("id");

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      Connection conn = DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/moviedb", "root", "zxt24131177");

      String query = "SELECT s.id, s.name, s.birthYear, " +
          "GROUP_CONCAT(DISTINCT CONCAT(m.id, '|||', m.title) ORDER BY m.title SEPARATOR ', ') AS movies " +
          "FROM stars s " +
          "LEFT JOIN stars_in_movies sim ON s.id = sim.starId " +
          "LEFT JOIN movies m ON sim.movieId = m.id " +
          "WHERE s.id = ? " +
          "GROUP BY s.id, s.name, s.birthYear";

      PreparedStatement ps = conn.prepareStatement(query);
      ps.setString(1, starId);
      ResultSet rs = ps.executeQuery();

      JsonObject jsonObject = new JsonObject();
      if (rs.next()) {
        jsonObject.addProperty("star_id", rs.getString("id"));
        jsonObject.addProperty("star_name", rs.getString("name"));
        // birthYear 可能是 NULL，用 getObject 避免空指针
        Object birthYear = rs.getObject("birthYear");
        if (birthYear != null) {
          jsonObject.addProperty("star_birth_year", (int) birthYear);
        } else {
          jsonObject.addProperty("star_birth_year", "N/A");
        }
        jsonObject.addProperty("star_movies", rs.getString("movies"));
      }
      out.write(jsonObject.toString());

      rs.close();
      ps.close();
      conn.close();
    } catch (Exception e) {
      e.printStackTrace();
      out.write("{\"error\": \"" + e.getMessage() + "\"}");
    }
  }
}