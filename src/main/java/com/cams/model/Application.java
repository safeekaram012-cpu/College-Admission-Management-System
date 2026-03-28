package com.cams.model;

import java.sql.Timestamp;

/**
 * Model class representing a row in the Applications table.
 * Also carries denormalised student/course fields for display purposes.
 */
public class Application {

    // Core DB fields
    private int       applicationId;
    private int       studentId;
    private int       courseId;
    private String    status;       // Pending | Approved | Rejected
    private Integer   meritRank;    // nullable until computed
    private Timestamp appliedAt;
    private Timestamp reviewedAt;
    private String    adminRemarks;

    // Denormalised fields (populated via JOINs in DAO)
    private String    studentName;
    private String    studentEmail;
    private double    academicScore;
    private String    courseName;
    private double    cutoffScore;

    // ── Constructors ─────────────────────────────────────────────────────

    public Application() {}

    public Application(int studentId, int courseId) {
        this.studentId = studentId;
        this.courseId  = courseId;
        this.status    = "Pending";
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int       getApplicationId()           { return applicationId; }
    public void      setApplicationId(int v)      { this.applicationId = v; }

    public int       getStudentId()               { return studentId; }
    public void      setStudentId(int v)          { this.studentId = v; }

    public int       getCourseId()                { return courseId; }
    public void      setCourseId(int v)           { this.courseId = v; }

    public String    getStatus()                  { return status; }
    public void      setStatus(String v)          { this.status = v; }

    public Integer   getMeritRank()               { return meritRank; }
    public void      setMeritRank(Integer v)      { this.meritRank = v; }

    public Timestamp getAppliedAt()               { return appliedAt; }
    public void      setAppliedAt(Timestamp v)    { this.appliedAt = v; }

    public Timestamp getReviewedAt()              { return reviewedAt; }
    public void      setReviewedAt(Timestamp v)   { this.reviewedAt = v; }

    public String    getAdminRemarks()            { return adminRemarks; }
    public void      setAdminRemarks(String v)    { this.adminRemarks = v; }

    // Denormalised
    public String    getStudentName()             { return studentName; }
    public void      setStudentName(String v)     { this.studentName = v; }

    public String    getStudentEmail()            { return studentEmail; }
    public void      setStudentEmail(String v)    { this.studentEmail = v; }

    public double    getAcademicScore()           { return academicScore; }
    public void      setAcademicScore(double v)   { this.academicScore = v; }

    public String    getCourseName()              { return courseName; }
    public void      setCourseName(String v)      { this.courseName = v; }

    public double    getCutoffScore()             { return cutoffScore; }
    public void      setCutoffScore(double v)     { this.cutoffScore = v; }

    @Override
    public String toString() {
        return String.format(
            "App[id=%d student=%s course=%s score=%.2f%% status=%s rank=%s]",
            applicationId, studentName, courseName, academicScore,
            status, meritRank == null ? "N/A" : "#" + meritRank);
    }
}
