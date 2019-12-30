package com.aak.user;


import com.aak.AuthorizationServerApplication;
import com.aak.repository.*;
import com.aak.configuration.*;
import com.aak.domain.*;
import com.aak.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.rest.core.util.MapUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.apache.commons.collections4.MapUtils;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;

@Component
public class MyAuthenticationSuccessHandle implements AuthenticationSuccessHandler {

    private Logger log = LoggerFactory.getLogger(getClass());
    private RequestCache requestCache= new HttpSessionRequestCache();

    @Autowired
    JdbcClientDetailsService jdbcClientDetailsService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    Account_LogRepository account_logRepository;

    public static MyAuthenticationSuccessHandle tokenUtil;


    public MyAuthenticationSuccessHandle(){

    }
    @PostConstruct
    public void init() {
        tokenUtil = this;
        tokenUtil.jdbcClientDetailsService = this.jdbcClientDetailsService;
        tokenUtil.stringRedisTemplate=this.stringRedisTemplate;
        tokenUtil.account_logRepository=this.account_logRepository;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("登录成功");
        //登陆成功后，删除错误登陆次数的统计
        //if(request.getParameter("username")!=null) //当能够登录成功说明能登陆，即在 loadusernamepassword 那一部分没有拦截，即 key 已经过期
        //   tokenUtil.stringRedisTemplate.delete(request.getParameter("username"));
        SavedRequest savedrequest = requestCache.getRequest(request,response);
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        objectNode.set("status", factory.textNode("success"));

        //直接设置跳转时，会导致 在使用:http://localhost:8080/oauth/authorize?client_id=test&redirect_uri=http://127.0.0.1/oauth/code&response_type=code&scope=read
        //时，不走redirect_uri=http://127.0.0.1/oauth/code的流程，而直接重定向
        //改为
        if(savedrequest!=null) {
            String before_url = savedrequest.getRedirectUrl();//获取当前请求之前的请求url
            log.info("redurecturl:" + before_url);
            if (!before_url.contains("redirect_uri"))//说明之前的请求是直接打开登录页,而不是authorize，所以跳转到默认页
            {
                response.sendRedirect("http://smart.cast.org.cn/");
                return ;
            }

            //登录处理流程：https://www.cnblogs.com/xifengxiaoma/p/10043173.html
            SavedRequestAwareAuthenticationSuccessHandler s=new SavedRequestAwareAuthenticationSuccessHandler();
            s.onAuthenticationSuccess(request,response,authentication);

        }
        else
        {
            log.info("savedrequest=null)");
            response.sendRedirect("http://smart.cast.org.cn/");
        }

        String system="unknown";
        if(response.getHeader("Location")!=null)
        {
            if(response.getHeader("Location").contains("talent"))
            {
                system="talent";
            }else if(response.getHeader("Location").contains("kexie")){
                system="kexie";
            }
        }
        account_login(authentication,system);
        return;
    }

    public void account_login(Authentication auth,String system){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");//设置日期格式
        System.out.println(df.format(new Date()));// 获取当前系统时间
        Account_Log account_log=new Account_Log(auth.getName(),"login",df.format(new Date()),system);
        tokenUtil.account_logRepository.saveAndFlush(account_log);
    }

}
