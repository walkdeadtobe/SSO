package com.aak.domain;

import jdk.nashorn.internal.objects.annotations.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import  lombok.*;

@Entity
@Table(name ="credentials")
public class Credentials implements Serializable {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Version
    private Integer version;

    @NotEmpty
    private String name;

    @NotEmpty
    private String password;

    @NotEmpty
    private String department;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Authority> authorities;

    private boolean enabled;

    public Credentials() {
    }

    public Credentials(Long id, Integer version, @NotEmpty String name, @NotEmpty String password, @NotEmpty String department, List<Authority> authorities, boolean enabled) {
        this.id=id;
        this.version = version;
        this.name = name;
        this.password = password;
        this.department = department;
        this.authorities = authorities;
        this.enabled = enabled;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
