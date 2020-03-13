package com.example.cookiedemo.application;

import android.app.Application;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import com.example.cookiedemo.greendao.DaoMaster;
import com.example.cookiedemo.greendao.DaoSession;
import com.example.cookiedemo.utils.DatabaseContext;

import org.greenrobot.greendao.database.Database;

import java.io.File;

import static com.example.cookiedemo.utils.Constants.TAB_NAME;

/**
 * Create on 2020/3/13 10:13
 * author revstar
 * Email 1967919189@qq.com
 */
public class CookieApplication extends Application {
    private static  CookieApplication app;
   private DaoSession daoSession;
    Database db;
    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
        }

    }
    public DaoSession  getDaoSession(File file){
       if (daoSession==null){
           DaoMaster.DevOpenHelper openHelper=new DaoMaster.DevOpenHelper(new DatabaseContext(this,file),TAB_NAME,null);
           db=openHelper.getReadableDb();
           DaoMaster daoMaster=new DaoMaster(db);
           daoSession=daoMaster.newSession();
       }
       daoSession.clear();
       return daoSession;
    }
    public void  closeDao(){
        if (db!=null){
            db.close();
        }
    }
    public  static  CookieApplication getInstance(){
        return  app;
    }
}
