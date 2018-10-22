/**
 * 
 */
package edu.pitt.medschool.test;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Isolachine
 *
 */
public class StringAppender {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        Instant instant = Instant.parse("2011-05-03T11:58:01Z");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String newTime = formatter.format(Date.from(Instant.ofEpochSecond(instant.getEpochSecond() + 18000)));
        
        System.out.println(formatter.format(Date.from(Instant.ofEpochSecond(instant.getEpochSecond() + 18000))));
        // new StringAppender().akoglu();

        // System.out.println(query());
        // System.out.println();
        //
        // List<String> list = new ArrayList<>();
        // list = new ArrayList<>();
        // for (int i = 64; i <= 81; i++) {
        // list.add("\"I" + i + "_3" + "\"");
        // }
        // String line = String.join(" + ", list);
        // line = "(" + line + ") / " + list.size();
        // System.out.println(line);
        //
        // StringBuffer sb = new StringBuffer("\"ID\",");
        // String[] cols = new String[] { "SR", "aEEG", "SZProb" };
        // for (String col : cols) {
        // for (int i = 1; i <= 48; i++) {
        // sb.append('"').append(col).append(i).append('"').append(',');
        // }
        // }
        // System.out.println(sb.toString());
    }

    public static String query() {
        String template = "select median(avg) as MEDIAN, count(avg) as COUNT from (select "
                + "(\"I64_3\" + \"I65_3\" + \"I66_3\" + \"I67_3\" + \"I68_3\" + \"I69_3\" + \"I70_3\""
                + " + \"I71_3\" + \"I72_3\" + \"I73_3\" + \"I74_3\" + \"I75_3\" + \"I76_3\" + \"I77_3\""
                + " + \"I78_3\" + \"I79_3\" + \"I80_3\" + \"I81_3\") / 18 as avg from \"%s\" "
                + "where arType = 'ar' LIMIT 172800) where time >= '%s' and time < '%s' + 48h and avg > 2 group by time(1h, %ss)";
        return template;
    }

    public String akoglu() {

        String qTemplate = "SELECT %s FROM \"%s\" WHERE time > '%s' and time < '%s' GROUP BY (10s, %s)";
        String coloumns;

        Map<String, Object> map = jdbcTemplate
                .queryForMap("SELECT f.SID, f.SID_Count FROM feature f WHERE f.electrode NOT LIKE '%Av17%'");
        System.out.println(map);

        return "";
    }

}
