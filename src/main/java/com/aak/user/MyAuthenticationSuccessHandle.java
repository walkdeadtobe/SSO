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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;

@Component
public class MyAuthenticationSuccessHandle implements AuthenticationSuccessHandler {

    private static final String SECRET_KEY = "secret";
    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ClientDetailRepository clientDetailRepository;

    private RequestCache requestCache= new HttpSessionRequestCache();

    private JdbcClientDetails jdbcClientDetails;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    JdbcClientDetailsService jdbcClientDetailsService;
    JdbcClientDetailsService jdbcClientDetailsService1;

    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    public static MyAuthenticationSuccessHandle tokenUtil;

    public MyAuthenticationSuccessHandle(){

    }
    @PostConstruct
    public void init() {
        tokenUtil = this;
        tokenUtil.jdbcClientDetailsService = this.jdbcClientDetailsService;
    }





    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("登录成功");
        SavedRequest savedrequest = requestCache.getRequest(request,response);
        log.info("-----------before MyAuthenticationSuccessHandle----------");
        log.info("request:"+request.toString());
        log.info("response:"+response.toString());

        if(savedrequest == null){

        }

        response.setContentType("application/json;charset=UTF-8");
        String redirect_url = request.getParameter("redirect_url");
        log.info("redirect_url:"+redirect_url);
        //response.sendRedirect(authUrl);
        //response.getWriter().write(objectMapper.writeValueAsString(authentication));//将java对象转成json字符串写入response，Authtication参数中包含我们的认证信息
        response.setStatus(HttpStatus.OK.value());
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        objectNode.set("status", factory.textNode("success"));

        //更新token
        String token=createToken(authentication);
        //user.setUserName(request.getParameter("username"));
        //user.setPassword(request.getParameter("password"));
        log.info("username:"+request.getParameter("username"));
        /*try {

            //user=userServiceimpl.findUserByUserName(request.getParameter("username"));
           // user.setPassword("root");
            //user.setTkoen(token);
           // userServiceimpl.save(user);


            if (user.equals(null))
            {
                logger.info("null");
                return ;
                //return new UsernameNotFoundException("could not find "+username);
            }
        }catch(Exception e)
        {
            logger.info(e.toString());
            return ;
        }
*/
        objectNode.set("token", factory.textNode(token));
        PrintWriter out = response.getWriter();
        out.write(objectNode.toString());
        response.setHeader("token",token);
        response.setHeader("ApiKey",token);

        out.write(tokenUtil.objectMapper.writeValueAsString(token));
        response.sendRedirect("http://210.14.118.96/");
        out.close();
        log.info("-----------after MyAuthenticationSuccessHandle----------");
        //log.info("request:"+request.toString());
        log.info("response:"+response.toString());

    }

    private String createToken(Authentication authentication) {
        log.info("name:"+authentication.getName());

        //jdbcClientDetailsService= new JdbcClientDetailsService(dataSource);
        /*if(this.jdbcClientDetails==null)
        {
            log.info("null");
            return null;
        }*/
        jdbcClientDetailsService1=new JdbcClientDetailsService(tokenUtil.dataSource);
        //ClientDetails clientDetails=jdbcClientDetailsService1.loadClientByClientId(authentication.getName());
        ClientDetails clientDetails = tokenUtil.jdbcClientDetailsService.loadClientByClientId(authentication.getName());
        //clientDetails = jdbcClientDetails.loadClientByClientId(authentication.getName());
        log.info("authentication.getName():"+authentication.getName());
        TokenRequest tokenRequest=new TokenRequest(MapUtils.EMPTY_SORTED_MAP,authentication.getName(),clientDetails.getScope(),clientDetails.getAuthorizedGrantTypes().toString());
        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);

        OAuth2AccessToken token = tokenUtil.authorizationServerTokenServices.createAccessToken(oAuth2Authentication);
        return token.toString();
    }
    }
