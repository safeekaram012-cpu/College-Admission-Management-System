package com.cams.util;

import java.util.Scanner;

/**
 * ConsoleUtil – shared helpers for pretty console output and input.
 */
public class ConsoleUtil {

    public static final String RESET  = "\033[0m";
    public static final String BOLD   = "\033[1m";
    public static final String GREEN  = "\033[32m";
    public static final String CYAN   = "\033[36m";
    public static final String YELLOW = "\033[33m";
    public static final String RED    = "\033[31m";
    public static final String BLUE   = "\033[34m";

    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleUtil() {}

    /** Print a full-width horizontal rule. */
    public static void printLine() {
        System.out.println("═".repeat(65));
    }

    /** Print a section header inside a box. */
    public static void printHeader(String title) {
        System.out.println();
        printLine();
        System.out.println(BOLD + CYAN + "  " + title + RESET);
        printLine();
    }

    /** Print a success message in green. */
    public static void success(String msg) {
        System.out.println(GREEN + "  ✔  " + msg + RESET);
    }

    /** Print an error message in red. */
    public static void error(String msg) {
        System.out.println(RED + "  ✘  " + msg + RESET);
    }

    /** Print an info message in yellow. */
    public static void info(String msg) {
        System.out.println(YELLOW + "  ℹ  " + msg + RESET);
    }

    /** Prompt and return trimmed string input. */
    public static String prompt(String label) {
        System.out.print(BOLD + "  " + label + ": " + RESET);
        return scanner.nextLine().trim();
    }

    /** Prompt and return integer input; returns -1 on parse error. */
    public static int promptInt(String label) {
        String raw = prompt(label);
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            error("Invalid number entered.");
            return -1;
        }
    }

    /** Prompt and return double input; returns -1 on parse error. */
    public static double promptDouble(String label) {
        String raw = prompt(label);
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            error("Invalid number entered.");
            return -1;
        }
    }

    public static Scanner getScanner() { return scanner; }
}
