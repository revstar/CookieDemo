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
import com.example.cookiedemo.bean.Cookies;
import com.example.cookiedemo.bean.ConvertCookies;
import com.example.cookiedemo.greendao.CookiesDao;
import com.example.cookiedemo.greendao.DaoMaster;
import com.example.cookiedemo.greendao.DaoSession;
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

public class GetCookiesActivity extends AppCompatActivity {

    private TextView tv_show_cookie;
    private ArrayList<ConvertCookies> mConvertCookiesList;
    private String showCookie;
    private String cookie_dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_cookies);
        cookie_dir = File.separator + "data" + File.separator + "data" + File.separator + getPackageName() + File.separator + "app_webview";
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
                    Thread.sleep(500);
                    getDataBaseFile(cookie_dir, "Cookies");
                    getCookies();
                } catch (InterruptedException | IOException e) {
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
            }
        }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
