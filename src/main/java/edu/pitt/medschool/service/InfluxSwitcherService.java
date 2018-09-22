package edu.pitt.medschool.service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import edu.pitt.medschool.config.InfluxappConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Switch between PSC and local InfluxDB
 */
@Service
public class InfluxSwitcherService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private JSch jsch = new JSch();

    @Value("${ssh-username}")
    private String username;

    public InfluxSwitcherService() throws JSchException {
        jsch.addIdentity(InfluxappConfig.SSH_PRIVATEKEY_PATH);
    }

    public void loginPsc() {

    }

    private Session generateNewSshSession() throws JSchException {
        Session session = jsch.getSession(username, InfluxappConfig.REMOTE_SSH_HOST);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey");
        session.setConfig(config);
        return session;
    }

}
