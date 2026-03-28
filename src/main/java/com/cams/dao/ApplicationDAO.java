package com.cams.dao;

import com.cams.config.DBConnection;
import com.cams.model.Application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ApplicationDAO – all JDBC operations for the Applications table.
 * Uses JOINs to return fully-populated Application objects (with
 * denormalised student name, course name, etc.) for display.
 */
public class ApplicationDAO {

    // Base SELECT shared by multiple queries
    private static final String SELECT_FULL =
        "SELECT a.*, s.full_name, s.email, s.academic_score, "
      + "       c.course_name, c.cutoff_score "
      + "FROM Applications a "
      + "JOIN Students s ON a.student_id = s.student_id "
      + "JOIN Courses  c ON a.course_id  = c.course_id ";

    // ── Create ────────────────────────────────────────────────────────────

    /**
     * Submit a new application.  Returns false if the student already
     * applied for this course (UNIQUE constraint).
     */
    public boolean submitApplication(int studentId, int courseId) {
        String sql = "INSERT INTO Applications (student_id, course_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("[DAO] Student already applied for this course.");
            } else {
                System.err.println("[DAO] submitApplication error: " + e.getMessage());
            }
        }
        return false;
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /** All applications for a specific student. */
    public List<Application> findByStudent(int studentId) {
        String sql = SELECT_FULL + "WHERE a.student_id = ? ORDER BY a.applied_at DESC";
        return executeQuery(sql, ps -> ps.setInt(1, studentId));
    }

    /** All applications for a specific course, ordered by score (merit). */
    public List<Application> findByCourse(int courseId) {
        String sql = SELECT_FULL
                + "WHERE a.course_id = ? ORDER BY s.academic_score DESC";
        return executeQuery(sql, ps -> ps.setInt(1, courseId));
    }

    /** All pending applications across all courses. */
    public List<Application> findAllPending() {
        String sql = SELECT_FULL
                + "WHERE a.status = 'Pending' ORDER BY c.course_name, s.academic_score DESC";
        return executeQuery(sql, ps -> {});
    }

    /** All applications regardless of status. */
    public List<Application> findAll() {
        String sql = SELECT_FULL + "ORDER BY c.course_name, s.academic_score DESC";
        return executeQuery(sql, ps -> {});
    }

    /** Approved applications – used for the admission list export. */
    public List<Application> findApproved() {
        String sql = SELECT_FULL
                + "WHERE a.status = 'Approved' ORDER BY c.course_name, a.merit_rank";
        return executeQuery(sql, ps -> {});
    }

    public Optional<Application> findById(int applicationId) {
        String sql = SELECT_FULL + "WHERE a.application_id = ?";
        List<Application> res = executeQuery(sql, ps -> ps.setInt(1, applicationId));
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    // ── Update ────────────────────────────────────────────────────────────

    /** Approve or reject an application and optionally store admin remarks. */
    public boolean updateStatus(int applicationId, String status, String remarks) {
        String sql = "UPDATE Applications "
                + "SET status=?, reviewed_at=NOW(), admin_remarks=? "
                + "WHERE application_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, remarks);
            ps.setInt   (3, applicationId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] updateStatus error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Recomputes merit ranks for all applications within a course,
     * ranking by academic_score descending.
     */
    public void computeMeritRanks(int courseId) {
        // Fetch ordered list
        List<Application> apps = findByCourse(courseId);

        String sql = "UPDATE Applications SET merit_rank=? "
                + "WHERE application_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int rank = 1;
            for (Application app : apps) {
                ps.setInt(1, rank++);
                ps.setInt(2, app.getApplicationId());
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            System.err.println("[DAO] computeMeritRanks error: " + e.getMessage());
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public boolean deleteApplication(int applicationId) {
        String sql = "DELETE FROM Applications WHERE application_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteApplication error: " + e.getMessage());
        }
        return false;
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    @FunctionalInterface
    private interface PSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<Application> executeQuery(String sql, PSetter setter) {
        List<Application> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setter.set(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[DAO] query error: " + e.getMessage());
        }
        return list;
    }

    private Application mapRow(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setApplicationId(rs.getInt      ("application_id"));
        a.setStudentId    (rs.getInt      ("student_id"));
        a.setCourseId     (rs.getInt      ("course_id"));
        a.setStatus       (rs.getString   ("status"));
        a.setMeritRank    ((Integer) rs.getObject("merit_rank"));  // nullable
        a.setAppliedAt    (rs.getTimestamp("applied_at"));
        a.setReviewedAt   (rs.getTimestamp("reviewed_at"));
        a.setAdminRemarks (rs.getString   ("admin_remarks"));
        // Denormalised
        a.setStudentName  (rs.getString("full_name"));
        a.setStudentEmail (rs.getString("email"));
        a.setAcademicScore(rs.getDouble("academic_score"));
        a.setCourseName   (rs.getString("course_name"));
        a.setCutoffScore  (rs.getDouble("cutoff_score"));
        return a;
    }
}
