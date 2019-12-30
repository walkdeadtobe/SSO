package com.aak.api;

import com.aak.configuration.IpConfiguration;
import com.aak.configuration.JdbcUserDetails;
import com.aak.domain.*;
import com.aak.repository.Account_LogRepository;
import com.aak.repository.CredentialRepository;
import com.aak.utils.AES;
import com.aak.utils.ApplicationSupport;
import com.aak.utils.MyUtils;
import com.aak.utils.SynUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.User;
import org.apache.commons.logging.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.springframework.data.redis.core.StringRedisTemplate;

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
    private JdbcClientDetailsService jdbcClientDetailsService;

    @Autowired
    private JdbcUserDetails jdbcUserDetails;

    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;

    @Autowired
    private IpConfiguration ipConfiguration;

    @SuppressWarnings("unchecked")
    @RequestMapping("/oauth/check_token")
    public String check_token(@RequestParam(value = "token") String token){
        try{
        TokenStore tokenStore = (TokenStore) ApplicationSupport.getBean("tokenStore");
        OAuth2AccessToken oAuth2AccessToken=tokenStore.readAccessToken(token);
        OAuth2Authentication oAuth2Authentication;

        oAuth2Authentication = tokenStore.readAuthentication(token);

        User user= (User)oAuth2Authentication.getPrincipal();
        Credentials credentials=credentialRepositoryy.findByName(user.getUsername());
        Collection<GrantedAuthority> authorities=user.getAuthorities();
        Authority authority=(Authority)authorities.iterator().next();

        return "{\"status\":200,\"resource_id\":\""+oAuth2Authentication.getOAuth2Request().getResourceIds().iterator().next()+"\",\"scope\":\""+oAuth2AccessToken.getScope().iterator().next()+"\",\"department\":\""+credentials.getDepartment()+"\",\"Authorities\":\""+authority.getAuthority()+"\",\"PERSON_ID\":\""+user.getUsername()+"\"}";

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

    /**
     * 按照 oauth2 的登陆验证流程，验证 用户名 和 密码 正确之后，会直接跳转回之前约定的网址，在此过程，我们添加一一些自己的东西
     * 所以 之前约定为 本地的网址，即/oauth/code ，之后可以在本函数中添加相关处理，并执行后续的流程
     * @param request
     * @param response
     */
    @RequestMapping(value="/oauth/code",method= RequestMethod.GET)
    static void  middle(HttpServletRequest request, HttpServletResponse response){
        Object s1 = request.getSession().getAttribute("refer");
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
       }catch(Exception e){
           System.out.println(e);
           log.info("remove failure");
           return ResponseEntity.status(400).header("error",e.toString()).build();
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
    public ResponseEntity   check_uuid_v1(@RequestParam(value = "uuid") String uuid){
        if(stringRedisTemplate.opsForValue().get(uuid)!=null){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(400).build();
        }
    }

    @RequestMapping(value="/outside/authenticate",method= RequestMethod.POST)
    public ResponseEntity   check_uuid(@RequestBody String json){
        String AppKey="123";///"de1a98dcf5a32ff7fddfbcfb795c518a";
        JSONObject app;
        try {
            app = new JSONObject(json);
            String AppSecret = SynUtil.AESDecode(AppKey,app.getString("appSecret"));
            String AuthCode = SynUtil.AESDecode(AppKey,app.getString("authCode"));
            log.info(AuthCode+" "+AppSecret);
        }catch(Exception e){
            log.info(e.toString());
            return ResponseEntity.status(500).body("{\"resultCode\":0,\"resultMessage\":\"格式错误\"}");
        }
        try{
            JSONObject back = new JSONObject();
            back.put("resultCode",1);
            back.put("resultMessage","验证正确");
            return ResponseEntity.status(200).body(back.toString(1));
        }catch(JSONException e){
            log.info(e.toString());
            return ResponseEntity.status(500).body("系统错误");
        }

    }

    @RequestMapping(value="/outside/uuid",method= RequestMethod.GET)
    public String   make_uuid(@RequestParam(value = "system") String system,@RequestParam(value = "appId") String appId){
        String url="http://111.203.146.69/kxyj/qcodelogin?appName=talent&appID="+appId;
        if(ipConfiguration.getPort()==80)
            url="http://111.203.146.69/kxyj/qcodelogin?appName=talent&appID="+appId;
        else
            url="http://sso-smart.cast.org.cn:8080/kxyj/qcodelogin?appName=talent&appID="+appId;
        String uuid= UUID.randomUUID().toString();
        log.info(uuid);
        try{
            if(appId.equals("kexieyijia")){
                String uuid_value="{\"state\":\"0\"}";
                stringRedisTemplate.opsForValue().set(uuid,uuid_value);
                url+="&authCode="+uuid+"&token="+uuid;
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
        MyUtils myUtils = new MyUtils();
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
            log.info("size="+C_token.size()+";username="+username);
            if(C_token!=null && C_token.size()>0) {
                Iterator<OAuth2AccessToken> iterator = C_token.iterator();
                String token = iterator.next().toString();
                String from=null;
                String url;///="http://210.14.118.96/ep/cookie.html";

                JSONObject jsonObject1 = myUtils.cookie_2_json(cookies);
                if(jsonObject1.has("from")){
                    try{
                        from = jsonObject1.getString("from");
                    }catch(JSONException e){
                        log.error(e.toString());
                    }
                }
                url = myUtils.get_redirect(from);
                return url+"?token="+token;
            }
        }else{

        }
        return null;
    }

    public void create_token(String user_name,String clientId){
        List<Authority> authorities=new ArrayList<>();
        Authority authority=new Authority();
        authority.setId(new Long(1));
        authority.setAuthority("6");
        authorities.add(authority);

        User user = new User(user_name,user_name,authorities);
        Authentication  authentication=new UsernamePasswordAuthenticationToken(user,null);

        ClientDetails clientDetails = jdbcClientDetailsService.loadClientByClientId(clientId);
        TokenRequest tokenRequest=new TokenRequest(MapUtils.EMPTY_SORTED_MAP,clientId,clientDetails.getScope(),clientDetails.getAuthorizedGrantTypes().toString());
        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
        authorizationServerTokenServices.createAccessToken(oAuth2Authentication);
    }

    /**
     * 两个版本并行存在，因而要做一些判断
     * 版本v1:参数token，ticket
     * 版本v2:参数json
     * @param token
     * @param ticket
     * @param json
     * @return
     */
    @RequestMapping(value="/outside/information",method= RequestMethod.POST)
    public ResponseEntity   get_information(@RequestParam(value ="token" ,required = false)String token,@RequestParam(value ="ticket" ,required = false)String ticket,@RequestBody(required = false) String json){
        try {
            String AppKey="de1a98dcf5a32ff7fddfbcfb795c518a";
            String user_name="username";
            String uuid=null;
            if(token != null){
                uuid=token;
                AES aes=new AES("");
                if(aes.Decrypt_now(ticket)!=null) {
                    user_name="kexieyijia_" + aes.Decrypt_now(ticket);
                }
                String uuid_value=stringRedisTemplate.opsForValue().get(uuid);
                JSONObject jsonObject=new JSONObject(uuid_value);
                if(jsonObject != null){
                    log.info("jsonObject_username:"+user_name);
                    jsonObject.put("username",user_name);
                    jsonObject.put("state","1");
                }
                stringRedisTemplate.opsForValue().set(uuid,jsonObject.toString());
            }else {
                SynUtil synUtil = new SynUtil();
                JSONObject json1 = new JSONObject(json);
                String authCode=json1.getString("authCode");
                String information1=json1.getString("information");
                uuid = authCode;
                JSONObject information = new JSONObject(information1);
                String uuid_value = stringRedisTemplate.opsForValue().get(uuid);
                JSONObject jsonObject = new JSONObject(uuid_value);
                if (jsonObject != null) {
                    user_name = synUtil.AESDecode(AppKey,information.getString("name"));
                    String duty = synUtil.AESDecode(AppKey,information.getString("position"));
                    user_name = "kexieyijia_" +  user_name ;
                    jsonObject.put("username", user_name);
                    jsonObject.put("state", "1");
                }
                stringRedisTemplate.opsForValue().set(uuid,jsonObject.toString());
            }

            if(jdbcUserDetails.loadUserByUsername(user_name)==null)
                jdbcUserDetails.addUser(user_name,user_name,"DEPART",6);
            create_token(user_name,"kexieyijia");

            return ResponseEntity.ok().build();

        }catch(JSONException e){
            log.info(e.toString());
            return ResponseEntity.status(500).body("{\"resultCode\":0,\"resultMessage\":\"格式错误\"}");
        }catch(Exception e){
            log.info(e.toString());
            return ResponseEntity.status(400).build();

        }finally {

        }
    }
}
