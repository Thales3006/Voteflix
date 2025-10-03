package com.thales.server.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.thales.common.model.Movie;
import com.thales.common.model.User;

public class DatabaseService {
    private final String url = "jdbc:sqlite:data/voteflix.db";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    public boolean checkUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public boolean isAdmin(int id){
            String sql = "SELECT is_admin FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getBoolean("is_admin");
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean createUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            int result = pstmt.executeUpdate();
            return result > 0;
        }
    }

    public int getUserId(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    public String getUsername(int id) throws SQLException {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        }
        return null;
    }


    public ArrayList<User> getUsers() throws SQLException {
        String sql = "SELECT id, username FROM users";
        ArrayList<User> users = new ArrayList<>();
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username")));
            }
        }
        
        return users;
    }

    public boolean updateUser(int id, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, id);
            int result = pstmt.executeUpdate();
            return result > 0;
        }
    }

    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            return result > 0;
        }
    }

    private String[] getGenres(Connection conn, int id) throws SQLException{
        String genreSql = "SELECT g.name as genre FROM movie_genres mg JOIN genres g ON mg.genre_id = g.id WHERE mg.movie_id = ?";
        ArrayList<String> genres = new ArrayList<>();
        try (PreparedStatement genreStmt = conn.prepareStatement(genreSql)) {
            genreStmt.setInt(1, id);
            ResultSet genreRs = genreStmt.executeQuery();
            while (genreRs.next()) {
                genres.add(genreRs.getString("genre"));
            }
        }
        return genres.toArray(new String[0]);
    }

    public ArrayList<Movie> getMovies() throws SQLException {
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
        }
        return movies;
    }
}