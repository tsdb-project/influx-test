package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.Accounts;
import edu.pitt.medschool.model.dto.AccountsExample;
import edu.pitt.medschool.model.mapper.AccountsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        accountsExample.createCriteria().andUsernameEqualTo(username);
        return accountsMapper.selectByExample(accountsExample);
    }

    @Transactional
    public int insertUser(Accounts userVO) {
        System.out.println(userVO.getFirstName());

        Accounts users = new Accounts();
        users.setUsername(userVO.getUsername());
        users.setFirstName(userVO.getFirstName());
        users.setLastName(userVO.getLastName());
        users.setRole(userVO.getRole());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String pwdString = encoder.encode(DEFAULTPASSWORD);
        users.setPassword(pwdString);


        return accountsMapper.insertSelective(users);
//        if (res == 1) {
//            Authorities authorities = new Authorities();
//            authorities.setId(users.getId());
//            authorities.setUsername(userVO.getUsername());
//            authorities.setAuthority(userVO.getRole());
//            res = authoritiesMapper.insertSelective(authorities);
//        }
    }

    @Transactional
    public int updateUser(Accounts userVO) {
        Accounts users = new Accounts();
        users.setId(userVO.getId());
        users.setFirstName(userVO.getFirstName());
        users.setLastName(userVO.getLastName());

//        Authorities authorities = new Authorities();
//        authorities.setId(userVO.getId());
//        authorities.setAuthority(userVO.getRole());

        return accountsMapper.updateByPrimaryKeySelective(users);
//        if (res == 1) {
//            AuthoritiesExample example = new AuthoritiesExample();
//            example.createCriteria().andIdEqualTo(userVO.getId());
//            res = authoritiesMapper.updateByExampleSelective(authorities, example);
//        }
    }

    public int resetPassword(Integer id) {
        Accounts users = new Accounts();
        users.setId(id);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        users.setPassword(encoder.encode(DEFAULTPASSWORD));
        return accountsMapper.updateByPrimaryKeySelective(users);
    }

    public int toggleEnabled(Integer id, Boolean enable) {
        Accounts users = new Accounts();
        users.setId(id);
        users.setEnable(enable);
        return accountsMapper.updateByPrimaryKeySelective(users);
    }

}
