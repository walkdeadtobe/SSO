package com.aak.api;

import com.aak.domain.Authority;
import com.aak.domain.Credentials;
import com.aak.domain.CredentialsAuthority;
import com.aak.repository.CredentialRepository;
import com.aak.repository.Credentials_AuthorityRepository;
import jdk.nashorn.internal.runtime.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Restful API about User information with authority
 * Now useless
 * @author chengr
 * @Time 2019-11-25
 */

@RestController
//@RequestMapping("user")
public class UsersController {
    public BCryptPasswordEncoder passwordEncoder;
    @Autowired
    public CredentialRepository credentialRepository;
    @Autowired
    public Credentials_AuthorityRepository credentials_authorityRepository;
    private Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/user/add",method = RequestMethod.GET)
    public String  addUser(@RequestParam(value = "name") String name, @RequestParam(value = "password") String password)
    {
        long authority_id=6;
        passwordEncoder=new BCryptPasswordEncoder();
        String password_real=passwordEncoder.encode(password);
        Long num=credentialRepository.count();
        //CredentialsAuthority credentialsAuthority=new CredentialsAuthority(num+1,authority_id);
        List<Authority> authorities=new ArrayList<Authority>();
        authorities.add(new Authority(authority_id,"ROLE_DEPART"));

        Credentials credentials=new Credentials(num+1,0,name,password_real,"DEPART",authorities,true);
        String sql_credential="INSERT INTO credentials  VALUES("+(num+1)+",b\'1\',\'"+name+"\',\'"+password_real+"\',\'DEPART\',\'0\')";
        String sql_credential_authority="INSERT INTO credentials_authorities  VALUES("+(num+1)+",6)";
        if(log.isDebugEnabled()){
            log.debug(sql_credential);
            log.debug(sql_credential_authority);
            log.debug("in debug");
        }
        log.info(sql_credential);
        log.info(sql_credential_authority);
        credentialRepository.saveAndFlush(credentials);

        return null;
    }
}
