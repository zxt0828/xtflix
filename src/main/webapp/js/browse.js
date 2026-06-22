// 页面加载时，调用 /api/genres 获取所有 genre
// 等于 Spring Boot 里前端 useEffect 里 fetch("/api/genres")

$.ajax({
  url: "api/genres",
  method: "GET",
  dataType: "json",
  success: function (genres) {
    let container = $("#genre-container");

    // 遍历每个 genre，生成一个可点击的链接
    for (let i = 0; i < genres.length; i++) {
      // 点击后跳转到 search.html?genre=Action（或其他 genre 名）
      // search.js 会从 URL 读取 genre 参数，自动发请求查询
      let link = '<a class="genre-link" href="search.html?genre=' +
          encodeURIComponent(genres[i]["genre_name"]) + '">' +
          genres[i]["genre_name"] + '</a>';
      container.append(link);
    }
  },
  error: function () {
    $("#genre-container").text("Failed to load genres.");
  }
});