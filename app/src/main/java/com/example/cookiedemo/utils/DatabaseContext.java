package com.example.cookiedemo.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * Create on 2020/3/10 16:00
 * author revstar
 * Email 1967919189@qq.com
 */
public class DatabaseContext  extends ContextWrapper {

    public static String dbPath = "";

    public DatabaseContext(Context base, String dbPath) {
        super(base);
        if(!TextUtils.isEmpty(dbPath)){
            this.dbPath = dbPath;
        }
    }

    @Override
    public File getDatabasePath(String name){
        File dbDir = new File(dbPath);
        if(!dbDir.exists()){
            dbDir.mkdir();
        }

        File dbFile = new File(dbPath, name);
        if(!dbFile.exists()){
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dbFile;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }
}