package com.aak.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.management.*;
import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Set;

/**
 * Common useful function
 */
@Component
public class MyUtils {
    public int port;
    private String client_id;
    private String web_server_redirect_uri;
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private JdbcClientDetailsService jdbcClientDetailsService;

    public  static  MyUtils myUtils;

    public MyUtils(){
    }
    @PostConstruct
    public void init() {
        myUtils = this;
        myUtils.jdbcClientDetailsService=this.jdbcClientDetailsService;
    }
    public static String passwordEncoder(String password){

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(password);

        return encodedPassword;
    }

    public JSONObject str_2_json(String str){
        if(str.contains("?")){
            str = str.split("\\?",2)[1];
        }
        String[] collection;
        String key,value;
        JSONObject object = new JSONObject();
        collection  = str.split("&");
        try {

            for(int i = 0; i < collection.length; i++){
                String[] collection1=null;
                if(collection[i].contains("=")){
                    collection1 = collection[i].split("=",2);
                    key = collection1[0];
                    value = collection1[1];
                    object.put(key, value);
                }
            }
        }catch (JSONException e){
            log.error(e.toString());
        }catch (Exception e){
            log.error(e.toString());
        }
        return object;
    }

    public JSONObject cookie_2_json(Cookie[] cookies){
        JSONObject object = new JSONObject();
        try{
            if (cookies != null) {
                for(int i = 0; i < cookies.length; i++) {
                    object.put(cookies[i].getName(),cookies[i].getValue());
                }
            }
        }catch (JSONException e){
            log.error(e.toString());
        }catch (Exception e){
            log.error(e.toString());
        }
        return object;
    }
    public JSONObject get_refer(String url){
        if(!url.matches(".{1,100}/oauth/authorize\\?client_id=(talent|kexie)&redirect_uri=/oauth/code\\?back_to=http://(210.14.118.96|smart.cast.org.cn)/(ep|talent)/(cookie_talent|cookie).html&response_type=code&scope=read&refer=.{0,100}"))
            return null;
        return str_2_json(url);
    }


    public String get_model_redirect(String clientId) {
        String model_redirect = null;
        log.info("get_model_redirect clientId:"+clientId);
        if(myUtils.jdbcClientDetailsService==null){
            log.info("myUtils.jdbcClientDetailsService==null");
        }
        if (clientId != null) {
            ClientDetails clientDetails = myUtils.jdbcClientDetailsService.loadClientByClientId(clientId);
            web_server_redirect_uri = clientDetails.getRegisteredRedirectUri().iterator().next();
            model_redirect = "/oauth/authorize?client_id=" + clientId + "&redirect_uri=" + web_server_redirect_uri + "&response_type=code&scope=read&refer=null";
            log.info("scope:"+clientDetails.getScope().iterator().next());
        }

        /*JSONObject object = new JSONObject();
        try {
            object.put("backUrl", backUrl);
            object.put("urltoken", web_server_redirect_uri.split("/oauth/code?back_to=")[1]);
        } catch (JSONException e) {
            log.error(e.toString());
        }*/
        return  model_redirect;
    }

    public String get_redirect(String clientId) {
        log.info("get_redirect clientId:"+clientId);
        if(myUtils.jdbcClientDetailsService==null){
            log.info("get_redirect myUtils.jdbcClientDetailsService==null");
        }
        if (clientId != null) {
            ClientDetails clientDetails = myUtils.jdbcClientDetailsService.loadClientByClientId(clientId);
            web_server_redirect_uri = clientDetails.getRegisteredRedirectUri().iterator().next();
            log.info("scope:"+clientDetails.getScope().iterator().next());
            return web_server_redirect_uri.split("/oauth/code\\?back_to=")[1];
        }
        return  null;
    }
}
