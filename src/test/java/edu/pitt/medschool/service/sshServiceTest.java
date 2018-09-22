package edu.pitt.medschool.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class sshServiceTest {

    private JSch jsch = new JSch();
    private Session session1, session2;

    @Before
    public void initClientTest() throws JSchException {
        // Path to private key file
        jsch.addIdentity(InfluxappConfig.SSH_PRIVATEKEY_PATH);
        session1 = generateNewSshSession();
        session2 = generateNewSshSession();
    }

    @Test
    public void clientRunTest() throws JSchException, IOException {
        String s1 = testRunner(session1, "hostname;whoami;cd ~/p5y;ls -alh;");
        System.out.println(s1);
        assertTrue(s1.contains("pvt.bridges."));
        assertTrue(s1.contains("idb_deploy_old"));
        String s2 = testRunner(session2, "ssh r732.pvt.bridges.psc.edu;");
        assertTrue(s2.contains("no active jobs on this node"));
        System.out.println(s2);
    }

    @Test
    public void portforwardTest() throws JSchException, InterruptedException {
        Session s3 = generateNewSshSession();
        s3.connect();
        s3.setPortForwardingL("localhost", 8086, "r014.pvt.bridges.psc.edu", 8086);
        System.err.println("Port forward set.");
        // Stop 20s for local connect test
        Thread.sleep(20000);
        s3.delPortForwardingL(8086);
        s3.disconnect();
    }

    private String testRunner(Session session, String command) throws JSchException, IOException {
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        channel.setInputStream(null);
        channel.setErrStream(System.err);
        channel.setPty(true);

        InputStream in = channel.getInputStream();
        channel.connect();

        String res = Util.inputStreamToString(in);
        if (channel.isClosed()) {
            System.out.println("exit-status: " + channel.getExitStatus());
        }

        channel.disconnect();
        session.disconnect();
        return res;
    }

    private Session generateNewSshSession() throws JSchException {
        // Change user name!
        Session s = jsch.getSession("xxx", InfluxappConfig.REMOTE_SSH_HOST);
        java.util.Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey");
        s.setConfig(config);
        return s;
    }

}
