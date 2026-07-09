package com.thales.server.repository;
import com.thales.server.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Movie;
import com.thales.common.model.MovieFilter;
import com.thales.common.model.StatusException;

public class MovieRepository implements Repository<Movie, Integer> {
    private final Database db;

    public MovieRepository(Database db) {
        this.db = db;
    }

    @Override
    public void create(Movie movie) throws StatusException {
        String checkMovieSql = "SELECT id FROM movies WHERE title = ? AND director = ? AND year = ?";
        String movieSql = "INSERT INTO movies(title, director, year, rating, rating_amount, synopsis) VALUES(?, ?, ?, 0, 0, ?)";
        String checkGenreSql = "SELECT id FROM genres WHERE name = ?";
        String createGenreSql = "INSERT INTO genres(name) VALUES(?)";
        String genreSql = "INSERT INTO movie_genres(movie_id, genre_id) VALUES(?, ?)";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement checkMovieStmt = conn.prepareStatement(checkMovieSql)) {
                    checkMovieStmt.setString(1, movie.getTitle());
                    checkMovieStmt.setString(2, movie.getDirector());
                    checkMovieStmt.setInt(3, movie.getYear());
                    if (checkMovieStmt.executeQuery().next()) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.ALREADY_EXISTS);
                    }
                }

                int movieId;
                try (PreparedStatement movieStmt = conn.prepareStatement(movieSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    movieStmt.setString(1, movie.getTitle());
                    movieStmt.setString(2, movie.getDirector());
                    movieStmt.setInt(3, movie.getYear());
                    movieStmt.setString(4, movie.getSynopsis());
                    movieStmt.executeUpdate();
                    ResultSet rs = movieStmt.getGeneratedKeys();
                    if (!rs.next()) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
                    }
                    movieId = rs.getInt(1);
                }

                upsertGenres(conn, movieId, movie.getGenre(), checkGenreSql, createGenreSql, genreSql);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void update(Movie movie) throws StatusException {
        String movieSql = "UPDATE movies SET title = ?, director = ?, year = ?, rating = COALESCE(?, rating), rating_amount = COALESCE(?, rating_amount), synopsis = ? WHERE id = ?";
        String deleteGenresSql = "DELETE FROM movie_genres WHERE movie_id = ?";
        String checkGenreSql = "SELECT id FROM genres WHERE name = ?";
        String createGenreSql = "INSERT INTO genres(name) VALUES(?)";
        String genreSql = "INSERT INTO movie_genres(movie_id, genre_id) VALUES(?, ?)";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement movieStmt = conn.prepareStatement(movieSql)) {
                    movieStmt.setString(1, movie.getTitle());
                    movieStmt.setString(2, movie.getDirector());
                    movieStmt.setInt(3, movie.getYear());
                    if (movie.getRating() != null) movieStmt.setFloat(4, movie.getRating());
                    else movieStmt.setNull(4, java.sql.Types.REAL);
                    if (movie.getRatingCount() != null) movieStmt.setInt(5, movie.getRatingCount());
                    else movieStmt.setNull(5, java.sql.Types.INTEGER);
                    movieStmt.setString(6, movie.getSynopsis());
                    movieStmt.setInt(7, movie.getId());
                    if (movieStmt.executeUpdate() == 0) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.NOT_FOUND);
                    }
                }

                try (PreparedStatement deleteGenresStmt = conn.prepareStatement(deleteGenresSql)) {
                    deleteGenresStmt.setInt(1, movie.getId());
                    deleteGenresStmt.executeUpdate();
                }

                upsertGenres(conn, movie.getId(), movie.getGenre(), checkGenreSql, createGenreSql, genreSql);
                deleteUnusedGenres(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Movie findById(Integer id) throws StatusException {
        String sql = "SELECT id, title, director, year, rating, rating_amount, synopsis FROM movies WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Movie(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("director"),
                    getGenres(conn, rs.getInt("id")),
                    rs.getInt("year"),
                    rs.getFloat("rating"),
                    rs.getInt("rating_amount"),
                    rs.getString("synopsis")
                );
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        throw new StatusException(ErrorStatus.NOT_FOUND);
    }

    @Override
    public List<Movie> findAll() throws StatusException {
        String movieSql = "SELECT m.id, m.title, m.director, m.year, COALESCE(AVG(r.rating), 0) AS rating, COUNT(r.rating) AS rating_amount, m.synopsis " +
                "FROM movies m LEFT JOIN reviews r ON m.id = r.movie_id " +
                "GROUP BY m.id, m.title, m.director, m.year, m.synopsis";
        String genreSql = "SELECT mg.movie_id, g.name FROM movie_genres mg JOIN genres g ON mg.genre_id = g.id";

        try (Connection conn = db.getConnection()) {
            Map<Integer, List<String>> genreMap = new HashMap<>();
            try (PreparedStatement gstmt = conn.prepareStatement(genreSql)) {
                ResultSet grs = gstmt.executeQuery();
                while (grs.next()) {
                    genreMap.computeIfAbsent(grs.getInt("movie_id"), _ -> new ArrayList<>())
                            .add(grs.getString("name"));
                }
            }

            List<Movie> movies = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(movieSql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    List<String> genres = genreMap.getOrDefault(id, List.of());
                    movies.add(new Movie(
                        id,
                        rs.getString("title"),
                        rs.getString("director"),
                        genres.toArray(new String[0]),
                        rs.getInt("year"),
                        rs.getFloat("rating"),
                        rs.getInt("rating_amount"),
                        rs.getString("synopsis")
                    ));
                }
            }
            return movies;
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void delete(Integer id) throws StatusException {
        String reviewsSql = "DELETE FROM reviews WHERE movie_id = ?";
        String genresSql = "DELETE FROM movie_genres WHERE movie_id = ?";
        String movieSql = "DELETE FROM movies WHERE id = ?";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement reviewsStmt = conn.prepareStatement(reviewsSql)) {
                    reviewsStmt.setInt(1, id);
                    reviewsStmt.executeUpdate();
                }
                try (PreparedStatement genresStmt = conn.prepareStatement(genresSql)) {
                    genresStmt.setInt(1, id);
                    genresStmt.executeUpdate();
                }
                try (PreparedStatement movieStmt = conn.prepareStatement(movieSql)) {
                    movieStmt.setInt(1, id);
                    if (movieStmt.executeUpdate() == 0) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.NOT_FOUND);
                    }
                }
                deleteUnusedGenres(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Movie> findAll(MovieFilter filter) throws StatusException {
        if (filter == null || (filter.genre() == null && filter.year() == null)) return findAll();

        StringBuilder movieSqlBuilder = new StringBuilder(
            "SELECT m.id, m.title, m.director, m.year, COALESCE(AVG(r.rating), 0) AS rating, COUNT(r.rating) AS rating_amount, m.synopsis " +
            "FROM movies m LEFT JOIN reviews r ON m.id = r.movie_id ");

        if (filter.genre() != null) {
            movieSqlBuilder.append("JOIN movie_genres mg ON m.id = mg.movie_id ")
                           .append("JOIN genres g ON mg.genre_id = g.id ");
        }

        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;

        if (filter.genre() != null) {
            movieSqlBuilder.append("WHERE g.name = ? ");
            params.add(filter.genre());
            hasWhere = true;
        }
        if (filter.year() != null) {
            movieSqlBuilder.append(hasWhere ? "AND " : "WHERE ").append("m.year = ? ");
            params.add(filter.year());
        }
        movieSqlBuilder.append("GROUP BY m.id, m.title, m.director, m.year, m.synopsis");

        String genreSql = "SELECT mg.movie_id, g.name FROM movie_genres mg JOIN genres g ON mg.genre_id = g.id";

        try (Connection conn = db.getConnection()) {
            Map<Integer, List<String>> genreMap = new HashMap<>();
            try (PreparedStatement gstmt = conn.prepareStatement(genreSql)) {
                ResultSet grs = gstmt.executeQuery();
                while (grs.next()) {
                    genreMap.computeIfAbsent(grs.getInt("movie_id"), _ -> new ArrayList<>())
                            .add(grs.getString("name"));
                }
            }
            List<Movie> movies = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(movieSqlBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i) instanceof Integer val) pstmt.setInt(i + 1, val);
                    else pstmt.setString(i + 1, (String) params.get(i));
                }
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    movies.add(new Movie(
                        id,
                        rs.getString("title"),
                        rs.getString("director"),
                        genreMap.getOrDefault(id, List.of()).toArray(new String[0]),
                        rs.getInt("year"),
                        rs.getFloat("rating"),
                        rs.getInt("rating_amount"),
                        rs.getString("synopsis")
                    ));
                }
            }
            return movies;
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String[] getGenres(Connection conn, int movieId) throws StatusException {
        String sql = "SELECT g.name AS genre FROM movie_genres mg JOIN genres g ON mg.genre_id = g.id WHERE mg.movie_id = ?";
        List<String> genres = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) genres.add(rs.getString("genre"));
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        return genres.toArray(new String[0]);
    }

    private void deleteUnusedGenres(Connection conn) throws StatusException {
        String sql = "DELETE FROM genres WHERE id NOT IN (SELECT DISTINCT genre_id FROM movie_genres)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void upsertGenres(Connection conn, int movieId, String[] genres,
                               String checkGenreSql, String createGenreSql, String genreSql) throws SQLException {
        try (PreparedStatement checkGenreStmt = conn.prepareStatement(checkGenreSql);
             PreparedStatement createGenreStmt = conn.prepareStatement(createGenreSql, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement genreStmt = conn.prepareStatement(genreSql)) {
            for (String genre : genres) {
                checkGenreStmt.setString(1, genre);
                ResultSet genreRs = checkGenreStmt.executeQuery();
                int genreId;
                if (!genreRs.next()) {
                    createGenreStmt.setString(1, genre);
                    createGenreStmt.executeUpdate();
                    ResultSet newGenreRs = createGenreStmt.getGeneratedKeys();
                    newGenreRs.next();
                    genreId = newGenreRs.getInt(1);
                } else {
                    genreId = genreRs.getInt("id");
                }
                genreStmt.setInt(1, movieId);
                genreStmt.setInt(2, genreId);
                genreStmt.executeUpdate();
            }
        }
    }
}
