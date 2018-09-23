package edu.pitt.medschool.service;

import com.jcraft.jsch.*;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Switch between PSC and local InfluxDB
 */
@Service
public class InfluxSwitcherService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private class Template {
        private Template() {
        }

        static final String COMMAND_BASIC = "/bin/bash -c \"%s\"";
        static final String NOW_PID_PATH = "/pylon5/bi5fpep/quz3/ondemand_log/nowpid";
        static final String READ_NOW_HOST = "cat /pylon5/bi5fpep/quz3/ondemand_log/nowhost";
    }

    private boolean isSshReady = false;
    private JSch jsch = new JSch();
    private Pattern jobIdPattern = Pattern.compile("[0-9]+");

    private String currentJobId = "";
    private String remoteInfluxHostname = "";

    @Value("${ssh-username}")
    private String username;

    /**
     * Initialize SSH client related info
     */
    public InfluxSwitcherService() {
        try {
            jsch.addIdentity(InfluxappConfig.SSH_PRIVATEKEY_PATH);
            HostKey hk = new HostKey(InfluxappConfig.REMOTE_SSH_HOST, HostKey.SSHRSA, InfluxappConfig.BRIDGE_LOGIN_PUBKEY_RSA);
            jsch.getHostKeyRepository().add(hk, null);
            isSshReady = true;
        } catch (JSchException e) {
            logger.error("Init SSH client failed: {}", Util.stackTraceErrorToString(e));
        }
    }

    /**
     * Submit start InfluxDB sbatch job on PSC
     *
     * @return True if command successfully inited, False if error happend
     */
    public boolean submitStartPscInflux() {
        if (!this.isSshReady) return false;
        // Avoid start one again
        if (!this.currentJobId.isEmpty()) return false;
        if (!this.remoteInfluxHostname.isEmpty()) return false;
        this.currentJobId = "";
        try {
            Matcher m = jobIdPattern.matcher(runCommandViaSSH(String.format(Template.COMMAND_BASIC, "cd /pylon5/bi5fpep/quz3/ondemand;sbatch start-influx;")));
            if (m.find()) {
                this.currentJobId = m.group();
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("SSH batch job failed to submit: {}", Util.stackTraceErrorToString(e));
            return false;
        }
        if (this.currentJobId.isEmpty()) return false;
        logger.warn("InfluxDB start job submitted with ID <{}>.", this.currentJobId);
        return true;
    }

    /**
     * Stop the InfluxDB job (Better wait 30s when to let InfluxDB stop
     *
     * @return True if idb stop signal successfully stopped, False if error happend
     */
    public boolean stopPscInflux() {
        if (!this.isSshReady) return false;
        // Avoid stop a not started server (Optional)
        if (this.currentJobId.isEmpty()) return false;
        try {
            String res = runCommandViaSSH(String.format("srun --jobid=%s kill -15 $(cat %s);", this.currentJobId, Template.NOW_PID_PATH));
            if (!res.trim().isEmpty()) return false;
        } catch (Exception e) {
            logger.error("SSH batch job failed to stop: {}", Util.stackTraceErrorToString(e));
            return false;
        }
        logger.warn("InfluxDB stop signal sent for ID <{}>.", this.currentJobId);
        this.currentJobId = "";
        this.remoteInfluxHostname = "";
        return true;
    }

    private Session forwardSession;

    public void startPortForward() {
        if (this.forwardSession != null) return;
        if (this.remoteInfluxHostname.isEmpty()) {
            tryInitRemoteInfluxHostname();
        }
        try {
            forwardSession = generateNewSshSession(true);
            forwardSession.connect();
            forwardSession.setPortForwardingL("localhost", 8086, remoteInfluxHostname, 8086);
            logger.warn("Port forward to <{}> started on <{}>", remoteInfluxHostname, TimeUtil.formatLocalDateTime(null, ""));
        } catch (JSchException e) {
            logger.error("Set port forward failed: {}", Util.stackTraceErrorToString(e));
        }
    }

    public void stopPortForward() {
        if (this.forwardSession == null) return;
        try {
            forwardSession.delPortForwardingL(8086);
            logger.warn("Port forward to <{}> stopped on <{}>", remoteInfluxHostname, TimeUtil.formatLocalDateTime(null, ""));
        } catch (JSchException e) {
            logger.error("Stop port forward failed: {}", Util.stackTraceErrorToString(e));
        } finally {
            forwardSession.disconnect();
            forwardSession = null;
        }
    }

    /**
     * Check if a InfluxDB job is in queue (wait) or started
     */
    public boolean pscInfluxIsInQueue() {
        String status = "";
        try {
            status = runCommandViaSSH(String.format(Template.COMMAND_BASIC, Template.READ_NOW_HOST)).trim();
        } catch (Exception e) {
            logger.error("Failed to get status of currently running InfluxDB");
        }
        return status.contains("No such file or directory");
    }

    /**
     * Try initialize the remote InfluxDB hostname
     */
    public void tryInitRemoteInfluxHostname() {
        try {
            this.remoteInfluxHostname = runCommandViaSSH(String.format(Template.COMMAND_BASIC, Template.READ_NOW_HOST)).trim();
            if (this.remoteInfluxHostname.contains("No such file or directory")) {
                this.remoteInfluxHostname = "";
            }
        } catch (Exception e) {
            logger.error("Failed to get remote Influx hostname");
        }
    }

    /**
     * Usually Idb takes 5 minutes to start, we have to check it make sure it actually started
     */
    public boolean hasPscInfluxStarted() {
        if (this.remoteInfluxHostname.isEmpty()) {
            tryInitRemoteInfluxHostname();
        }
        String res;
        try {
            res = runCommandViaSSH(String.format(Template.COMMAND_BASIC, "tail -n50 /pylon5/bi5fpep/quz3/ondemand_log/*.log"));
        } catch (Exception e) {
            logger.error("Failed to determine InfluxDB status: {}", Util.stackTraceErrorToString(e));
            return false;
        }
        return res.contains("Compacting file") || res.contains("Listening on HTTP");
    }

    /**
     * Generate a new SSH session and run the command
     */
    private String runCommandViaSSH(String command) throws JSchException, IOException {
        Session session = generateNewSshSession(true);
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setOutputStream(null);
        channel.setPty(true);
        InputStream in = channel.getInputStream();
        channel.connect();

        StringBuilder sb = new StringBuilder(1024);
        byte[] buffer = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(buffer, 0, 1024);
                if (i < 0) break;
                sb.append(new String(buffer, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                break;
            }
        }
        logger.info("SSH command <{}>, status: {}.", command, channel.getExitStatus());
        if (channel.getExitStatus() != 0) {
            logger.warn("SSH command <{}> message: '{}'", command, sb.toString());
        }
        in.close();
        channel.disconnect();
        session.disconnect();
        return sb.toString();
    }

    /**
     * Generate a new SSH session from JSch
     */
    private Session generateNewSshSession(boolean needCompress) throws JSchException {
        Session session = jsch.getSession(username, InfluxappConfig.REMOTE_SSH_HOST);
        java.util.Properties config = new Properties();
        config.put("StrictHostKeyChecking", "yes");
        config.put("PreferredAuthentications", "publickey");
        if (needCompress) {
            config.put("compression.s2c", "zlib,none");
            config.put("compression.c2s", "zlib,none");
            config.put("compression_level", "9");
        }
        session.setConfig(config);
        return session;
    }

}
