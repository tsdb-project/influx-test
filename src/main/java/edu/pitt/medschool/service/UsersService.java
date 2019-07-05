package edu.pitt.medschool.service;

import edu.pitt.medschool.model.dao.AccountsDao;
import edu.pitt.medschool.model.dao.VersionDao;
import edu.pitt.medschool.model.dto.Accounts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    @Autowired
    AccountsDao accountsDao;
    @Autowired
    VersionDao versionDao;

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
}
