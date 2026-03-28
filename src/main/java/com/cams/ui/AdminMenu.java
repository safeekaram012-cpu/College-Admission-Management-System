package com.cams.ui;

import com.cams.dao.AdminDAO;
import com.cams.model.Admin;
import com.cams.model.Application;
import com.cams.model.Course;
import com.cams.service.ApplicationService;
import com.cams.service.CourseService;
import com.cams.service.ReportService;
import com.cams.util.ConsoleUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.cams.util.ConsoleUtil.*;

/**
 * AdminMenu – console UI for the admin portal.
 *
 *  Features:
 *   • Login / logout
 *   • View all / pending applications
 *   • Approve or reject individual applications
 *   • Auto-process a course by merit + cutoff
 *   • Manage courses (list, add, update cutoff, delete)
 *   • Generate CSV + PDF admission list
 */
public class AdminMenu {

    private final AdminDAO          adminDAO   = new AdminDAO();
    private final ApplicationService appSvc    = new ApplicationService();
    private final CourseService      courseSvc = new CourseService();
    private final ReportService      reportSvc = new ReportService();

    private Admin currentAdmin = null;

    // ── Entry point ───────────────────────────────────────────────────────

    public void show() {
        printHeader("ADMIN PORTAL");

        // Login loop
        for (int attempts = 0; attempts < 3; attempts++) {
            String username = prompt("Username");
            String password = prompt("Password");

            Optional<Admin> opt = adminDAO.login(username, password);
            if (opt.isPresent()) {
                currentAdmin = opt.get();
                success("Welcome, " + currentAdmin.getUsername() + "!");
                mainMenu();
                return;
            }
            error("Invalid credentials. " + (2 - attempts) + " attempt(s) remaining.");
        }
        error("Too many failed attempts. Returning to main menu.");
    }

    // ── Main menu ─────────────────────────────────────────────────────────

    private void mainMenu() {
        while (true) {
            printHeader("ADMIN MAIN MENU");
            System.out.println("  1. View all applications");
            System.out.println("  2. View pending applications");
            System.out.println("  3. Approve / Reject application");
            System.out.println("  4. Auto-process course by merit");
            System.out.println("  5. Manage courses");
            System.out.println("  6. Generate admission list (CSV + PDF)");
            System.out.println("  0. Logout");
            printLine();

            int choice = promptInt("Enter choice");
            switch (choice) {
                case 1 -> viewAllApplications();
                case 2 -> viewPendingApplications();
                case 3 -> approveRejectApplication();
                case 4 -> autoProcessCourse();
                case 5 -> manageCourses();
                case 6 -> generateReports();
                case 0 -> { success("Logged out."); return; }
                default -> error("Invalid option.");
            }
        }
    }

    // ── View applications ─────────────────────────────────────────────────

    private void viewAllApplications() {
        printHeader("ALL APPLICATIONS");
        List<Application> list = appSvc.getAllApplications();
        if (list.isEmpty()) { info("No applications found."); return; }
        printApplicationTable(list);
    }

    private void viewPendingApplications() {
        printHeader("PENDING APPLICATIONS");
        List<Application> list = appSvc.getPendingApplications();
        if (list.isEmpty()) { info("No pending applications."); return; }
        printApplicationTable(list);
    }

    // ── Approve / Reject ──────────────────────────────────────────────────

    private void approveRejectApplication() {
        printHeader("APPROVE / REJECT APPLICATION");
        viewPendingApplications();

        int appId = promptInt("Enter Application ID to review (0 to cancel)");
        if (appId == 0) return;

        Optional<Application> opt = appSvc.findById(appId);
        if (opt.isEmpty()) { error("Application not found."); return; }

        Application app = opt.get();
        System.out.printf("%n  Student : %s  (score: %.2f%%)%n", app.getStudentName(), app.getAcademicScore());
        System.out.printf("  Course  : %s  (cutoff: %.2f%%)%n", app.getCourseName(), app.getCutoffScore());

        System.out.println("  1. Approve");
        System.out.println("  2. Reject");
        int action = promptInt("Action");
        if (action != 1 && action != 2) { error("Cancelled."); return; }

        String status  = action == 1 ? "Approved" : "Rejected";
        String remarks = prompt("Remarks (optional)");

        boolean ok = appSvc.updateStatus(appId, status, remarks.isEmpty() ? null : remarks);
        if (ok) success("Application " + appId + " marked as " + status + ".");
        else    error("Update failed.");
    }

    // ── Auto-process ──────────────────────────────────────────────────────

    private void autoProcessCourse() {
        printHeader("AUTO-PROCESS COURSE BY MERIT");
        listCourses();

        int courseId = promptInt("Enter Course ID to process (0 to cancel)");
        if (courseId == 0) return;

        info("Computing merit ranks and applying cutoff...");
        int approvedCount = appSvc.autoProcessByCourse(courseId);
        success(approvedCount + " application(s) approved. Remaining rejected.");
    }

    // ── Course management ─────────────────────────────────────────────────

    private void manageCourses() {
        printHeader("COURSE MANAGEMENT");
        System.out.println("  1. List courses");
        System.out.println("  2. Add new course");
        System.out.println("  3. Update course cutoff");
        System.out.println("  4. Delete course");
        System.out.println("  0. Back");

        int choice = promptInt("Choice");
        switch (choice) {
            case 1 -> listCourses();
            case 2 -> addCourse();
            case 3 -> updateCutoff();
            case 4 -> deleteCourse();
        }
    }

    private void listCourses() {
        List<Course> courses = courseSvc.getAllCourses();
        System.out.printf("%n  %-4s %-36s %-16s %7s %7s%n",
                "ID", "Course Name", "Department", "Seats", "Cutoff");
        printLine();
        for (Course c : courses) {
            System.out.printf("  %-4d %-36s %-16s %7d %6.1f%%%n",
                    c.getCourseId(), c.getCourseName(), c.getDepartment(),
                    c.getTotalSeats(), c.getCutoffScore());
        }
    }

    private void addCourse() {
        printHeader("ADD NEW COURSE");
        String name    = prompt("Course name");
        String dept    = prompt("Department");
        int    seats   = promptInt("Total seats");
        double cutoff  = promptDouble("Cutoff score (%)");
        int    years   = promptInt("Duration (years)");
        String desc    = prompt("Description");

        try {
            Optional<Course> opt = courseSvc.addCourse(name, dept, seats, cutoff, years, desc);
            if (opt.isPresent()) success("Course added with ID: " + opt.get().getCourseId());
            else                 error("Failed to add course.");
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void updateCutoff() {
        listCourses();
        int    courseId = promptInt("Enter Course ID");
        double newCutoff = promptDouble("New cutoff score (%)");

        Optional<Course> opt = courseSvc.findById(courseId);
        if (opt.isEmpty()) { error("Course not found."); return; }

        Course c = opt.get();
        c.setCutoffScore(newCutoff);
        if (courseSvc.updateCourse(c)) success("Cutoff updated to " + newCutoff + "%.");
        else                            error("Update failed.");
    }

    private void deleteCourse() {
        listCourses();
        int courseId = promptInt("Enter Course ID to delete");
        String confirm = prompt("Type 'yes' to confirm deletion");
        if ("yes".equalsIgnoreCase(confirm)) {
            if (courseSvc.deleteCourse(courseId)) success("Course deleted.");
            else                                   error("Delete failed.");
        } else {
            info("Deletion cancelled.");
        }
    }

    // ── Reports ───────────────────────────────────────────────────────────

    private void generateReports() {
        printHeader("GENERATE ADMISSION LIST");
        try {
            reportSvc.generateCSV("admission_list.csv");
            reportSvc.generatePDF("admission_list.pdf");
            success("Reports saved in the project root directory.");
        } catch (IOException e) {
            error("Export error: " + e.getMessage());
        }
    }

    // ── Display helpers ───────────────────────────────────────────────────

    private void printApplicationTable(List<Application> list) {
        System.out.printf("%n  %-5s %-22s %-28s %7s %-10s %-5s%n",
                "ID", "Student", "Course", "Score", "Status", "Rank");
        printLine();
        for (Application a : list) {
            System.out.printf("  %-5d %-22s %-28s %6.1f%% %-10s %-5s%n",
                    a.getApplicationId(),
                    truncate(a.getStudentName(), 22),
                    truncate(a.getCourseName(), 28),
                    a.getAcademicScore(),
                    a.getStatus(),
                    a.getMeritRank() == null ? "-" : "#" + a.getMeritRank());
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }
}
