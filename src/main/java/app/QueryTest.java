/**
 *
 */
package app;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

/**
 * @author Isolachine
 */
public class QueryTest {

    private static void SOP(Object s) {
        if (s == null) {
            System.out.println();
        } else {
            System.out.println(s);
        }
    }

    /**
     * Print out InfluxDB query result, if has error then print error
     *
     * @param result QueryResult instance
     */
    static void printResult(QueryResult result) {
        if (result.hasError()) {
            SOP("Error with system: " + result.getError());
        } else {
            List<QueryResult.Result> listResult = result.getResults();
            if (listResult.get(0).hasError()) {
                SOP("Error with query: " + listResult.get(0).getError());
            } else {
                for (List<Object> res : listResult.get(0).getSeries().get(0).getValues()) {
                    SOP(res);
                }
            }
        }
        SOP(null);
    }

    static String getUserInputColumns(List<String> colNames) {
        Scanner sc = new Scanner(System.in);
        String tmpCols;
        do {
            tmpCols = sc.nextLine().toUpperCase();
            if (!colNames.contains(tmpCols)) {
                tmpCols = null;
                SOP("User inputed column name not exist, please reinput!");
            }
        } while (tmpCols == null);
        return tmpCols;
    }

    static double getUserInputValue() {
        Scanner sc = new Scanner(System.in);
        double val = 0;
        try {
            val = Double.parseDouble(sc.nextLine());
        } catch (Exception e) {
            // TODO: Do something in the future!
            val = Double.parseDouble(sc.nextLine());
        }
        return val;
    }

    static void queryRunner(InfluxDB idb, String queryString) {
        Query q = new Query(queryString, InfluxappConfig.IFX_DBNAME);
        QueryResult result = idb.query(q);
        printResult(result);
    }

    /**
     * User interact with query A
     */
    static void routineA(InfluxDB idb, String tableName, List<String> colNames) {
        SOP("Query A testing, case-s ...");
        SOP("Find all patients where values in column X exceed value Y in at least Z consecutive records in the first 8 hours of available data.");

        // TODO: First 8 hour of aval data
        String template = "SELECT * FROM (SELECT COUNT(%s) AS cocount FROM %s WHERE %s > %f GROUP BY TIME(%ds)) WHERE cocount = %d";

        SOP("Input a column X (eg. I10_1): ");
        String col = getUserInputColumns(colNames);

        // TODO: Make sure it correct (threshold)
        SOP("Input a threshold value Y (eg. 8): ");
        double thrVal = getUserInputValue();

        SOP("Input consecutive threshold Z (eg. 10): ");
        int thrSec = (int) getUserInputValue();

        SOP(null);
        SOP("Your matches are:");
        String finalD = String.format(template, col, tableName, col, thrVal, thrSec, thrSec);
        queryRunner(idb, finalD);

        SOP(null);
    }

    /**
     * User interact with query B
     */
    static void routineB(InfluxDB idb, String tableName, List<String> colNames) {
        SOP("Query B testing, case-s ...");
        SOP("Find all patients where the hourly mean values in column X and column Y differ by at least Z% for at least Q hourly epochs.");

        String template = "SELECT * FROM (SELECT COUNT(diff) AS c FROM (" + "SELECT * FROM (SELECT (MEAN(%s) - MEAN(%s)) / MEAN(%s) AS diff FROM %s GROUP BY TIME(1h)) " + "WHERE diff > %f OR diff < - %f) GROUP BY TIME(%dh)) WHERE c = %d";

        SOP("Input a column X (eg. I10_1): ");
        String colA = getUserInputColumns(colNames);

        SOP("Input a column Y (eg. I11_1): ");
        String colB = getUserInputColumns(colNames);

        SOP("Input value difference tolerance Z, in % form (eg. 3): ");
        double valDiff = getUserInputValue() / 100;

        SOP("Input hourly epochs Q (eg. 5): ");
        int hEp = (int) getUserInputValue();

        SOP(null);
        SOP("Your matches are:");
        String finalD = String.format(template, colA, colB, colA, tableName, valDiff, valDiff, hEp, hEp);
        queryRunner(idb, finalD);

        SOP(null);
    }

    /**
     *
     */
    static void exportColumnSelect() {

    }

    static List<String> getColNames(InfluxDB idb, String pid) {
        Query q = new Query("SHOW FIELD KEYS FROM \"" + pid + "\"", InfluxappConfig.IFX_DBNAME);
        QueryResult qR = idb.query(q);
        List<List<Object>> qRR = qR.getResults().get(0).getSeries().get(0).getValues();
        List<String> colN = new ArrayList<>(qRR.size());

        for (List<Object> qRRO : qRR) {
            colN.add((String) qRRO.get(0));
        }

        return colN;
    }

    static void insertColumns() {
        String[] names = {"Artifact Intensity", "Seizure Detections", "Rhythmicity Spectrogram, Left Hemisphere", "Rhythmicity Spectrogram, Right Hemisphere", "FFT Spectrogram, Left Hemisphere", "FFT Spectrogram, Right Hemisphere", "Asymmetry, Relative Spectrogram, Asym Hemi", "Asymmetry, Absolute Index (EASI), 1 - 18 Hz, Asym Hemi", "Asymmetry, Relative Index (REASI)01, 1 - 18 Hz, Asym Hemi", "aEEG, Left Hemisphere", "aEEG, Right Hemisphere", "Suppression Ratio, Left Hemisphere",
                "Suppression Ratio, Right Hemisphere", "Time_Column"};
        int[] columnsNumbers = {4, 1, 97, 97, 40, 40, 34, 1, 1, 5, 5, 1, 1, 1};
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
        BatchPoints records = BatchPoints.database(InfluxappConfig.IFX_DBNAME).consistency(ConsistencyLevel.ALL).build();

        for (int i = 1; i <= names.length; i++) {
            for (int j = 1; j <= columnsNumbers[i - 1]; j++) {
                Point record = Point.measurement("columns").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).addField("name", names[i - 1]).tag("column", "I" + i + "_" + j).build();
                records.point(record);
            }
            influxDB.write(records);
            records = BatchPoints.database(InfluxappConfig.IFX_DBNAME).consistency(ConsistencyLevel.ALL).build();
        }
    }

    public static void main(String[] args) {

        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

        String tableName = "data_PUH-2010-080_noar";
        List<String> colNames = getColNames(influxDB, tableName);


        queryRunner(influxDB, "SELECT * FROM (SELECT COUNT(I10_1) AS cocount FROM \"data_PUH-2010-080_noar\" WHERE I10_1 > 80 GROUP BY TIME(10s)) WHERE cocount = 10");
        queryRunner(influxDB,
                "SELECT * FROM (SELECT COUNT(diff) AS c FROM ("
                        + "SELECT * FROM (SELECT (MEAN(I10_1) - MEAN(I11_1)) / MEAN(I10_1) AS diff FROM \"data_PUH-2010-080_noar\" GROUP BY TIME(1h)) "
                        + "WHERE diff > 0.03 OR diff < - 0.03) GROUP BY TIME(5h)) WHERE c = 5");


        //routineA(influxDB, tableName, colNames);
        //routineB(influxDB, tableName, colNames);

    }
}
