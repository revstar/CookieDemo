package com.example.cookiedemo.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

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
    @Property(nameInDb = "host_key")
    public String host_key;
    @Property(nameInDb = "name")
    public String name;
    @Property(nameInDb = "value")
    public String value;
    @Property(nameInDb = "path")
    public String path;
    @Transient
    public boolean is_secure = false;
    @Property(nameInDb = "expires_utc")
    public Long expires_utc;
@Generated(hash = 1463590952)
public Cookies(String host_key, String name, String value, String path,
        Long expires_utc) {
    this.host_key = host_key;
    this.name = name;
    this.value = value;
    this.path = path;
    this.expires_utc = expires_utc;
}
@Generated(hash = 1310612704)
public Cookies() {
}
public String getHost_key() {
    return this.host_key;
}
public void setHost_key(String host_key) {
    this.host_key = host_key;
}
public String getName() {
    return this.name;
}
public void setName(String name) {
    this.name = name;
}
public String getValue() {
    return this.value;
}
public void setValue(String value) {
    this.value = value;
}
public String getPath() {
    return this.path;
}
public void setPath(String path) {
    this.path = path;
}
public Long getExpires_utc() {
    return this.expires_utc;
}
public void setExpires_utc(Long expires_utc) {
    this.expires_utc = expires_utc;
}

    public void setIs_secure(){
        if (name!=null&&name.equals("_samesite_flag_")){
            this.is_secure=true;
        }
    }
}


