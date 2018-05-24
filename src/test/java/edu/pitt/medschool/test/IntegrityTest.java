/**
 * 
 */
package edu.pitt.medschool.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class IntegrityTest {

    public static void main(String[] args) {

        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
                new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
        Query query = new Query("show measurements", "data");
        QueryResult result = idb.query(query);

        for (List<Object> id : result.getResults().get(0).getSeries().get(0).getValues()) {
            String iid = (String) id.get(0);
            if (iid.startsWith("PUH-2015") || iid.startsWith("PUH-2016") || iid.startsWith("PUH-2017") || iid.startsWith("PUH-2018")) {
                Query numq = new Query("select count(\"I1_1\") from \"" + iid + "\"", "data");
                Double count = (Double) idb.query(numq).getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
                System.out.println(iid + " : " + count.intValue());
            }
        }

    }

}
