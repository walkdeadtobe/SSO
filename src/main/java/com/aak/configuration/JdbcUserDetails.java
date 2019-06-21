package com.aak.configuration;

import com.aak.domain.*;
import com.aak.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.commons.logging.*;

import java.util.List;

public class JdbcUserDetails implements UserDetailsService{

    public static  Log log=LogFactory.getLog(JdbcUserDetails.class);

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private Credentials_AuthorityRepository credentials_authorityRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            log.info("loadUserByUsername" + username);
            Credentials credentials = credentialRepository.findByName(username);
            log.info("ame");
            log.info("credentials.toString():" + credentials.getId().toString());
            CredentialsAuthority credentials_authority = credentials_authorityRepository.findByCredentialsid(credentials.getId());
            log.info("credentials_authority..toString():" + credentials_authority.toString());
            List<Authority> authority = authorityRepository.findAuthoritiesById(credentials_authority.getAuthority());
            log.info("authority.toString():" + authority.toString());

        if(credentials==null){

            throw new UsernameNotFoundException("User"+username+"can not be found");
        }


        User user = new User(credentials.getName(),credentials.getPassword(),credentials.isEnabled(),true,true,true,authority);

        log.info(user.toString());
        return  user;

        }catch (Exception e){
            log.info(e.toString());
        }
        return null;

    }
}
