package com.example.cookiedemo.acticity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookiedemo.R;
import com.example.cookiedemo.application.CookieApplication;
import com.example.cookiedemo.bean.Cookies;
import com.example.cookiedemo.bean.ConvertCookies;
import com.example.cookiedemo.greendao.CookiesDao;
import com.example.cookiedemo.utils.Constants;
import com.example.cookiedemo.utils.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetCookiesActivity extends AppCompatActivity {

    private TextView tv_show_cookie;
    private ArrayList<ConvertCookies> mConvertCookiesList;
    private String showCookie;
    private String cookie_dir;
    private File cookiesFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_cookies);
        cookie_dir = getFilesDir().getParent();

        tv_show_cookie = findViewById(R.id.tv_show_cookie);
        tv_show_cookie.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_show_cookie.setText("正在获取Cookie...");
        startThreadGetCookie();

    }

    private void startThreadGetCookie() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("开启线程等待" + Constants.WAITTING_TIME, "秒");
                    Thread.sleep(Constants.WAITTING_TIME);
                    if (cookiesFile != null) {
                        getCookies();
                    } else {
                        getDataBaseFile(cookie_dir, "Cookies");

                    }
                } catch (final InterruptedException | IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("发生异常", e.getMessage() + "");

                        }
                    });
                    e.printStackTrace();
                }

            }
        }).start();

    }


    private void getDataBaseFile(String cookie_dir, String dbName) throws IOException {
        File f = new File(cookie_dir);
        if (!f.exists()) {
            return;
        }
        File[] files = f.listFiles();
        if (files == null) {
            return;
        }
        for (File _file : files) {
            if (_file.isDirectory()) {
                getDataBaseFile(_file.getPath(), dbName);
                Log.d("directory:", _file.getName());
            }
            Log.d("fileName:", _file.getName());

            if (_file.isFile() && _file.getName().equals(dbName)) {
                boolean canRead = _file.canRead();
                int size = (int) _file.length();
                Log.d("文件大小:", size + "");
                cookiesFile = _file;
                try {
                    Log.d("获取cookes数据库成功", "等待" + Constants.WAITTING_TIME + "秒");
                    Thread.sleep(3000);
                    getCookies();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void getCookies() {

        try {
            Log.d("getCookies:", "getCookies");
            Log.d("文件是否可以执行",  cookiesFile.canRead()+"");
            CookiesDao cookiesDao = CookieApplication.getInstance().getDaoSession(cookiesFile).getCookiesDao();
            List<Cookies> cookiesList = cookiesDao.loadAll();
            if (cookiesList == null || cookiesList.size() < 15) {
                startThreadGetCookie();
                return;
            }
            if (mConvertCookiesList == null) {
                mConvertCookiesList = new ArrayList<>();
            }
            mConvertCookiesList.clear();
            for (Cookies cookies : cookiesList) {
                if (cookies != null) {
                    cookies.setIs_secure();
                    ConvertCookies convertCookies = new ConvertCookies(cookies.host_key, cookies.name, cookies.value, cookies.path, cookies.is_secure, cookies.expires_utc);
                    mConvertCookiesList.add(convertCookies);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    showCookie = gson.toJson(mConvertCookiesList);
                    if (showCookie != null && tv_show_cookie != null) {
                        showCookie = GsonUtil.toFormat(showCookie, true, false);
                        tv_show_cookie.setText(showCookie);
                        ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData clipData = ClipData.newPlainText("Label", showCookie);
                            cm.setPrimaryClip(clipData);
                            Toast.makeText(getApplicationContext(), "已复制到剪切板", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            CookieApplication.getInstance().closeDao();

        } catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("发生异常", e.getMessage() + "");
                }
            });
            e.printStackTrace();
        }
    }


}
