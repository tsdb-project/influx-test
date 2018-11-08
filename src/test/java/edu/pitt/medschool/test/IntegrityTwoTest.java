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

    final static String[] DIRS = new String[] { "/Volumes/CSV Exports/Corrected files 07.10.18",
            "/Volumes/CSV Exports/Exported files with artifact reduction/Subset-04262018-04282018",
            "/Volumes/CSV Exports/Exported files with artifact reduction/Subset-AR-upload-04292018",
            "/Volumes/CSV Exports/Exported files with artifact reduction/Subset-upload-334-a02",
            "/Volumes/CSV Exports/Exported files with artifact reduction/Subset-upload-515-a01",
            "/Volumes/CSV Exports/Exported Files without artifact reduction/Subset-Initial-04192018",
            "/Volumes/CSV Exports/Exported Files without artifact reduction/Subset-noar-b00",
            "/Volumes/CSV Exports/Exported Files without artifact reduction/Subset-Upload-437-b02",
            "/Volumes/CSV Exports/Exported Files without artifact reduction/Subset-Upload-598-b01",
            "/Volumes/CSV Export2/Exorted files without artifact reduction/Subset1-noar-272-c03",
            "/Volumes/CSV Export2/Exorted files without artifact reduction/Subset1-noar-342-c04",
            "/Volumes/CSV Export2/Exorted files without artifact reduction/Subset1-noar-381-c02/sub1",
            "/Volumes/CSV Export2/Exorted files without artifact reduction/Subset1-noar-381-c02/sub2",
            "/Volumes/CSV Export2/Exorted files without artifact reduction/Subset1-noar-435-c01",
            "/Volumes/CSV Export2/Exported files with artifact reduction/Subset1-ar-275-d04",
            "/Volumes/CSV Export2/Exported files with artifact reduction/Subset1-ar-357-d03",
            "/Volumes/CSV Export2/Exported files with artifact reduction/Subset1-ar-401-d01",
            "/Volumes/CSV Export2/Exported files with artifact reduction/Subset1-ar-413-d02",
            "/Volumes/CSV Export3/Exported files WITH artifact reduction/Subset-ar-270-e02/sub0",
            "/Volumes/CSV Export3/Exported files WITH artifact reduction/Subset-ar-270-e02/sub1",
            "/Volumes/CSV Export3/Exported files WITH artifact reduction/Subset-ar-270-e02/sub2",
            "/Volumes/CSV Export3/Exported files WITH artifact reduction/Subset-ar-296-e01/sub1",
            "/Volumes/CSV Export3/Exported files WITH artifact reduction/Subset-ar-296-e01/sub2",
            "/Volumes/CSV Export3/Exported files WITHOUT artifact reduction/Subset-noar-265-f02/sub0",
            "/Volumes/CSV Export3/Exported files WITHOUT artifact reduction/Subset-noar-265-f02/sub1",
            "/Volumes/CSV Export3/Exported files WITHOUT artifact reduction/Subset-noar-265-f02/sub2",
            "/Volumes/CSV Export3/Exported files WITHOUT artifact reduction/Subset-noar-298-f01" };

    public static void main(String[] args) throws IOException, ParseException {
        FileWriter writer = new FileWriter(new File("/tsdb/all.csv"));
        writer.write("dir,filename,header,row,diff\n");
        for (String dir : DIRS) {
            File[] files = new File(dir).listFiles();

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

                Date header = TimeUtil.dateTimeFormatToDate(date[1] + ' ' + time[1], "yyyy.MM.dd HH:mm:ss",
                        TimeUtil.nycTimeZone);
                Date row = TimeUtil.serialTimeToDate(Double.valueOf(firstRow[0]), TimeUtil.utcTimeZone);

                long diff = (row.getTime() - header.getTime()) / 1000;

                String headerTime = formatter.format(header);

                String rowTime = formatter.format(row);

                String rowWrite = dir + ',' + file.getName() + ',' + headerTime + ',' + rowTime + ',' + diff + "\n";
                writer.write(rowWrite);

                csvReader.close();
            }
        }

        writer.close();
    }

}
