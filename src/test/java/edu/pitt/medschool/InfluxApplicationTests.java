package edu.pitt.medschool;

import static org.junit.Assert.assertEquals;

import edu.pitt.medschool.framework.util.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InfluxApplicationTests {

    @Test
    public void contextLoads() {
        assertEquals(1, 1);
    }

    @Test
    public void testDnsIp() {
        String addr = Util.getIpFromHostname("upmc_influx_1.dreamprc.com");
        System.out.println(addr);
        assertEquals(addr, "127.0.0.1");
    }
}
