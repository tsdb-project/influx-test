package edu.pitt.medschool.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class UUIDTest {

    public static void main(String[] args) throws IOException {
        File dir = new File("/tsdb/uab");
        File[] files = dir.listFiles();
        for (File file : files) {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();

            System.out.println(file.getName());

            boolean validUUId = line.substring(line.length() - 40, line.length() - 4)
                    .matches("([\\w\\d]){8}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){12}");
            System.out.println(validUUId);

            br.close();
        }
    }
}
