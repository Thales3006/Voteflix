package com.thales.server.repository;
import com.thales.server.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Review;
import com.thales.common.model.StatusException;

public class ReviewRepository implements Repository<Review, Integer> {
    private final Database db;

    public ReviewRepository(Database db) {
        this.db = db;
    }

    @Override
    public void create(Review review) throws StatusException {
        String checkUserSql = "SELECT id FROM users WHERE id = ?";
        String checkMovieSql = "SELECT id FROM movies WHERE id = ?";
        String checkReviewSql = "SELECT id FROM reviews WHERE movie_id = ? AND user_id = ?";
        String insertSql = "INSERT INTO reviews(movie_id, user_id, rating, title, description) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement checkMovieStmt = conn.prepareStatement(checkMovieSql)) {
                    checkMovieStmt.setInt(1, review.getMovieId());
                    if (!checkMovieStmt.executeQuery().next()) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.NOT_FOUND);
                    }
                }
                try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql)) {
                    checkUserStmt.setInt(1, review.getUserId());
                    if (!checkUserStmt.executeQuery().next()) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.NOT_FOUND);
                    }
                }
                try (PreparedStatement checkReviewStmt = conn.prepareStatement(checkReviewSql)) {
                    checkReviewStmt.setInt(1, review.getMovieId());
                    checkReviewStmt.setInt(2, review.getUserId());
                    if (checkReviewStmt.executeQuery().next()) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.ALREADY_EXISTS);
                    }
                }
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setInt(1, review.getMovieId());
                    pstmt.setInt(2, review.getUserId());
                    pstmt.setFloat(3, review.getRating());
                    pstmt.setString(4, review.getTitle());
                    pstmt.setString(5, review.getDescription());
                    if (pstmt.executeUpdate() == 0) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
                    }
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

    @Override
    public void update(Review review) throws StatusException {
        String sql = "UPDATE reviews SET rating = ?, title = ?, description = ?, edited = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setFloat(1, review.getRating());
            pstmt.setString(2, review.getTitle());
            pstmt.setString(3, review.getDescription());
            pstmt.setBoolean(4, true);
            pstmt.setInt(5, review.getId());
            if (pstmt.executeUpdate() == 0) throw new StatusException(ErrorStatus.NOT_FOUND);
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Review findById(Integer id) throws StatusException {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        throw new StatusException(ErrorStatus.NOT_FOUND);
    }

    @Override
    public List<Review> findAll() throws StatusException {
        String sql = "SELECT * FROM reviews";
        List<Review> reviews = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) reviews.add(mapRow(rs));
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        return reviews;
    }

    @Override
    public void delete(Integer id) throws StatusException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            if (pstmt.executeUpdate() == 0) throw new StatusException(ErrorStatus.NOT_FOUND);
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Review> findByMovieId(int movieId) throws StatusException {
        String checkSql = "SELECT id FROM movies WHERE id = ?";
        String sql = "SELECT * FROM reviews WHERE movie_id = ?";
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, movieId);
                if (!checkStmt.executeQuery().next()) throw new StatusException(ErrorStatus.NOT_FOUND);
            }
            List<Review> reviews = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, movieId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) reviews.add(mapRow(rs));
            }
            return reviews;
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Review> findByUserId(int userId) throws StatusException {
        String checkSql = "SELECT id FROM users WHERE id = ?";
        String sql = "SELECT * FROM reviews WHERE user_id = ?";
        try (Connection conn = db.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, userId);
                if (!checkStmt.executeQuery().next()) throw new StatusException(ErrorStatus.NOT_FOUND);
            }
            List<Review> reviews = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) reviews.add(mapRow(rs));
            }
            return reviews;
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        return new Review(
            rs.getInt("id"),
            rs.getInt("movie_id"),
            rs.getInt("user_id"),
            (int) rs.getFloat("rating"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getBoolean("edited"),
            parseDate(rs.getString("date"))
        );
    }

    private LocalDate parseDate(String raw) {
        if (raw == null) return null;
        return LocalDate.parse(raw.substring(0, 10));
    }
}
