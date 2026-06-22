package xtflix;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SearchServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    response.setContentType("application/json");

    // 1. 取搜索参数
    String title = request.getParameter("title");
    String year = request.getParameter("year");
    String director = request.getParameter("director");
    String star = request.getParameter("star");
    String genre = request.getParameter("genre");
    String titleChar = request.getParameter("titlechar");
    //分页排序的参数
    String sort = request.getParameter("sort");       // "title" 或 "rating"
    String order = request.getParameter("order");     // "asc" 或 "desc"
    String limitStr = request.getParameter("limit");  // "20", "50", "100"
    String pageStr = request.getParameter("page");    // "1", "2", "3"...

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      Connection connection = DriverManager.getConnection(
          "jdbc:mysql://localhost:3306/moviedb", "root", "zxt24131177");

      // 2. 动态拼接 SQL
      String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
          "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres, " +
          "GROUP_CONCAT(DISTINCT CONCAT(s.id, '|||', s.name) ORDER BY s.name SEPARATOR ', ') AS stars " +
          "FROM movies m " +
          "JOIN ratings r ON m.id = r.movieId " +
          "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
          "LEFT JOIN genres g ON gim.genreId = g.id " +
          "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
          "LEFT JOIN stars s ON sim.starId = s.id ";

      // 动态 WHERE 条件
      List<String> conditions = new ArrayList<>();
      List<Object> params = new ArrayList<>();

      if (title != null && !title.isEmpty()) {
        conditions.add("m.title LIKE ?");
        params.add("%" + title + "%");
      }
      if (year != null && !year.isEmpty()) {
        conditions.add("m.year = ?");
        params.add(Integer.parseInt(year));
      }
      if (director != null && !director.isEmpty()) {
        conditions.add("m.director LIKE ?");
        params.add("%" + director + "%");
      }
      if (star != null && !star.isEmpty()) {
        conditions.add("s.name LIKE ?");
        params.add("%" + star + "%");
      }
      if (genre != null && !genre.isEmpty()) {
        conditions.add("g.name = ?");
        params.add(genre);
      }
      if (titleChar != null && !titleChar.isEmpty()) {
        if (titleChar.equals("*")) {
          conditions.add("m.title REGEXP '^[^a-zA-Z0-9]'");
        } else {
          conditions.add("m.title LIKE ?");
          params.add(titleChar + "%");
        }
      }

      if (!conditions.isEmpty()) {
        query += "WHERE " + String.join(" AND ", conditions) + " ";
      }

      query += "GROUP BY m.id, m.title, m.year, m.director, r.rating ";

      if ("title".equals(sort)) {
        query += "ORDER BY m.title " + ("asc".equals(order) ? "ASC" : "DESC") + " ";
      } else {
        query += "ORDER BY r.rating " + ("asc".equals(order) ? "ASC" : "DESC") + " ";
      }

      // 动态分页
      int limit = 20;
      int page = 1;
      try {
        if (limitStr != null) limit = Integer.parseInt(limitStr);
        if (pageStr != null) page = Integer.parseInt(pageStr);
      } catch (NumberFormatException e) {
        // 参数非法就用默认值
      }
      int offset = (page - 1) * limit;
      query += "LIMIT " + limit + " OFFSET " + offset;

      // 3. 设置参数并执行
      PreparedStatement statement = connection.prepareStatement(query);
      for (int i = 0; i < params.size(); i++) {
        Object param = params.get(i);
        if (param instanceof Integer) {
          statement.setInt(i + 1, (Integer) param);
        } else {
          statement.setString(i + 1, (String) param);
        }
      }

      ResultSet rs = statement.executeQuery();

      // 4. 拼 JSON（跟 MovieListServlet 一样）
      JsonArray movieArray = new JsonArray();
      while (rs.next()) {
        JsonObject movie = new JsonObject();
        movie.addProperty("id", rs.getString("id"));
        movie.addProperty("title", rs.getString("title"));
        movie.addProperty("year", rs.getInt("year"));
        movie.addProperty("director", rs.getString("director"));
        movie.addProperty("rating", rs.getFloat("rating"));
        movie.addProperty("genres", rs.getString("genres"));
        movie.addProperty("stars", rs.getString("stars"));
        movieArray.add(movie);
      }

      rs.close();
      statement.close();
      connection.close();

      response.getWriter().write(movieArray.toString());

    } catch (Exception e) {
      JsonObject error = new JsonObject();
      error.addProperty("errorMessage", e.getMessage());
      response.getWriter().write(error.toString());
    }
  }
}
