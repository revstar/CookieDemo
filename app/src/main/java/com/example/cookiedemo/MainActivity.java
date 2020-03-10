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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cookie_dir = "/data/data/com.example.cookiedemo/app_webview/";
        setContentView(R.layout.activity_main);
        getPermission();
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

    /**
     * 获取权限
     */
    public void getPermission() {


        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }


            if (permissionsList.size() == 0) {
//                initData();

            } else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        0);
            }
        } else {
//            initData();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    showTip();
                } else {
//                    initData();
                }
            }
            default:
                break;

        }
    }

    private void writeData() {
        String filePath = "/data/data/com.example.cookiedemo/test/";
        String fileName = "data.txt";
        writeTxtToFile("Wx:lcti1314", filePath, fileName);
    }

    // 将字符串写入到文本文件中
    private void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

//生成文件

    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

//生成文件夹

    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase(String cookie_dir, String dbName) throws IOException {
        // Open your local db as the input stream
        InputStream myInput = this.getAssets().open(dbName);
//        cacheName=this.getFilesDir().getParent()+"/cache/Cookies.db";
//        cacheName = this.getCacheDir() + "/Cookies.db";
//        File file = new File(cacheName);
//        InputStream myInput = new FileInputStream(file);
        // Path to the just created empty db
//        if (this.getDatabasePath(dbName).exists()) {
//            this.getDatabasePath(dbName).delete();
//        }
        File outFileName = this.getDatabasePath(dbName);

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

    //读取指定目录下的所有TXT文件的文件内容
    private String getFileContent(File file) {
        String content = "";
        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    Log.d("TestFile", "The File doesn't not exist.");
                } catch (IOException e) {
                    Log.d("TestFile", e.getMessage());
                }
            }
        }
        return content;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }

    @Override
    public void onClick(View v) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    writeData();
//                    String content=getFileContent(new File("/data/data/com.example.cookiedemo/app_webview/data.txt"));
                    copyDataBase(cookie_dir, "Cookies");
                    DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(getApplicationContext(), "Cookies");
//                  copyFile("/data/data/com.example.cookiedemo/app_webview/Cookies.db","/data/data/com.example.cookiedemo/databases/Cookies.db");
//                   getAllFiles("/data/data/com.example.cookiedemo/app_webview/","db");
//                    DaoMaster.DevOpenHelper openHelper=new DaoMaster.DevOpenHelper(new DatabaseContext(getApplication(),cookie_url),"Cookies",null);
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
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                showCookie = gson.toJson(mConvertCookiesList);
                                new ShowCookiePopup(getApplicationContext(),showCookie).showPopupWindow();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取指定目录内所有文件路径
     * @param dirPath 需要查询的文件目录
     * @param _type 查询类型，比如mp3什么的
     */
    public static JSONArray getAllFiles(String dirPath, String _type) {
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
            return null;
        }

        File[] files = f.listFiles();

        if(files==null){//判断权限
            return null;
        }

        JSONArray fileList = new JSONArray();
        for (File _file : files) {//遍历目录
            if(_file.isFile() && _file.getName().endsWith(_type)){
                String _name=_file.getName();
                String filePath = _file.getAbsolutePath();//获取文件路径
                String fileName = _file.getName().substring(0,_name.length()-4);//获取文件名
//                Log.d("LOGCAT","fileName:"+fileName);
//                Log.d("LOGCAT","filePath:"+filePath);
                try {
                    JSONObject _fInfo = new JSONObject();
                    _fInfo.put("name", fileName);
                    _fInfo.put("path", filePath);
                    fileList.put(_fInfo);
                }catch (Exception e){
                }
            } else if(_file.isDirectory()){//查询子目录
                getAllFiles(_file.getAbsolutePath(), _type);
            } else{
            }
        }
        return fileList;
    }

}
