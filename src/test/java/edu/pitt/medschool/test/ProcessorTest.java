/**
 * 
 */
package edu.pitt.medschool.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * @author Isolachine
 *
 */
public class ProcessorTest {
    public static void main(String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println(cores);

        String uuid = UUID.randomUUID().toString();

        System.out.println(uuid);

        try {
            System.out.println(Files.size(Paths.get("/tsdb/1/PUH-2010-014_01_ar.csv")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("=====");
            Files.move(Paths.get("/tsdb/1/PUH-2010-014_01_ar.csv"), Paths.get("/tsdb/finished/PUH-2010-014_01_ar.csv"), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            e.printStackTrace();
            File file = new File("/tsdb/1");
            file.setWritable(true);
            System.out.println("-------");
            try {
                Files.move(Paths.get("/tsdb/1/PUH-2010-014_01_ar.csv"), Paths.get("/tsdb/finished/PUH-2010-014_01_ar.csv"), StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        File file = new File("/tsdb/1/PUH-2010-014_01_ar.csv");
        Path p = Paths.get("/tsdb/1/.PUH-2010-014_01_ar.lock");
        System.out.println(file.getParentFile());
        try {
            FileWriter fw = new FileWriter(p.toFile());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
