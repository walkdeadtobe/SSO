package com.aak.api;

import com.aak.configuration.JdbcClientDetails;
import com.aak.configuration.JdbcUserDetails;
import com.aak.domain.Authority;
import com.aak.domain.Credentials;
import com.aak.domain.CredentialsAuthority;
import com.aak.repository.AuthorityRepository;
import com.aak.repository.CredentialRepository;
import com.aak.repository.Credentials_AuthorityRepository;
import com.aak.utils.ApplicationSupport;
import jdk.nashorn.internal.runtime.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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


    private  static String  ROLE_ADMIN="ROLE_ADMIN";

    @Autowired
    public CredentialRepository credentialRepository;
    @Autowired
    public Credentials_AuthorityRepository credentials_authorityRepository;

    @Autowired
    public AuthorityRepository authorityRepository;

    @Autowired
    private JdbcUserDetails jdbcUserDetails;

    private Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/user/add",method = RequestMethod.GET)
    public ResponseEntity  addUser(@RequestParam(value = "name") String name, @RequestParam(value = "password") String password, @RequestParam(value = "token") String token )
    {
        if (!get_authoruty(token).equals(ROLE_ADMIN))
        {
            return ResponseEntity.status(401).body("权限不足");
        }
        long authority_id=6;

        if(jdbcUserDetails.addUser(name,password,"DEPART",authority_id))
            return ResponseEntity.status(200).body("增加用户成功");
        else
            return ResponseEntity.status(500).body("服务器错误");
    }

    @RequestMapping(value = "/user/delete",method = RequestMethod.GET)
    public ResponseEntity  addUser(@RequestParam(value = "name") String name, @RequestParam(value = "token") String token )
    {
        if (!get_authoruty(token).equals(ROLE_ADMIN))
        {
            return ResponseEntity.status(401).body("权限不足");
        }
        if(jdbcUserDetails.deleteUser(name))
            return ResponseEntity.status(200).body("删除用户成功");
        else
            return ResponseEntity.status(500).body("服务器错误");
    }

    @Transactional
    @Modifying
    @CacheEvict(cacheNames="secondlevels",allEntries = true)
    @RequestMapping(value = "/user/update",method = RequestMethod.GET)
    public ResponseEntity updateUser(@RequestParam(value = "name") String name, @RequestParam(value = "password") String password, @RequestParam(value = "token") String token)
    {
        if (!get_authoruty(token).equals(ROLE_ADMIN))
        {
            return ResponseEntity.status(401).body("权限不足");
        }
        //jdbcUserDetails= new JdbcUserDetails();
        if(jdbcUserDetails.updateUserPassword(name,password))
            return ResponseEntity.status(200).body("更改密码成功") ;
        else
            return ResponseEntity.status(500).body("服务器错误");
    }

    public String get_authoruty(String token){
        TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(token);
        oAuth2Authentication.getPrincipal();
        User user= (User)oAuth2Authentication.getPrincipal();
        Credentials credentials=credentialRepository.findByName(user.getUsername());
        String  authority=credentials.getAuthorities().get(0).getAuthority();
        log.info(authority);
        return authority;
    }

}
