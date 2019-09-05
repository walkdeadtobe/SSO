package com.aak.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


@Component
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private String Max_Attempt="5";

    public static MyAuthenticationFailureHandler tokenUtil;

    @PostConstruct
    public void init() {
        tokenUtil = this;
        tokenUtil.stringRedisTemplate = this.stringRedisTemplate;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException authentication) throws IOException, ServletException {

        logger.info("登录失败");
        //String ip=request.getRemoteAddr();
        String username=request.getParameter("username");
        logger.info(username);
        String auth_id=username;
        try {
            String num_str = tokenUtil.stringRedisTemplate.opsForValue().get(auth_id);
            logger.info("11");
            if(num_str.equals(Max_Attempt)){
                long left=0;
                logger.info("22");
                try {
                    logger.info("44");
                    left=tokenUtil.stringRedisTemplate.getExpire(auth_id)/60;
                    logger.info("55");
                    if(left==-1)
                    {
                        tokenUtil.stringRedisTemplate.expire(auth_id,30, TimeUnit.MINUTES);
                        left=30;
                    }
                    logger.info("66");
                }catch(Exception e){
                    tokenUtil.stringRedisTemplate.expire(auth_id,30, TimeUnit.MINUTES);
                    left=30;
                    logger.info("33");
                }
                logger.info("77");
                if(left==0)
                    left=1;
                logger.info(left+"=left");

                //response.sendError(HttpServletResponse.SC_BAD_REQUEST,"用户名或密码错误，请"+String.valueOf(left)+"分钟后 重新尝试登陆");
                //request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION,"用户名或密码错误，请"+String.valueOf(left)+"分钟后 重新尝试登陆");
                //response.sendRedirect("/login?error=true");
                request.getSession().setAttribute("error","用户名或密码错误次数超过限制，请"+left+"分钟后 重新尝试登陆");
                logger.info("88");
                response.sendRedirect("/login?error=true");
                return;

            }else{
                logger.info("num1:"+num_str);
                tokenUtil.stringRedisTemplate.boundValueOps(auth_id).increment(1);
                tokenUtil.stringRedisTemplate.boundValueOps(auth_id).expire(30, TimeUnit.MINUTES);
                logger.info("num2:"+num_str);
            }
        }catch(Exception e){
            logger.info(e.toString());
            tokenUtil.stringRedisTemplate.opsForValue().set(auth_id,"1");
            tokenUtil.stringRedisTemplate.boundValueOps(auth_id).expire(30, TimeUnit.MINUTES);
        }
        request.getSession().setAttribute("error","用户名或密码错误");
        response.sendRedirect("/login?error=true");
        return;
        //response.setContentType("application/json;charset=UTF-8");
        //response.getWriter().write(objectMapper.writeValueAsString(authentication));//将java对象转成json字符串写入response，Authtication参数中包含我们的认证信息
    }
}
