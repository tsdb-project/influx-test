package edu.pitt.medschool.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class CsvMergeTest {
    final static String DIR = "/tsdb/post-processing/out/oo2";

    public static void main(String[] args) throws IOException {

        File[] files = new File(DIR).listFiles();
        Arrays.sort(files);
        FileWriter writer = new FileWriter(new File(DIR + "_output.csv"));

        if (files.length > 0) {
            FileReader reader = new FileReader(files[0]);
            BufferedReader br = new BufferedReader(reader);
            if (br.ready()) {
                writer.write("\"PID\",\"Timestamp\",\"Timebins\",\"CZ-PZ\",\"Fz-Cz\",\"P4-O2\",\"C4-P4\",\"F4-C4\",\"FP2-F4\",\"P3-O1\",\"C3-P3\",\"F3-C3\",\"FP1-F3\",\"T6-O2\",\"T4-T6\",\"F8-T4\",\"FP2-F8\",\"T5-O1\",\"T3-T5\",\"F7-T3\",\"FP1-F7\"" + "\n");
            }
            br.close();
        }

        for (File file : files) {
            System.out.println(file.getPath());
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            if (br.ready()) {
                // HAS HEADER OR NOT
                // br.readLine();
            }
            while (br.ready()) {
                writer.write(br.readLine() + "\n");
            }
            br.close();
        }
        writer.close();
    }
}
