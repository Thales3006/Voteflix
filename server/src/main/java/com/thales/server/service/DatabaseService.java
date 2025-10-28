package com.thales.server.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Movie;
import com.thales.common.model.Review;
import com.thales.common.model.StatusException;
import com.thales.common.model.User;

public class DatabaseService {
    private final String url = "jdbc:sqlite:data/voteflix.db";

    private synchronized Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public synchronized boolean checkUser(String username, String password) throws StatusException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    public synchronized boolean isAdmin(int id) throws StatusException {
            String sql = "SELECT is_admin FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getBoolean("is_admin");
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    public synchronized void createUser(String username, String password) throws StatusException {
        String checkSql = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new StatusException(ErrorStatus.ALREADY_EXISTS);
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    public synchronized int getUserId(String username) throws StatusException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
        throw new StatusException(ErrorStatus.NOT_FOUND);
    }

    public synchronized String getUsername(int id) throws StatusException {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
        throw new StatusException(ErrorStatus.NOT_FOUND);
    }


    public synchronized ArrayList<User> getUsers() throws StatusException {
        String sql = "SELECT id, username FROM users";
        ArrayList<User> users = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username")));
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
        
        return users;
    }

    public synchronized void updateUser(int id, String newPassword) throws StatusException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, id);
            int result = pstmt.executeUpdate();
            if (result <= 0){
                throw new StatusException(ErrorStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    public synchronized void deleteUser(int id) throws StatusException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            if (result <= 0){
                throw new StatusException(ErrorStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    private synchronized String[] getGenres(Connection conn, int id) throws StatusException{
        String genreSql = "SELECT g.name as genre FROM movie_genres mg JOIN genres g ON mg.genre_id = g.id WHERE mg.movie_id = ?";
        ArrayList<String> genres = new ArrayList<>();
        try (PreparedStatement genreStmt = conn.prepareStatement(genreSql)) {
            genreStmt.setInt(1, id);
            ResultSet genreRs = genreStmt.executeQuery();
            while (genreRs.next()) {
                genres.add(genreRs.getString("genre"));
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
        return genres.toArray(new String[0]);
    }

    private synchronized boolean deleteUnusedGenres(Connection conn) throws StatusException {
        String sql = "DELETE FROM genres WHERE id NOT IN (SELECT DISTINCT genre_id FROM movie_genres)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    public synchronized ArrayList<Movie> getMovies() throws StatusException {
        String sql = "SELECT id, title, director, year, rating, rating_amount, synopsis FROM movies";
        ArrayList<Movie> movies = new ArrayList<>();
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                movies.add(new Movie(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("director"),
                    getGenres(conn, rs.getInt("id")),
                    rs.getInt("year"),
                    rs.getFloat("rating"),
                    rs.getInt("rating_amount"),
                    rs.getString("synopsis")
                ));
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
        return movies;
    }

    public synchronized void createMovie(Movie movie) throws StatusException {
        String movieSql = "INSERT INTO movies(title, director, year, rating, rating_amount, synopsis) VALUES(?, ?, ?, 0, 0, ?)";
        String checkGenreSql = "SELECT id FROM genres WHERE name = ?";
        String createGenreSql = "INSERT INTO genres(name) VALUES(?)";
        String genreSql = "INSERT INTO movie_genres(movie_id, genre_id) VALUES(?, ?)";
        
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement movieStmt = conn.prepareStatement(movieSql, PreparedStatement.RETURN_GENERATED_KEYS);
                movieStmt.setString(1, movie.getTitle());
                movieStmt.setString(2, movie.getDirector());
                movieStmt.setInt(3, movie.getYear());
                movieStmt.setString(4, movie.getSynopsis());
                movieStmt.executeUpdate();

                ResultSet rs = movieStmt.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    throw new StatusException(ErrorStatus.NOT_FOUND);
                }
                int movieId = rs.getInt(1);
                PreparedStatement checkGenreStmt = conn.prepareStatement(checkGenreSql);
                PreparedStatement createGenreStmt = conn.prepareStatement(createGenreSql, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement genreStmt = conn.prepareStatement(genreSql);
                
                for (String genre : movie.getGenre()) {
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
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public synchronized void updateMovie(Movie movie) throws StatusException {
        String movieSql = "UPDATE movies SET title = ?, director = ?, year = ?, synopsis = ? WHERE id = ?";
        String deleteGenresSql = "DELETE FROM movie_genres WHERE movie_id = ?";
        String checkGenreSql = "SELECT id FROM genres WHERE name = ?";
        String createGenreSql = "INSERT INTO genres(name) VALUES(?)";
        String genreSql = "INSERT INTO movie_genres(movie_id, genre_id) VALUES(?, ?)";

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement movieStmt = conn.prepareStatement(movieSql);
                movieStmt.setString(1, movie.getTitle());
                movieStmt.setString(2, movie.getDirector());
                movieStmt.setInt(3, movie.getYear());
                movieStmt.setString(4, movie.getSynopsis());
                movieStmt.setInt(5, movie.getID());
                int result = movieStmt.executeUpdate();

                if (result == 0) {
                    conn.rollback();
                    throw new StatusException(ErrorStatus.NOT_FOUND);
                }
                PreparedStatement deleteGenresStmt = conn.prepareStatement(deleteGenresSql);
                deleteGenresStmt.setInt(1, movie.getID());
                deleteGenresStmt.executeUpdate();

                PreparedStatement checkGenreStmt = conn.prepareStatement(checkGenreSql);
                PreparedStatement createGenreStmt = conn.prepareStatement(createGenreSql, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement genreStmt = conn.prepareStatement(genreSql);

                for (String genre : movie.getGenre()) {
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

                    genreStmt.setInt(1, movie.getID());
                    genreStmt.setInt(2, genreId);
                    genreStmt.executeUpdate();
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

    public synchronized void deleteMovie(int id) throws StatusException {
        String movieSql = "DELETE FROM movies WHERE id = ?";
        String genresSql = "DELETE FROM movie_genres WHERE movie_id = ?";
        String reviewsSql = "DELETE FROM reviews WHERE movie_id = ?";
        
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement reviewsStmt = conn.prepareStatement(reviewsSql);
                reviewsStmt.setInt(1, id);
                reviewsStmt.executeUpdate();

                PreparedStatement genresStmt = conn.prepareStatement(genresSql);
                genresStmt.setInt(1, id);
                genresStmt.executeUpdate();

                PreparedStatement movieStmt = conn.prepareStatement(movieSql);
                movieStmt.setInt(1, id);
                int result = movieStmt.executeUpdate();

                deleteUnusedGenres(conn);
                conn.commit();
                if(result == 0){
                    throw new StatusException(ErrorStatus.NOT_FOUND);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public synchronized void createReview(Review review) throws StatusException {
        String sql = "INSERT INTO reviews(movie_id, user_id, rating, title, description) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, review.getMovieID());
            pstmt.setInt(2, review.getUserID());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getTitle());
            pstmt.setString(5, review.getDescription());
            
            int result = pstmt.executeUpdate();
            if(result == 0){
                throw new StatusException(ErrorStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    public synchronized ArrayList<Review> getMovieReviews(int movieID) throws StatusException {
        String sql = "SELECT * FROM reviews WHERE movie_id = ?";
        ArrayList<Review> reviews = new ArrayList<>();
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, movieID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
               reviews.add(new Review(
                  rs.getInt("id"),
                  rs.getInt("movie_id"),
                  rs.getInt("user_id"),
                  rs.getInt("rating"),
                  rs.getString("title"),
                  rs.getString("description"),
                  rs.getDate("date").toLocalDate()
               ));
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
        return reviews;
    }

    public synchronized ArrayList<Review> getUserReviews(int userID) throws StatusException {
        String sql = "SELECT * FROM reviews WHERE user_id = ?";
        ArrayList<Review> reviews = new ArrayList<>();
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
               reviews.add(new Review(
                  rs.getInt("id"),
                  rs.getInt("movie_id"),
                  rs.getInt("user_id"),
                  rs.getInt("rating"),
                  rs.getString("title"),
                  rs.getString("description"),
                  rs.getDate("date").toLocalDate()
               ));
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
        return reviews;
    }

    public synchronized void updateReview(Review review) throws StatusException {
        String sql = "UPDATE reviews SET rating = ?, title = ?, description = ? WHERE id = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, review.getRating());
            pstmt.setString(2, review.getTitle());
            pstmt.setString(3, review.getDescription());
            pstmt.setInt(4, review.getID());
            int result = pstmt.executeUpdate();
            if(result == 0){
                throw new StatusException(ErrorStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }

    public synchronized void deleteReview(int id) throws StatusException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            if(result == 0){
                throw new StatusException(ErrorStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        } 
    }
}