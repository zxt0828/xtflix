# xtflix

A full-stack movie database web application built with Java Servlets, JDBC, MySQL, and jQuery — without Spring Boot — to demonstrate foundational understanding of how web frameworks work under the hood.

## About

xtflix lets users browse, search, and purchase movies from a database of ~10,000 films. It was built from scratch using the low-level Java web stack to understand what frameworks like Spring Boot abstract away: manual servlet routing, raw JDBC queries, session management, and request/response handling.

## Tech Stack

- **Backend:** Java 11, Servlets (javax.servlet), JDBC, Gson
- **Frontend:** HTML/CSS, JavaScript, jQuery/AJAX
- **Database:** MySQL (moviedb — 9 tables, ~10K movies, ~60K stars)
- **Server:** Apache Tomcat 9
- **Build:** Maven (WAR packaging)
- **Deployment:** AWS EC2 + RDS

## Features

- **Movie List** — Top 20 rated movies with genres, stars, and ratings (GROUP_CONCAT aggregation)
- **Single Movie / Single Star** — Detail pages with cross-linked navigation using PreparedStatement
- **User Login** — POST-based authentication with HttpSession management
- **Search** — Multi-field search (title, year, director, star) with dynamic SQL WHERE clause construction
- **Browse by Genre** — Genre list fetched dynamically from DB; click to filter movies
- **Browse by Title** — A-Z, 0-9, and special character filtering with LIKE and REGEXP
- **Sorting** — Sort results by title or rating, ascending or descending
- **Pagination** — Server-side pagination with SQL LIMIT/OFFSET, configurable page size (20/50/100)
- **Shopping Cart** — Session-based cart with add, update quantity, and remove functionality
- **Checkout** — Credit card validation against DB + transactional writes to sales table with confirmation page
- **Navigation Bar** — Consistent nav across all pages (Top 20, Search, Browse, Cart, Logout)
- **Login Protection** — Servlet Filter redirects unauthenticated users to login page; only login endpoint is public

## Architecture

```
Browser (jQuery/AJAX)
    ↓ HTTP GET/POST
Tomcat 9
    ↓ web.xml routing
Servlet Filter (authentication check)
    ↓
Servlets (Controller + Service + DAO in one layer)
    ↓ JDBC / PreparedStatement
MySQL (moviedb)
```

Each servlet handles the full request cycle: parse parameters, build dynamic SQL, execute query, serialize to JSON with Gson, and write the response. This is what Spring Boot's `@RestController` + `@RequestParam` + JPA + Jackson do automatically — here it's all manual.

### Key Backend Patterns

- **Dynamic SQL construction:** `List<String> conditions` + `String.join(" AND ", conditions)` for flexible WHERE clauses
- **PreparedStatement:** All user inputs go through parameterized queries to prevent SQL injection
- **GROUP_CONCAT:** Single-query aggregation of genres and stars per movie, avoiding N+1 queries
- **Server-side pagination:** `LIMIT ? OFFSET ?` computed from page number and page size
- **Session-based cart:** Shopping cart stored in HttpSession, no database writes until checkout
- **Transactional checkout:** Credit card validation + sales insert in a single transaction with rollback on failure

## Project Structure

```
src/main/
├── java/xtflix/
│   ├── MovieListServlet.java      # Top 20 movies API
│   ├── SingleMovieServlet.java    # Single movie detail API
│   ├── SingleStarServlet.java     # Single star detail API
│   ├── SearchServlet.java         # Unified search/browse/sort/paginate API
│   ├── GenreListServlet.java      # All genres API
│   ├── LoginServlet.java          # Authentication API
│   ├── CartServlet.java           # Shopping cart CRUD API
│   ├── CheckoutServlet.java       # Payment + order processing API
│   ├── LoginFilter.java           # Authentication filter for protected routes
│   └── User.java                  # User session model
└── webapp/
    ├── WEB-INF/web.xml            # Servlet + filter routing config
    ├── index.html                 # Top 20 movies page
    ├── search.html                # Search + results page
    ├── browse.html                # Browse by genre/title page
    ├── single-movie.html          # Movie detail page
    ├── single-star.html           # Star detail page
    ├── login.html                 # Login page
    ├── cart.html                  # Shopping cart page
    ├── checkout.html              # Checkout + confirmation page
    └── js/
        ├── movie-list.js
        ├── search.js
        ├── browse.js
        ├── single-movie.js
        ├── single-star.js
        ├── login.js
        ├── cart.js
        └── checkout.js
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/movies` | Top 20 movies by rating |
| GET | `/api/single-movie?id=` | Single movie details |
| GET | `/api/single-star?id=` | Single star details |
| GET | `/api/search?title=&year=&director=&star=&genre=&titleChar=&sort=&order=&limit=&page=` | Unified search with sort/pagination |
| GET | `/api/genres` | All genre names |
| POST | `/api/login` | User authentication |
| GET/POST | `/api/cart` | View cart / add, update, remove items |
| POST | `/api/checkout` | Validate payment + create sale records |

## Database Schema

```
movies (id, title, year, director)
stars (id, name, birthYear)
stars_in_movies (starId, movieId)
genres (id, name)
genres_in_movies (genreId, movieId)
ratings (movieId, rating, numVotes)
customers (id, firstName, lastName, ccId, address, email, password)
creditcards (id, firstName, lastName, expiration)
sales (id, customerId, movieId, saleDate)
```

## Local Setup

**Prerequisites:** Java 11, Maven, MySQL, Tomcat 9

```bash
# 1. Create database and import data
mysql -u root -p < createtable.sql
mysql -u root -p moviedb < movie-data.sql

# 2. Build
mvn clean package

# 3. Deploy WAR to Tomcat
cp target/xtflix.war $CATALINA_HOME/webapps/

# 4. Start Tomcat and visit
open http://localhost:8080/xtflix/
```

## Spring Boot Comparison

This project intentionally avoids Spring Boot to expose what the framework does under the hood:

| This Project | Spring Boot Equivalent |
|---|---|
| `web.xml` servlet mappings | `@GetMapping` / `@PostMapping` |
| `HttpServletRequest.getParameter()` | `@RequestParam` |
| Manual JDBC + PreparedStatement | Spring Data JPA |
| Gson serialization | Jackson `@ResponseBody` |
| `HttpSession` management | Spring Security session |
| Servlet Filter | `SecurityFilterChain` |
| `DriverManager.getConnection()` | `spring.datasource.*` auto-config |
| WAR + Tomcat deployment | Embedded Tomcat JAR |