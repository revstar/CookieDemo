package com.example.cookiedemo.acticity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookiedemo.R;
import com.example.cookiedemo.bean.ConvertCookies;
import com.example.cookiedemo.bean.Cookies;
import com.example.cookiedemo.greendao.CookiesDao;
import com.example.cookiedemo.greendao.DaoMaster;
import com.example.cookiedemo.greendao.DaoSession;
import com.example.cookiedemo.popup.ShowCookiePopup;
import com.example.cookiedemo.utils.Constants;
import com.example.cookiedemo.utils.DatabaseContext;
import com.example.cookiedemo.utils.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.IOException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    WebView mWebView;
    Button btn_output;
    TextView tv_show_url;
    private String url = "https://login.m.taobao.com/login.htm";
    private String https_my_tao_bao_url = "https://h5.m.taobao.com/mlapp/mytaobao.html";
    private String http_my_tao_bao_url = "http://h5.m.taobao.com/mlapp/mytaobao.html";
    private String cookie_dir;
    private ArrayList<ConvertCookies> mConvertCookiesList;
    private String showCookie;
    private File cookiesFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cookie_dir = getFilesDir().getParent();
        setContentView(R.layout.activity_main);
        initView();


    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        mWebView = findViewById(R.id.web_view);
        btn_output = findViewById(R.id.btn_output);
        tv_show_url = findViewById(R.id.tv_show_url);
        btn_output.setOnClickListener(this);
        WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDomStorageEnabled(true);
        mWebView.requestFocus();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        tv_show_url.setText(url);
        mWebView.loadUrl(url);
        mWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "点击", Toast.LENGTH_SHORT).show();

            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("重定向:", url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String s) {
                Log.d("url:", s);
                tv_show_url.setText(s);
                flush();
                if (s.equals(http_my_tao_bao_url) || s.equals(https_my_tao_bao_url)) {
                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    String cookieStr=CookieManager.getInstance().getCookie(s);

                    Intent intent=new Intent(MainActivity.this, GetCookiesActivity.class);
                    intent.putExtra("cookieStr",cookieStr);
                    startActivity(intent);
                }
                super.onPageFinished(view, s);

            }

        });


    }


    private void flush() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush();
        } else {

            CookieSyncManager.getInstance().sync();
        }
    }


    @Override
    public void onClick(View v) {

        startThreadGetCookie();

    }

    private void startThreadGetCookie() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (cookiesFile != null) {
                        Thread.sleep(Constants.WAITTING_TIME);
                        getCookies();
                    } else {
                        getDataBaseFile(cookie_dir, "Cookies");

                    }
                } catch (InterruptedException | IOException e) {
                    sendMessage(1, e.getMessage());
                    e.printStackTrace();
                }

            }
        }).start();


    }

    private MyHandler mMyHandler = new MyHandler(this);

    private static class MyHandler extends Handler {


        private final WeakReference<MainActivity> mMainActivityWeakReference;


        private MyHandler(MainActivity activity) {
            this.mMainActivityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mMainActivityWeakReference.get().showTastTips(msg.obj.toString());
            } else if (msg.what == 2) {
                new ShowCookiePopup(mMainActivityWeakReference.get(), msg.obj.toString()).showPopupWindow();
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
            Log.d("===", "f为空");
            return;
        }
        final File[] files = f.listFiles();
        if (files == null) {
            Log.d("===", "files为空");
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
                Log.d("获取数据库成功:", "等待:" + Constants.WAITTING_TIME + "秒");
                getCookies();

            }
        }

    }

    private void getCookies() {

        try {
            Log.d("getCookies:", "getCookies");
            //writeData();
            //String content=getFileContent(new File("/data/data/com.example.cookiedemo/app_webview/data.txt"));
//            DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "Cookies");
            //copyFile("/data/data/com.example.cookiedemo/app_webview/Cookies.db","/data/data/com.example.cookiedemo/databases/Cookies.db");
            //getAllFiles("/data/data/com.example.cookiedemo/app_webview/","db");
            DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(new DatabaseContext(getApplication(), cookiesFile), "Cookies", null);
            Database db = openHelper.getReadableDb();
            DaoMaster daoMaster = new DaoMaster(db);
            DaoSession daoSession = daoMaster.newSession();
            daoSession.clear();

            CookiesDao cookiesDao = daoSession.getCookiesDao();
            List<Cookies> cookiesList = cookiesDao.loadAll();
            if (cookiesList == null || cookiesList.size() < 1) {
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
            db.close();
                Gson gson = new GsonBuilder().serializeNulls().create();
                showCookie = gson.toJson(mConvertCookiesList);
                if (showCookie != null) {
                    showCookie = GsonUtil.toFormat(showCookie, true, false);
                    ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        ClipData clipData = ClipData.newPlainText("Label", showCookie);
                        cm.setPrimaryClip(clipData);
                        sendMessage(2, showCookie);
                        sendMessage(1, "已复制到剪切板");
                    }
            }

        } catch (final Exception e) {
            sendMessage(1, e.getMessage());
            e.printStackTrace();

        }
    }

    private void sendMessage(int what, String obj) {
        if (mMyHandler != null) {
            Message message = Message.obtain();
            message.what = what;
            message.obj = obj;
            mMyHandler.sendMessage(message);
        }
    }
}
