package edu.pitt.medschool.framework;

import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.Util;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class appUtilTest {

    @Test
    public void testFiles() {
        List<String> s = Arrays.asList(Util.getAllSpecificFileInDirectory("src/main/resources/myBatis", "xml"));
        assertEquals(s.size(), 4);

        List<FileBean> b = Util.filesInFolder("src/main/resources");
        assertEquals(b.size(), 0);
    }

    @Test
    public void testDns() {
        assertEquals(Util.getIpFromHostname("local.tsdb.science"), "127.0.0.1");
    }
}
