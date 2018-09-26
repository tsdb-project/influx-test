package edu.pitt.medschool.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;

public class SshServiceTest {

    private JSch jsch = new JSch();
    private Session session1, session2;

    InfluxSwitcherService iss = new InfluxSwitcherService();
    private Pattern jobIdPattern = Pattern.compile("[0-9]+");

    @Before
    public void initClientTest() throws JSchException {
        HostKey hk = new HostKey(InfluxappConfig.REMOTE_SSH_HOST, HostKey.SSHRSA, InfluxappConfig.BRIDGE_LOGIN_PUBKEY_RSA);
        jsch.getHostKeyRepository().add(hk, null);
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
        String s2 = testRunner(session2, "grep --help;ssh r732.pvt.bridges.psc.edu;");
        assertTrue(s2.contains("no active jobs on this node"));
        System.out.println(s2);
    }

    @Test
    public void portforwardTest() throws JSchException, InterruptedException {
        Session s3 = generateNewSshSession();
        s3.connect();
        s3.setPortForwardingL("localhost", 8086, "r014.pvt.bridges.psc.edu", 8086);
        System.err.println("Port forward set.");
        // Stop 1s for local connect test
        Thread.sleep(1000);
        s3.delPortForwardingL(8086);
        s3.disconnect();
    }

    @Test
    public void submitJobTest() throws JSchException, IOException {
        Session s = generateNewSshSession();
        String res = testRunner(s, "cd ~/dev;sbatch 1;");
        s.disconnect();
        Matcher m = jobIdPattern.matcher(res);
        while (m.find()) {
            System.err.println(String.format("JobID: <%s>", m.group()));
        }
        // Group count = 0 means one match or matched all
        assertEquals(m.groupCount(), 0);
    }

    @Test
    public void patternTest() {
        Matcher m = jobIdPattern.matcher("Unable to allocate resources: Invalid");
        while (m.find()) {
            assertEquals(m.group(), "");
        }
    }

    @Test
    public void isLocalIdbRunning() {
        System.err.println("Local IDB status: " + iss.getHasStartedLocalInflux());
    }

    private String testRunner(Session session, String command) throws JSchException, IOException {
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setEnv("LC_ALL", "en_US.UTF-8");
        channel.setEnv("LANG", "en_US.UTF-8");
        channel.setCommand(command);

        channel.setInputStream(null);
        channel.setOutputStream(null);
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
        config.put("StrictHostKeyChecking", "yes");
        config.put("PreferredAuthentications", "publickey");
        config.put("compression.s2c", "zlib,none");
        config.put("compression.c2s", "zlib,none");
        config.put("compression_level", "6");
        s.setConfig(config);
        return s;
    }

}
