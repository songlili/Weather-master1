package cn.itpeter.weather.bean;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by annpeter on 11/24/15.
 */
public class LeftLocationInfo {

    Set<LocationInfo> mLocationInfoSet = new HashSet<LocationInfo>();

    public LeftLocationInfo(Context context){
        refreshLocationInfo(context);
    }

    public void writeToDisk(Context context){
        if(mLocationInfoSet.size() > 0) {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            Log.i("LOG---", json);
            File file = new File(context.getCacheDir() + "/location.json");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(json.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<LocationInfo> getLastLocationInfo(Context context){
        refreshLocationInfo(context);
        return new ArrayList<LocationInfo>(mLocationInfoSet);
    }

    private void refreshLocationInfo(Context context){

        StringBuffer sb = new StringBuffer();
        File file = new File(context.getCacheDir()+"/location.json");
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(sb.length() > 0){
            Gson gson = new Gson();
            LeftLocationInfo locationInfoList = gson.fromJson(sb.toString(), LeftLocationInfo.class);
            mLocationInfoSet.clear();
            mLocationInfoSet.addAll(locationInfoList.getmLocationInfoSet());
        }
    }

    public Set<LocationInfo> getmLocationInfoSet() {
        return mLocationInfoSet;
    }

    public void setmLocationInfoSet(Set<LocationInfo> mLocationInfoSet) {
        this.mLocationInfoSet = mLocationInfoSet;
    }

    static public class LocationInfo{
        String mFullLocation;
        String mWeatherCode;

        public LocationInfo(String mFullLocation, String mWeatherCode) {
            this.mFullLocation = mFullLocation;
            this.mWeatherCode = mWeatherCode;
        }

        public String getmFullLocation() {
            return mFullLocation;
        }

        public void setmFullLocation(String mFullLocation) {
            this.mFullLocation = mFullLocation;
        }

        public String getmWeatherCode() {
            return mWeatherCode;
        }

        public void setmWeatherCode(String mWeatherCode) {
            this.mWeatherCode = mWeatherCode;
        }
    }

    public void removeCity(Context context, String fullLocationName){

        LocationInfo locationInfo = null;

        for(LocationInfo info : mLocationInfoSet){
            if(info.getmFullLocation().equals(fullLocationName)){
                locationInfo = info;
            }
        }

        mLocationInfoSet.remove(locationInfo);
        writeToDisk(context);
    }
}
