package com.example.cookiedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
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


import org.json.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieStore;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    WebView mWebView;
    Button btn_copy;
    TextView tvCookie;
//    JSONObject jsonObject;
//    private String url = "https://main.m.taobao.com/olist/index.html?spm=a2141.7756461.toolbar.i2";

    private String url="https://main.m.taobao.com/";
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.web_view);
        btn_copy = findViewById(R.id.btn_copy);
        tvCookie=findViewById(R.id.tv_cookie);
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
        mThread.start();
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
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                CookieManager cookieManager = CookieManager.getInstance();
                String cookieStr = cookieManager.getCookie(url);
                if (cookieStr != null) {
                    try {
//                         jsonObject= Cookie.toJSONObject(cookieStr);
                        tvCookie.setText(cookieStr);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);

            }
        });

    }

    Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {


        }
    });


    @Override
    public void onClick(View v) {
            ClipboardManager cm= (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm!=null){
                ClipData clipData=ClipData.newPlainText("Label",tvCookie.getText().toString());
                cm.setPrimaryClip(clipData);
                Toast.makeText(MainActivity.this,"复制成功",Toast.LENGTH_SHORT).show();
            }
    }
}
