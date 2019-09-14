package com.aak.domain;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.*;

//import com.sun.org.glassfish.gmbal.NameValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.core.GrantedAuthority;
@Entity
@Table(name ="oauth_client_details")
public class ClientDetail implements ClientDetails    {
    @Id
    private String clientId;

    //@Autowired
    @NotEmpty
    private String authorities;

    //@Autowired
    @NotEmpty
    private String resource_ids;

    //@Autowired
    @NotEmpty
    private String client_secret;

    //@Autowired
    @NotEmpty
    private String scope;

    //@Autowired
    @NotEmpty
    private String authorized_grant_types;

    //@Autowired
    @NotEmpty
    private String web_server_redirect_uri;

    //@Autowired
    @NotEmpty
    private Integer access_token_validity;

    //@Autowired
    @NotEmpty
    private Integer refresh_token_validity;

    //@Autowired
    @NotEmpty
    private String additional_information;

    //@Autowired
    @NotEmpty
    private String autoapprove;


    public String getClientId(){
        return clientId;
    }

    public Set<String> getResourceIds(){
        Set<String> s=new HashSet<String>();
        s.add(resource_ids);
        return s;
    }


    public String getClientSecret(){
        return client_secret;
    }

    public Set<String> getScope(){
        Set<String> s=new HashSet<String>();
        s.add(scope);
        return s;
    }

    public Set<String> getAuthorizedGrantTypes(){
        Set<String> s=new HashSet<String>();
        s.add(authorized_grant_types);
        return s;
    }

    public Set<String> getRegisteredRedirectUri(){
        Set<String> s=new HashSet<String>();
        s.add(web_server_redirect_uri);
        return s;
    }



    public Integer getAccessTokenValiditySeconds(){
        return  access_token_validity;
    }

    public Integer getRefreshTokenValiditySeconds(){
        return refresh_token_validity;
    }

    public boolean isSecretRequired(){
        return false;
    }

    public boolean isScoped(){
        return true;
    }

    public boolean isAutoApprove(String s){
        return true;
    }

    public Map<String,Object> getAdditionalInformation(){
        return null;
    }

    public Collection<GrantedAuthority> getAuthorities(){
        return null;
    }

}
