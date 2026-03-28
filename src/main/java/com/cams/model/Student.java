package com.cams.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing a row in the Students table.
 */
public class Student {

    private int       studentId;
    private String    fullName;
    private String    email;
    private String    password;       // stored as SHA-256 hex
    private String    phone;
    private Date      dob;
    private String    gender;
    private String    address;
    private double    academicScore;  // 0 – 100 percentage
    private Timestamp createdAt;

    // ── Constructors ─────────────────────────────────────────────────────

    /** Default no-arg constructor required by some DAO patterns. */
    public Student() {}

    /** Constructor used for new student registration (no ID yet). */
    public Student(String fullName, String email, String password,
                   String phone, Date dob, String gender,
                   String address, double academicScore) {
        this.fullName      = fullName;
        this.email         = email;
        this.password      = password;
        this.phone         = phone;
        this.dob           = dob;
        this.gender        = gender;
        this.address       = address;
        this.academicScore = academicScore;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int       getStudentId()      { return studentId; }
    public void      setStudentId(int v) { this.studentId = v; }

    public String    getFullName()       { return fullName; }
    public void      setFullName(String v){ this.fullName = v; }

    public String    getEmail()          { return email; }
    public void      setEmail(String v)  { this.email = v; }

    public String    getPassword()       { return password; }
    public void      setPassword(String v){ this.password = v; }

    public String    getPhone()          { return phone; }
    public void      setPhone(String v)  { this.phone = v; }

    public Date      getDob()            { return dob; }
    public void      setDob(Date v)      { this.dob = v; }

    public String    getGender()         { return gender; }
    public void      setGender(String v) { this.gender = v; }

    public String    getAddress()        { return address; }
    public void      setAddress(String v){ this.address = v; }

    public double    getAcademicScore()       { return academicScore; }
    public void      setAcademicScore(double v){ this.academicScore = v; }

    public Timestamp getCreatedAt()      { return createdAt; }
    public void      setCreatedAt(Timestamp v){ this.createdAt = v; }

    @Override
    public String toString() {
        return String.format("Student[id=%d, name=%s, email=%s, score=%.2f%%]",
                studentId, fullName, email, academicScore);
    }
}
