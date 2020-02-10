package edu.pitt.medschool.service;

import com.jcraft.jsch.*;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private Session forwardSession;

    private String currentJobId = "";
    private String remoteInfluxHostname = "";
    private AtomicBoolean hasStartedPscInflux = new AtomicBoolean(false);

    private String systemOs = System.getProperty("os.name");

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
     * Setup a remote PSC InfluxDB server (blocking)
     */
    public void setupRemoteInflux() {
        if (this.hasStartedPscInflux.get()) {
            logger.error("Can't setup remote InfluxDB if there is another InfluxDB running on remote.");
            return;
        }
        if (this.getHasStartedLocalInflux()) {
            logger.error("Can't setup remote InfluxDB if local one is running.");
            return;
        }
        try {
            if (submitStartPscInflux()) {
                while (pscInfluxIsInQueue()) {
                    // Check every 30s to ensure that we are no longer in queue
                    Thread.sleep(30_000);
                }
                // InfluxDB takes over 3 min to start
                Thread.sleep(200_000);
                while (!hasPscInfluxStarted()) {
                    // Check every 15s to ensure that Influx is available
                    Thread.sleep(15_000);
                }
                while (!startPortForward()) {
                    // Check every 15s to start port forwaring
                    Thread.sleep(15_000);
                }
                this.hasStartedPscInflux.set(true);
            } else {
                this.hasStartedPscInflux.set(false);
            }
        } catch (InterruptedException e) {
            logger.error("PSC start thread failure!");
            this.stopRemoteInflux();
            this.hasStartedPscInflux.set(false);
        }
    }

    /**
     * Stop a remote PSC server
     */
    public boolean stopRemoteInflux() {
        // Stop a never started server, consider stop successfully
        if (!this.hasStartedPscInflux.get()) return true;
        if (stopPscInflux()) {
            if (stopPortForward()) {
                // Stopped successfully
                this.hasStartedPscInflux.set(false);
                return true;
            } else {
                logger.error("Failed to stop PSC InfluxDB tunnel.");
                return false;
            }
        } else {
            logger.error("Failed to stop PSC InfluxDB job.");
            return false;
        }
    }

    /**
     * Start a local useable influxdb
     * Should only run this on the Mac Pro!
     */
    public void setupLocalInflux() {
        if (this.getHasStartedLocalInflux()) return;
        if (this.isSystemWindows()) {
            logger.error("Start local InfluxDB does NOT support Windows");
            return;
        }
        try {
            String[] command = {"/bin/bash","/Volumes/INFLUX_RAID/config/startDB.sh"};
            Runtime.getRuntime().exec(command);
            // Local InfluxDB takes up to 5 mins for starting
            Thread.sleep(300_000);
        } catch (Exception e) {
            logger.error("Start local failed: {}", Util.stackTraceErrorToString(e));
            // Local may still started in this case
            this.stopLocalInflux();
        }
    }

    /**
     * Stop a local InfluxDB
     */
    public void stopLocalInflux() {
        if (this.systemOs.toLowerCase().contains("windows")) {
            logger.error("Stop local InfluxDB does NOT support Windows");
            return;
        }
        try {
            Process p = Runtime.getRuntime().exec("pkill -15 influxd");
            p.waitFor();
            // From man document (pkill):
            // 0: One or more processes matched the criteria. 1: No processes matched.
            // 2: Syntax error in the command line. 3: Fatal error: out of memory etc.
            switch (p.exitValue()) {
                case 0:
                    logger.warn("Local InfluxDB SIGTERM success");
                    break;
                case 1:
                    logger.warn("No running Local InfluxDB");
                    break;
                default:
                    throw new RuntimeException(String.format("pkill return status <%d>", p.exitValue()));
            }
        } catch (Exception e) {
            logger.error("Stop local failed: {}", Util.stackTraceErrorToString(e));
        }
    }

    /**
     * Check the status of a PSC InfluxDB
     */
    public boolean getHasStartedPscInflux() {
        // We can trust `hasStartedPscInflux`
        return this.hasStartedPscInflux.get();
    }

    /**
     * Check the status of a Local InfluxDB
     */
    public boolean getHasStartedLocalInflux() {
        String[] command = new String[]{"/bin/bash", "-c", "ps ux|grep influxd"};
        if (this.isSystemWindows()) {
            // Special command for Windows, tested Win 10 only
            command = new String[]{"tasklist.exe", "/FI", "IMAGENAME eq influxd.exe"};
        }
        boolean started = false;
        String line;
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = input.readLine()) != null) {
                String lineLc = line.toLowerCase();
                // First for *NIX, second for Windows
                // We always have the '-config' set for our InfluxDB instance
                if (lineLc.contains("-config") || lineLc.contains("influxd.exe")) {
                    started = true;
                    break;
                }
            }
            if (!started) {
                while ((line = err.readLine()) != null) {
                    String lineLc = line.toLowerCase();
                    // First for *NIX, second for Windows
                    // We always have the '-config' set for our InfluxDB instance
                    if (lineLc.contains("-config") || lineLc.contains("influxd.exe")) {
                        started = true;
                        break;
                    }
                }
            }
            err.close();
            input.close();
            p.destroyForcibly();
        } catch (IOException e) {
            logger.error("Get local InfluxDB status failed: {}", e.getLocalizedMessage());
        }
        return started;
    }

    /**
     * Submit start InfluxDB sbatch job on PSC
     *
     * @return True if command successfully inited, False if error happend
     */
    private boolean submitStartPscInflux() {
        if (!this.isSshReady) return false;
        // Avoid start one again
        if (!this.currentJobId.isEmpty()) return false;
        if (!this.remoteInfluxHostname.isEmpty()) return false;
        this.currentJobId = "";
        try {
            Matcher m = jobIdPattern.matcher(runOneCommandViaSSH(String.format(Template.COMMAND_BASIC, "cd /pylon5/bi5fpep/quz3/ondemand;sbatch start-influx;")));
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
     * Stop the InfluxDB job (InfluxDB stop process may take 30s)
     *
     * @return True if idb stop signal successfully stopped, False if error happend
     */
    private boolean stopPscInflux() {
        if (!this.isSshReady) return false;
        // Avoid stop a not started server (Optional)
        if (this.currentJobId.isEmpty()) return false;
        try {
            String res = runOneCommandViaSSH(String.format("srun --jobid=%s kill -15 $(cat %s);", this.currentJobId, Template.NOW_PID_PATH)).trim();
            if (!res.isEmpty()) {
                logger.error("Stop PSC influx said: {}", res);
                return false;
            }
        } catch (Exception e) {
            logger.error("SSH batch job failed to stop: {}", Util.stackTraceErrorToString(e));
            return false;
        }
        logger.warn("InfluxDB stop signal sent for ID <{}>.", this.currentJobId);
        this.currentJobId = "";
        this.remoteInfluxHostname = "";
        return true;
    }

    private boolean startPortForward() {
        if (this.forwardSession != null) return false;
        if (this.remoteInfluxHostname.isEmpty()) {
            return false;
        }
        try {
            forwardSession = generateNewSshSession(true);
            forwardSession.connect();
            forwardSession.setPortForwardingL("localhost", 8086, remoteInfluxHostname, 8086);
            logger.warn("Port forward to <{}> started on <{}>", remoteInfluxHostname, TimeUtil.formatLocalDateTime(null, ""));
        } catch (JSchException e) {
            logger.error("Set port forward failed: {}", Util.stackTraceErrorToString(e));
            return false;
        }
        return true;
    }

    private boolean stopPortForward() {
        if (this.forwardSession == null) return false;
        try {
            forwardSession.delPortForwardingL(8086);
            logger.warn("Port forward to <{}> stopped on <{}>", remoteInfluxHostname, TimeUtil.formatLocalDateTime(null, ""));
        } catch (JSchException e) {
            logger.error("Stop port forward failed: {}", Util.stackTraceErrorToString(e));
            return false;
        } finally {
            forwardSession.disconnect();
            forwardSession = null;
        }
        return true;
    }

    /**
     * Check if a InfluxDB job is in queue (wait) or started
     */
    public boolean pscInfluxIsInQueue() {
        String status = "";
        try {
            status = runOneCommandViaSSH(String.format(Template.COMMAND_BASIC, Template.READ_NOW_HOST)).trim();
        } catch (Exception e) {
            logger.error("Failed to get status of currently running InfluxDB");
            return false;
        }
        boolean inQueue = status.contains("No such file or directory");
        if (!inQueue) {
            // If not in queue, then the remote hostname is here
            this.remoteInfluxHostname = status.trim();
        }
        return inQueue;
    }

    /**
     * Usually Idb takes 5 minutes to start, we have to check it make sure it actually started
     * In is intented for use with early starting stage
     */
    private boolean hasPscInfluxStarted() {
        if (this.remoteInfluxHostname.isEmpty()) {
            return false;
        }
        String res = "";
        try {
            res = runOneCommandViaSSH(String.format(Template.COMMAND_BASIC, "tail -n50 /pylon5/bi5fpep/quz3/ondemand_log/*.log"));
        } catch (Exception e) {
            logger.error("Failed to determine InfluxDB status: {}, returns: {}", e.getLocalizedMessage(), res);
            return false;
        }
        return res.contains("Compacting file") || res.contains("Listening on HTTP");
    }

    /**
     * Generate a new SSH session and run the command
     */
    private String runOneCommandViaSSH(String command) throws JSchException, IOException {
        Session session = generateNewSshSession(true);
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

        String res = readOutputFromChannelStream(channel, in);

        if (channel.getExitStatus() != 0) {
            logger.warn("SSH command <{}> message: '{}'", command, res);
        } else {
            logger.info("SSH command <{}> success.", command);
        }
        in.close();
        channel.disconnect();
        session.disconnect();
        return res;
    }

    /**
     * Read output from JSch and convert it to string
     */
    private String readOutputFromChannelStream(ChannelExec channel, InputStream in) throws IOException {
        int buf_size = 1024;
        StringBuilder sb = new StringBuilder(buf_size);
        byte[] buffer = new byte[buf_size];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(buffer, 0, buf_size);
                // When i==-1, EOF reached
                if (i < 0) break;
                sb.append(new String(buffer, 0, i, StandardCharsets.UTF_8));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                break;
            }
        }
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

    /**
     * Is this OS Micro$oft Win?
     */
    public boolean isSystemWindows() {
        return this.systemOs.toLowerCase().contains("windows");
    }

}
