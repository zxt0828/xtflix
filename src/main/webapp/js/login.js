// 点击登录按钮时触发
// Spring Boot + React 里对应 onSubmit 事件 + fetch POST
$("#login-btn").click(function () {

  // 从输入框取值
  let email = $("#email").val();
  let password = $("#password").val();

  // 发 POST 请求到 LoginServlet
  // 对应 Spring Boot 里 fetch("/api/login", { method: "POST", body: ... })
  $.ajax({
    url: "api/login",
    method: "POST",
    data: {
      email: email,
      password: password
    },
    success: function (response) {
      if (response["status"] === "success") {
        // 登录成功，跳转首页
        window.location.href = "index.html";
      } else {
        // 登录失败，显示错误信息
        $("#error-message").text(response["message"]);
        $("#error-message").show();
      }
    },
    error: function () {
      $("#error-message").text("Something went wrong. Please try again.");
      $("#error-message").show();
    }
  });
});

// 按回车也能登录
$(document).keypress(function (event) {
  if (event.which === 13) {
    $("#login-btn").click();
  }
});