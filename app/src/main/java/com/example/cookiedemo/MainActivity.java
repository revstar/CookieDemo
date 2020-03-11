package com.example.cookiedemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookiedemo.bean.ConvertCookies;
import com.example.cookiedemo.bean.Cookies;
import com.example.cookiedemo.greendao.CookiesDao;
import com.example.cookiedemo.greendao.DaoMaster;
import com.example.cookiedemo.greendao.DaoSession;
import com.example.cookiedemo.popup.ShowCookiePopup;
import com.example.cookiedemo.utils.DatabaseContext;
import com.example.cookiedemo.utils.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.database.Database;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    WebView mWebView;
    Button btn_output;
    private String url = "https://main.m.taobao.com/";
    private String cookie_dir;
    private ArrayList<ConvertCookies> mConvertCookiesList;
    private String showCookie;

    private File dataBaseFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cookie_dir =File.separator+"data"+File.separator+"data"+File.separator+getPackageName()+File.separator+"app_webview";
        setContentView(R.layout.activity_main);
        initView();



    }
    @SuppressLint("SetJavaScriptEnabled")
    private void initView(){
        mWebView = findViewById(R.id.web_view);
        btn_output = findViewById(R.id.btn_output);
        btn_output.setOnClickListener(this);
        WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDomStorageEnabled(true);
        mWebView.requestFocus();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

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
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d("url", url);
                super.onPageFinished(view, url);


            }

            @Override
            public void onLoadResource(WebView view, String load) {
                url = load;
                super.onLoadResource(view, url);


            }


            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);

            }
        });
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
                boolean canRead=_file.canRead();
                dataBaseFile = _file;
                return;
//                InputStream myInput=new FileInputStream(_file);
//                File outFileName=this.getDatabasePath(dbName);
//                // Open the empty db as the output stream
//                OutputStream myOutput=new FileOutputStream(outFileName);
//                // transfer bytes from the inputfile to the outputfile
//                byte[]buffer=new byte[1024];
//                int length;
//                while ((length=myInput.read(buffer))>0){
//                    myOutput.write(buffer,0,length);
//                }
//                // Close the streams
//                myOutput.flush();
//                myOutput.close();
//                myInput.close();
            }
        }

    }


    @Override
    public void onClick(View v) {

        getCookies();
    }


    private void getCookies() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    writeData();
//                    String content=getFileContent(new File("/data/data/com.example.cookiedemo/app_webview/data.txt"));
                    getDataBaseFile(cookie_dir, "Cookies");
//                    DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "Cookies");
//                  copyFile("/data/data/com.example.cookiedemo/app_webview/Cookies.db","/data/data/com.example.cookiedemo/databases/Cookies.db");
//                   getAllFiles("/data/data/com.example.cookiedemo/app_webview/","db");
                    DaoMaster.DevOpenHelper openHelper=new DaoMaster.DevOpenHelper(new DatabaseContext(getApplication(),dataBaseFile),"Cookies",null);
                    Database db = openHelper.getReadableDb();
                    DaoMaster daoMaster = new DaoMaster(db);
                    DaoSession daoSession = daoMaster.newSession();
                    CookiesDao cookiesDao=daoSession.getCookiesDao();

                    List<Cookies> cookiesList = cookiesDao.loadAll();
                    if (cookiesList != null) {
                        if (mConvertCookiesList == null) {
                            mConvertCookiesList = new ArrayList<>();
                        }
                        mConvertCookiesList.clear();
                        for (Cookies cookies : cookiesList) {
                            if (cookies != null) {
                                ConvertCookies convertCookies = new ConvertCookies(cookies.host_key, cookies.name, cookies.value, cookies.path, cookies.is_secure, cookies.expires_utc);
                                mConvertCookiesList.add(convertCookies);
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                showCookie = gson.toJson(mConvertCookiesList);
                                new ShowCookiePopup(getApplicationContext(), showCookie).showPopupWindow();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
