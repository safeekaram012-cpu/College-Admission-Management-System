package com.cams.service;

import com.cams.dao.CourseDAO;
import com.cams.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * CourseService – thin service layer for course management.
 */
public class CourseService {

    private final CourseDAO courseDAO = new CourseDAO();

    public Optional<Course> addCourse(String name, String dept, int seats,
                                      double cutoff, int years, String desc) {
        if (name.isBlank()) throw new IllegalArgumentException("Course name required.");
        if (seats <= 0)     throw new IllegalArgumentException("Total seats must be > 0.");
        if (cutoff < 0 || cutoff > 100) throw new IllegalArgumentException("Cutoff must be 0-100.");

        Course c = new Course(name, dept, seats, cutoff, years, desc);
        return courseDAO.addCourse(c) ? Optional.of(c) : Optional.empty();
    }

    public List<Course> getAllCourses() {
        return courseDAO.findAll();
    }

    public Optional<Course> findById(int id) {
        return courseDAO.findById(id);
    }

    public boolean updateCourse(Course c) {
        return courseDAO.updateCourse(c);
    }

    public boolean deleteCourse(int id) {
        return courseDAO.deleteCourse(id);
    }
}
