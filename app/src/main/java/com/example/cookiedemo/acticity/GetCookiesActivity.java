package com.example.cookiedemo.acticity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookiedemo.R;
import com.example.cookiedemo.application.CookieApplication;
import com.example.cookiedemo.bean.Cookies;
import com.example.cookiedemo.bean.ConvertCookies;
import com.example.cookiedemo.greendao.CookiesDao;
import com.example.cookiedemo.popup.ShowCookiePopup;
import com.example.cookiedemo.utils.Constants;
import com.example.cookiedemo.utils.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetCookiesActivity extends AppCompatActivity {

    private TextView tv_show_cookie;
    private ArrayList<ConvertCookies> mConvertCookiesList;
    private String showCookie;
    private String cookie_dir;
    private File cookiesFile;
    private String home_url = "https://main.m.taobao.com/";

    private String cookieStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_cookies);
        cookieStr = getIntent().getStringExtra("cookieStr");
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

                if (cookiesFile != null) {
                    getCookies();
                } else {
                    try {
                        getDataBaseFile(cookie_dir, "Cookies");
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendMessage(1, e.getMessage());
                    }

                }


            }
        }).start();

    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {

        private WeakReference<GetCookiesActivity> weakActivity;

        private MyHandler(GetCookiesActivity activity) {
            weakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                weakActivity.get().showTastTips(msg.obj.toString());
            } else if (msg.what == 2) {

                if (weakActivity.get().tv_show_cookie != null) {
                    weakActivity.get().tv_show_cookie.setText(msg.obj.toString());
                }
            }

        }
    }

    public void showTastTips(String tips) {
        if (tips != null) {
            Toast.makeText(this, tips, Toast.LENGTH_SHORT).show();
        }
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
                int size = (int) _file.length();
                Log.d("文件大小:", size + "");
                cookiesFile = _file;
                getCookies();
            }
        }

    }

    private void getCookies() {

        try {
            Log.d("getCookies:", "getCookies");
            Log.d("文件是否可以执行", cookiesFile.canRead() + "");
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
            for (int i = 0; i < mConvertCookiesList.size(); i++) {
                mConvertCookiesList.get(i).setValue("");
            }

            String[] cookieArray = cookieStr.split(";");
            for (String item : cookieArray) {
                if (item != null) {
                    String[] last = item.split("=", 2);
                    String last0 = last[0];
                    String last1 = last[1];
                    last0 = last0.replaceAll("[^\u4e00-\u9fa5a-zA-Z0-9]", "");

                    for (int i = 0; i < mConvertCookiesList.size(); i++) {
                        if (mConvertCookiesList.get(i) != null) {
                            String name = mConvertCookiesList.get(i).getName();
                            name = name.replaceAll("[^\u4e00-\u9fa5a-zA-Z0-9]", "");
                            if (last0.equals(name)) {
                                mConvertCookiesList.get(i).setValue(last1);
                                break;
                            }
                        }
                    }
                }
            }
            //重新处理一遍，去掉value为空的选项
            for (int i = mConvertCookiesList.size() - 1; i >= 0; i--) {
                if (mConvertCookiesList.get(i) != null) {
                    String values = mConvertCookiesList.get(i).getValue();
                    if (values == null || values.equals("")) {
                        mConvertCookiesList.remove(i);
                    }
                }
            }


            Gson gson = new GsonBuilder().serializeNulls().create();
            showCookie = gson.toJson(mConvertCookiesList);
            if (showCookie != null && tv_show_cookie != null) {
                showCookie = GsonUtil.toFormat(showCookie, true, false);
                ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    ClipData clipData = ClipData.newPlainText("Label", showCookie);
                    cm.setPrimaryClip(clipData);
                    sendMessage(2, showCookie);
                    sendMessage(1, "已复制到剪切板");
                }
            }

            CookieApplication.getInstance().closeDao();

        } catch (final Exception e) {
            sendMessage(1, e.getMessage());
            e.printStackTrace();
        }
    }


    private void sendMessage(int what, String obj) {
        if (mHandler != null) {
            Message message = Message.obtain();
            message.what = what;
            message.obj = obj;
            mHandler.sendMessage(message);
        }
    }


}
