package com.cams.ui;

import com.cams.model.Application;
import com.cams.model.Course;
import com.cams.model.Student;
import com.cams.service.ApplicationService;
import com.cams.service.CourseService;
import com.cams.service.StudentService;
import com.cams.util.ConsoleUtil;

import java.util.List;
import java.util.Optional;

import static com.cams.util.ConsoleUtil.*;

/**
 * StudentMenu – console UI for the student portal.
 *
 *  Features:
 *   • Register new account
 *   • Login / logout
 *   • View available courses
 *   • Apply for a course
 *   • View application status
 *   • View profile
 */
public class StudentMenu {

    private final StudentService     studentSvc = new StudentService();
    private final CourseService      courseSvc  = new CourseService();
    private final ApplicationService appSvc     = new ApplicationService();

    private Student currentStudent = null;

    // ── Entry point ───────────────────────────────────────────────────────

    public void show() {
        printHeader("STUDENT PORTAL");
        System.out.println("  1. Register");
        System.out.println("  2. Login");
        System.out.println("  0. Back");
        printLine();

        int choice = promptInt("Choice");
        switch (choice) {
            case 1 -> { registerFlow(); if (currentStudent != null) studentMainMenu(); }
            case 2 -> { loginFlow();    if (currentStudent != null) studentMainMenu(); }
        }
    }

    // ── Registration ──────────────────────────────────────────────────────

    private void registerFlow() {
        printHeader("STUDENT REGISTRATION");

        String name    = prompt("Full name");
        String email   = prompt("Email");
        String pass    = prompt("Password (min 6 chars)");
        String phone   = prompt("Phone number");
        String dob     = prompt("Date of birth (YYYY-MM-DD)");
        String gender  = prompt("Gender (Male/Female/Other)");
        String address = prompt("Address");
        double score   = promptDouble("Academic score (%)");

        try {
            Optional<Student> opt = studentSvc.register(
                    name, email, pass, phone, dob, gender, address, score);
            if (opt.isPresent()) {
                currentStudent = opt.get();
                success("Registration successful! Your Student ID: " + currentStudent.getStudentId());
            } else {
                error("Registration failed. Email may already be in use.");
            }
        } catch (IllegalArgumentException e) {
            error("Validation error: " + e.getMessage());
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────

    private void loginFlow() {
        printHeader("STUDENT LOGIN");
        String email = prompt("Email");
        String pass  = prompt("Password");

        Optional<Student> opt = studentSvc.login(email, pass);
        if (opt.isPresent()) {
            currentStudent = opt.get();
            success("Welcome back, " + currentStudent.getFullName() + "!");
        } else {
            error("Invalid email or password.");
        }
    }

    // ── Main menu (post-login) ─────────────────────────────────────────────

    private void studentMainMenu() {
        while (true) {
            printHeader("STUDENT MENU  –  " + currentStudent.getFullName());
            System.out.println("  1. View available courses");
            System.out.println("  2. Apply for a course");
            System.out.println("  3. View my applications");
            System.out.println("  4. View my profile");
            System.out.println("  0. Logout");
            printLine();

            int choice = promptInt("Choice");
            switch (choice) {
                case 1 -> viewCourses();
                case 2 -> applyForCourse();
                case 3 -> viewMyApplications();
                case 4 -> viewProfile();
                case 0 -> { currentStudent = null; success("Logged out."); return; }
                default -> error("Invalid option.");
            }
        }
    }

    // ── View courses ──────────────────────────────────────────────────────

    private void viewCourses() {
        printHeader("AVAILABLE COURSES");
        List<Course> courses = courseSvc.getAllCourses();
        System.out.printf("  %-4s %-36s %-16s %7s %8s %6s%n",
                "ID", "Course Name", "Department", "Seats", "Cutoff", "Yrs");
        printLine();
        for (Course c : courses) {
            boolean eligible = currentStudent.getAcademicScore() >= c.getCutoffScore();
            String  mark     = eligible ? GREEN + " ✔" + RESET : RED + " ✘" + RESET;
            System.out.printf("  %-4d %-36s %-16s %7d %7.1f%% %5d  %s%n",
                    c.getCourseId(), c.getCourseName(), c.getDepartment(),
                    c.getTotalSeats(), c.getCutoffScore(), c.getDurationYears(), mark);
        }
        System.out.println();
        info("✔ = you meet the cutoff  ✘ = below cutoff");
    }

    // ── Apply for course ──────────────────────────────────────────────────

    private void applyForCourse() {
        printHeader("APPLY FOR A COURSE");
        viewCourses();

        int courseId = promptInt("Enter Course ID to apply (0 to cancel)");
        if (courseId == 0) return;

        Optional<Application> opt = appSvc.applyForCourse(
                currentStudent.getStudentId(),
                courseId,
                currentStudent.getAcademicScore());

        if (opt.isPresent()) {
            success("Application submitted for: " + opt.get().getCourseName());
            info("Status: Pending – an admin will review your application.");
        } else {
            error("Application could not be submitted.");
        }
    }

    // ── View applications ─────────────────────────────────────────────────

    private void viewMyApplications() {
        printHeader("MY APPLICATIONS");
        List<Application> apps = appSvc.getApplicationsByStudent(currentStudent.getStudentId());

        if (apps.isEmpty()) { info("You have not applied for any courses yet."); return; }

        System.out.printf("  %-5s %-36s %-10s %-5s  %s%n",
                "ID", "Course", "Status", "Rank", "Applied At");
        printLine();
        for (Application a : apps) {
            String statusColour = switch (a.getStatus()) {
                case "Approved" -> GREEN;
                case "Rejected" -> RED;
                default         -> YELLOW;
            };
            System.out.printf("  %-5d %-36s %s%-10s%s %-5s  %s%n",
                    a.getApplicationId(),
                    a.getCourseName(),
                    statusColour, a.getStatus(), RESET,
                    a.getMeritRank() == null ? "-" : "#" + a.getMeritRank(),
                    a.getAppliedAt());
        }
    }

    // ── View profile ──────────────────────────────────────────────────────

    private void viewProfile() {
        printHeader("MY PROFILE");
        Student s = currentStudent;
        System.out.printf("  %-20s : %s%n", "Student ID",     s.getStudentId());
        System.out.printf("  %-20s : %s%n", "Full Name",      s.getFullName());
        System.out.printf("  %-20s : %s%n", "Email",          s.getEmail());
        System.out.printf("  %-20s : %s%n", "Phone",          s.getPhone());
        System.out.printf("  %-20s : %s%n", "Date of Birth",  s.getDob());
        System.out.printf("  %-20s : %s%n", "Gender",         s.getGender());
        System.out.printf("  %-20s : %s%n", "Address",        s.getAddress());
        System.out.printf("  %-20s : %.2f%%%n","Academic Score", s.getAcademicScore());
        System.out.printf("  %-20s : %s%n", "Registered At",  s.getCreatedAt());
    }
}
