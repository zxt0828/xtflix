// ============================================
// nav.js — 所有页面共用的导航栏
// 每个 HTML 引入这个文件就自动生成导航栏
// ============================================

let navHtml = '<div class="xtflix-nav">' +
    '<a class="nav-brand" href="index.html">xtflix</a>' +
    '<div class="nav-links">' +
    '<a href="index.html">Top 20</a>' +
    '<a href="search.html">Search</a>' +
    '<a href="browse.html">Browse</a>' +
    '<a href="cart.html">Cart</a>' +
    '<a href="#" id="logout-link">Logout</a>' +
    '</div>' +
    '</div>';

let navStyle = '<style>' +
    '.xtflix-nav { display: flex; justify-content: space-between; align-items: center; ' +
    'max-width: 1200px; margin: 0 auto 30px; padding: 15px 20px; ' +
    'background: #1a1a2e; border-radius: 10px; }' +
    '.nav-brand { color: #e94560; font-size: 22px; font-weight: bold; text-decoration: none; }' +
    '.nav-links { display: flex; gap: 20px; }' +
    '.nav-links a { color: #a0a0b0; text-decoration: none; font-size: 14px; padding: 6px 12px; ' +
    'border-radius: 6px; transition: all 0.2s; }' +
    '.nav-links a:hover { color: white; background: #e94560; }' +
    '</style>';

// 插入到 body 最前面
$("body").prepend(navStyle + navHtml);

// Logout 点击事件
$(document).on("click", "#logout-link", function (e) {
  e.preventDefault();
  // 让 session 失效，跳转到登录页
  $.ajax({
    url: "api/login",
    method: "POST",
    data: { action: "logout" },
    success: function () {
      window.location.href = "login.html";
    },
    error: function () {
      // 即使请求失败也跳转
      window.location.href = "login.html";
    }
  });
});