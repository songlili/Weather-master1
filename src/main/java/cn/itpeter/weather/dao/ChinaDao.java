package cn.itpeter.weather.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cn.itpeter.weather.bean.China;
import cn.itpeter.weather.bean.City;
import cn.itpeter.weather.bean.County;
import cn.itpeter.weather.bean.Pair;
import cn.itpeter.weather.bean.Province;

/**
 * Created by annpeter on 11/15/15.
 */
public class ChinaDao {

    Context context = null;

    public ChinaDao(Context context){
        this.context = context;
    }

    /**
     * 由于我拿到的中国城市信息是xml格式的,我先将其转化为json,然后在这里写入数据库中
     */
    public void jsonToDb(){

        ChinaDBOpenHelper dbOpenHelper = new ChinaDBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();

        //获取到json字符串,并保存在StringBuffer中
        StringBuffer sb = new StringBuffer();
        byte[] buffer = new byte[1024];
        String line;
        try {
            InputStream is = context.getAssets().open("location.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine())!= null){
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将json字符串转化为对象
        Gson gson = new Gson();
        China chinaBean = gson.fromJson(sb.toString(), China.class);
        List<Province> provinceList = chinaBean.getProvinceList();

        for(Province province : provinceList){
            ContentValues values = new ContentValues(); //插入所有省份信息
            values.put("_id", province.getId());
            values.put("name", province.getName());
            database.insert("province", null, values);
            values.clear();

            for(City city : province.getCityList()){
                ContentValues values1 = new ContentValues();    //插入所有城市信息
                values1.put("_id", city.getId());
                values1.put("name", city.getName());
                values1.put("provinceId", province.getId());
                database.insert("city", null, values1);
                values1.clear();

                for(County county : city.getCountyList()){      //插入所有区域信息,其中County中的_id为天气城市代码
                    ContentValues values2 = new ContentValues();
                    values2.put("_id", county.getWeatherCode());
                    values2.put("name", county.getName());
                    values2.put("cityId", city.getId());
                    database.insert("county", null, values2);
                    values2.clear();
                }
            }
        }

        database.close();
        dbOpenHelper.close();
    }

    public List<Pair<Integer, String>> getAllProvinceList(){

        ChinaDBOpenHelper dbOpenHelper = new ChinaDBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();

        List<Pair<Integer, String>> list = new ArrayList<Pair<Integer, String>>();
        Cursor cursor = database.query("province", new String[]{"_id", "name"}, null, null, null, null, null);
        while (cursor.moveToNext()){
            list.add(new Pair<Integer, String>(cursor.getInt(0), cursor.getString(1)));
        }

        database.close();
        dbOpenHelper.close();
        return list;
    }

    public List<Pair<Integer, String>> getCityList(int provinceId){
        ChinaDBOpenHelper dbOpenHelper = new ChinaDBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();

        List<Pair<Integer, String>> list = new ArrayList<Pair<Integer, String>>();
        Cursor cursor = database.query("city", new String[]{"_id", "name"}, "provinceId=?", new String[]{""+provinceId}, null, null, null);
        while (cursor.moveToNext()){
            list.add(new Pair<Integer, String>(cursor.getInt(0), cursor.getString(1)));
        }

        database.close();
        dbOpenHelper.close();
        return list;
    }

    public List<Pair<Integer, String>> getCountyList(int cityId){
        Log.i("log-----cityId", ""+cityId);

        ChinaDBOpenHelper dbOpenHelper = new ChinaDBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();

        List<Pair<Integer, String>> list = new ArrayList<Pair<Integer, String>>();
        Cursor cursor = database.query("county", new String[]{"_id", "name"}, "cityId=?", new String[]{"" + cityId}, null, null, null);
        while (cursor.moveToNext()){
            list.add(new Pair<Integer, String>(cursor.getInt(0), cursor.getString(1)));
        }

        database.close();
        dbOpenHelper.close();
        return list;
    }

    /**
     *
     * @param location 省份,城市,区域
     * @return  区域代码
     */
    public String getWeatherCode(String location){
        String resturnStr = null;

        String province = location.substring(0, location.indexOf(','));
        String city = location.substring(location.indexOf(',')+1, location.lastIndexOf(','));
        String county = location.substring(location.lastIndexOf(',')+1, location.length());

        ChinaDBOpenHelper dbOpenHelper = new ChinaDBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();

        String sql = "select _id from county where cityId = (select _id from city where (" +
                "provinceId=(select _id from province where name='"+province+"') and name='"+ city +"')) and name='"+county+"';";
        Log.i("LOG", sql);
        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToNext();
        if(county != null) {
            resturnStr = cursor.getString(0);
        }

        database.close();
        dbOpenHelper.close();
        return resturnStr;
    }
}
