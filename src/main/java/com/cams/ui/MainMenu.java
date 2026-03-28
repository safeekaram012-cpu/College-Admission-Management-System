package com.cams.ui;

import com.cams.config.DBConnection;
import com.cams.util.ConsoleUtil;

import static com.cams.util.ConsoleUtil.*;

/**
 * MainMenu – application entry point.
 * Presents the top-level menu and routes to Admin or Student portals.
 */
public class MainMenu {

    private final AdminMenu   adminMenu   = new AdminMenu();
    private final StudentMenu studentMenu = new StudentMenu();

    public void start() {
        printBanner();
        while (true) {
            printHeader("WELCOME TO COLLEGE ADMISSION MANAGEMENT SYSTEM");
            System.out.println("  1. Student Portal");
            System.out.println("  2. Admin Portal");
            System.out.println("  0. Exit");
            printLine();

            int choice = promptInt("Select option");
            switch (choice) {
                case 1 -> studentMenu.show();
                case 2 -> adminMenu.show();
                case 0 -> {
                    DBConnection.closeConnection();
                    System.out.println();
                    success("Thank you for using CAMS. Goodbye!");
                    System.out.println();
                    System.exit(0);
                }
                default -> error("Invalid option. Please try again.");
            }
        }
    }

    private void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("  ╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║        COLLEGE ADMISSION MANAGEMENT SYSTEM (CAMS)           ║");
        System.out.println("  ║              Java + JDBC + MySQL  –  v1.0                   ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }
}
