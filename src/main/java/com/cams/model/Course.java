package com.cams.model;

/**
 * Model class representing a row in the Courses table.
 */
public class Course {

    private int    courseId;
    private String courseName;
    private String department;
    private int    totalSeats;
    private double cutoffScore;    // minimum academic % required
    private int    durationYears;
    private String description;

    // ── Constructors ─────────────────────────────────────────────────────

    public Course() {}

    public Course(String courseName, String department, int totalSeats,
                  double cutoffScore, int durationYears, String description) {
        this.courseName    = courseName;
        this.department    = department;
        this.totalSeats    = totalSeats;
        this.cutoffScore   = cutoffScore;
        this.durationYears = durationYears;
        this.description   = description;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int    getCourseId()           { return courseId; }
    public void   setCourseId(int v)      { this.courseId = v; }

    public String getCourseName()         { return courseName; }
    public void   setCourseName(String v) { this.courseName = v; }

    public String getDepartment()         { return department; }
    public void   setDepartment(String v) { this.department = v; }

    public int    getTotalSeats()         { return totalSeats; }
    public void   setTotalSeats(int v)    { this.totalSeats = v; }

    public double getCutoffScore()        { return cutoffScore; }
    public void   setCutoffScore(double v){ this.cutoffScore = v; }

    public int    getDurationYears()      { return durationYears; }
    public void   setDurationYears(int v) { this.durationYears = v; }

    public String getDescription()        { return description; }
    public void   setDescription(String v){ this.description = v; }

    @Override
    public String toString() {
        return String.format("Course[id=%d, name=%-35s dept=%-15s cutoff=%.1f%% seats=%d]",
                courseId, courseName, department, cutoffScore, totalSeats);
    }
}
