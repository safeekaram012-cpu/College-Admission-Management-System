package com.cams.dao;

import com.cams.config.DBConnection;
import com.cams.model.Student;
import com.cams.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * StudentDAO – handles all JDBC operations for the Students table.
 */
public class StudentDAO {

    // ── Create ──────────────────────────────────────────────────────────

    /**
     * Inserts a new student and populates the generated student_id.
     *
     * @param student the student to persist (password should already be hashed)
     * @return true on success
     */
    public boolean registerStudent(Student student) {
        String sql = "INSERT INTO Students "
                + "(full_name, email, password, phone, dob, gender, address, academic_score) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getFullName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPassword());        // pre-hashed
            ps.setString(4, student.getPhone());
            ps.setDate  (5, student.getDob());
            ps.setString(6, student.getGender());
            ps.setString(7, student.getAddress());
            ps.setDouble(8, student.getAcademicScore());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    student.setStudentId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("[DAO] Email already registered: " + student.getEmail());
            } else {
                System.err.println("[DAO] registerStudent error: " + e.getMessage());
            }
        }
        return false;
    }

    // ── Read ─────────────────────────────────────────────────────────────

    /**
     * Authenticates a student by email + plain-text password.
     *
     * @return Optional containing the Student, or empty if auth fails
     */
    public Optional<Student> login(String email, String plainPassword) {
        String sql = "SELECT * FROM Students WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordUtil.verify(plainPassword, storedHash)) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO] login error: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Fetch student by primary key. */
    public Optional<Student> findById(int studentId) {
        String sql = "SELECT * FROM Students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[DAO] findById error: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Returns every student ordered by academic score descending (for merit list). */
    public List<Student> findAllByScore() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM Students ORDER BY academic_score DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[DAO] findAllByScore error: " + e.getMessage());
        }
        return list;
    }

    // ── Update ────────────────────────────────────────────────────────────

    /** Update mutable profile fields for an existing student. */
    public boolean updateProfile(Student s) {
        String sql = "UPDATE Students SET full_name=?, phone=?, gender=?, address=?, "
                + "academic_score=? WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getFullName());
            ps.setString(2, s.getPhone());
            ps.setString(3, s.getGender());
            ps.setString(4, s.getAddress());
            ps.setDouble(5, s.getAcademicScore());
            ps.setInt   (6, s.getStudentId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] updateProfile error: " + e.getMessage());
        }
        return false;
    }

    // ── Delete ────────────────────────────────────────────────────────────

    /** Remove a student record (cascades to Applications via FK). */
    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM Students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] deleteStudent error: " + e.getMessage());
        }
        return false;
    }

    // ── Mapping Helper ────────────────────────────────────────────────────

    /** Maps the current ResultSet row to a Student object. */
    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId    (rs.getInt      ("student_id"));
        s.setFullName     (rs.getString   ("full_name"));
        s.setEmail        (rs.getString   ("email"));
        s.setPassword     (rs.getString   ("password"));
        s.setPhone        (rs.getString   ("phone"));
        s.setDob          (rs.getDate     ("dob"));
        s.setGender       (rs.getString   ("gender"));
        s.setAddress      (rs.getString   ("address"));
        s.setAcademicScore(rs.getDouble   ("academic_score"));
        s.setCreatedAt    (rs.getTimestamp("created_at"));
        return s;
    }
}
