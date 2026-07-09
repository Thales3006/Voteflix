package com.thales.server.repository;
import com.thales.server.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;
import com.thales.common.model.User;
import com.thales.common.model.UserFilter;

public class UserRepository implements Repository<User, Integer> {
    private final Database db;

    public UserRepository(Database db) {
        this.db = db;
    }

    @Override
    public void create(User user) throws StatusException {
        String checkSql = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, user.getUsername());
                    if (checkStmt.executeQuery().next()) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.ALREADY_EXISTS);
                    }
                }
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, user.getUsername());
                    insertStmt.setString(2, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
                    insertStmt.executeUpdate();
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
    public void update(User user) throws StatusException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
            pstmt.setInt(2, user.getId());
            if (pstmt.executeUpdate() == 0) throw new StatusException(ErrorStatus.NOT_FOUND);
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public User findById(Integer id) throws StatusException {
        String sql = "SELECT id, username FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return new User(rs.getInt("id"), rs.getString("username"));
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        throw new StatusException(ErrorStatus.NOT_FOUND);
    }

    @Override
    public List<User> findAll() throws StatusException {
        String sql = "SELECT id, username FROM users";
        List<User> users = new ArrayList<>();
        try (Connection conn = db.getConnection();
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

    public List<User> findAll(UserFilter filter) throws StatusException {
        if (filter == null || filter.username() == null) return findAll();
        String sql = "SELECT id, username FROM users WHERE username LIKE ?";
        List<User> users = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + filter.username() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("username")));
            }
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        return users;
    }

    @Override
    public void delete(Integer id) throws StatusException {
        String reviewsSql = "DELETE FROM reviews WHERE user_id = ?";
        String userSql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement delReviewsStmt = conn.prepareStatement(reviewsSql)) {
                    delReviewsStmt.setInt(1, id);
                    delReviewsStmt.executeUpdate();
                }
                try (PreparedStatement delUserStmt = conn.prepareStatement(userSql)) {
                    delUserStmt.setInt(1, id);
                    if (delUserStmt.executeUpdate() == 0) {
                        conn.rollback();
                        throw new StatusException(ErrorStatus.NOT_FOUND);
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

    public boolean checkCredentials(String username, String password) throws StatusException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) return false;
            return BCrypt.checkpw(password, rs.getString("password"));
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean isAdmin(int id) throws StatusException {
        String sql = "SELECT is_admin FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getBoolean("is_admin");
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public int findIdByUsername(String username) throws StatusException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        throw new StatusException(ErrorStatus.NOT_FOUND);
    }

    public String findUsernameById(int id) throws StatusException {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
        throw new StatusException(ErrorStatus.NOT_FOUND);
    }
}
