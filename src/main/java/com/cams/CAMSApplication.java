package com.cams;

import com.cams.ui.MainMenu;

/**
 * CAMSApplication – bootstrap class.
 *
 * Compile & run:
 *   javac -cp "lib/*" -d out $(find src -name "*.java")
 *   java  -cp "out:lib/*" com.cams.CAMSApplication
 *
 * Windows:
 *   javac -cp "lib\*" -d out  (find src -name "*.java" manually or use wildcard)
 *   java  -cp "out;lib\*" com.cams.CAMSApplication
 */
public class CAMSApplication {

    public static void main(String[] args) {
        new MainMenu().start();
    }
}
