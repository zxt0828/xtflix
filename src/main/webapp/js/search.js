// ============================================
// search.js — 搜索 / Browse by Genre / Browse by Title + 排序分页 + 加入购物车
// ============================================

let currentPage = 1;
let currentSearchParams = {};

// ============================================
// 通用渲染函数
// ============================================
function renderMovies(movies) {
  $("#movie-body").empty();

  if (movies.length === 0) {
    $("#result-count").text("No movies found.").show();
    $("#movie-table").hide();
    $("#controls").hide();
    return;
  }

  $("#result-count").text(movies.length + " movies found (Page " + currentPage + ")").show();
  $("#movie-table").show();
  $("#controls").css("display", "flex");

  $("#page-info").text("Page " + currentPage);
  if (currentPage <= 1) {
    $("#prev-btn").addClass("disabled");
  } else {
    $("#prev-btn").removeClass("disabled");
  }
  let limit = parseInt($("#per-page").val());
  if (movies.length < limit) {
    $("#next-btn").addClass("disabled");
  } else {
    $("#next-btn").removeClass("disabled");
  }

  for (let i = 0; i < movies.length; i++) {
    let movie = movies[i];

    // 处理 genres
    let genresHtml = "";
    if (movie["genres"]) {
      let genres = movie["genres"].split(", ");
      for (let j = 0; j < genres.length; j++) {
        genresHtml += '<a class="genre-tag" href="search.html?genre=' +
            encodeURIComponent(genres[j]) + '">' + genres[j] + '</a>';
      }
    }

    // 处理 stars
    let starsHtml = "";
    if (movie["stars"]) {
      let stars = movie["stars"].split(", ");
      for (let j = 0; j < stars.length; j++) {
        let parts = stars[j].split("|||");
        if (parts.length === 2) {
          starsHtml += '<a href="single-star.html?id=' + parts[0] + '">' + parts[1] + '</a>';
          if (j < stars.length - 1) starsHtml += ", ";
        }
      }
    }

    let row = "<tr>" +
        '<td><a href="single-movie.html?id=' + movie["id"] + '">' + movie["title"] + '</a></td>' +
        "<td>" + movie["year"] + "</td>" +
        "<td>" + movie["director"] + "</td>" +
        "<td>" + genresHtml + "</td>" +
        "<td>" + starsHtml + "</td>" +
        "<td>" + movie["rating"] + "</td>" +
        '<td><button class="add-cart-btn" data-movie-id="' + movie["id"] + '">Add</button></td>' +
        "</tr>";

    $("#movie-body").append(row);
  }
}

// ============================================
// 加入购物车按钮点击事件（事件委托）
// ============================================
$(document).on("click", ".add-cart-btn", function () {
  let movieId = $(this).data("movie-id");
  let btn = $(this);

  $.ajax({
    url: "api/cart",
    method: "POST",
    data: { action: "add", movieId: movieId },
    dataType: "json",
    success: function (result) {
      btn.text("Added ✓");
      btn.prop("disabled", true);
      setTimeout(function () {
        btn.text("Add");
        btn.prop("disabled", false);
      }, 1500);
    },
    error: function () {
      alert("Failed to add to cart");
    }
  });
});

// ============================================
// 发送搜索请求
// ============================================
function doSearch(searchParams, page) {
  currentPage = page;
  currentSearchParams = searchParams;

  let requestData = $.extend({}, searchParams, {
    sort: $("#sort-by").val(),
    order: $("#sort-order").val(),
    limit: $("#per-page").val(),
    page: currentPage
  });

  $.ajax({
    url: "api/search",
    method: "GET",
    data: requestData,
    dataType: "json",
    success: renderMovies,
    error: function () {
      $("#result-count").text("Error occurred. Please try again.").show();
    }
  });
}

// ============================================
// 检查 URL 参数
// ============================================
let urlParams = new URLSearchParams(window.location.search);
let genreFromUrl = urlParams.get("genre");
let titleCharFromUrl = urlParams.get("titleChar");

if (genreFromUrl) {
  $(".search-form").hide();
  $("h1").text("Genre: " + genreFromUrl);
  doSearch({ genre: genreFromUrl }, 1);

} else if (titleCharFromUrl) {
  $(".search-form").hide();
  if (titleCharFromUrl === "*") {
    $("h1").text("Titles starting with: special characters");
  } else {
    $("h1").text("Titles starting with: " + titleCharFromUrl.toUpperCase());
  }
  doSearch({ titleChar: titleCharFromUrl }, 1);
}

// ============================================
// 搜索按钮
// ============================================
$("#search-btn").click(function () {
  let searchParams = {
    title: $("#title").val(),
    year: $("#year").val(),
    director: $("#director").val(),
    star: $("#star").val()
  };
  doSearch(searchParams, 1);
});

$(document).keypress(function (event) {
  if (event.which === 13) {
    $("#search-btn").click();
  }
});

// ============================================
// 分页
// ============================================
$("#prev-btn").click(function () {
  if (currentPage > 1) {
    doSearch(currentSearchParams, currentPage - 1);
  }
});

$("#next-btn").click(function () {
  doSearch(currentSearchParams, currentPage + 1);
});

// ============================================
// 排序/每页切换
// ============================================
$("#sort-by, #sort-order, #per-page").change(function () {
  doSearch(currentSearchParams, 1);
});