package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.Accounts;
import edu.pitt.medschool.model.dto.AccountsExample;
import edu.pitt.medschool.model.mapper.AccountsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
public class AccountsDao {

    private final String DEFAULTPASSWORD = "123456";


    @Autowired
    AccountsMapper accountsMapper;

    public List<Accounts> selectAll() {
        return accountsMapper.selectAll();
    }

    public Accounts selectById(int userId) {
        return accountsMapper.selectByPrimaryKey(userId);
    }

    public List<Accounts> selectByUsername(String username) {
        AccountsExample accountsExample = new AccountsExample();
        AccountsExample.Criteria criteria = accountsExample.createCriteria();
        criteria.andUsernameEqualTo(username);
        return accountsMapper.selectByExample(accountsExample);
    }

    @Transactional
    public int insertUser(Accounts userVO) {
        Accounts users = new Accounts();
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        users.setUsername(userVO.getUsername());
        users.setFirstName(userVO.getFirstName());
        users.setLastName(userVO.getLastName());
        users.setEmail(userVO.getEmail());
        users.setCreateTime(americaDateTime);
        users.setLastUpdate(americaDateTime);
        users.setRole(userVO.getRole());
        users.setDatabaseVersion(userVO.getDatabaseVersion());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String pwdString = encoder.encode(userVO.getPassword());
        users.setPassword(pwdString);


        return accountsMapper.insertSelective(users);
    }

    @Transactional
    public int updateUser(Accounts userVO) {
        Accounts users = new Accounts();
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        users.setId(userVO.getId());
        users.setFirstName(userVO.getFirstName());
        users.setLastName(userVO.getLastName());
        users.setEmail(userVO.getEmail());
        users.setLastUpdate(americaDateTime);
        users.setRole(userVO.getRole());
        users.setDatabaseVersion(userVO.getDatabaseVersion());
        return accountsMapper.updateByPrimaryKeySelective(users);
    }

    public int resetPassword(Integer id) {
        Accounts users = new Accounts();
        users.setId(id);
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        users.setLastUpdate(americaDateTime);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        users.setPassword(encoder.encode(DEFAULTPASSWORD));
        return accountsMapper.updateByPrimaryKeySelective(users);
    }

    public int changePassword(Integer id, String password) {
        Accounts users = new Accounts();
        users.setId(id);
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        users.setLastUpdate(americaDateTime);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        users.setPassword(encoder.encode(password));
        return accountsMapper.updateByPrimaryKeySelective(users);
    }

    public int toggleEnabled(Integer id, Boolean enable) {
        Accounts users = new Accounts();
        users.setId(id);
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        users.setLastUpdate(americaDateTime);
        users.setEnable(enable);
        return accountsMapper.updateByPrimaryKeySelective(users);
    }

}
