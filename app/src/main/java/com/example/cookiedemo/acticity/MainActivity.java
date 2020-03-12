package com.example.cookiedemo.acticity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.cookiedemo.utils.FileUtils;
import com.example.cookiedemo.utils.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    WebView mWebView;
    Button btn_output;
    TextView tv_show_url;
    private String url = "https://login.m.taobao.com/login.htm";
    private String home_url = "https://main.m.taobao.com/";
    private String https_my_tao_bao_url = "https://h5.m.taobao.com/mlapp/mytaobao.html";
    private String http_my_tao_bao_url = "http://h5.m.taobao.com/mlapp/mytaobao.html";
    private String cookie_dir;
    private ArrayList<ConvertCookies> mConvertCookiesList;
    private String showCookie;
    private boolean isSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cookie_dir = File.separator + "data" + File.separator + "data" + File.separator + getPackageName() + File.separator + "app_webview";
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

                Toast.makeText(getApplicationContext(), "重定向:" + url, Toast.LENGTH_SHORT).show();
                Log.d("重定向:", url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String s) {
                Log.d("url:", s);
                tv_show_url.setText(s);
                Toast.makeText(getApplicationContext(), "可以获取了:" + s, Toast.LENGTH_SHORT).show();
                if (s.equals(http_my_tao_bao_url) || s.equals(https_my_tao_bao_url)) {
                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, GetCookiesActivity.class));
                }
                super.onPageFinished(view, s);

            }

        });
    }

    /**
     * 将cookie同步到WebView
     *
     * @param url    WebView要加载的url
     * @param cookie 要同步的cookie
     * @return true 同步cookie成功，false同步cookie失败
     * @Author JPH
     */
    public boolean syncCookie(String url, String cookie) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(getApplicationContext());
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, cookie);//如果没有特殊需求，这里只需要将session id以"key=value"形式作为cookie即可
        String newCookie = cookieManager.getCookie(url);
        return TextUtils.isEmpty(newCookie) ? false : true;
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

                InputStream myInput = new FileInputStream(_file);

                File outFileName = this.getDatabasePath(dbName);
                boolean isExit = outFileName.exists();

                if (!isExit) {
                    FileUtils.createFile(outFileName);
                }
                // Open the empty db as the output stream
                OutputStream myOutput = new FileOutputStream(outFileName);
                // transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                // Close the streams
                myOutput.flush();
                myOutput.close();
                myInput.close();
                Log.d("复制后的大小", outFileName.length() + "");

//


            }
        }

    }


    @Override
    public void onClick(View v) {

        startThreadGetCookie();

    }

    private void getCookies() {

        try {
            Log.d("getCookies:", "getCookies");
            //writeData();
            //String content=getFileContent(new File("/data/data/com.example.cookiedemo/app_webview/data.txt"));
            DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "Cookies");
            //copyFile("/data/data/com.example.cookiedemo/app_webview/Cookies.db","/data/data/com.example.cookiedemo/databases/Cookies.db");
            //getAllFiles("/data/data/com.example.cookiedemo/app_webview/","db");
            // DaoMaster.DevOpenHelper openHelper=new DaoMaster.DevOpenHelper(new DatabaseContext(getApplication(),dataBaseFile),"Cookies",null);
            Database db = openHelper.getReadableDb();
            DaoMaster daoMaster = new DaoMaster(db);
            DaoSession daoSession = daoMaster.newSession();
            daoSession.clear();

            CookiesDao cookiesDao = daoSession.getCookiesDao();
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
            db.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    showCookie = gson.toJson(mConvertCookiesList);
                    if (showCookie != null ) {
                        showCookie = GsonUtil.toFormat(showCookie, true, false);
                        ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData clipData = ClipData.newPlainText("Label", showCookie);
                            cm.setPrimaryClip(clipData);
                            new ShowCookiePopup(getApplicationContext(),showCookie).showPopupWindow();
                            Toast.makeText(getApplicationContext(), "已复制到剪切板", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startThreadGetCookie() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    getDataBaseFile(cookie_dir, "Cookies");
                    getCookies();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
