package edu.pitt.medschool.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class ExportTransformation {
    public static final Integer MAX_LINE = 200000;
    // 3 is the first aggr. group in a query, etc.
    public static final Integer COLUMN_NUMBER = 3;
    public static final String DIR = "/tsdb/post-processing/out/";

    public static Map<String, Integer> patientRowMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        readMap();
        int longestPatient = 0;
        for (String key : patientRowMap.keySet()) {
            longestPatient = Math.max(patientRowMap.get(key), longestPatient);
        }

        longestPatient = Math.min(longestPatient, MAX_LINE);

        File newFile = new File(DIR + "transformed.csv");
        Writer writer = new FileWriter(newFile);
        CSVWriter csvWriter = new CSVWriter(writer);
        csvWriter.writeNext(new String[] { "ID" });
        for (int i = 0; i < longestPatient; i++) {
            csvWriter.writeNext(new String[] { Integer.toString(i) });
        }
        csvWriter.close();

        File copyFile = new File(DIR + "transformed_copy.csv");
        FileUtils.copyFile(newFile, copyFile);

        File file = new File(DIR + "long.csv");
        Reader reader = new FileReader(file);
        CSVReader csvReader = new CSVReader(reader);

        Map<String, List<String>> patientDataMap = new HashMap<>();

        Iterator<String[]> iterator = csvReader.iterator();
        iterator.next();

        int i = 0;

        List<String> patientBatch = new ArrayList<>();

        while (iterator.hasNext()) {
            String[] row = iterator.next();
            String id = row[0];

            if (patientDataMap.get(id) == null) {
                patientDataMap.put(id, new ArrayList<>());
            }
            List<String> patientDataList = patientDataMap.get(id);
            patientDataList.add(row[COLUMN_NUMBER]);

            if (patientDataList.size() == patientRowMap.get(id)) {
                patientBatch.add(id);
            }

            if (patientBatch.size() >= 50 || !iterator.hasNext()) {

                CSVReader copyReader = new CSVReader(new FileReader(copyFile));

                Iterator<String[]> copyIterator = copyReader.iterator();

                String[] ids = copyIterator.next();
                List<String> idList = new ArrayList<>(Arrays.asList(ids));
                idList.addAll(patientBatch);

                int lineSize = idList.size();

                ids = idList.toArray(new String[lineSize]);
                Writer lineWriter = new FileWriter(newFile);
                CSVWriter csvLineWriter = new CSVWriter(lineWriter);
                csvLineWriter.writeNext(ids);

                int index = 0;
                while (copyIterator.hasNext()) {
                    String[] newLine = copyIterator.next();

                    List<String> lineList = new ArrayList<>(Arrays.asList(newLine));

                    for (String batchId : patientBatch) {
                        if (index >= patientDataMap.get(batchId).size()) {
                            lineList.add("");
                        } else {
                            lineList.add(patientDataMap.get(batchId).get(index));
                        }
                    }

                    newLine = lineList.toArray(new String[lineSize]);

                    csvLineWriter.writeNext(newLine);
                    index++;

                }

                for (String batchId : patientBatch) {
                    patientDataMap.remove(batchId);
                }
                patientBatch = new ArrayList<>();

                copyReader.close();
                csvLineWriter.close();
                FileUtils.forceDelete(copyFile);
                FileUtils.copyFile(newFile, copyFile);
            }

            i++;
            if (i % 10000 == 0) {
                System.out.println(NumberFormat.getNumberInstance(Locale.US).format(i));
            }

        }
        csvReader.close();
        FileUtils.forceDelete(copyFile);
    }

    public static void readMap() throws IOException {
        File file = new File(DIR + "long.csv");
        Reader reader = new FileReader(file);
        CSVReader csvReader = new CSVReader(reader);

        Iterator<String[]> iterator = csvReader.iterator();
        iterator.next();
        int i = 0;
        while (iterator.hasNext()) {
            String[] row = iterator.next();
            String id = row[0];
            patientRowMap.put(id, patientRowMap.getOrDefault(id, 0) + 1);
            i++;
            if (i % 1000000 == 0) {
                System.out.println(NumberFormat.getNumberInstance(Locale.US).format(i));
            }
        }

        System.out.println(NumberFormat.getNumberInstance(Locale.US).format(i));
        csvReader.close();
    }

    public static void testRead() throws IOException {
        File file = new File(DIR + "long.csv");
        Reader reader = new FileReader(file);
        CSVReader csvReader = new CSVReader(reader);

        File newFile = new File(DIR + "transformed.csv");
        Writer writer = new FileWriter(newFile);
        CSVWriter csvWriter = new CSVWriter(writer);

        Iterator<String[]> iterator = csvReader.iterator();
        iterator.next();
        int i = 0;
        while (iterator.hasNext()) {
            String[] row = iterator.next();
            String id = row[0];
            if (id.equals("PUH-2014-055")) {
                csvWriter.writeNext(row);
            }
            i++;
            if (i % 1000000 == 0) {
                System.out.println(NumberFormat.getNumberInstance(Locale.US).format(i));
            }
        }

        System.out.println(NumberFormat.getNumberInstance(Locale.US).format(i));
        csvReader.close();
        csvWriter.close();
    }

    public void original() throws IOException {
        File file = new File(DIR + "long.csv");
        Reader reader = new FileReader(file);
        CSVReader csvReader = new CSVReader(reader);

        File newFile = new File(DIR + "transformed.csv");
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
