package com.cams.service;

import com.cams.dao.StudentDAO;
import com.cams.model.Student;
import com.cams.util.PasswordUtil;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * StudentService – business logic for student operations.
 * Validates input before delegating to StudentDAO.
 */
public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();

    /**
     * Registers a new student after validating fields and hashing the password.
     *
     * @return the persisted Student (with generated ID), or empty on failure
     */
    public Optional<Student> register(String fullName, String email, String plainPassword,
                                      String phone, String dobStr, String gender,
                                      String address, double academicScore) {

        // ── Input validation ─────────────────────────────────────────────
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Full name is required.");
        if (email == null || !email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Invalid email address.");
        if (plainPassword == null || plainPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (academicScore < 0 || academicScore > 100)
            throw new IllegalArgumentException("Academic score must be between 0 and 100.");

        Date dob = null;
        if (dobStr != null && !dobStr.isBlank()) {
            try { dob = Date.valueOf(dobStr); }      // expects YYYY-MM-DD
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Date of birth must be in YYYY-MM-DD format.");
            }
        }

        // ── Build and persist ────────────────────────────────────────────
        Student student = new Student(
                fullName, email, PasswordUtil.hash(plainPassword),
                phone, dob, gender, address, academicScore);

        boolean ok = studentDAO.registerStudent(student);
        return ok ? Optional.of(student) : Optional.empty();
    }

    /**
     * Authenticates a student.
     *
     * @return Optional<Student> if credentials are correct
     */
    public Optional<Student> login(String email, String plainPassword) {
        if (email == null || plainPassword == null)
            return Optional.empty();
        return studentDAO.login(email, plainPassword);
    }

    /** Returns all students ordered by academic score (highest first). */
    public List<Student> getAllStudentsByMerit() {
        return studentDAO.findAllByScore();
    }

    public Optional<Student> findById(int id) {
        return studentDAO.findById(id);
    }

    public boolean updateProfile(Student s) {
        return studentDAO.updateProfile(s);
    }

    public boolean deleteStudent(int id) {
        return studentDAO.deleteStudent(id);
    }
}
