-- ============================================================
--  College Admission Management System (CAMS) - Database Script
--  Run: mysql -u root -p < db.sql
-- ============================================================

DROP DATABASE IF EXISTS cams_db;
CREATE DATABASE cams_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cams_db;

-- ─────────────────────────────────────────────
-- Table: Students
-- ─────────────────────────────────────────────
CREATE TABLE Students (
    student_id   INT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(100)        NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    password     VARCHAR(255)        NOT NULL,  -- stored as SHA-256 hex
    phone        VARCHAR(15),
    dob          DATE,
    gender       ENUM('Male','Female','Other'),
    address      TEXT,
    academic_score DECIMAL(5,2)      NOT NULL DEFAULT 0.00,  -- percentage 0-100
    created_at   TIMESTAMP           DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- Table: Courses
-- ─────────────────────────────────────────────
CREATE TABLE Courses (
    course_id    INT AUTO_INCREMENT PRIMARY KEY,
    course_name  VARCHAR(150)        NOT NULL,
    department   VARCHAR(100)        NOT NULL,
    total_seats  INT                 NOT NULL DEFAULT 60,
    cutoff_score DECIMAL(5,2)        NOT NULL DEFAULT 60.00,  -- min % required
    duration_years INT               NOT NULL DEFAULT 4,
    description  TEXT
);

-- ─────────────────────────────────────────────
-- Table: Applications
-- ─────────────────────────────────────────────
CREATE TABLE Applications (
    application_id  INT AUTO_INCREMENT PRIMARY KEY,
    student_id      INT NOT NULL,
    course_id       INT NOT NULL,
    status          ENUM('Pending','Approved','Rejected') DEFAULT 'Pending',
    merit_rank      INT,                              -- computed rank within course
    applied_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at     TIMESTAMP NULL,
    admin_remarks   VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES Courses(course_id)  ON DELETE CASCADE,
    UNIQUE KEY unique_application (student_id, course_id)   -- one app per course
);

-- ─────────────────────────────────────────────
-- Table: Admins  (simple single-table auth)
-- ─────────────────────────────────────────────
CREATE TABLE Admins (
    admin_id   INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL   -- SHA-256 hex
);

-- ─────────────────────────────────────────────
-- Sample Data – Admins
-- password = "admin123"  → SHA-256
-- ─────────────────────────────────────────────
INSERT INTO Admins (username, password) VALUES
('admin', 'ef92b778bafe771207914cbc1992a8e3cf23da1a7c05df8c3e1c2e9e01c3a2f1');

-- ─────────────────────────────────────────────
-- Sample Data – Courses
-- ─────────────────────────────────────────────
INSERT INTO Courses (course_name, department, total_seats, cutoff_score, duration_years, description) VALUES
('B.Tech Computer Science',     'Engineering',       60, 75.00, 4, 'Bachelor of Technology in Computer Science and Engineering'),
('B.Tech Electronics',          'Engineering',       60, 70.00, 4, 'Bachelor of Technology in Electronics and Communication'),
('B.Sc Mathematics',            'Science',           40, 65.00, 3, 'Bachelor of Science with honours in Mathematics'),
('MBA Business Administration', 'Management',        50, 60.00, 2, 'Master of Business Administration – General Management'),
('B.Tech Mechanical Engg',      'Engineering',       60, 68.00, 4, 'Bachelor of Technology in Mechanical Engineering');

-- ─────────────────────────────────────────────
-- Sample Data – Students
-- All passwords = "pass123"
-- SHA-256("pass123") = 9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf
-- ─────────────────────────────────────────────
INSERT INTO Students (full_name, email, password, phone, dob, gender, address, academic_score) VALUES
('Aarav Sharma',      'aarav@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543210', '2004-03-15', 'Male',   '12 MG Road, Bangalore',      88.50),
('Priya Nair',        'priya@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543211', '2004-07-22', 'Female', '45 Anna Salai, Chennai',     92.00),
('Ravi Kumar',        'ravi@example.com',     '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543212', '2003-11-10', 'Male',   '7 Park Street, Kolkata',     62.00),
('Sneha Patel',       'sneha@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543213', '2004-01-05', 'Female', '88 CG Road, Ahmedabad',      78.75),
('Arjun Mehta',       'arjun@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543214', '2003-09-30', 'Male',   '33 Connaught Place, Delhi',  55.00),
('Divya Reddy',       'divya@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543215', '2004-06-18', 'Female', '21 Banjara Hills, Hyderabad',95.50),
('Karan Singh',       'karan@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543216', '2003-12-25', 'Male',   '5 Sector 17, Chandigarh',    71.00),
('Meera Joshi',       'meera@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543217', '2004-04-14', 'Female', '60 FC Road, Pune',           83.25),
('Rohan Das',         'rohan@example.com',    '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543218', '2003-08-08', 'Male',   '14 Salt Lake, Kolkata',      67.50),
('Ananya Iyer',       'ananya@example.com',   '9b8769a4a742959a2d0298c36fb70623f2a2d753380db50312c9a3631b07dbf', '9876543219', '2004-02-28', 'Female', '99 Adyar, Chennai',          80.00);

-- ─────────────────────────────────────────────
-- Sample Applications (a few pre-seeded)
-- ─────────────────────────────────────────────
INSERT INTO Applications (student_id, course_id, status, applied_at) VALUES
(1, 1, 'Pending',  NOW()),
(2, 1, 'Pending',  NOW()),
(3, 3, 'Pending',  NOW()),
(4, 2, 'Pending',  NOW()),
(6, 1, 'Pending',  NOW()),
(7, 4, 'Pending',  NOW()),
(8, 1, 'Pending',  NOW()),
(9, 5, 'Pending',  NOW()),
(10,2, 'Pending',  NOW());

-- ─────────────────────────────────────────────
-- Stored Procedure: Compute Merit Ranks
-- Call CALL compute_merit_ranks(course_id);
-- ─────────────────────────────────────────────
DELIMITER $$
CREATE PROCEDURE compute_merit_ranks(IN p_course_id INT)
BEGIN
    SET @rank = 0;
    UPDATE Applications a
    JOIN Students s ON a.student_id = s.student_id
    SET a.merit_rank = (@rank := @rank + 1)
    WHERE a.course_id = p_course_id
    ORDER BY s.academic_score DESC;
END$$
DELIMITER ;

SELECT 'Database setup complete!' AS Status;
