package xtflix;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CheckoutServlet extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException{
    response.setContentType("application/json");
    JsonObject result = new JsonObject();

    //去session拿东西
    HttpSession session = request.getSession();
    Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
    User user = (User) session.getAttribute("user");

    if(cart == null || cart.isEmpty()){
      result.addProperty("status", "error");
      result.addProperty("message", "Cart is empty");
      response.getWriter().println(result.toString());
      return;
    }

    if(user == null){
      result.addProperty("status", "error");
      result.addProperty("message", "Not logged in");
      response.getWriter().println(result.toString());
      return;
    }

    //取信用卡的参数
    String ccId = request.getParameter("ccId");
    String firstName = request.getParameter("firstName");
    String lastName = request.getParameter("lastName");
    String expiration = request.getParameter("expiration");

    Connection conn = null;

    try{
      Class.forName("com.mysql.cj.jdbc.Driver");
      conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "root", "zxt24131177");

      PreparedStatement verify = conn.prepareStatement(
          "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?");
      verify.setString(1, ccId);
      verify.setString(2, firstName);
      verify.setString(3, lastName);
      verify.setString(4, expiration);
      ResultSet rs = verify.executeQuery();

      if(!rs.next()){
        //如果验证失败
        result.addProperty("status", "error");
        result.addProperty("message", "Invalid credit card information");
        rs.close();
        verify.close();
        conn.close();
        response.getWriter().write(result.toString());
        return;
      }
      rs.close();
      verify.close();

      //开启事务，spring中的@Transactional就是try-catch封装了下面的三个
      conn.setAutoCommit(false);

      //防止sql注入
      PreparedStatement insertStmt = conn.prepareStatement(
          "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, CURDATE())");

      JsonArray salesArray = new JsonArray();

      for(Map.Entry<String, Integer> entry : cart.entrySet()){
        String movieId = entry.getKey();
        int quantity = entry.getValue();

        for(int i = 0; i < quantity; i++){
          insertStmt.setInt(1, user.getId());
          insertStmt.setString(2, movieId);
          insertStmt.addBatch();
        }

        JsonObject sale = new JsonObject();
        sale.addProperty("movieId", movieId);
        sale.addProperty("quantity", quantity);
        salesArray.add(sale);
      }

      insertStmt.executeBatch();
      conn.commit();//这里就是手动的
      insertStmt.close();

      //清空cart
      session.removeAttribute("cart");

      result.addProperty("status", "success");
      result.addProperty("message", "Purchase successful!");
      result.add("sales", salesArray);

    }catch(Exception e){
      //执行失败就rollback
      try {
        if (conn != null) conn.rollback();
      } catch (Exception rollbackEx) {
        // 回滚也失败就没办法了，写个日志
        rollbackEx.printStackTrace();
      }
      result.addProperty("status", "error");
      result.addProperty("message", e.getMessage());
    }finally{
      try {
        if (conn != null) conn.close();
      } catch (Exception closeEx) {
        // 忽略关闭异常，写个日志
        closeEx.printStackTrace();
      }
    }

    response.getWriter().write(result.toString());
  }
}
