/**
 *
 */
package edu.pitt.medschool.framework.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Isolachine
 */
public class Util {

    public static String getIpFromHostname(String host) {
        String addr = "localhost";
        try {
            addr = InetAddress.getByName("upmc_influx_1.dreamprc.com").getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return addr;
    }

    /**
     * Get all CSV files under a directory
     *
     * @param dir String directory path
     * @return String Full file path
     */
    public static String[] getAllCsvFileInDirectory(String dir) {
        return getAllSpecificFileInDirectory(dir, "csv");
    }

    /**
     * Get all specific files under a directory
     *
     * @param dir  String directory path
     * @param type String file extension
     * @return String Full file path
     */
    public static String[] getAllSpecificFileInDirectory(String dir, String type) {
        File folder = new File(dir);
        if (folder.isFile()) {
            if (dir.toLowerCase().endsWith("." + type))
                return new String[]{dir};
            else
                return new String[0];
        }

        FilenameFilter extensionFileFilter = (dirs, name) -> {
            // Filter hidden or not wanted file
            return !name.startsWith(".") && name.toLowerCase().endsWith("." + type);
        };
        File[] files = folder.listFiles(extensionFileFilter);

        assert files != null;
        if (files.length == 0)
            return new String[0];

        LinkedList<String> file_list = new LinkedList<>();
        for (File file : files) {
            if (file.isFile())
                file_list.add(file.getAbsolutePath());
        }

        return file_list.toArray(new String[0]);
    }

    public static List<FileBean> filesInFolder(String directory) {
        File folder = new File(directory);

        if (!directory.endsWith("/")) {
            directory += "/";
        }

        File[] listOfFiles = folder.listFiles();
        List<FileBean> fileBeans = new ArrayList<>();

        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()
                        && FilenameUtils.getExtension(listOfFile.getName()).toLowerCase().equals("csv")
                        && !listOfFile.getName().startsWith(".")) {
                    FileBean fileBean = new FileBean();
                    fileBean.setName(listOfFile.getName());
                    fileBean.setDirectory(directory);
                    long length = FileUtils.sizeOf(listOfFile);
                    fileBean.setBytes(length);
                    fileBean.setSize(FileUtils.byteCountToDisplaySize(length));
                    fileBeans.add(fileBean);
                }
            }
        }
        return fileBeans;
    }

    /**
     * Generate info inside a exception
     *
     * @return String
     */
    public static String stackTraceErrorToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


    public static void SOP(Object s) {
        if (s == null) {
            System.out.println();
            return;
        }
        System.out.println(s);
    }

    public static void main(String[] args) {

    }
}
