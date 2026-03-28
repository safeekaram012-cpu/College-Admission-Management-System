# College Admission Management System (CAMS)

A full-featured **Java + JDBC + MySQL** console application that manages the complete college admission workflow — from student registration to merit-based approval and PDF/CSV report generation.

---

## Project Structure

```
CAMS/
├── src/main/java/com/cams/
│   ├── CAMSApplication.java          <- Main entry point
│   ├── config/
│   │   └── DBConnection.java         <- JDBC singleton connection
│   ├── model/
│   │   ├── Student.java
│   │   ├── Course.java
│   │   ├── Application.java
│   │   └── Admin.java
│   ├── dao/
│   │   ├── StudentDAO.java           <- CRUD for Students table
│   │   ├── CourseDAO.java            <- CRUD for Courses table
│   │   ├── ApplicationDAO.java       <- CRUD + merit ranking
│   │   └── AdminDAO.java             <- Admin authentication
│   ├── service/
│   │   ├── StudentService.java       <- Registration & login logic
│   │   ├── CourseService.java        <- Course management logic
│   │   ├── ApplicationService.java   <- Merit calculation & approval
│   │   └── ReportService.java        <- CSV + PDF export
│   └── ui/
│       ├── MainMenu.java             <- Top-level menu
│       ├── AdminMenu.java            <- Admin portal UI
│       └── StudentMenu.java          <- Student portal UI
├── lib/
│   └── mysql-connector-j-*.jar       <- (you add this - see setup)
├── db.sql                            <- Database creation + sample data
├── build.sh                          <- Build & run (Linux/macOS)
├── build.bat                         <- Build & run (Windows)
└── README.md
```

---

## Prerequisites

- Java JDK 11 or higher
- MySQL Server 8.0+
- MySQL Connector/J 8.x or 9.x

---

## Setup Instructions

### Step 1 - Add MySQL Connector JAR

1. Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/
2. Select Platform Independent -> download the .zip or .tar.gz
3. Extract and copy `mysql-connector-j-*.jar` into the `lib/` folder

### Step 2 - Create the database

```bash
mysql -u root -p < db.sql
```

This creates `cams_db` with tables and sample data (10 students, 5 courses, 9 applications).

### Step 3 - Configure DB credentials

Open `src/main/java/com/cams/config/DBConnection.java` and update:

```java
private static final String USER     = "root";    // your MySQL username
private static final String PASSWORD = "root";    // your MySQL password
```

### Step 4 - Compile and run

**Linux / macOS:**
```bash
chmod +x build.sh
./build.sh
```

**Windows:**
```cmd
build.bat
```

**Manual (any OS):**
```bash
# Linux/macOS
mkdir -p out
find src -name "*.java" > sources.txt
javac -cp "lib/*" -d out @sources.txt
java -cp "out:lib/*" com.cams.CAMSApplication

# Windows
mkdir out
dir /s /b src\*.java > sources.txt
javac -cp "lib\*" -d out @sources.txt
java -cp "out;lib\*" com.cams.CAMSApplication
```

---

## Default Credentials

| Role    | Username / Email          | Password   |
|---------|---------------------------|------------|
| Admin   | admin                     | admin123   |
| Student | aarav@example.com (etc.)  | pass123    |

---

## Features

- Student registration and login (SHA-256 password hashing)
- Browse courses with eligibility indicator (meets/below cutoff)
- Apply for courses (one application per course per student)
- Merit ranking by academic score
- Cutoff enforcement on application submission
- Admin manual approve/reject with remarks
- Admin auto-process entire course by merit + seats
- CSV export: admission_list.csv
- PDF export: admission_list.pdf (pure Java, no external lib)
- Full exception handling on all DB operations
- Colour-coded console UI

---

## Database Schema

```
Students     (student_id, full_name, email, password, phone, dob, gender, address, academic_score, created_at)
Courses      (course_id, course_name, department, total_seats, cutoff_score, duration_years, description)
Applications (application_id, student_id, course_id, status, merit_rank, applied_at, reviewed_at, admin_remarks)
Admins       (admin_id, username, password)
```

---

## Output Files

Generated in the project root after admin runs "Generate Reports":

| File                 | Contents                                        |
|----------------------|-------------------------------------------------|
| admission_list.csv   | Approved students with rank, score, course      |
| admission_list.pdf   | Formatted admission list (pure Java PDF)        |

---

## Architecture

```
UI Layer       AdminMenu / StudentMenu / MainMenu
     |
Service Layer  StudentService / CourseService / ApplicationService / ReportService
     |
DAO Layer      StudentDAO / CourseDAO / ApplicationDAO / AdminDAO
     |
Config         DBConnection (JDBC singleton)
     |
MySQL          cams_db
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| ClassNotFoundException: Driver | Add MySQL JAR to lib/ |
| Access denied for user | Fix password in DBConnection.java |
| Unknown database cams_db | Run db.sql first |
| Duplicate entry on apply | Student already applied for that course |
| ANSI colours not showing (Windows) | Use Windows Terminal |

---

## License

MIT License - free to use, modify, and distribute.
