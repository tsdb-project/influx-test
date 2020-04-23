package edu.pitt.medschool.service;

import edu.pitt.medschool.model.Email;
import edu.pitt.medschool.model.dao.AccountsDao;
import edu.pitt.medschool.model.dao.VersionDao;
import edu.pitt.medschool.model.dto.Accounts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    @Autowired
    AccountsDao accountsDao;
    @Autowired
    VersionDao versionDao;
    @Autowired
    private JavaMailSender mailSender;

    public List<Accounts> selectAll() {
        return accountsDao.selectAll();
    }

    public List<Accounts> selectByUserName(String username) {
        return accountsDao.selectByUsername(username);
    }

    public int insertUser(Accounts userVO) {
        userVO.setDatabaseVersion(versionDao.getLatestVersion());
        return accountsDao.insertUser(userVO);
    }

    public int updateUser(Accounts userVO) {
        if(userVO.getRole().equals("ROLE_ADMIN")){
            userVO.setDatabaseVersion(0);
        }
        return accountsDao.updateUser(userVO);
    }

    public Accounts selectById(Integer userId) {
        return accountsDao.selectById(userId);
    }

    public int resetPassword(Integer id) {
        return accountsDao.resetPassword(id);
    }

    public int changePassword(Integer id, String password) {
        return accountsDao.changePassword(id,password);
    }

    public int toggleEnabled(Integer id, Boolean enable) {
        return accountsDao.toggleEnabled(id, enable);
    }

    public int getVersionByUserName(String username){
        Accounts account = accountsDao.selectByUsername(username).get(0);
        if(account.getRole().equals("ROLE_ADMIN")){
            return versionDao.getLatestVersion();
        }else {
            return account.getDatabaseVersion();
        }
    }

    public boolean sendEmailMessage(Email email){
        SimpleMailMessage message = new SimpleMailMessage();
        List<Accounts> admins = accountsDao.selectByRole("ROLE_ADMIN");
        try{
            for(Accounts a: admins){
                message.setFrom(email.getEmailAddress());
                message.setTo(a.getEmail());
                message.setSubject("feedback from brainFlux");
                message.setText("From: "+email.getEmailAddress()+"\nUsername: "+email.getUsername()+"\n"+email.getContent());
                mailSender.send(message);
            }

            return true;
        }catch (Exception e){
            return false;
        }

    }

    public boolean newUserAlert(Accounts accounts){
        SimpleMailMessage message = new SimpleMailMessage();
        List<Accounts> admins = accountsDao.selectByRole("ROLE_ADMIN");
        try{
            for(Accounts a: admins){
                message.setFrom(accounts.getEmail());
                message.setTo(a.getEmail());
                message.setSubject("BrainFlux has new user");
                message.setText("From: "+accounts.getEmail()+"\nUsername: "+accounts.getUsername()+"\n"+accounts.getDescription());
                mailSender.send(message);
            }

            return true;
        }catch (Exception e){
            return false;
        }
    }
}
