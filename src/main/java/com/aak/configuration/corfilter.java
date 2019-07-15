package com.aak.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class corfilter implements Filter {

    Logger logger= LoggerFactory.getLogger(CorsFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;


        try {
            if("OPTIONS".equalsIgnoreCase(request.getMethod())){
                response.setHeader("Access-Control-Allow-Origin",request.getHeader("origin"));
                //response.setHeader("Access-Control-Allow-Origin","http://210.14.118.96,http://111.203.146.56:8080i");  //允许跨域访问的域
                //response.setHeader("Access-Control-Allow-Origin","http://111.203.146.56:8080");
                response.setHeader("Access-Control-Allow-Methods","POST,GET,OPTIONS,DELETE,PUT");  //允许使用的请求方法
                response.setHeader("Access-Control-Expose-Headers","*");
                response.setHeader("Access-Control-Allow-Headers", "x-requested-with,Cache-Control,Pragma,Content-Type,Authorization");  //允许使用的请求方法
                response.setHeader("Access-Control-Allow-Credentials","true");//是否允许请求带有验证信息
                response.setStatus(HttpServletResponse.SC_OK);
            }else {

                filterChain.doFilter(servletRequest, response);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {

    }
}