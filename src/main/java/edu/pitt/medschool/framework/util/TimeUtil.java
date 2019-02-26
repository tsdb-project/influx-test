package edu.pitt.medschool.framework.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Utilities regarding time problems Time is IMPORTANT in this project
 */
public class TimeUtil {

    public static TimeZone nycTimeZone = TimeZone.getTimeZone("America/New_York");
    public static TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    public static long oneHourMil = 3600000;

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
     * @param timeZone Null for UTC timezone
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
     * @param timeZone Null for UTC timezone
     * @return Instant
     * @throws ParseException Wrong format
     */
    public static Date dateTimeFormatToDate(String dateTime, String format, TimeZone timeZone) throws ParseException {
        if (timeZone == null)
            timeZone = utcTimeZone;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(timeZone);
        return sdf.parse(dateTime);
    }

    /**
     * Convert some string to a UNIX timestamp
     *
     * @param dateTime String
     * @param format   String format
     * @param timeZone Null for UTC timezone
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
     */
    public static String timestampToUTCDateTimeFormat(long unixTime, String format) {
        TimeZone timeZone = utcTimeZone;
        DateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(timeZone);
        return formatter.format(new Date(unixTime));
    }

    /**
     * Serial time to a Date object
     *
     * @param sTime Serial number
     * @param tz    Null for UTC timezone
     * @return Java Date Obj
     */
    public static Date serialTimeToDate(double sTime, TimeZone tz) {
        if (tz == null)
            tz = utcTimeZone;
        return DateUtil.getJavaDate(sTime, tz);
    }

    /**
     * Convert serial# time to a specific timestamp
     *
     * @param serial   Serial number
     * @param timeZone Null for UTC timezone
     * @return Apache POI defined timestamp
     */
    public static long serialTimeToLongDate(double serial, TimeZone timeZone) {
        return serialTimeToDate(serial, timeZone).getTime();
    }

    public static int timestampToAge(long birthDate) {
        LocalDate dob = Instant.ofEpochMilli(birthDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(dob, now).getYears();
    }

    public static String[] secondToString(int totalSeconds) {
        if (totalSeconds == 0) {
            return new String[] { "", "" };
        }
        if (totalSeconds % 86400 == 0) {
            return new String[] { String.valueOf((totalSeconds / 86400)), "86400" };
        }
        if (totalSeconds % 3600 == 0) {
            return new String[] { String.valueOf((totalSeconds / 3600)), "3600" };
        }
        if (totalSeconds % 60 == 0) {
            return new String[] { String.valueOf((totalSeconds / 60)), "60" };
        }
        return new String[] { String.valueOf(totalSeconds), "1" };
    }

    /**
     * Format the local date time (Tue, Sep 18 2018, 07:18:49 PM)
     *
     * @param ldt    Any LDT, null for now
     * @param format Formatter, empty for "E, MMM dd uuuu, hh:mm:ss a"
     */
    public static String formatLocalDateTime(LocalDateTime ldt, String format) {
        if (ldt == null)
            ldt = LocalDateTime.now();
        if (format.isEmpty())
            format = "E, MMM dd uuuu, hh:mm:ss a";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return ldt.format(formatter);
    }

    /**
     * Is a given date a DST shift date?
     */
    public static boolean isThisDayOnDstShift(TimeZone tz, Date now) {
        Calendar c = Calendar.getInstance(tz);
        c.setTime(now);
        c.set(Calendar.HOUR_OF_DAY, 12); // We want to ignore 2am-3am as we only care dates
        Date normalized_now = c.getTime();
        c.add(Calendar.DATE, -1);
        Date dayBefore = c.getTime();
        return tz.inDaylightTime(dayBefore) != tz.inDaylightTime(normalized_now);
    }

    public static long addOneHourToTimestamp(long ts) {
        return ts + oneHourMil;
    }

    public static long subOneHourToTimestamp(long ts) {
        return ts - oneHourMil;
    }

    public static boolean dateIsSameDay(Date a, Date b) {
        return DateUtils.isSameDay(a, b);
    }

    public static void main(String[] args) {
        System.out.println(serialTimeToDate(41499.5643171296, null));
    }

}
