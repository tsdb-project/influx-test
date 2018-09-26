package edu.pitt.medschool.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class ExportTransformation {
    public static void main(String[] args) throws IOException {

    }

    public void original() throws IOException {
        File file = new File("/Users/Isolachine/Downloads/out/long.csv");
        Reader reader = new FileReader(file);
        CSVReader csvReader = new CSVReader(reader);

        File newFile = new File("/Users/Isolachine/Downloads/out/transformed.csv");
        Writer writer = new FileWriter(newFile);
        CSVWriter csvWriter = new CSVWriter(writer);

        List<String> pids = new ArrayList<>();
        List<List<String>> matrix = new ArrayList<>();

        Iterator<String[]> iterator = csvReader.iterator();
        iterator.next();
        int i = 0;
        while (iterator.hasNext()) {
            String[] row = iterator.next();
            if (!pids.contains(row[0])) {
                pids.add(row[0]);
                matrix.add(new ArrayList<>());
            }
            matrix.get(pids.indexOf(row[0])).add(row[3]);
            i++;
            if (i % 200000 == 0) {
                System.out.println(i);
            }
        }

        System.out.println(i);
        System.out.println(pids.size());

        int maxSize = 0;
        for (List<String> list : matrix) {
            maxSize = Math.max(list.size(), maxSize);
        }

        csvWriter.writeNext(pids.toArray(new String[pids.size()]));

        for (int j = 0; j < maxSize; j++) {
            String[] newRow = new String[pids.size()];
            for (int k = 0; k < pids.size(); k++) {
                if (j >= matrix.get(k).size()) {
                    newRow[k] = "";
                } else {
                    newRow[k] = matrix.get(k).get(j);
                }
            }
            csvWriter.writeNext(newRow);
        }

        csvReader.close();
        csvWriter.close();
    }
}
