/**
 * 
 */
package app.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Isolachine
 *
 */
public class Util {
    public static long dateToTimestamp(String dateOfBirth) throws ParseException {
        return dateTimeFormatToTimestamp(dateOfBirth, "mm/dd/yyyy");
    }
    
    public static long dateTimeFormatToTimestamp(String dateTime, String format) throws ParseException {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(timeZone);
        long unixTime = formatter.parse(dateTime).getTime();
        return unixTime;
    }
    
    public static String timestampToUTCDate(long unixTime) {
        return timestampToUTCDateTimeFormat(unixTime, "mm/dd/yyyy");
    }
    
    public static String timestampToUTCDateTimeFormat(long unixTime, String format) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(timeZone);
        Date date = new Date(unixTime);
        String dateString = formatter.format(date);
        return dateString;
    }
    
    public static void main(String[] args) throws ParseException {
        System.out.println(dateToTimestamp("1/2/1934"));
        System.out.println(timestampToUTCDate(dateToTimestamp("1/1/1934")));
        System.out.println(dateTimeFormatToTimestamp("2017.10.28 15:00:17", "yyyy.MM.dd HH:mm:ss"));
        System.out.println(timestampToUTCDateTimeFormat(dateTimeFormatToTimestamp("2017.10.28 15:00:17", "yyyy.MM.dd HH:mm:ss"), "yyyy.MM.dd HH:mm:ss"));
    }
}
