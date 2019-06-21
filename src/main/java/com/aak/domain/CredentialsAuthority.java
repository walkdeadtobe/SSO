package com.aak.domain;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
@Entity
@Table(name ="credentials_authorities")
public class CredentialsAuthority implements Serializable {

    @Id
    @Column(name = "credentials_id")
    private Long credentialsid;

    @NotEmpty
    @Column(name = "authorities_id")
    private Long authoritiesid;



    public Long getId() {
        return credentialsid;
    }

    public void setId(Long credentialid) {
        this.credentialsid = credentialid;
    }


    public Long getAuthority() {
        return authoritiesid;
    }

    public void setAuthority(Long authoritieid ){
        this.authoritiesid = authoritieid;
    }

}
