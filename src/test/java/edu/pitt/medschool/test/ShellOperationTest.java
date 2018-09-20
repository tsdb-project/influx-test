package edu.pitt.medschool.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellOperationTest {

    public static void main(String[] args) {
        String line;
        try {
            Process p = Runtime.getRuntime().exec(new String[] { "sh", "-c", "ps aux | grep influxd" });
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception err) {
            System.out.println(err);
        }
    }
}
