package edu.pitt.medschool.framework.util;

import org.apache.poi.ss.usermodel.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static edu.pitt.medschool.framework.util.Util.SOP;

/**
 * Utilities regarding time problems
 * Time is IMPORTANT in this project
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
     * Convert serial# time to a specific timestamp
     *
     * @param serial   String Serial number
     * @param timeZone Null for UTC timezone
     * @return Apache POI defined timestamp
     */
    public static long serialTimeToLongDate(String serial, TimeZone timeZone) {
        if (timeZone == null)
            timeZone = utcTimeZone;
        double sTime = Double.valueOf(serial);
        Date d = DateUtil.getJavaDate(sTime, timeZone);
        return d.getTime();
    }

    public static int timestampToAge(long birthDate) {
        LocalDate dob = Instant.ofEpochMilli(birthDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(dob, now).getYears();
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

    public static void main(String[] args) throws ParseException {
        SOP(secondToString(30)[0] + ":" + secondToString(30)[1]);
        SOP(secondToString(3600)[0] + ":" + secondToString(3600)[1]);
        SOP(secondToString(18000)[0] + ":" + secondToString(18000)[1]);

        SOP(dateToTimestamp("1/2/1934"));
        SOP(timestampToUTCDate(dateToTimestamp("1/1/1934")));
        SOP(dateTimeFormatToTimestamp("2017.10.28 15:00:17", "yyyy.MM.dd HH:mm:ss", null));
        SOP(timestampToUTCDateTimeFormat(dateTimeFormatToTimestamp("2017.10.28 15:00:17", "yyyy.MM.dd HH:mm:ss", null), "yyyy.MM.dd HH:mm:ss"));
        SOP(timestampToUTCDateTimeFormat(
                subOneHourToTimestamp(dateTimeFormatToTimestamp("2017.03.01 00:45:11", "yyyy.MM.dd HH:mm:ss", null)), "yyyy.MM.dd HH:mm:ss"));
        SOP(timestampToUTCDateTimeFormat(
                addOneHourToTimestamp(dateTimeFormatToTimestamp("2017.12.31 23:55:22", "yyyy.MM.dd HH:mm:ss", null)), "yyyy.MM.dd HH:mm:ss"));
        SOP(timestampToUTCDateTimeFormat(serialTimeToLongDate("43036.6402314815", null), "yyyy-MM-dd HH:mm:ss"));

        SOP(null);
        Calendar c = Calendar.getInstance(nycTimeZone);
        c.set(2011, Calendar.JANUARY, 1, 15, 0); // 3.13 and 11.6
        for (int i = 0; i < 365; i++) {
            if (isThisDayOnDstShift(nycTimeZone, c.getTime())) {
                SOP("DST on: " + c.getTime());
            }
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

    }
}
