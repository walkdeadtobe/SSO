package com.aak.api;

import com.aak.configuration.MyApplication;
import com.aak.domain.*;
import com.aak.repository.Account_LogRepository;
import com.aak.repository.ClientDetailRepository;
import com.aak.repository.CredentialRepository;
import com.aak.utils.AES;
import com.aak.utils.ApplicationSupport;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.LogFactory;
import org.hibernate.hql.internal.ast.tree.AbstractNullnessCheckNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.User;
import org.apache.commons.logging.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Null;
import java.lang.Class;
import java.security.Principal;
import java.util.*;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@RestController
public class OauthController {
    public static Log log= LogFactory.getLog(OauthController.class);
    @Autowired
    CredentialRepository credentialRepositoryy;

    @Autowired
    Account_LogRepository account_logRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    JdbcClientDetailsService jdbcClientDetailsService;

    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private MyApplication myApplication ;

    @RequestMapping("/oauth/check_token")
    public String check_token(@RequestParam(value = "token") String token){
        try{
        TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
        OAuth2AccessToken oAuth2AccessToken=tokenStore.readAccessToken(token);
        OAuth2Authentication oAuth2Authentication;

        oAuth2Authentication = tokenStore.readAuthentication(token);

        if( oAuth2Authentication.getPrincipal().getClass().getName()==User.class.getName())
        {
            User user= (User)oAuth2Authentication.getPrincipal();
            Credentials credentials=credentialRepositoryy.findByName(user.getUsername());

            Collection<GrantedAuthority> authorities=user.getAuthorities();
            Authority authority=(Authority)authorities.iterator().next();

            return "{\"status\":200,\"resource_id\":\""+oAuth2Authentication.getOAuth2Request().getResourceIds().iterator().next()+"\",\"scope\":\""+oAuth2AccessToken.getScope().iterator().next()+"\",\"department\":\""+credentials.getDepartment()+"\",\"Authorities\":\""+authority.getAuthority()+"\",\"PERSON_ID\":\""+user.getUsername()+"\"}";

        }else if (oAuth2Authentication.getUserAuthentication().getCredentials().getClass().getName()==Credentials.class.getName())
        {
            Credentials credentials=(Credentials)oAuth2Authentication.getUserAuthentication().getCredentials();
            List<Authority> authorities=credentials.getAuthorities();
            return "{\"status\":200,\"resource_id\":\""+oAuth2Authentication.getOAuth2Request().getResourceIds().iterator().next()+"\",\"scope\":\""+oAuth2AccessToken.getScope().iterator().next()+"\",\"department\":\""+credentials.getDepartment()+"\",\"Authorities\":\""+authorities.get(0).toString()+"\",\"PERSON_ID\":\""+credentials.getName()+"\"}";

        }else{

        }



        }catch (Exception e){
            log.info(e.toString());
        }
        return "{\"status\":500,\"error\":\"Internal Server Error\",\"message\": \"token expired\"}";

    }
    @RequestMapping(value="/oauth/getAuth",method= RequestMethod.GET)
    static OAuth2Authentication  getAuthenticationInOauth2Server(@RequestParam(value = "token") String token){
        TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(token);
        log.info(oAuth2Authentication.toString());

        return oAuth2Authentication;

    }

    @RequestMapping(value="/oauth/code",method= RequestMethod.GET)
    static void  test(HttpServletRequest request, HttpServletResponse response){
        //log.info("in /oauth/code");
        RequestCache requestCache= new HttpSessionRequestCache();
        Object s1 = request.getSession().getAttribute("refer");
        //SavedRequest savedrequest = requestCache.getRequest(request,response);

        try {
            response.sendRedirect(request.getParameter("back_to")+"?code="+request.getParameter("code")+"&refer="+request.getSession().getAttribute("refer"));
        }catch (Exception e){
            System.out.println(e.toString());
        }

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
    @RequestMapping(value = "/log/account", method = RequestMethod.GET)
    public String get_account_log(@RequestParam(value = "name") String name) {
        List<Account_Log> list_account = account_logRepository.findAccount_LogsByUsername(name);
        Iterator<Account_Log> iterator = list_account.iterator();
        System.out.println(list_account.size());

        JSONObject object = new JSONObject();
        try {
            object.put("username", name);
            ArrayList<JSONObject> log = new ArrayList<JSONObject>();
            if (!list_account.isEmpty()) {
                Account_Log last_item = list_account.get(0);
                for (int i = 1; i < list_account.size(); i++) {
                    Account_Log curr = list_account.get(i);
                    JSONObject t = new JSONObject();
                    if (!curr.getType().equals(last_item.getType()) && curr.getType().equals("logout")) {
                        t.put("login", last_item.getTimestamp());
                        t.put("logout", curr.getTimestamp());
                        log.add(t);
                    } else if (curr.getType().equals(last_item.getType()) && curr.getType().equals("login")) { // 两次连续login
                        long dur = curr.getDate().getTime() - last_item.getDate().getTime();
                        Date mid_date;
                        if (dur > 2 * 60 * 60 * 1000) { // 间隔大于两小时
                            mid_date = new Date(last_item.getDate().getTime() + 2 * 60 * 60 * 1000);
                        } else {
                            mid_date = new Date(last_item.getDate().getTime() + dur / 2);
                        }

                        t.put("login", last_item.getTimestamp());
                        t.put("logout", Account_Log.DateFormat.format(mid_date));
                        log.add(t);
                    }

                    last_item = curr;
                }
            }
            object.put("log", new JSONArray(log));
        } catch (JSONException e) {
            log.info(e.toString());
        }

        return object.toString();
    }


    @RequestMapping(value="/outside/check_uuid",method= RequestMethod.GET)
    public ResponseEntity   check_uuid(@RequestParam(value = "uuid") String uuid){
            if(stringRedisTemplate.opsForValue().get(uuid)!=null){
                return ResponseEntity.ok().build();
            }else{
                return ResponseEntity.status(400).build();
            }
    }

    @RequestMapping(value="/outside/uuid",method= RequestMethod.GET)
    public String   make_uuid(@RequestParam(value = "system") String system,@RequestParam(value = "appId") String appId){
        String url="http://111.203.146.69/kxyj/qcodelogin?appName=talent";
        if(myApplication.getPort()=="80")
            url="http://111.203.146.69/kxyj/qcodelogin?appName=talent";
        else
            url=" http://sso-smart.cast.org.cn:8080/kxyj/qcodelogin?appName=talent";
        String uuid= UUID.randomUUID().toString();
        String appKey= UUID.randomUUID().toString().substring(2,18);

        log.info(uuid);
        try{
            if(appId.equals("kexieyijia")){
                String uuid_value="{\"state\":\"0\",\"appKey\":\""+appKey+"\"}";
                stringRedisTemplate.opsForValue().set(uuid,uuid_value);
                url+="&token="+uuid+"&appKey="+appKey;
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("url",url);
                jsonObject.put("uuid",uuid);
                url=jsonObject.toString();
            }else{

            }
        }catch (Exception e){
            log.info(e.toString());
            return null;
        }
        return url;
    }

    @RequestMapping(value="/outside/confirm_information",method= RequestMethod.GET)
    public String   confirm_login(HttpServletRequest request,@RequestParam(value = "uuid") String uuid){
        String uuid_value,username;
        JSONObject jsonObject;
        Cookie[] cookies=request.getCookies();
        if(stringRedisTemplate.hasKey(uuid)){
            try{
                log.info("hasuuid:"+uuid);
                uuid_value=stringRedisTemplate.opsForValue().get(uuid);
               if( new JSONObject(uuid_value).get("state").equals("0")){
                   return null;
               }
                //stringRedisTemplate.opsForValue().get(uuid);
                jsonObject=new JSONObject(uuid_value);
                username=jsonObject.get("username").toString();
            }catch (Exception e){
                log.info(e.toString());
                return null;
            }

            TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
            Collection<OAuth2AccessToken> C_token=tokenStore.findTokensByClientIdAndUserName("kexieyijia",username);
            log.info("size="+C_token.size());
            if(C_token!=null && C_token.size()>0) {
                Iterator<OAuth2AccessToken> iterator = C_token.iterator();
                String token = iterator.next().toString();
                String from=null;
                String url="http://210.14.118.96/ep/cookie.html";
                if(myApplication.getPort()=="80")
                    url="http://210.14.118.96/ep/cookie.html";
                else
                    url=" http://smart.cast.org.cn/talent/cookie.html";

                for(int i=0;i<cookies.length;i++)
                {
                    if(cookies[i].getName()=="from"){
                        from=cookies[i].getValue();
                    }
                }
                if(from!=null){
                    if(myApplication.getPort()=="80"){
                        //根据from返回 talent
                        if(from=="talent"){
                            //url="http://210.14.118.96/ep";
                        }else{
                            //url="http://210.14.118.96";
                        }

                    }else{
                        //根据from 返回ep
                        if(from=="talent"){
                            //url="http://smart.cast.org.cn/talent";
                        }else{
                            //url="http://smart.cast.org.cn/";
                        }
                    }
                }
                return url+"?token="+token;
            }
        }else{

        }
        return null;
    }

    @RequestMapping(value="/outside/information",method= RequestMethod.POST)
    public ResponseEntity   get_information(@RequestParam(value = "token") String uuid,@RequestParam(value = "ticket") String ticket){
        try {
            //JSONObject json = new JSONObject(body);
            //String uuid=json.get("token").toString();
            //String ticket=json.get("ticket").toString();
            log.info("ticket:"+ticket);
            AES aes=new AES("");
            String user_name="username";
            String uuid_value=stringRedisTemplate.opsForValue().get(uuid);
            JSONObject jsonObject=new JSONObject(uuid_value);
            if( jsonObject!=null) {
                if(aes.Decrypt_now(ticket)!=null) {
                    user_name="kexieyijia_" + aes.Decrypt_now(ticket);
                }
                jsonObject.put("username",user_name);
                jsonObject.put("state","1");
                String appKey=(String)jsonObject.get("appKey");
                aes.Decrypt_now(ticket);
            }
            log.info("jsonObject.toString()"+jsonObject.toString());
            stringRedisTemplate.opsForValue().set(uuid,jsonObject.toString());
            log.info("uuid:"+stringRedisTemplate.opsForValue().get(uuid));


            List<Authority> authorities=new ArrayList<>();

            Authority authority=new Authority();
            authority.setId(new Long(1));
            authority.setAuthority("6");
            authorities.add(authority);

            Credentials principal=new Credentials();
            principal.setName(user_name);
            principal.setPassword(user_name);
            principal.setDepartment("kexieyijia");
            principal.setEnabled(true);
            principal.setId(new Long(1));
            principal.setVersion(1);
            principal.setAuthorities(authorities);

            Authentication  authentication=new UsernamePasswordAuthenticationToken(user_name,principal);

            ClientDetails clientDetails = jdbcClientDetailsService.loadClientByClientId("kexieyijia");
            TokenRequest tokenRequest=new TokenRequest(MapUtils.EMPTY_SORTED_MAP,uuid,clientDetails.getScope(),clientDetails.getAuthorizedGrantTypes().toString());
            OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);
            OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
            OAuth2AccessToken ltoken = authorizationServerTokenServices.createAccessToken(oAuth2Authentication);
            //return token.toString();
            return ResponseEntity.ok().build();

        }catch (Exception e){
            log.info(e.toString());

        }
        return ResponseEntity.status(400).build();



    }





}
