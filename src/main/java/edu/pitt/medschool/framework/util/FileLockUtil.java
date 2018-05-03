/**
 * 
 */
package edu.pitt.medschool.framework.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is implemented to handle file locks and moves on import.
 * 
 * @author Isolachine
 */
public class FileLockUtil {
    private static File lockFile(File file) {
        StringBuffer sb = new StringBuffer(file.getParent());
        sb.append("/.");
        sb.append(file.getName().substring(0, file.getName().length() - 3));
        sb.append("lock");
        return new File(sb.toString());
    }

    private static File lockFile(String fileName) {
        File file = new File(fileName);
        return lockFile(file);
    }

    public static boolean isLocked(File file) {
        return lockFile(file).exists();
    }

    public static boolean isLocked(String file) {
        return lockFile(file).exists();
    }

    public static boolean aquire(File file) {
        try {
            FileWriter fw = new FileWriter(lockFile(file));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean aquire(String file) {
        return aquire(new File(file));
    }

    public static boolean release(File file) {
        return lockFile(file).delete();
    }

    public static boolean release(String file) {
        return lockFile(file).delete();
    }

    public static void main(String[] args) throws InterruptedException {
        File file = new File("/tsdb/1/PUH-2010-014_01_ar.csv");
        System.out.println(isLocked(file));
        System.out.println(aquire(file));
        Thread.sleep(3000L);
        System.out.println(release(file));
    }
}
