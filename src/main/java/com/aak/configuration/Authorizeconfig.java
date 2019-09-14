package com.aak.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Configure to filter authorization request,add tags in the session , to remember where the request from
 * @author chengr
 * @Time 2019-9-10
 */

@Component
@WebFilter(urlPatterns={"/oauth/authorize"})
public class Authorizeconfig  implements Filter {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        log.info("--------------------------------------------------------------:w");
        if(servletRequest instanceof HttpServletRequest) {
            //requestWrapper = new RequestWrapper((HttpServletRequest) servletRequest);
            log.info("----------------------------------------------------------:s");
            //String uri=((HttpServletRequest) servletRequest).getRequestURI();  uri不包含query_string
            String query=((HttpServletRequest) servletRequest).getQueryString();
            if(query!=null && query.contains("refer=")) {
                String[] back=query.split("&");
                String refer="";
                log.info("query="+query);
                if (back != null) {
                    for (int i = 0; i < back.length; i++) {
                        if (back[i].contains("refer=")) {

                            log.info("back[i]:" + back[i]);
                            refer = back[i].split("refer=")[1];
                            if (refer != null && !refer.equals(""))
                                ((HttpServletRequest) servletRequest).getSession().setAttribute("refer", refer);
                        }
                    }
                }
            }
        }
        if(requestWrapper == null) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            filterChain.doFilter(requestWrapper, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
