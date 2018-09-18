package edu.pitt.medschool.framework;

import edu.pitt.medschool.framework.util.TimeUtil;
import org.junit.Test;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import static edu.pitt.medschool.framework.util.TimeUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class appTimeUtilTest {

    private String format = "yyyy.MM.dd HH:mm:ss";

    @Test
    public void testTimezone() {
        assertEquals(Date.from(Instant.parse("2018-09-02T09:18:11.563847612Z")).toString(),
                "Sun Sep 02 05:18:11 EDT 2018");
    }

    @Test
    public void test1() throws Exception {
        assertEquals("30:1", secondToString(30)[0] + ":" + secondToString(30)[1]);
        assertEquals("1:3600", secondToString(3600)[0] + ":" + secondToString(3600)[1]);
        assertEquals("5:3600", secondToString(18000)[0] + ":" + secondToString(18000)[1]);

        assertEquals(String.valueOf(dateToTimestamp("1/2/1934")), "-1135987140000");
        assertEquals(timestampToUTCDate(dateToTimestamp("1/1/1934")), "01/01/1934");
    }

    @Test
    public void testConvert() throws Exception {
        assertEquals("1509202817000", String.valueOf(dateTimeFormatToTimestamp("2017.10.28 15:00:17", "yyyy.MM.dd HH:mm:ss", null)));
        assertEquals("2017.10.28 15:00:17", timestampToUTCDateTimeFormat(dateTimeFormatToTimestamp("2017.10.28 15:00:17", format, null), format));
        assertEquals("2017.02.28 23:45:11", timestampToUTCDateTimeFormat(subOneHourToTimestamp(dateTimeFormatToTimestamp("2017.03.01 00:45:11", format, null)), format));
        assertEquals("2018.01.01 00:55:22", timestampToUTCDateTimeFormat(addOneHourToTimestamp(dateTimeFormatToTimestamp("2017.12.31 23:55:22", format, null)), format));
        assertEquals("2017-10-28 15:21:56", timestampToUTCDateTimeFormat(serialTimeToLongDate(43036.6402314815, null), "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testDST() throws Exception {
        Date testStartDate = TimeUtil.dateTimeFormatToDate("2011.03.1323:22:33", "yyyy.MM.ddHH:mm:ss", TimeUtil.nycTimeZone);
        assertTrue(TimeUtil.isThisDayOnDstShift(TimeUtil.nycTimeZone, testStartDate));

        Calendar c = Calendar.getInstance(nycTimeZone);
        // 3.13 and 11.6
        c.set(2011, Calendar.JANUARY, 1, 15, 0);
        for (int i = 0; i < 365; i++) {
            if (isThisDayOnDstShift(nycTimeZone, c.getTime())) {
                int d = c.get(Calendar.DAY_OF_MONTH);
                int m = c.get(Calendar.MONTH);
                assertTrue(d == 13 || d == 6);
                assertTrue(m == 2 || m == 10);
            }
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

}
