// ============================================
// cart.js — 购物车页面逻辑
// ============================================

// 加载购物车内容
function loadCart() {
  $.ajax({
    url: "api/cart",
    method: "GET",
    dataType: "json",
    success: function (cartItems) {
      $("#cart-body").empty();

      if (cartItems.length === 0) {
        $("#cart-table").hide();
        $("#cart-actions").hide();
        $("#empty-msg").show();
        return;
      }

      $("#empty-msg").hide();
      $("#cart-table").show();
      $("#cart-actions").show();

      for (let i = 0; i < cartItems.length; i++) {
        let item = cartItems[i];
        let row = "<tr>" +
            "<td>" + item["title"] + "</td>" +
            '<td><input type="number" class="quantity-input" ' +
            'data-movie-id="' + item["movieId"] + '" ' +
            'value="' + item["quantity"] + '" min="1"></td>' +
            '<td>' +
            '<button class="update-btn" data-movie-id="' + item["movieId"] + '">Update</button>' +
            '<button class="remove-btn" data-movie-id="' + item["movieId"] + '">Remove</button>' +
            '</td>' +
            "</tr>";
        $("#cart-body").append(row);
      }
    }
  });
}

// 页面加载时获取购物车
loadCart();

// 更新数量
$(document).on("click", ".update-btn", function () {
  let movieId = $(this).data("movie-id");
  let quantity = $('input[data-movie-id="' + movieId + '"]').val();

  $.ajax({
    url: "api/cart",
    method: "POST",
    data: { action: "update", movieId: movieId, quantity: quantity },
    dataType: "json",
    success: function () {
      loadCart();
    }
  });
});

// 删除
$(document).on("click", ".remove-btn", function () {
  let movieId = $(this).data("movie-id");

  $.ajax({
    url: "api/cart",
    method: "POST",
    data: { action: "remove", movieId: movieId },
    dataType: "json",
    success: function () {
      loadCart();
    }
  });
});

// 结账
$("#checkout-btn").click(function () {
  window.location.href = "checkout.html";
});