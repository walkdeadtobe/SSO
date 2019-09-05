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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public static MyAuthenticationSuccessHandle tokenUtil;


    public MyAuthenticationSuccessHandle(){

    }
    @PostConstruct
    public void init() {
        tokenUtil = this;
        tokenUtil.jdbcClientDetailsService = this.jdbcClientDetailsService;
        tokenUtil.stringRedisTemplate=this.stringRedisTemplate;
    }





    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("登录成功");
        //登陆成功后，删除错误登陆次数的统计
        //if(request.getParameter("username")!=null) //当能够登录成功说明能登陆，即在 loadusernamepassword 那一部分没有拦截，即 key 已经过期
        //   tokenUtil.stringRedisTemplate.delete(request.getParameter("username"));
        SavedRequest savedrequest = requestCache.getRequest(request,response);
        log.info("-----------before MyAuthenticationSuccessHandle----------");
        //log.info("request:"+request.toString());
        log.info("response:"+response.toString());

        //log.info(response.getHeaderNames().toString());
        //log.info(authentication.getName());


        response.setContentType("application/json;charset=UTF-8");
        //String redirect_uri = request.getParameter("redirect_uri");
        //log.info("redirect_uri:"+redirect_uri);
        //response.sendRedirect(authUrl);
        //response.getWriter().write(objectMapper.writeValueAsString(authentication));//将java对象转成json字符串写入response，Authtication参数中包含我们的认证信息
       // response.setStatus(301);
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode objectNode = factory.objectNode();
        objectNode.set("status", factory.textNode("success"));


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
        /*
        //token
        String token=createToken(authentication);
        //user.setUserName(request.getParameter("username"));
        //user.setPassword(request.getParameter("password"));
        log.info("username:"+request.getParameter("username"));
        objectNode.set("token", factory.textNode(token));
        //PrintWriter out = response.getWriter();
        //out.write(objectNode.toString());
        response.setHeader("token",token);
        response.setHeader("ApiKey",token);
         */

        //out.write(tokenUtil.objectMapper.writeValueAsString(token));

        //response.sendRedirect("http://smart.cast.org.cn/");
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
            //SavedRequestAwareAuthenticationSuccessHandler.


        }

        //out.close();
        Collection<String> res_hea= response.getHeaderNames();
        Iterator<String> it=res_hea.iterator();
        log.info("-----------------start of  header ----------------");
        while(it.hasNext()){
            log.info(it.next());
        }

        log.info("------------------end of header---------------");
        log.info(response.getHeader("Location"));
        log.info("-----------after MyAuthenticationSuccessHandle----------");
        log.info("-----------成功登陆----------");
        return;
        //log.info("request:"+request.toString());
        //log.info("response:"+response.toString());
        //onAuthenticationSuccess(request,response,authentication);

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
