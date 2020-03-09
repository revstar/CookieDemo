package com.example.cookiedemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.example.cookiedemo.greendao.DaoMaster;
import com.example.cookiedemo.greendao.DaoSession;
import com.example.cookiedemo.utils.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    WebView mWebView;
    Button btn_copy;
    TextView tvCookie;
//    JSONObject jsonObject;
//    private String url = "https://main.m.taobao.com/olist/index.html?spm=a2141.7756461.toolbar.i2";

    private String url = "https://main.m.taobao.com/";
    private String cookie_url = "file:///android_asset/Cookies.db";
    private ArrayList<ConvertCookies> mConvertCookiesList;
    private String showCookie;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.web_view);
        btn_copy = findViewById(R.id.btn_copy);
        tvCookie = findViewById(R.id.tv_cookie);
        btn_copy.setOnClickListener(this);
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

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            copyDataBase("Cookies");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "Cookies");
                        Database db = openHelper.getReadableDb();
                        DaoMaster daoMaster = new DaoMaster(db);
                        DaoSession daoSession = daoMaster.newSession();
                        List<Cookies> cookiesList = daoSession.getCookiesDao().loadAll();
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
                                    Toast.makeText(getApplicationContext(),"执行",Toast.LENGTH_SHORT).show();
                                   Gson gson=new GsonBuilder().serializeNulls().create();
                                    showCookie=gson.toJson(mConvertCookiesList);
                                    if (tvCookie!=null){
                                        tvCookie.setText(showCookie);
                                    }
                                }
                            });

                        }
                    }
                }).start();
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);


            }


            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);

            }
        });

    }



    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase(String dbname) throws IOException {
        // Open your local db as the input stream
        InputStream myInput = this.getAssets().open(dbname);
        // Path to the just created empty db
        File outFileName = this.getDatabasePath(dbname);

        if (!outFileName.exists()) {
            outFileName.getParentFile().mkdirs();

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
        }
    }


    @Override
    public void onClick(View v) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clipData = ClipData.newPlainText("Label", tvCookie.getText().toString());
            cm.setPrimaryClip(clipData);
            Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
        }
    }


}
