package com.aak.api;

import com.aak.configuration.MyApplication;
import com.aak.configuration.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.approval.Approval;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.Arrays.asList;

@Controller
public class LoginController {
    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private ApprovalStore approvalStore;

    @Autowired
    private MyApplication myApplication ;

    private Logger log = LoggerFactory.getLogger(getClass());
    /*@RequestMapping(value = "/*",method = RequestMethod.OPTIONS)
    public ResponseEntity handleOptions(){
        return ResponseEntity.noContent().header("Access-Control-Allow-Origin","http://111.203.146.56:8080")
                .allow(HttpMethod.GET,HttpMethod.POST,HttpMethod.PUT).build();
    }*/
    @RequestMapping("/")
    public ModelAndView root(Map<String,Object> model, Principal principal,HttpServletRequest request,HttpServletResponse response){

       /* List<Approval> approvals=clientDetailsService.listClientDetails().stream()
                .map(clientDetails -> approvalStore.getApprovals(principal.getName(),clientDetails.getClientId()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
       */
        /*ModelAndView model_back = new ModelAndView("login");
        String query=request.getQueryString();
        if(query!=null&query.contains("login?logout=")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            if(query.contains("talent"))
                model_back.addObject("from","talent");
            else if(query.contains("kexie"))
                model_back.addObject("from","keixe");
            else
                model_back.addObject("from","all");
            log.info("query:"+query);
            return model_back;
            //return new ModelAndView("redirect:/login?logout",model);
        }*/
        //model.put("approvals",approvals);
        //model.put("clientDetails",clientDetailsService.listClientDetails());
        return new ModelAndView ("index",model);
        //return "index";
    }

    @Autowired
    private TokenStore tokenStore;

    @RequestMapping(value="/approval/revoke",method= RequestMethod.POST)
    public String revokApproval(@ModelAttribute Approval approval){

        approvalStore.revokeApprovals(asList(approval));
        tokenStore.findTokensByClientIdAndUserName(approval.getClientId(),approval.getUserId())
                .forEach(tokenStore::removeAccessToken) ;
        return "redirect:/";
    }

    @RequestMapping("/login")
    public ModelAndView loginPage(HttpServletRequest request, HttpServletResponse response) {
        RequestCache requestCache= new HttpSessionRequestCache();
        SavedRequest savedrequest = requestCache.getRequest(request,response);
        ModelAndView model= new ModelAndView("login");
        //log.info(request.getCookies());
        //判断是否是从logout重定向而来


        if(savedrequest!=null&&savedrequest.getRedirectUrl()!=null&&savedrequest.getRedirectUrl().matches(".{1,100}/oauth/authorize\\?client_id=(talent|kexie)&redirect_uri=/oauth/code\\?back_to=http://(210.14.118.96|smart.cast.org.cn)/(ep|talent)/(cookie_talent|cookie).html&response_type=code&scope=read&refer=http://(210.14.118.96|smart.cast.org.cn).{1,100}")){
                log.info("bbb");
                //log.info(savedrequest.getRedirectUrl());
                String url=savedrequest.getRedirectUrl();
                //url.matches("http://(\d{1,3}.){3}\d{1,3}(/api/(dxs|dkp|dzk|ddj|kj|kejie|qichuang|talent|zhiku)")
                //if(url.split("redirect_uri"))
                log.info("getRedirectUrl:"+url);
                String[] back=url.split("&");
                if(back!=null) {
                    for (int i = 0; i < back.length; i++) {
                        if (back[i].contains("refer="))
                            request.getSession().setAttribute("refer", back[i].split("refer=")[1]);
                    }
                }
                if(savedrequest.getRedirectUrl().contains("/cookie.html")){
                    model.addObject("from", "kexie");
                }else if(savedrequest.getRedirectUrl().contains("/cookie_talent.html")) {
                    model.addObject("from", "talent");
                }else{
                    model.addObject("from", "all");
                }
            }else{
                log.info("ccc");
                Cookie[] c=request.getCookies();
                if(c!=null)
                {
                    for(int i=0;i<c.length;i++)
                    {
                        if(c[i].getName().equals("from")){
                            String port=myApplication.getPort();
                            log.info("port:"+port);
                            log.info("from:"+c[i].getValue());
                            if(c[i].getValue().equals("talent")){
                                if(port.equals("80"))
                                    return new ModelAndView("redirect:/oauth/authorize?client_id=talent&redirect_uri=/oauth/code?back_to=http://210.14.118.96/ep/cookie_talent.html&response_type=code&scope=read");
                                else
                                    return new ModelAndView("redirect:/oauth/authorize?client_id=talent&redirect_uri=/oauth/code?back_to=http://smart.cast.org.cn/talent/cookie_talent.html&response_type=code&scope=read");
                            }else if(c[i].getValue().equals("kexie")){
                                if(port.equals("80"))
                                    return new ModelAndView("redirect:/oauth/authorize?client_id=kexie&redirect_uri=/oauth/code?back_to=http://210.14.118.96/ep/cookie.html&response_type=code&scope=read");
                                else
                                    return new ModelAndView("redirect:/oauth/authorize?client_id=kexie&redirect_uri=/oauth/code?back_to=http://smart.cast.org.cn/talent/cookie.html&response_type=code&scope=read");
                            }else{
                                //
                            }
                        }
                    }
                }

                //log.info("null");
                model.addObject("from", "all");
            }




        //log.info("redirect_url:"+savedrequest.getRedirectUrl());
        return model;
        //return "login";
    }



    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public ModelAndView logoutPage (HttpServletRequest request, HttpServletResponse response, RedirectAttributes attributes) {


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        String query=request.getQueryString();
        if(query!=null&&query.contains("from="))
        {
            String port=myApplication.getPort();
            log.info(query);
            if(query.contains("talent")){
                //attributes.addFlashAttribute("logout_from","talent");
                if(port.equals("80"))
                    return new ModelAndView("redirect:/oauth/authorize?client_id=talent&redirect_uri=http://210.14.118.96/ep/cookie_talent.html&response_type=code&scope=read");
                else
                    return new ModelAndView("redirect:/oauth/authorize?client_id=talent&redirect_uri=http://smart.cast.org.cn/talent/cookie_talent.html&response_type=code&scope=read");
            }else if(query.contains("kexie")){
                //attributes.addFlashAttribute("logout_from","kexie");
                if(port.equals("80"))
                    return new ModelAndView("redirect:/oauth/authorize?client_id=kexie&redirect_uri=http://210.14.118.96/ep/cookie.html&response_type=code&scope=read");
                else
                    return new ModelAndView("redirect:/oauth/authorize?client_id=kexie&redirect_uri=http://smart.cast.org.cn/talent/cookie.html&response_type=code&scope=read");
            }
        }
        return new ModelAndView("redirect:/login?logout");
    }
}
