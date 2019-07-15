package com.aak.api;

import com.aak.domain.*;
import com.aak.repository.ClientDetailRepository;
import com.aak.repository.CredentialRepository;
import com.aak.utils.ApplicationSupport;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.userdetails.User;
import org.apache.commons.logging.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.Class;
import java.util.Collection;
import java.util.List;

@RestController
public class OauthController {
    public static Log log= LogFactory.getLog(OauthController.class);
    @Autowired
    CredentialRepository credentialRepositoryy;

    @RequestMapping("/oauth/check_token")
    public String check_token(@RequestParam(value = "token") String token){
        try{
        TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
        OAuth2AccessToken oAuth2AccessToken=tokenStore.readAccessToken(token);
        OAuth2Authentication oAuth2Authentication;

        oAuth2Authentication = tokenStore.readAuthentication(token);

        User user= (User)oAuth2Authentication.getUserAuthentication().getPrincipal();
        oAuth2Authentication.getUserAuthentication().getPrincipal();

        Credentials credentials=credentialRepositoryy.findByName(user.getUsername());


        log.info("user.getUsername():"+user.getUsername());
        log.info("oAuth2AccessToken.toString():"+user.getPassword());
        Collection<GrantedAuthority> authorities=user.getAuthorities();
        Authority authority=(Authority)authorities.iterator().next();
        log.info("auth:"+authority.getAuthority());
        log.info(authorities.getClass().toString());
        //Collection<Authority> back=(Collection<Authority>)authorities;
        log.info("(user.getAuthorities().toString())):"+user.getAuthorities().toString());
        log.info("oAuth2AccessToken.toString():"+oAuth2AccessToken.toString());
        log.info("oAuth2AccessToken.getValue()"+oAuth2AccessToken.getValue());
        log.info(oAuth2AccessToken.getAdditionalInformation());
        log.info(oAuth2AccessToken.getRefreshToken());

        log.info("oAuth2Authentication.getCredentials().toString():"+oAuth2Authentication.getCredentials().toString());
        log.info("oAuth2Authentication.getUserAuthentication().toString():"+oAuth2Authentication.getUserAuthentication().getPrincipal());
        log.info(oAuth2Authentication.getOAuth2Request().getAuthorities());
        log.info(oAuth2Authentication.getOAuth2Request().getGrantType());

        log.info(oAuth2Authentication.getOAuth2Request().getResourceIds().iterator().next());
        return "{\"status\":200,\"resource_id\":\""+oAuth2Authentication.getOAuth2Request().getResourceIds().iterator().next()+"\",\"scope\":\""+oAuth2AccessToken.getScope().iterator().next()+"\",\"department\":\""+credentials.getDepartment()+"\",\"Authorities\":\""+authority.getAuthority()+"\",\"PERSON_ID\":\""+user.getUsername()+"\"}";

        }catch (Exception e){
            log.info(e.toString());
            return "{\"status\":500,\"error\":\"Internal Server Error\",\"message\": \"token expired\"}";
        }

    }
    @RequestMapping(value="/oauth/getAuth",method= RequestMethod.GET)
    static OAuth2Authentication  getAuthenticationInOauth2Server(@RequestParam(value = "token") String token){
        TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(token);
        log.info(oAuth2Authentication.toString());

        return oAuth2Authentication;

    }

    //public void   logout(HttpServletRequest request, HttpServletResponse response){
    @RequestMapping(value="/oauth/revoke_token",method= RequestMethod.GET)
    public ResponseEntity   logout(@RequestParam(value = "token") String token){
       try {
           //String token=request.getHeader("token");
           TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
           OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
           tokenStore.removeAccessToken(accessToken);
           log.info("remove ok");
           return ResponseEntity.ok().build();

           //(new HttpServletResponse()).setStatus(200);
           //return;
       }catch(Exception e){
           System.out.println(e);
           log.info("remove failure");
           return ResponseEntity.ok().header("error",e.toString()).build();
           //response.addHeader("error",e.toString());
           //response.setStatus(400);
           //return;

       }


    }




}
