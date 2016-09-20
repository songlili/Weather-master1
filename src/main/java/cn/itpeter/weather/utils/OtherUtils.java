package cn.itpeter.weather.utils;

import android.content.Context;

/**
 * Created by annpeter on 11/15/15.
 */
public class OtherUtils {
    /**
     * 获取省,市,区域的最后一级--区域
     * @param location province,city,county
     * @return county
     */
    public static String getLowestLocation(String location){
        int begin = location.lastIndexOf(',') + 1;
        int end = location.length();
        try {
            if( begin >= end){
                throw new Exception("location error: begin="+begin+", end="+end);
            }else{
                return location.substring(begin, end);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 将Android使用的相对像素值转化为绝对像素
     * @param context
     * @param dpValue Android使用的相对像素
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     *将绝对像素值转化为Android使用的相对像素值
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
