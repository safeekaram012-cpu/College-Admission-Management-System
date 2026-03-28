package com.cams.dao;

import com.cams.config.DBConnection;
import com.cams.model.Admin;
import com.cams.util.PasswordUtil;

import java.sql.*;
import java.util.Optional;

/**
 * AdminDAO – handles authentication for admin users.
 */
public class AdminDAO {

    public Optional<Admin> login(String username, String plainPassword) {
        String sql = "SELECT * FROM Admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordUtil.verify(plainPassword, storedHash)) {
                    Admin a = new Admin();
                    a.setAdminId (rs.getInt   ("admin_id"));
                    a.setUsername(rs.getString("username"));
                    a.setPassword(storedHash);
                    return Optional.of(a);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO] admin login error: " + e.getMessage());
        }
        return Optional.empty();
    }
}
