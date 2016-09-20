package cn.itpeter.weather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.itpeter.weather.R;
import cn.itpeter.weather.bean.Pair;
import cn.itpeter.weather.bean.WeatherInfo;
import cn.itpeter.weather.dao.ChinaDao;
import cn.itpeter.weather.utils.OtherUtils;

public class MainActivity extends BaseActivity {

    @ViewInject(R.id.tv_location)
    TextView tv_location = null;
    @ViewInject(R.id.ll_extra_info)
    LinearLayout ll_extra_info = null;
    @ViewInject(R.id.ll_up)
    LinearLayout ll_up = null;
    @ViewInject(R.id.ll_down)
    LinearLayout ll_down = null;
    @ViewInject(R.id.rl_marsk)
    RelativeLayout rl_marsk = null;
    @ViewInject(R.id.vp_future_weather)
    private ViewPager viewPager = null;
    @ViewInject(R.id.ll_dot_list)
    LinearLayout ll_dot_list;

    private int refreshSourceCount;                 //表示需要从网络加载的资源个数,现有今天的天气资源和未来的天气资源,
                                                    //只有两个资源都返回时,才让网络加载ProgressBar隐藏
    private String currLocation = null;             //当前设置天气城市全路径,  格式:省,市,区
    private static MessageHandler handler = null;   //用于本activity中所有的发送和接收消息
    private WeatherInfo.Result todayInfoResult = null;              //今天天气信息
    private List<WeatherInfo.Result> futureInfoResultList = null;   //未来天气信息list,保存的是网上获取到的信息的java bean的列表
    List<FutureWeatherInfo> futureWeatherInfoList = null;           //未来天气信息数据列表,只保存了Ui中用到的时间,天气状态和温度的信息
    List<Pair<TextView, TextView>> extraInfoList = null;            //今日天气的额外信息布局列表:最高最低温度,湿度,风力,风向
    private ViewPageAdapter viewPagerAdapter = null;                //viewPager的适配器
    /**
     *CODE用于消息传送的标识
     */
    private static final int CODE_REQUEST_FOR_LOCATION = 1;         //选择地理位置信息的申请CODE
    private static final int CODE_REQUEST_TODAY_WEATHER_INFO = 2;   //今日天气信息的申请CODE
    private static final int CODE_REQUEST_FUTURE_WEATHER_INFO = 3;  //未来天气信息的申请CODE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        ViewUtils.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    void initUi() {

        extraInfoList = getExtraInfoList();

        ll_up.getBackground().setAlpha(150);
        ll_down.getBackground().setAlpha(150);
        //设置温度,湿度,风力,风向的布局信息布局信息; 就是前面那几个写死的文字
        setTodayExtraInfo();

        viewPagerAdapter = new ViewPageAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        addDots();

        if(currLocation != null){
            refreshTodayAndFutureData(currLocation, null);  //获取数据
        }
    }

    @Override
    void initData() {
        handler = new MessageHandler(this);
        futureWeatherInfoList = new ArrayList<FutureWeatherInfo>();
        //获取缓存中是否有位置信息,如果有获取到并用于获取网络天气信息,如果没有则跳转到设置地理位置的界面
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        currLocation = sharedPreferences.getString("location", null);
        if(currLocation == null){
            Intent intent = new Intent(MainActivity.this, SelectLocationActivity.class);
            startActivityForResult(intent, CODE_REQUEST_FOR_LOCATION);
        }
    }

    @Override
    void initEvent() {

        //监听地理位置选择
        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectLocationActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("location", currLocation);
                intent.putExtras(bundle);

                startActivityForResult(intent, CODE_REQUEST_FOR_LOCATION);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDot(position);
            }
        });
    }

    /**
     * 每次地理位置改变就应当删除ll_dot_list中的元素,然后重新根据有多少个page添加
     */
    private void addDots(){
        ll_dot_list.removeAllViews();
        int pageCount = viewPagerAdapter.getCount();
        for(int i = 0; i < pageCount; i++){
            View view = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
            if(i!=0){
                params.leftMargin = 5;
            }
            view.setLayoutParams(params);
            view.setBackgroundResource(R.drawable.dot_selector);
            ll_dot_list.addView(view);
        }
    }

    /**
     * 每次滑动的时候修改dot的状态
     * @param position
     */
    private void updateDot(int position){
        int pageCount = viewPagerAdapter.getCount();
        for(int i = 0;i < pageCount; i++){
            ll_dot_list.getChildAt(i).setEnabled(i == position);
        }
    }

    private class ViewPageAdapter extends PagerAdapter{
        @Override
        public int getCount() {
            int temp = futureWeatherInfoList.size() % 3;

            int ret = futureWeatherInfoList.size() / 3;
            if(temp != 0){
                 ret += 1;
            }
            return ret;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = View.inflate(MainActivity.this, R.layout.future_weather_info, null);
            LinearLayout ll_future_weather_info_list = (LinearLayout)view.findViewById(R.id.ll_future_weather_info_list);
            for(int i = 0; i < ll_future_weather_info_list.getChildCount(); i++){
                LinearLayout item = (LinearLayout)ll_future_weather_info_list.getChildAt(i);
                TextView tv_time = (TextView)item.findViewById(R.id.tv_time);
                ImageView iv_weather_status = (ImageView)item.findViewById(R.id.iv_weather_status);
                TextView tv_temperature = (TextView)item.findViewById(R.id.tv_temperature);

                if((position * 3 + i) < futureWeatherInfoList.size()){
                    FutureWeatherInfo weatherInfo = futureWeatherInfoList.get(position * 3 + i);
                    String time = weatherInfo.getTime();
                    tv_time.setText(time.substring(time.lastIndexOf('-')+1, time.length())+"日");
                    iv_weather_status.setImageBitmap(weatherInfo.getStatusIconBitmap());
                    tv_temperature.setText(weatherInfo.getTemperature());
                }else{
                    break;
                }
            }

            container.addView(view);
            return view;
        }
    }

    /**
     * 获取温度,湿度,风力,风向布局上的控件
     * @return 布局元素的列表
     */
    private List<Pair<TextView, TextView>> getExtraInfoList() {
        List<Pair<TextView, TextView>> list = new ArrayList<Pair<TextView, TextView>>();
        int count = ll_extra_info.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = ll_extra_info.getChildAt(i);
            TextView tv_key = (TextView) view.findViewById(R.id.tv_key);
            TextView tv_value = (TextView) view.findViewById(R.id.tv_value);

            Pair<TextView, TextView> pair = new Pair(tv_key, tv_value);
            list.add(pair);
        }
        return list;
    }


    /**
     *设置温度,湿度,风力,风向的布局信息布局信息; 就是前面那几个写死的文字
     */
    private void setTodayExtraInfo() {
        extraInfoList.get(0).getT1().setText("最高/最低温度");
        extraInfoList.get(1).getT1().setText("相对湿度");
        extraInfoList.get(2).getT1().setText("风力");
        extraInfoList.get(3).getT1().setText("风向");
    }

    /**
     * 刷新温度,湿度,风力,风向的值的信息
     * @param hightLowTemp  最高最低温度
     * @param humidity  湿度
     * @param windPower 风力
     * @param windDirection 风向
     */
    private void refreshTodayInfoExtraInfo(String hightLowTemp, String humidity, String windPower, String windDirection) {
        extraInfoList.get(0).getT2().setText(hightLowTemp);
        extraInfoList.get(1).getT2().setText(humidity);
        extraInfoList.get(2).getT2().setText(windPower);
        extraInfoList.get(3).getT2().setText(windDirection);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_REQUEST_FOR_LOCATION && resultCode != 0) {
            Bundle bundle = data.getExtras();
            String location = (String)bundle.get("location");
            refreshTodayAndFutureData(location, (String) bundle.get("weatherCode"));
        }
    }

    /**
     *从网络上获取今天和未来的天气信息,然后通过消息刷新UI
     * @param location 位置全路径
     * @param weatherCode 无weatherCode,可传入null
     *
     */
    public void refreshTodayAndFutureData(String location, String weatherCode){

        refreshSourceCount = 0;
        rl_marsk.setVisibility(View.VISIBLE);   //获取天气信息中,设置加载网络状态为可见

        String locationTemp = OtherUtils.getLowestLocation(location);
        tv_location.setText(locationTemp);  //刷新最新的区域信息用于显示

        if(weatherCode == null){
            weatherCode = new ChinaDao(this).getWeatherCode(location);  //根据location全路径到数据库中获取weatherCode
        }

        HttpUtils httpUtils = new HttpUtils();
        final String urlToday = "http://api.k780.com:88/?app=weather.today&weaid="+weatherCode
                +"&&appkey=16412&sign=808aee0076d9d01d4569a7f4ca6b4f82&format=json";

        final String urlFuture = "http://api.k780.com:88/?app=weather.future&weaid="+weatherCode+
                "&&appkey=16412&sign=808aee0076d9d01d4569a7f4ca6b4f82&format=json";

        Log.i("LOG--urlToday----", urlToday);
        Log.i("LOG--urlFuture----", urlFuture);

        //获取今日天气信息
        Log.i("time", new Date().getTime()+"");
        httpUtils.send(HttpRequest.HttpMethod.GET, urlToday, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Log.i("time", new Date().getTime() + "");
                Gson gson = new Gson();

                WeatherInfo.Today todayWeatherInfo = gson.fromJson(responseInfo.result, WeatherInfo.Today.class);
                Log.i("time", new Date().getTime()+"");
                todayInfoResult = todayWeatherInfo.getResult();
                Log.i("LOG--Today", todayWeatherInfo.toString());
                if (todayInfoResult == null) { //请求参数错误
                    Log.i("LOG--Today", "获取今日信息参数错误");
                } else {
                    refreshSourceCount++;
                    handler.sendEmptyMessage(CODE_REQUEST_TODAY_WEATHER_INFO);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Toast.makeText(MainActivity.this, "请检查您的网络哦", Toast.LENGTH_LONG).show();
                Log.i("LOG--Today", "获取今日信息失败" + msg);
            }
        });

        //获取未来天气信息
        httpUtils.send(HttpRequest.HttpMethod.GET, urlFuture, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                Gson gson = new Gson();

                WeatherInfo.Future futureWeatherInfo = gson.fromJson(responseInfo.result, WeatherInfo.Future.class);

                futureInfoResultList = futureWeatherInfo.getResult();
                Log.i("LOG--Future", futureWeatherInfo.toString());
                if(futureInfoResultList == null){ //请求参数错误
                    Log.i("LOG--Future", "获取未来天气信息参数错误");
                }else {
                    refreshSourceCount++;
                    handler.sendEmptyMessage(CODE_REQUEST_FUTURE_WEATHER_INFO);
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.i("LOG--Future", "获取未来天气信息请求失败"+msg);
            }
        });
    }

    /**
     * 未来一天的天气信息集合,包括UI上的控件和相应的数据
     */
    private class FutureWeatherInfo {
        private String time = null;
        private String weatid = null;
        private String temperature = null;

        public FutureWeatherInfo(String temperature, String time, String weatid) {
            this.temperature = temperature;
            this.time = time;
            this.weatid = weatid;
        }

        public String getTime(){
            return time;
        }

        public Bitmap getStatusIconBitmap(){
            Bitmap bitmap = null;
            String iconPath = "weatherIcon/"+weatid+".png";
            try {
                InputStream is = getAssets().open(iconPath);
                bitmap = BitmapFactory.decodeStream(is);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        public String getTemperature(){
            return temperature;
        }
    }

    /**
     * 处理消息,网络返回消息
     */
    private class MessageHandler extends Handler {

        WeakReference reference = null;

        MessageHandler(MainActivity activity) {
            reference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("log-----", msg.toString());
            switch (msg.what){
                case CODE_REQUEST_TODAY_WEATHER_INFO:
                    refreshTodayInfo();
                    break;
                case CODE_REQUEST_FUTURE_WEATHER_INFO:
                    refreshFutureInfo();

                    addDots();  //重新ViewPage下面点小点
                    updateDot(0);
                    break;
            }

            if(refreshSourceCount == 2){
                rl_marsk.setVisibility(View.INVISIBLE);
                refreshSourceCount = 0;
            }
        }

        /**
         * 刷新今日天气的数据
         */
        private void refreshTodayInfo(){
            TextView tv_today_summary_info = (TextView)findViewById(R.id.tv_today_summary_info);
            tv_today_summary_info.setText(todayInfoResult.getWeather());

            ImageView tv_today_summary_icon = (ImageView)findViewById(R.id.tv_today_summary_icon);
            String iconPath = "weatherIcon/"+ todayInfoResult.getWeatid()+".png";

            InputStream inputStream = null;
            try {
                inputStream = getAssets().open(iconPath);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                tv_today_summary_icon.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshTodayInfoExtraInfo(todayInfoResult.getTemperature(), todayInfoResult.getHumidity(), todayInfoResult.getWinp(), todayInfoResult.getWind());
        }

        /**
         * 刷新未来天气的数据
         */
        private void refreshFutureInfo(){
            int i = 0;
            futureWeatherInfoList.clear();  //清空以前的未来天气信息

            for(WeatherInfo.Result result : futureInfoResultList){
                if(result != null){
                    if(i == 0){//发布时间更新
                        String time = result.getDays();
                        String remark = "中国气象局" +time.substring(time.indexOf('-')+1,
                                time.lastIndexOf('-'))+ "月"+ time.substring(time.lastIndexOf('-')+1, time.length())+"日发布";
                        TextView tv_remarks = (TextView)findViewById(R.id.tv_remarks);
                        tv_remarks.setText(remark);
                        i++;
                    }

                    FutureWeatherInfo weatherInfo = new FutureWeatherInfo(result.getTemperature(), result.getDays(), result.getWeatid());
                    futureWeatherInfoList.add(weatherInfo);
                }
            }
            viewPagerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置给Button的,用于跳转到选择联系人,分享App
     * @param view
     */
    public void goToSelectContact(View view){
        Intent intent = new Intent(this, ShowAllContactActivity.class);
        startActivity(intent);
    }

}
