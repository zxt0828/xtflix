package xtflix;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenreListServlet extends HttpServlet {
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
    //先设置返回的类型是json，out是用来输出的
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    try{
      //先建立的数据库jdbc的连接
      Class.forName("com.mysql.cj.jdbc.Driver");
      Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "root", "zxt24131177");

      //写sql语句
      String query = "SELECT id, name FROM genres ORDER BY name";

      //创建查询sql的执行器 和 返回的结果集
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(query);

      //拼接结果集的json
      JsonArray jsonArray = new JsonArray();
      while(rs.next()){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("genre_id", rs.getInt("id"));
        jsonObject.addProperty("genre_name", rs.getString("name"));
        jsonArray.add(jsonObject);
      }

      //关掉所有东西
      rs.close();
      statement.close();
      conn.close();

      //out输出json
      out.write(jsonArray.toString());

    }catch(Exception e){
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("error", e.getMessage());
      out.write(jsonObject.toString());
      response.setStatus(500);
    }finally{
      out.close();
    }
  }
}
