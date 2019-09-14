package com.aak.configuration;

import com.aak.domain.*;
import com.aak.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.apache.commons.logging.*;
import java.util.List;

/**
 * Implemention of UserDetailsService for retrieval of  User information
 * @author chengr
 * @Time 2019-9-10
 */
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
