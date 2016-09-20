package cn.itpeter.weather.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class ChinaDBOpenHelper extends SQLiteOpenHelper {


    public ChinaDBOpenHelper(Context context) {
        super(context, "china.db", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table province(_id integer primary key autoincrement, name char(20))");
        db.execSQL("create table city(_id integer primary key autoincrement,provinceId integer, name char(20), foreign key(provinceId) references province(_id))");
        db.execSQL("create table county(_id integer primary key autoincrement,cityId integer, name char(20), foreign key(cityId) references city(_id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
