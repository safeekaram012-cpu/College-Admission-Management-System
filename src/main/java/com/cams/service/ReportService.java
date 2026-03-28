package com.cams.service;

import com.cams.model.Application;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ReportService – generates the final admission list in CSV and PDF formats.
 *
 * PDF is generated using a pure-Java approach (no external libs required):
 * we embed minimal PDF syntax to produce a readable, printable document.
 * If you prefer iText / PDFBox, swap out generatePDF() accordingly.
 */
public class ReportService {

    private final ApplicationService appService = new ApplicationService();

    // ── CSV Export ────────────────────────────────────────────────────────

    /**
     * Writes approved applications to admission_list.csv.
     *
     * @param filePath destination path, e.g. "admission_list.csv"
     */
    public void generateCSV(String filePath) throws IOException {
        List<Application> approved = appService.getApprovedApplications();

        if (approved.isEmpty()) {
            System.out.println("  No approved applications to export.");
            return;
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            // Header row
            pw.println("Application ID,Student Name,Email,Academic Score (%),Course,Merit Rank,Status,Reviewed At,Admin Remarks");

            for (Application a : approved) {
                pw.printf("%d,\"%s\",%s,%.2f,\"%s\",%s,%s,%s,\"%s\"%n",
                        a.getApplicationId(),
                        esc(a.getStudentName()),
                        a.getStudentEmail(),
                        a.getAcademicScore(),
                        esc(a.getCourseName()),
                        a.getMeritRank() == null ? "" : a.getMeritRank(),
                        a.getStatus(),
                        a.getReviewedAt() == null ? "" : a.getReviewedAt().toString(),
                        esc(a.getAdminRemarks()));
            }
        }

        System.out.println("  CSV exported → " + filePath + "  (" + approved.size() + " records)");
    }

    // ── PDF Export (pure Java – no external dependency) ───────────────────

    /**
     * Generates a minimal but valid PDF admission list.
     * Layout: title page header + one row per approved student.
     */
    public void generatePDF(String filePath) throws IOException {
        List<Application> approved = appService.getApprovedApplications();

        if (approved.isEmpty()) {
            System.out.println("  No approved applications to export.");
            return;
        }

        // Build page content as plain text embedded in PDF stream
        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 16 Tf\n");
        content.append("50 780 Td\n");
        content.append("(College Admission Management System) Tj\n");
        content.append("/F1 12 Tf\n");
        content.append("0 -20 Td\n");
        content.append("(Final Admission List - ").append(LocalDate.now()).append(") Tj\n");
        content.append("/F1 10 Tf\n");
        content.append("0 -15 Td\n");
        content.append("(----------------------------------------------------------------------) Tj\n");
        content.append("0 -12 Td\n");
        content.append("(No  Name                     Course                     Score  Rank) Tj\n");
        content.append("0 -10 Td\n");
        content.append("(----------------------------------------------------------------------) Tj\n");

        int i = 1;
        for (Application a : approved) {
            content.append("0 -12 Td\n");
            String line = String.format("%-4d %-24s %-26s %5.1f  %-4s",
                    i++,
                    truncate(a.getStudentName(), 24),
                    truncate(a.getCourseName(), 26),
                    a.getAcademicScore(),
                    a.getMeritRank() == null ? "-" : "#" + a.getMeritRank());
            content.append("(").append(pdfEsc(line)).append(") Tj\n");
        }

        content.append("ET\n");
        String streamData = content.toString();

        // Assemble minimal PDF
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        // Object 1 – catalog
        int off1 = pdf.length();
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Object 2 – pages
        int off2 = pdf.length();
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        // Object 3 – page
        int off3 = pdf.length();
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R ")
           .append("/MediaBox [0 0 612 842] ")
           .append("/Contents 4 0 R ")
           .append("/Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");

        // Object 4 – stream
        byte[] streamBytes = streamData.getBytes("ISO-8859-1");
        int off4 = pdf.length();
        pdf.append("4 0 obj\n<< /Length ").append(streamBytes.length).append(" >>\nstream\n");
        String pdfBefore = pdf.toString();
        pdf.append(streamData);
        pdf.append("\nendstream\nendobj\n");

        // Object 5 – font
        int off5 = pdf.length();
        pdf.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n");

        // Cross-reference table
        int xrefOffset = pdf.length();
        String xref = buildXref(new int[]{0, off1, off2, off3, off4, off5});
        pdf.append(xref);
        pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdf.toString().getBytes("ISO-8859-1"));
        }

        System.out.println("  PDF exported → " + filePath + "  (" + approved.size() + " records)");
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Escape double-quotes for CSV. */
    private String esc(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }

    /** Escape PDF string special characters. */
    private String pdfEsc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private String buildXref(int[] offsets) {
        StringBuilder sb = new StringBuilder("xref\n");
        sb.append("0 ").append(offsets.length).append("\n");
        sb.append("0000000000 65535 f \n");
        for (int i = 1; i < offsets.length; i++) {
            sb.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        return sb.toString();
    }
}
