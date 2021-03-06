package com.aak.configuration;

import com.aak.domain.*;
import com.aak.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientDetails;

/**
 * Implemention of ClientDetailsService for retrieval of  Client information
 * @author chengr
 * @Time 2019-9-10
 */
public class JdbcClientDetails implements ClientDetailsService
{

    public static  Log log=LogFactory.getLog(JdbcClientDetails.class);

    @Autowired
    private ClientDetailRepository clientDetailRepository;


    @Override
    public ClientDetails loadClientByClientId (String username) throws UsernameNotFoundException {
        log.info("loadClientByClientId:"+username);
        log.info("find_before");
        ClientDetail clientDetail = clientDetailRepository.findByClientId(username);
        log.info("find_after");


        if(clientDetail==null){

            throw new UsernameNotFoundException("User"+username+"can not be found");
        }


        //ClientDetails ClientDetails = new ClientDetails();

        log.info(clientDetail.toString());
        return  clientDetail;


    }
}
