package edu.pitt.medschool.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.pitt.medschool.model.dao.AccountsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.Accounts;
import edu.pitt.medschool.model.dto.CustomUserDetails;

@Service("customUserDetailService")
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private AccountsDao accountsDao;

    @Transactional(readOnly = true)
    @Override
    public CustomUserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {

        List<Accounts> user = accountsDao.selectByUsername(username);
        List<GrantedAuthority> authorities = buildUserAuthority(user);

        Accounts vo = user.get(0);

        return buildUserForAuthentication(vo, authorities);

    }

    private CustomUserDetails buildUserForAuthentication(Accounts user, List<GrantedAuthority> authorities) {
        User myUserDetails = new User(user.getUsername(), user.getPassword(), user.getEnable(), true, true, true, authorities);
        CustomUserDetails customUserDetails = new CustomUserDetails(myUserDetails);
        customUserDetails.setId(user.getId());
        customUserDetails.setFirstname(user.getFirstName());
        customUserDetails.setLastname(user.getLastName());
        return customUserDetails;
    }

    private List<GrantedAuthority> buildUserAuthority(List<Accounts> user) {
        Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();
        for (Accounts vo : user) {
            setAuths.add(new SimpleGrantedAuthority(vo.getRole()));
        }
        List<GrantedAuthority> Result = new ArrayList<GrantedAuthority>(setAuths);

        return Result;
    }
}
