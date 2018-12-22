package edu.pitt.medschool.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

public class BatchRename {
    public static void main(String[] args) throws IOException {
        File dir = new File("/Users/Isolachine/Downloads/final");
        // Collection<File> col = FileUtils.listFiles(dir, new String[] { "csv" }, true);
        // for (File file : col) {
        // System.out.println(file.getName());
        // }

        File[] files = dir.listFiles();
        Arrays.sort(files);
        int count = 1;
        for (File file : files) {
            System.out.println(file.getName() + ',' + count + ".csv");
            FileUtils.copyFile(file, new File("/Users/Isolachine/Downloads/final2/" + count + ".csv"));
            count++;
        }
    }
}
