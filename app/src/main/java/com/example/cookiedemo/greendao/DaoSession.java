package com.example.cookiedemo.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.example.cookiedemo.bean.Cookies;

import com.example.cookiedemo.greendao.CookiesDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig cookiesDaoConfig;

    private final CookiesDao cookiesDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        cookiesDaoConfig = daoConfigMap.get(CookiesDao.class).clone();
        cookiesDaoConfig.initIdentityScope(type);

        cookiesDao = new CookiesDao(cookiesDaoConfig, this);

        registerDao(Cookies.class, cookiesDao);
    }
    
    public void clear() {
        cookiesDaoConfig.clearIdentityScope();
    }

    public CookiesDao getCookiesDao() {
        return cookiesDao;
    }

}