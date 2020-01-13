package com.aak.configuration;

import com.aak.domain.*;
import com.aak.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.commons.logging.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implemention of UserDetailsService for retrieval of  User information
 * @author chengr
 * @Time 2019-9-10
 */
@Service
public class JdbcUserDetails implements UserDetailsService{

    public static  Log log=LogFactory.getLog(JdbcUserDetails.class);

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private Credentials_AuthorityRepository credentials_authorityRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public BCryptPasswordEncoder passwordEncoder;

    private String Max_Attempt="5";


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String num_str=stringRedisTemplate.opsForValue().get(username);
        if(num_str != null&&num_str.equals(Max_Attempt)){
            log.info("num_str.equals(Max_Attempt)");
            throw new UsernameNotFoundException("User  "+username+"can not be found because exceed");
        }
        if(stringRedisTemplate!=null)
        {
            log.info("stringRedisTemplate!=null");
        }
        try {
            log.info("loadUserByUsername" + username);
            Credentials credentials = credentialRepository.findByName(username);
            CredentialsAuthority credentials_authority = credentials_authorityRepository.findByCredentialsid(credentials.getId());
            List<Authority> authority = authorityRepository.findAuthoritiesById(credentials_authority.getAuthority());

        if(credentials==null){
            throw new UsernameNotFoundException("User"+username+"can not be found");
        }

        User user = new User(credentials.getName(),credentials.getPassword(),credentials.isEnabled(),true,true,true,authority);
        return  user;
        }catch (Exception e){
            log.info(e.toString());
        }
        return null;

    }

    public boolean addUser(String username,String password,String department,long authority_id){
        try{
            passwordEncoder=new BCryptPasswordEncoder();
            String authority=authorityRepository.findAuthoritiesById(authority_id).get(0).getAuthority();
            log.info("authority:"+authority);
            String password_real=passwordEncoder.encode(password);
            Long num=credentialRepository.count();
            //CredentialsAuthority credentialsAuthority=new CredentialsAuthority(num+1,authority_id);
            List<Authority> authorities=new ArrayList<Authority>();
            authorities.add(new Authority(authority_id,authority));

            Credentials credentials=new Credentials(num+1,0,username,password_real,department,authorities,true);
            //String sql_credential="INSERT INTO credentials  VALUES("+(num+1)+",b\'1\',\'"+username+"\',\'"+password_real+"\',\'DEPART\',\'0\')";
            //String sql_credential_authority="INSERT INTO credentials_authorities  VALUES("+(num+1)+",6)";
            //log.info(sql_credential);
            //log.info(sql_credential_authority);
            credentialRepository.saveAndFlush(credentials);
            return true;
        }catch(Exception e){
            log.error(e.toString());
            return false;
        }
    }


    public boolean deleteUser(String username){
        try {
            credentialRepository.deleteCredentialsByName(username);
            return true;
        }catch(Exception e){
            log.error(e.toString());
            return false;
        }
    }

    public boolean updateUserPassword(String username,String password){
        try {
            passwordEncoder = new BCryptPasswordEncoder();
            String password_real = passwordEncoder.encode(password);
            Credentials find = credentialRepository.findByName(username);
            find.setPassword(password_real);
            return true;
        }catch(Exception e){
            log.error(e.toString());
            return false;
        }
    }
}
