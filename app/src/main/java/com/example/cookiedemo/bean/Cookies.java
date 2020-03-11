package com.example.cookiedemo.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

import java.math.BigInteger;

/**
 * Create on 2020/3/9 15:44
 * author revstar
 * Email 1967919189@qq.com
 */
@Entity(
        nameInDb = "cookies",
        createInDb = false

)
public class Cookies {
    public String host_key;
    public String name;
    public String value;
    public String path;
    public boolean is_secure;
    public Long expires_utc;





    public Cookies() {
    }

    @Generated(hash = 340821515)
    public Cookies(String host_key, String name, String value, String path,
            boolean is_secure, Long expires_utc) {
        this.host_key = host_key;
        this.name = name;
        this.value = value;
        this.path = path;
        this.is_secure = is_secure;
        this.expires_utc = expires_utc;
    }

    public long getExpires_utc() {
        return expires_utc;
    }

    public void setExpires_utc(long expires_utc) {
        this.expires_utc = expires_utc;
    }

    public String getHost_key() {
        return host_key;
    }

    public void setHost_key(String host_key) {
        this.host_key = host_key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isIs_secure() {
        return is_secure;
    }

    public void setIs_secure(boolean is_secure) {
        this.is_secure = is_secure;
    }


    public boolean getIs_secure() {
        return this.is_secure;
    }

    public void setExpires_utc(Long expires_utc) {
        this.expires_utc = expires_utc;
    }
}
