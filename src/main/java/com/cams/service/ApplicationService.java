package com.cams.service;

import com.cams.dao.ApplicationDAO;
import com.cams.dao.CourseDAO;
import com.cams.model.Application;
import com.cams.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * ApplicationService – core business logic for admissions.
 *
 *  Key responsibilities:
 *   • Submit applications (check eligibility by cutoff score)
 *   • Approve / reject with admin remarks
 *   • Auto-approve by merit (batch processing for a course)
 *   • Retrieve filtered application lists
 */
public class ApplicationService {

    private final ApplicationDAO appDAO    = new ApplicationDAO();
    private final CourseDAO      courseDAO = new CourseDAO();

    // ── Student-facing ─────────────────────────────────────────────────────

    /**
     * Submits an application after checking the student's score
     * against the course cut-off.
     *
     * @param studentScore the applicant's academic percentage
     * @return the new Application, or empty if ineligible / already applied
     */
    public Optional<Application> applyForCourse(int studentId, int courseId,
                                                 double studentScore) {
        Optional<Course> courseOpt = courseDAO.findById(courseId);
        if (courseOpt.isEmpty()) {
            System.out.println("  Course not found.");
            return Optional.empty();
        }

        Course course = courseOpt.get();

        // ── Merit eligibility check ─────────────────────────────────────
        if (studentScore < course.getCutoffScore()) {
            System.out.printf("  ✘ Your score (%.2f%%) is below the cut-off (%.2f%%) for %s.%n",
                    studentScore, course.getCutoffScore(), course.getCourseName());
            return Optional.empty();
        }

        boolean ok = appDAO.submitApplication(studentId, courseId);
        if (!ok) return Optional.empty();

        // Recompute merit ranks for this course after new application
        appDAO.computeMeritRanks(courseId);

        Application a = new Application(studentId, courseId);
        a.setCourseName(course.getCourseName());
        return Optional.of(a);
    }

    /** Returns all applications submitted by one student. */
    public List<Application> getApplicationsByStudent(int studentId) {
        return appDAO.findByStudent(studentId);
    }

    // ── Admin-facing ──────────────────────────────────────────────────────

    /** Returns all pending applications across all courses. */
    public List<Application> getPendingApplications() {
        return appDAO.findAllPending();
    }

    /** Returns all applications (any status). */
    public List<Application> getAllApplications() {
        return appDAO.findAll();
    }

    /** Returns only approved applications (for the final admission list). */
    public List<Application> getApprovedApplications() {
        return appDAO.findApproved();
    }

    /** Applications for a specific course, ranked by merit. */
    public List<Application> getApplicationsByCourse(int courseId) {
        return appDAO.findByCourse(courseId);
    }

    /**
     * Manually approve or reject a single application.
     *
     * @param status  "Approved" or "Rejected"
     * @param remarks optional admin comment
     */
    public boolean updateStatus(int applicationId, String status, String remarks) {
        if (!status.equals("Approved") && !status.equals("Rejected"))
            throw new IllegalArgumentException("Status must be 'Approved' or 'Rejected'.");
        return appDAO.updateStatus(applicationId, status, remarks);
    }

    /**
     * Auto-processes all Pending applications for a given course:
     *  • Approves top-N students (N = totalSeats) whose score >= cutoff
     *  • Rejects the rest
     *
     * @param courseId the course to process
     * @return number of students approved
     */
    public int autoProcessByCourse(int courseId) {
        Optional<Course> courseOpt = courseDAO.findById(courseId);
        if (courseOpt.isEmpty()) return 0;

        Course course = courseOpt.get();
        // Re-rank before processing
        appDAO.computeMeritRanks(courseId);

        List<Application> apps = appDAO.findByCourse(courseId);
        int approved = 0;

        for (Application app : apps) {
            if (!"Pending".equals(app.getStatus())) continue;  // skip already-reviewed

            boolean eligible = app.getAcademicScore() >= course.getCutoffScore()
                    && approved < course.getTotalSeats();

            String newStatus = eligible ? "Approved" : "Rejected";
            String remark    = eligible
                    ? "Auto-approved: merit rank #" + app.getMeritRank()
                    : (app.getAcademicScore() < course.getCutoffScore()
                        ? "Rejected: score below cut-off"
                        : "Rejected: no seats available");

            appDAO.updateStatus(app.getApplicationId(), newStatus, remark);
            if (eligible) approved++;
        }
        return approved;
    }

    public Optional<Application> findById(int id) {
        return appDAO.findById(id);
    }

    public boolean deleteApplication(int id) {
        return appDAO.deleteApplication(id);
    }

    /** Recompute merit ranks for a course (admin utility). */
    public void recomputeRanks(int courseId) {
        appDAO.computeMeritRanks(courseId);
    }
}
