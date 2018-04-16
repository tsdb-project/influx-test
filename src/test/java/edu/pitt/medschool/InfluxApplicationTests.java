package edu.pitt.medschool;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InfluxApplicationTests {

    @Test
    public void contextLoads() {
        assertEquals(1, 1);
    }

    @Test
    public void testDnsIp() {
        String addr = "localhost";
        try {
            addr = InetAddress.getByName("upmc_influx_1.dreamprc.com").getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(addr);
        assertEquals(addr, "127.0.0.1");
    }
}
