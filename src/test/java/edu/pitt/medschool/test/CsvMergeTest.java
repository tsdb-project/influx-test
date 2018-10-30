package edu.pitt.medschool.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CsvMergeTest {
    final static String DIR = "/tsdb/merge/";

    public static void main(String[] args) throws IOException {
        File[] files = new File(DIR).listFiles();
        FileWriter writer = new FileWriter(new File(DIR + "output.csv"));

        if (files.length > 0) {
            FileReader reader = new FileReader(files[0]);
            BufferedReader br = new BufferedReader(reader);
            if (br.ready()) {
                writer.write(br.readLine() + "\n");
            }
            br.close();
        }

        for (File file : files) {
            System.out.println(file.getPath());
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            if (br.ready()) {
                br.readLine();
            }
            while (br.ready()) {
                writer.write(br.readLine() + "\n");
            }
            br.close();
        }
        writer.close();
    }
}
