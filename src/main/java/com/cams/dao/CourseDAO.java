package com.cams.dao;

import com.cams.config.DBConnection;
import com.cams.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CourseDAO – handles all JDBC operations for the Courses table.
 */
public class CourseDAO {

    // ── Create ────────────────────────────────────────────────────────────

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO Courses "
                + "(course_name, department, total_seats, cutoff_score, duration_years, description) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, course.getCourseName());
            ps.setString(2, course.getDepartment());
            ps.setInt   (3, course.getTotalSeats());
            ps.setDouble(4, course.getCutoffScore());
            ps.setInt   (5, course.getDurationYears());
            ps.setString(6, course.getDescription());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) course.setCourseId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] addCourse error: " + e.getMessage());
        }
        return false;
    }

    // ── Read ──────────────────────────────────────────────────────────────

    public List<Course> findAll() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM Courses ORDER BY course_name";
        try (Connection conn = DBConnection.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findAll courses error: " + e.getMessage());
        }
        return list;
    }

    public Optional<Course> findById(int courseId) {
        String sql = "SELECT * FROM Courses WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] findById course error: " + e.getMessage());
        }
        return Optional.empty();
    }

    // ── Update ────────────────────────────────────────────────────────────

    public boolean updateCourse(Course c) {
        String sql = "UPDATE Courses SET course_name=?, department=?, total_seats=?, "
                + "cutoff_score=?, duration_years=?, description=? WHERE course_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCourseName());
            ps.setString(2, c.getDepartment());
            ps.setInt   (3, c.getTotalSeats());
            ps.setDouble(4, c.getCutoffScore());
            ps.setInt   (5, c.getDurationYears());
            ps.setString(6, c.getDescription());
            ps.setInt   (7, c.getCourseId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateCourse error: " + e.getMessage());
        }
        return false;
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM Courses WHERE course_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteCourse error: " + e.getMessage());
        }
        return false;
    }

    // ── Mapping Helper ────────────────────────────────────────────────────

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId     (rs.getInt   ("course_id"));
        c.setCourseName   (rs.getString("course_name"));
        c.setDepartment   (rs.getString("department"));
        c.setTotalSeats   (rs.getInt   ("total_seats"));
        c.setCutoffScore  (rs.getDouble("cutoff_score"));
        c.setDurationYears(rs.getInt   ("duration_years"));
        c.setDescription  (rs.getString("description"));
        return c;
    }
}
