package com.example.cookiedemo.bean;

import android.util.Log;

import java.math.BigInteger;
import java.util.Date;

/**
 * Create on 2020/3/9 16:38
 * author revstar
 * Email 1967919189@qq.com
 */
public class ConvertCookies {
    public String domain;
    public String name;
    public String value;
    public String path;
    public boolean secure;
    public Long expires;

    public ConvertCookies(String domain, String name, String value, String path, boolean secure, long expires) {
        this.domain = domain;
        this.name = name;
        this.value = value;
        this.path = path;
        this.expires = expires;
        this.secure=secure;
        if (expires==0){
            this.expires=null;
        }else {
            this.expires=  expires/1000000-11644473600L;
        }
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

    public Boolean isSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }

    public Long getExpires() {

        return expires;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }
}
