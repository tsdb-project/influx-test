package edu.pitt.medschool.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.opencsv.CSVReader;

import edu.pitt.medschool.framework.util.TimeUtil;

public class IntegrityTwoTest {

    final static String DIR = "/tsdb/ar/";

    public static void main(String[] args) throws IOException, ParseException {
        File[] files = new File(DIR).listFiles();

        FileWriter writer = new FileWriter(new File("/tsdb/aaa.csv"));

        for (File file : files) {
            System.out.println(file.getName());
            FileReader reader = new FileReader(file);
            CSVReader csvReader = new CSVReader(reader);

            csvReader.readNext();
            csvReader.readNext();
            csvReader.readNext();
            csvReader.readNext();

            String[] date = csvReader.readNext();
            String[] time = csvReader.readNext();

            csvReader.readNext();
            csvReader.readNext();
            String[] firstRow = csvReader.readNext();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            formatter.setTimeZone(TimeUtil.nycTimeZone);

            Date header = TimeUtil.dateTimeFormatToDate(date[1] + ' ' + time[1], "yyyy.MM.dd HH:mm:ss", TimeUtil.nycTimeZone);
            Date row = TimeUtil.serialTimeToDate(Double.valueOf(firstRow[0]), TimeUtil.utcTimeZone);

            long diff = (row.getTime() - header.getTime()) / 1000;

            String headerTime = formatter.format(header);

            String rowTime = formatter.format(row);

            String rowWrite = file.getName() + ',' + headerTime + ',' + rowTime + ',' + diff + "\n";
            writer.write(rowWrite);

            csvReader.close();
        }
        writer.close();
    }

}
