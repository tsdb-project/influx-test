/**
 *
 */
package edu.pitt.medschool.framework.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.DateUtil;

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
     * Convert a DoB String (e.g. 04/27/1995) to UNIX timestamp
     *
     * @param dateOfBirth DOB String
     * @return long UNIX Timestamp
     * @throws ParseException Shouldn't happen
     */
    public static long dateToTimestamp(String dateOfBirth) throws ParseException {
        return dateTimeFormatToTimestamp(dateOfBirth, "mm/dd/yyyy", null);
    }

    /**
     * Convert timestamp to instant
     *
     * @param dateTime String
     * @param format   String format
     * @param timeZone Null for NY(PGH) timezone
     * @return Instant
     * @throws ParseException Wrong format
     */
    public static Instant dateTimeFormatToInstant(String dateTime, String format, TimeZone timeZone) throws ParseException {
        return dateTimeFormatToDate(dateTime, format, timeZone).toInstant();
    }

    /**
     * Convert timestamp to date (for DB)
     *
     * @param dateTime String
     * @param format   String format
     * @param timeZone Null for NY(PGH) timezone
     * @return Instant
     * @throws ParseException Wrong format
     */
    public static Date dateTimeFormatToDate(String dateTime, String format, TimeZone timeZone) throws ParseException {
        if (timeZone == null)
            timeZone = TimeZone.getTimeZone("America/New_York");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(timeZone);
        return sdf.parse(dateTime);
    }

    /**
     * Convert some string to a UNIX timestamp
     *
     * @param dateTime String
     * @param format   String format
     * @param timeZone Null for NY(PGH) timezone
     * @return long UNIX Timestamp
     * @throws ParseException Wrong format
     */
    public static long dateTimeFormatToTimestamp(String dateTime, String format, TimeZone timeZone) throws ParseException {
        return dateTimeFormatToInstant(dateTime, format, timeZone).toEpochMilli();
    }

    /**
     * Convert UNIX Timestamp to UTC Time string (US format)
     *
     * @param unixTime UNIX Timestamp
     * @return String Formatted DateTime
     */
    public static String timestampToUTCDate(long unixTime) {
        return timestampToUTCDateTimeFormat(unixTime, "mm/dd/yyyy");
    }

    /**
     * Convert UNIX Timestamp to UTC Time (Any format)
     *
     * @param unixTime UNIX Timestamp
     * @param format   String UTC
     * @return
     */
    public static String timestampToUTCDateTimeFormat(long unixTime, String format) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(timeZone);
        return formatter.format(new Date(unixTime));
    }

    /**
     * Convert serial# time to a specific timestamp
     *
     * @param serial   String Serial number
     * @param timeZone Null for NY(PGH) timezone
     * @return Apache POI defined timestamp
     */
    public static long serialTimeToLongDate(String serial, TimeZone timeZone) {
        if (timeZone == null)
            timeZone = TimeZone.getTimeZone("UTC");
        double sTime = Double.valueOf(serial);
        Date d = DateUtil.getJavaDate(sTime, timeZone);
        return d.getTime();
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

        FilenameFilter txtFileFilter = (dirs, name) -> {
            // Filter hidden or not wanted file
            if (!name.startsWith(".") && name.toLowerCase().endsWith("." + type))
                return true;
            return false;
        };
        File[] files = folder.listFiles(txtFileFilter);

        assert files != null;
        if (files.length == 0)
            return new String[0];

        LinkedList<String> file_list = new LinkedList<>();
        for (File file : files) {
            if (file.isFile())
                file_list.add(file.getAbsolutePath());
        }

        return file_list.toArray(new String[file_list.size()]);
    }

    public static int timestampToAge(long birthDate) {
        LocalDate dob = Instant.ofEpochMilli(birthDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(dob, now).getYears();
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
                if (listOfFile.isFile() && FilenameUtils.getExtension(listOfFile.getName()).toLowerCase().equals("csv")) {
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
        StringBuilder sb = new StringBuilder("Error message: ");
        sb.append(e.getMessage());
        sb.append(".\n Stack trace:\n");
        StackTraceElement[] ste = e.getStackTrace();
        for (StackTraceElement aste : ste) {
            sb.append("  Source file: '");
            sb.append(aste.getFileName());
            sb.append("', class name: '");
            sb.append(aste.getClassName());
            sb.append("'. On method '");
            sb.append(aste.getMethodName());
            sb.append("' line: ");
            sb.append(aste.getLineNumber());
            sb.append(".\n");
        }
        return sb.toString();
    }

    public static String[] secondToString(int totalSeconds) {
        if (totalSeconds == 0) {
            return new String[]{"", ""};
        }
        if (totalSeconds % 86400 == 0) {
            return new String[]{String.valueOf((totalSeconds / 86400)), "86400"};
        }
        if (totalSeconds % 3600 == 0) {
            return new String[]{String.valueOf((totalSeconds / 3600)), "3600"};
        }
        if (totalSeconds % 60 == 0) {
            return new String[]{String.valueOf((totalSeconds / 60)), "60"};
        }
        return new String[]{String.valueOf(totalSeconds), "1"};
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(secondToString(30)[0] + ":" + secondToString(30)[1]);
        System.out.println(secondToString(3600)[0] + ":" + secondToString(3600)[1]);
        System.out.println(secondToString(18000)[0] + ":" + secondToString(18000)[1]);
        // System.out.println(FileUtils.sizeOf(new File("/tsdb/testing3")));
        // System.out.println(filesInFolder("/Users/Isolachine/tsdb/testing2"));
        //
        // System.out.println(dateToTimestamp("1/2/1934"));
        // System.out.println(timestampToUTCDate(dateToTimestamp("1/1/1934")));
        // System.out.println(dateTimeFormatToTimestamp("2017.10.28 15:00:17", "yyyy.MM.dd HH:mm:ss"));
        // System.out.println(timestampToUTCDateTimeFormat(dateTimeFormatToTimestamp("2017.10.28 15:00:17", "yyyy.MM.dd HH:mm:ss"), "yyyy.MM.dd HH:mm:ss"));
        // System.out.println(timestampToUTCDateTimeFormat(serialTimeToLongDate("43036.6402314815"), "yyyy-MM-dd HH:mm:ss"));
        //
        // String[] testF = getAllSpecificFileInDirectory("E:\\Grad@Pitt\\TS ProjectData", "csv");
        // for (String a : testF) {
        // System.out.println(a);
        // }
    }
}
