package com.aak.api;
import com.aak.configuration.IpConfiguration;
import com.aak.domain.Account_Log;
import com.aak.repository.Account_LogRepository;
import com.aak.utils.MyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
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
import java.awt.geom.IllegalPathStateException;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import java.util.Date;
import java.text.SimpleDateFormat;

@Controller
public class LoginController {
    @Autowired
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    private ApprovalStore approvalStore;

    @Autowired
    Account_LogRepository account_logRepository;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    IpConfiguration ipConfiguration;

    private Logger log = LoggerFactory.getLogger(getClass());


    @RequestMapping("/")
    public ModelAndView root(Map<String,Object> model, Principal principal,HttpServletRequest request,HttpServletResponse response){
        return new ModelAndView ("index",model);
    }


    @RequestMapping(value="/approval/revoke",method= RequestMethod.POST)
    public String revokApproval(@ModelAttribute Approval approval){

        approvalStore.revokeApprovals(asList(approval));
        tokenStore.findTokensByClientIdAndUserName(approval.getClientId(),approval.getUserId())
                .forEach(tokenStore::removeAccessToken) ;
        return "redirect:/";
    }

    /**
     * 当访问 /login 页面时，需要根据各种情况做相应处理
     * 具体包括：
     *          1.对 /login?error=true 情况，需提取错误信息并返回；
     *          2.对于 /login 判断是否是否从 /oauth/authorize 跳转过来的，根据相关信息，判断请求源自的系统，并返回给前端；
     *          3.单纯的访问login，根据已有的 system 信息，自动跳转至 /oauth/authorize，执行oauth2 相关流程；
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/login")
    public ModelAndView loginPage(HttpServletRequest request, HttpServletResponse response) {
        RequestCache requestCache= new HttpSessionRequestCache();
        SavedRequest savedrequest = requestCache.getRequest(request,response);
        ModelAndView model= new ModelAndView("login");

        //对于 /login?error=true 页面 应该返回考虑可能返回的错误提示
        if(request.getQueryString()!=null&&request.getQueryString().contains("error=true")&&request.getSession().getAttribute("error")!=null)
        {
            log.info(request.getQueryString());
            model.addObject("error",request.getSession().getAttribute("error"));
            request.getSession().removeAttribute("error");
            return model;
        }
        MyUtils myutils = new MyUtils();
        JSONObject jsonObject;
        //判断是否是从logout重定向而来
        if(savedrequest!=null&&savedrequest.getRedirectUrl()!=null&&myutils.get_refer(savedrequest.getRedirectUrl())!=null){
            jsonObject= myutils.get_refer(savedrequest.getRedirectUrl());
            try {
                model.addObject("from", jsonObject.get("client_id"));
                request.getSession().setAttribute("refer", jsonObject.get("refer"));
            }catch (JSONException e){
                log.error(e.toString());
            }
            }else{
                String redirect=null;
                jsonObject = myutils.cookie_2_json(request.getCookies());

                if(jsonObject.has("from")){
                    try{
                        String clientId = jsonObject.getString("from");
                        redirect = myutils.get_model_redirect(clientId);
                    }catch (JSONException e){
                        log.error(e.toString());
                    }
                }

                if(redirect != null){
                    return new ModelAndView("redirect:"+redirect);
                }else {
                    model.addObject("from", "all");
                }
            }
        return model;
    }

    /**
     * 背景：在从A系统登出后，会重新跳转到登陆页面
     * 需求：此时，输入 用户名 与 密码 即可 登陆 之前登出的系统
     * 思路：前后端配合，前端跳转页面时，要求指明 from 哪个系统，在这里做处理，根据一直来源的 '系统' 信息 完成 Oauth登陆 前几部分，只需输入用户名、密码即可
     * 吐槽：在实际用户使用中，个人觉得没有这个需求；主要需求来源于 切换账户登陆方便（演示权限 需要切换不同账户）
     * @author  chengrui
     * @Time 2019-12-18
     * @param request
     * @param response
     * @return
     *
     */
    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public ModelAndView logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String query=request.getQueryString();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
            account_logout(auth,query);
        }
        MyUtils myUtils = new MyUtils();
        if(query!=null&&query.contains("from="))
        {
            JSONObject jsonObject=myUtils.str_2_json(query);

            if(jsonObject.has("from")){
                try{
                    String redirect = myUtils.get_model_redirect(jsonObject.getString("from"));
                    return new ModelAndView("redirect:"+redirect);
                }catch (JSONException e) {
                    log.error(e.toString());
                    return new ModelAndView("redirect:/login?logout");
                }
            }
        }
        return new ModelAndView("redirect:/login?logout");
    }

    /**
     * 按照格式记录用户的登陆登出操作，用作后续分析
     * @author chengrui
     * @Time 2019-12-18
     * @param auth
     * @param query
     */
    public void account_logout(Authentication auth,String query){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        String system="unknown";
        MyUtils myUtils = new MyUtils();

        if(query!=null){
           JSONObject jsonObject = myUtils.str_2_json(query);
           try{
               system = jsonObject.getString("from");
           }catch (JSONException e){
               log.error(e.toString());
           }
        }
        Account_Log account_log=new Account_Log(auth.getName(),"logout",df.format(new Date()),system);
        account_logRepository.saveAndFlush(account_log);
    }

    public void account_list(){
        List<Account_Log> list_account=account_logRepository.findAccount_LogsByUsername("root");
        Iterator<Account_Log> iterator=list_account.iterator();
        System.out.println(list_account.size());
        while(iterator.hasNext()){
            Account_Log account_log=iterator.next();
            System.out.println(account_log.getTimestamp());
        }

    }
}
