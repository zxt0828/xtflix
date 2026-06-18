/**
 * single-movie.js
 *
 * 从 URL 参数拿到电影 id，发 AJAX 请求到 /api/single-movie?id=xxx，
 * 拿到单部电影的 JSON 数据，渲染到详情页里。
 */

$(document).ready(function () {

  // 从 URL 拿到 id 参数
  // 比如 single-movie.html?id=tt0012345 → 拿到 "tt0012345"
  var urlParams = new URLSearchParams(window.location.search);
  var movieId = urlParams.get("id");

  $.ajax({
    url: "api/single-movie",
    method: "GET",
    data: { id: movieId },   // jQuery 会自动拼成 ?id=tt0012345
    dataType: "json",

    success: function (data) {
      var container = $("#movie-detail");
      container.empty();

      // 如果没有数据（比如 id 不存在）
      if (!data.movie_title) {
        container.html('<p class="loading">Movie not found.</p>');
        return;
      }

      // 更新页面标题
      document.title = "Xtflix - " + data.movie_title;

      // --- 构建 Genres 标签 ---
      var genresHtml = "";
      if (data.movie_genres) {
        var genres = data.movie_genres.split(", ");
        for (var g = 0; g < genres.length; g++) {
          genresHtml += '<span class="genre-tag">' + genres[g] + "</span>";
        }
      }

      // --- 构建 Stars 列表（带超链接）---
      var starsHtml = "";
      if (data.movie_stars) {
        var starPairs = data.movie_stars.split(", ");
        starsHtml = '<ul class="stars-list">';
        for (var s = 0; s < starPairs.length; s++) {
          var parts = starPairs[s].split("|||");
          var starId = parts[0];
          var starName = parts[1];
          starsHtml += '<li><a class="star-link" href="single-star.html?id=' + starId + '">' + starName + "</a></li>";
        }
        starsHtml += "</ul>";
      }

      // --- 拼装完整页面 ---
      var html =
          '<div class="movie-header">' +
          '<h1 class="movie-title">' + data.movie_title + "</h1>" +
          '<div class="movie-meta">' +
          "<span>" + data.movie_year + "</span>" +
          "<span>Directed by " + data.movie_director + "</span>" +
          '<span class="rating-badge">' + data.movie_rating + "</span>" +
          "</div>" +
          '<p class="movie-id">ID: ' + data.movie_id + "</p>" +
          "</div>" +

          '<div class="detail-section">' +
          '<p class="detail-label">Genres</p>' +
          genresHtml +
          "</div>" +

          '<div class="detail-section">' +
          '<p class="detail-label">Stars</p>' +
          starsHtml +
          "</div>";

      container.html(html);
    },

    error: function (jqXHR, textStatus) {
      $("#movie-detail").html(
          '<p class="loading">Failed to load movie: ' + textStatus + "</p>"
      );
    }
  });
});