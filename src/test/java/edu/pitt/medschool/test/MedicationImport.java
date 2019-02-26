package edu.pitt.medschool.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MedicationImport {
    static void write() throws IOException {
        File file = new File("/Users/Isolachine/Archives/brain_flux/combined meds.csv");
        Reader reader = new FileReader(file);
        BufferedReader bf = new BufferedReader(reader);

        File newFile = new File("/Users/Isolachine/Downloads/meds.csv");
        Writer writer = new FileWriter(newFile);
        BufferedWriter bw = new BufferedWriter(writer);
        while (bf.ready()) {
            String line = '"' + bf.readLine().replace("\t", "\",\"") + '"';
            bw.write(line + "\n");
        }
        bf.close();
        bw.close();
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/Isolachine/Archives/brain_flux/combined meds.csv");
        Reader reader = new FileReader(file);
        BufferedReader bf = new BufferedReader(reader);

        File newFile = new File("/Users/Isolachine/Downloads/meds1.csv");
        Writer writer = new FileWriter(newFile);
        BufferedWriter bw = new BufferedWriter(writer);

        DateTimeFormatter f = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
        int i = 0;
        while (bf.ready()) {
            String[] line = bf.readLine().split("\t");
            if (i > 0) {
                line[2] = LocalDateTime.from(f.parse(line[2])).toString();
            }
            bw.write('"' + String.join("\",\"", line) + '"' + "\n");
            i++;
            if (i % 100000 == 0) {
                System.out.println(i);
            }
        }
        bf.close();
        bw.close();

    }
}
