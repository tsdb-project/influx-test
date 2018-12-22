package edu.pitt.medschool.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

// PUH-2017-058
// PUH-2016-259

public class LongestSeries {
    public static void main(String[] args) throws IOException {

        int k = 1000;

        File file = new File("/Users/Isolachine/Downloads/wide.csv");

        Reader reader = new FileReader(file);
        CSVReader csvReader = new CSVReader(reader);

        String[] headerOld = csvReader.readNext();
        String[] header = new String[k + 2];
        for (int i = 0; i < k + 2; i++) {
            header[i] = headerOld[i];
        }
        CSVWriter csvWriter = new CSVWriter(new FileWriter(new File("/Users/Isolachine/Downloads/wide_good_" + k + ".csv")));
        csvWriter.writeNext(header);

        List<Integer> list = new ArrayList<>();
        while (reader.ready()) {
            String[] row = csvReader.readNext();
            int longest = 0;
            int current = 0;
            int first = 2, last = 0;
            for (int i = 2; i < row.length; i++) {
                if (row[i].equals("N/A")) {
                    longest = Math.max(current, longest);
                    current = 0;
                    first = i + 1;
                } else {
                    last = i;
                    current++;
                }
            }
            longest = Math.max(current, longest);
            System.out.println(row[0] + ':' + longest + ',' + first + ',' + last);
            list.add(longest);

            if (longest >= k) {
                String[] newRow = new String[k + 2];
                int count = 2;
                for (int i = 2; i < row.length; i++) {
                    if (count == k + 2) {
                        break;
                    }
                    if (row[i].equals("N/A")) {
                        count = 2;
                    } else {
                        newRow[count++] = row[i];
                    }
                }
                newRow[0] = row[0];
                newRow[1] = row[1];
                csvWriter.writeNext(newRow);
            }

        }
        csvWriter.close();
        Collections.sort(list);

        System.out.println(list);

        csvReader.close();
    }
}
