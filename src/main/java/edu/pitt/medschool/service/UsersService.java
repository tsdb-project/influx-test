package edu.pitt.medschool.service;

import edu.pitt.medschool.model.dao.AccountsDao;
import edu.pitt.medschool.model.dto.Accounts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    @Autowired
    AccountsDao accountsDao;

    public List<Accounts> selectAll() {
        return accountsDao.selectAll();
    }

    public List<Accounts> selectByUserName(String username) {
        return accountsDao.selectByUsername(username);
    }

    public int insertUser(Accounts userVO) {
        return accountsDao.insertUser(userVO);
    }

    public int updateUser(Accounts userVO) {
        return accountsDao.updateUser(userVO);
    }

    public Accounts selectById(Integer userId) {
        return accountsDao.selectById(userId);
    }

    public int resetPassword(Integer id) {
        return accountsDao.resetPassword(id);
    }

    public int toggleEnabled(Integer id, Boolean enable) {
        return accountsDao.toggleEnabled(id, enable);
    }
}
