/**
 * movie-list.js
 *
 * 页面加载后发 AJAX 请求到 /api/movies，
 * 拿到 Top 20 电影的 JSON 数据，渲染到表格里。
 */

$(document).ready(function () {

  $.ajax({
    url: "api/movies",
    method: "GET",
    dataType: "json",

    success: function (data) {
      var tbody = $("#movie-body");
      tbody.empty();

      for (var i = 0; i < data.length; i++) {
        var movie = data[i];

        // --- Genres: 拆成小标签 ---
        var genresHtml = "";
        if (movie.movie_genres) {
          var genres = movie.movie_genres.split(", ");
          for (var g = 0; g < genres.length; g++) {
            genresHtml += '<span class="genre-tag">' + genres[g] + "</span>";
          }
        }

        // --- Stars: 拆出 id 和 name，生成超链接 ---
        // 后端传过来的格式: "nm001|||Tom Hanks, nm002|||Meg Ryan"
        var starsHtml = "";
        if (movie.movie_stars) {
          var starPairs = movie.movie_stars.split(", ");
          for (var s = 0; s < starPairs.length; s++) {
            var parts = starPairs[s].split("|||");
            var starId = parts[0];
            var starName = parts[1];
            if (s > 0) {
              starsHtml += '<span class="star-separator"> · </span>';
            }
            starsHtml += '<a class="star-link" href="single-star.html?id=' + starId + '">' + starName + "</a>";
          }
        }

        var row = "<tr>" +
            '<td class="movie-rank">' + (i + 1) + "</td>" +
            '<td class="movie-title"><a href="single-movie.html?id=' + movie.movie_id + '">' + movie.movie_title + "</a></td>" +
            '<td class="movie-year">' + movie.movie_year + "</td>" +
            '<td class="movie-director">' + movie.movie_director + "</td>" +
            "<td>" + genresHtml + "</td>" +
            "<td>" + starsHtml + "</td>" +
            '<td><span class="rating-badge">' + movie.movie_rating + "</span></td>" +
            "</tr>";
        tbody.append(row);
      }
    },

    error: function (jqXHR, textStatus, errorThrown) {
      var tbody = $("#movie-body");
      tbody.empty();
      tbody.append(
          '<tr><td colspan="7" class="loading">Failed to load movies: ' +
          textStatus + "</td></tr>"
      );
    }
  });
});