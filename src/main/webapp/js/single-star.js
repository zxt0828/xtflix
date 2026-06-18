/**
 * single-star.js
 *
 * 从 URL 参数拿到 star id，发 AJAX 请求到 /api/single-star?id=xxx，
 * 拿到演员的 JSON 数据，渲染到详情页里。
 */

$(document).ready(function () {

  var urlParams = new URLSearchParams(window.location.search);
  var starId = urlParams.get("id");

  $.ajax({
    url: "api/single-star",
    method: "GET",
    data: { id: starId },
    dataType: "json",

    success: function (data) {
      var container = $("#star-detail");
      container.empty();

      if (!data.star_name) {
        container.html('<p class="loading">Star not found.</p>');
        return;
      }

      document.title = "Xtflix - " + data.star_name;

      // --- 构建电影列表（带超链接）---
      var moviesHtml = "";
      if (data.star_movies) {
        var moviePairs = data.star_movies.split(", ");
        moviesHtml = '<ul class="movies-list">';
        for (var i = 0; i < moviePairs.length; i++) {
          var parts = moviePairs[i].split("|||");
          var movieId = parts[0];
          var movieTitle = parts[1];
          moviesHtml += '<li><a class="movie-link" href="single-movie.html?id=' + movieId + '">' + movieTitle + "</a></li>";
        }
        moviesHtml += "</ul>";
      }

      // --- 拼装完整页面 ---
      var html =
          '<div class="star-header">' +
          '<h1 class="star-name">' + data.star_name + "</h1>" +
          '<div class="star-meta">Born: ' + data.star_birth_year + "</div>" +
          '<p class="star-id">ID: ' + data.star_id + "</p>" +
          "</div>" +

          '<div class="detail-section">' +
          '<p class="detail-label">Movies</p>' +
          moviesHtml +
          "</div>";

      container.html(html);
    },

    error: function (jqXHR, textStatus) {
      $("#star-detail").html(
          '<p class="loading">Failed to load star: ' + textStatus + "</p>"
      );
    }
  });
});