package xtflix;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CartServlet extends HttpServlet {
  //首先要从session里面先拿到数据，有就拿，没有就创建，因为可能买多部影片
  private Map<String, Integer> getCart(HttpSession session) {
    Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
    if(cart == null) {
      cart = new HashMap<>();
      session.setAttribute("cart", cart);
    }
    return cart;
  }

  //cart功能需要doPost也需要doGet
  //Get返回购物车
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    HttpSession session = request.getSession();
    Map<String, Integer> cart = getCart(session);

    JsonArray jsonArray = new JsonArray();

    if(!cart.isEmpty()) {
      try{
        Class.forName("com.mysql.cj.jdbc.Driver");
        java.sql.Connection coon = java.sql.DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/moviedb", "root", "zxt24131177");

        for(Map.Entry<String, Integer> entry : cart.entrySet()){
          String movieId = entry.getKey();
          Integer quantity = entry.getValue();

          java.sql.PreparedStatement ps = coon.prepareStatement("SELECT title FROM movies WHERE id = ?");
          ps.setString(1, movieId); //把sql语句中的第一个？设置为id
          java.sql.ResultSet rs = ps.executeQuery();

          String title = movieId;
          while(rs.next()){
            title = rs.getString("title");
          }
          rs.close();
          ps.close();

          JsonObject jsonObject = new JsonObject();
          jsonObject.addProperty("movieId", movieId);
          jsonObject.addProperty("title", title);
          jsonObject.addProperty("quantity", quantity);
          jsonArray.add(jsonObject);
        }
        coon.close();
      }catch(Exception e){
        // 查不到就只返回 movieId
        JsonObject error = new JsonObject();
        error.addProperty("errorMessage", e.getMessage());
        jsonArray.add(error);
      }
    }
    response.getWriter().print(jsonArray);
  }

  //Post写入数据: add，update，remove
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    HttpSession session = request.getSession();
    Map<String, Integer> cart = getCart(session);

    String action = request.getParameter("action");
    String movieId = request.getParameter("movieId");

    JsonObject result = new JsonObject();

    if ("add".equals(action) && movieId != null) {
      // 如果已经在购物车里，数量+1；否则加入，数量为1
      cart.put(movieId, cart.getOrDefault(movieId, 0) + 1);
      result.addProperty("status", "success");
      result.addProperty("message", "Added to cart");

    } else if ("update".equals(action) && movieId != null) {
      String quantityStr = request.getParameter("quantity");
      int quantity = Integer.parseInt(quantityStr);
      if (quantity > 0) {
        cart.put(movieId, quantity);
      } else {
        cart.remove(movieId);
      }
      result.addProperty("status", "success");
      result.addProperty("message", "Cart updated");

    } else if ("remove".equals(action) && movieId != null) {
      cart.remove(movieId);
      result.addProperty("status", "success");
      result.addProperty("message", "Removed from cart");

    } else {
      result.addProperty("status", "error");
      result.addProperty("message", "Invalid action");
      response.setStatus(400);
    }

    result.addProperty("cartSize", cart.size());
    response.getWriter().write(result.toString());
  }
}
