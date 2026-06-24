// ============================================
// checkout.js — 结账页面逻辑
// ============================================

$("#pay-btn").click(function () {
  let ccId = $("#ccId").val();
  let firstName = $("#firstName").val();
  let lastName = $("#lastName").val();
  let expiration = $("#expiration").val();

  // 简单前端验证
  if (!ccId || !firstName || !lastName || !expiration) {
    $("#error-msg").text("Please fill in all fields.").show();
    return;
  }

  // 禁用按钮防止重复提交
  $("#pay-btn").prop("disabled", true).text("Processing...");
  $("#error-msg").hide();

  $.ajax({
    url: "api/checkout",
    method: "POST",
    data: {
      ccId: ccId,
      firstName: firstName,
      lastName: lastName,
      expiration: expiration
    },
    dataType: "json",
    success: function (result) {
      if (result["status"] === "success") {
        // 隐藏表单，显示确认页面
        $("#checkout-form").hide();
        $("#back-to-cart").hide();

        // 渲染收据
        let sales = result["sales"];
        for (let i = 0; i < sales.length; i++) {
          let row = "<tr>" +
              "<td>" + sales[i]["movieId"] + "</td>" +
              "<td>" + sales[i]["quantity"] + "</td>" +
              "</tr>";
          $("#receipt-body").append(row);
        }

        $("#success-container").show();
      } else {
        // 显示错误信息
        $("#error-msg").text(result["message"]).show();
        $("#pay-btn").prop("disabled", false).text("Pay Now");
      }
    },
    error: function () {
      $("#error-msg").text("Server error. Please try again.").show();
      $("#pay-btn").prop("disabled", false).text("Pay Now");
    }
  });
});