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

public class SingleMovieServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // 从 URL 参数拿到电影 id（等于 Spring Boot 的 @RequestParam）
        String movieId = request.getParameter("id");

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/moviedb", "root", "zxt24131177");

            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres, " +
                "GROUP_CONCAT(DISTINCT CONCAT(s.id, '|||', s.name) ORDER BY s.name SEPARATOR ', ') AS stars " +
                "FROM movies m " +
                "JOIN ratings r ON m.id = r.movieId " +
                "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                "LEFT JOIN genres g ON gim.genreId = g.id " +
                "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                "LEFT JOIN stars s ON sim.starId = s.id " +
                "WHERE m.id = ? " +
                "GROUP BY m.id, m.title, m.year, m.director, r.rating";

            // 用 PreparedStatement 而不是 Statement，防止 SQL 注入
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, movieId);  // 把 ? 替换成实际的 movieId
            ResultSet rs = ps.executeQuery();

            JsonObject jsonObject = new JsonObject();
            if (rs.next()) {
              jsonObject.addProperty("movie_id", rs.getString("id"));
              jsonObject.addProperty("movie_title", rs.getString("title"));
              jsonObject.addProperty("movie_year", rs.getInt("year"));
              jsonObject.addProperty("movie_director", rs.getString("director"));
              jsonObject.addProperty("movie_rating", rs.getDouble("rating"));
              jsonObject.addProperty("movie_genres", rs.getString("genres"));
              jsonObject.addProperty("movie_stars", rs.getString("stars"));
            }
            out.write(jsonObject.toString());

            rs.close();
            ps.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

}
