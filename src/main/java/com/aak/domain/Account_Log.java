package com.aak.domain;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@IdClass(Account_Log.class)
@Table(name = "account_log")
public class Account_Log implements Serializable {

    @Id
    private String username;

    @Id
    private String timestamp;

    @NotEmpty
    private String type;

    @NotEmpty
    private String from_system;

    public static SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //日期格式


    public Account_Log() {

    }

    public Account_Log(String username, String type, String timestamp, String from_system) {
        this.timestamp = timestamp;
        this.type = type;
        this.username = username;
        this.from_system = from_system;
    }

    public String getUsernanme() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        try {
            return DateFormat.parse(timestamp);
        } catch (ParseException e) {
            return null;
        }

    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSystem() {
        return from_system;
    }

    public void setSystem(String from_system) {
        this.from_system = from_system;
    }


}
