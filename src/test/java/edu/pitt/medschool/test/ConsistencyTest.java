/**
 * 
 */
package edu.pitt.medschool.test;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import edu.pitt.medschool.config.InfluxappConfig;
import okhttp3.OkHttpClient;

/**
 * @author Isolachine
 *
 */
public class ConsistencyTest {

    public static void main(String[] args) {

        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder());
        Query query = new Query("show measurements", "data");
        QueryResult result = idb.query(query);

        for (List<Object> id : result.getResults().get(0).getSeries().get(0).getValues()) {
            String iid = (String) id.get(0);
            Query numq = new Query("select count(\"I100_1\") from \"" + iid + "\"", "data");
            System.out.println(iid + " : " + idb.query(numq).getResults().get(0).getSeries().get(0).getValues().get(0).get(1));
        }

    }

}
