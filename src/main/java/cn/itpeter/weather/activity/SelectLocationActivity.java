package cn.itpeter.weather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.itpeter.weather.R;
import cn.itpeter.weather.bean.LeftLocationInfo;
import cn.itpeter.weather.bean.Pair;
import cn.itpeter.weather.dao.ChinaDao;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

public class SelectLocationActivity extends BaseActivity {

    @ViewInject(R.id.tv_auto_location)
    TextView tv_auto_location = null;
    @ViewInject(R.id.rl_hand_location)
    RelativeLayout rl_hand_location = null;
    @ViewInject(R.id.ll_wheel_views)
    LinearLayout ll_wheel_views = null;
    @ViewInject(R.id.tv_hand_location)
    TextView tv_hand_location = null;
    @ViewInject(R.id.tv_curr_location)
    TextView tv_curr_location = null;
    @ViewInject(R.id.rl_gps_location)
    RelativeLayout rl_gps_location = null;

    @ViewInject(R.id.wv_province)
    WheelView wv_province = null;
    @ViewInject(R.id.wv_city)
    WheelView wv_city = null;
    @ViewInject(R.id.wv_county)
    WheelView wv_county = null;

    private static final int TYPE_PROVINCE = 1; //滚轮类型,省份滚轮,选择省份
    private static final int TYPE_CITY = 2;     //滚轮类型,城市滚轮,选择城市
    private static final int TYPE_COUNTY = 3;   //滚轮类型,区域滚轮,选择区域

    private static final int CODE_GPS_LOCATION_REQUEST_SUCCESS = 4; //用于标识GPS获取位置信息成功
    private static final int CODE_GPS_LOCATION_REQUEST_FAILURE = 5; //用于标识GPS获取位置信息失败
    private static final int CODE_LOCATION_RESULT = 6;              //返回上一级页面Result代码

    private static MessageHandler handler = null;       //本Activity全局消息处理中心

    ChinaDao chinaDao = null;                       //地理位置查询Dao
    List<Pair<Integer, String>> provinceList = null;//省份list
    List<Pair<Integer, String>> cityList = null;    //城市list
    List<Pair<Integer, String>> countyList = null;  //区域list

    String weatherCode = null;                      //保存当前城市代码

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_select_location);
        ViewUtils.inject(this);

        super.onCreate(savedInstanceState);
    }


    @Override
    void initData() {

        handler = new MessageHandler(this);

        chinaDao = new ChinaDao(this);
        provinceList = chinaDao.getAllProvinceList();
        cityList = chinaDao.getCityList(provinceList.get(0).getT1());
        countyList = chinaDao.getCountyList(cityList.get(0).getT1());
    }

    @Override
    void initUi() {

        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String currLocation = sharedPreferences.getString("location", "");
        tv_curr_location.setText(currLocation);

        getGPSLocation();

        init3WheelView();
    }

    @Override
    void initEvent() {

        //3个滚轮设置滚动监听
        wv_province.addScrollingListener(new LocationScrollListener(TYPE_PROVINCE));
        wv_city.addScrollingListener(new LocationScrollListener(TYPE_CITY));
        wv_county.addScrollingListener(new LocationScrollListener(TYPE_COUNTY));

        //手动设置地理位置点击监听,点击后,将其值更新为当前地理位置
        rl_hand_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_wheel_views.setVisibility(View.VISIBLE);
            }
        });

        //自动地理位置点击监听,点击后,将其值更新为当前地理位置
        rl_gps_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = tv_hand_location.getText().toString();
                if (!"当前定位不可用".equals(location)) {
                    tv_curr_location.setText(location);
                }
            }
        });
    }

    /**
     * 初始化3个滚轮的视图,并设置Adapter
     */
    private void init3WheelView(){
        wv_province.setWheelBackground(R.drawable.wheel_bg_holo);
        wv_province.setWheelForeground(R.drawable.wheel_val_holo);
        wv_province.setShadowColor(0xFF000000, 0x88000000, 0x00000000);

        wv_city.setWheelBackground(R.drawable.wheel_bg_holo);
        wv_city.setWheelForeground(R.drawable.wheel_val_holo);
        wv_city.setShadowColor(0xFF000000, 0x88000000, 0x00000000);

        wv_county.setWheelBackground(R.drawable.wheel_bg_holo);
        wv_county.setWheelForeground(R.drawable.wheel_val_holo);
        wv_county.setShadowColor(0xFF000000, 0x88000000, 0x00000000);

        wv_province.setViewAdapter(new LocationAdapter(this, provinceList));
        wv_city.setViewAdapter(new LocationAdapter(this, cityList));
        wv_county.setViewAdapter(new LocationAdapter(this, countyList));
    }

    private class MessageHandler extends Handler {

        WeakReference reference = null;

        MessageHandler(SelectLocationActivity activity) {
            reference = new WeakReference<SelectLocationActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            SelectLocationActivity activity = (SelectLocationActivity) reference.get();

            super.handleMessage(msg);

            switch (msg.what) {
                case CODE_GPS_LOCATION_REQUEST_SUCCESS:
                    Bundle bundle = msg.getData();
                    String location = bundle.getString("location");
                    tv_auto_location.setText(location);
                    break;
                case CODE_GPS_LOCATION_REQUEST_FAILURE:
                    tv_auto_location.setText("当前定位不可用");
                    break;
            }

        }
    }


    /**
     * 用GPS获取到坐标,然后在通过百度API取得当前省份,城市,区域信息
     */
    private void getGPSLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestService = locationManager.getBestProvider(criteria, true);

        boolean permited = checkPermission("android.permission.ACCESS_COARSE_LOCATION", Binder.getCallingPid(), Binder.getCallingUid())
                == PackageManager.PERMISSION_GRANTED;

        final Message message = Message.obtain();

        Log.i("LOG----", "permited:"+permited+",  bestService:"+bestService);

        if(bestService == null){
            bestService = "gps";
        }

        if (bestService != null && permited) {
            Location location = locationManager.getLastKnownLocation(bestService);
            if(location != null){
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String urlStr = "http://api.map.baidu.com/geocoder/v2/?ak=1d803394838fad58ba4b163c8fe37e44&location=" + latitude + "," +
                        longitude + "&output=json&coordtype=wgs84ll";

                HttpUtils httpUtils = new HttpUtils();

                httpUtils.send(HttpRequest.HttpMethod.GET, urlStr, new RequestCallBack<String>() {
                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {

                        try {
                            JSONObject json = new JSONObject(responseInfo.result);
                            if (json.getInt("status") == 0) {
                                JSONObject addrJson = json.getJSONObject("result").getJSONObject("addressComponent");
                                String province = addrJson.getString("province");
                                String city = addrJson.getString("city");
                                String county = addrJson.getString("county");

                                message.what = CODE_GPS_LOCATION_REQUEST_SUCCESS;
                                Bundle bundle = new Bundle();
                                bundle.putString("location", province + "," + city + "," + county);
                                handler.sendMessage(message);
                            } else {
                                message.what = CODE_GPS_LOCATION_REQUEST_FAILURE;
                                handler.sendMessage(message);
                                Log.i("LOG---", "百度认证错误");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        message.what = CODE_GPS_LOCATION_REQUEST_FAILURE;
                        handler.sendMessage(message);
                        Log.i("LOG---", "百度获取地理位置失败");
                    }
                });
            }else {
                message.what = CODE_GPS_LOCATION_REQUEST_FAILURE;
                handler.sendMessage(message);
                Log.i("LOG---", "本地获取Location失败");
            }

        } else {
            message.what = CODE_GPS_LOCATION_REQUEST_FAILURE;
            handler.sendMessage(message);
            Log.i("LOG---", "获取service失败");
        }
    }


    class LocationScrollListener implements OnWheelScrollListener {

        int currentType;

        /**
         *
         * @param type 当前是省滚轮,城市滚轮还是区域滚轮 TYPE_PROVINCE, TYPE_CITY, TYPE_COUNTY
         */
        LocationScrollListener(int type) {
            this.currentType = type;
        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            if (currentType == TYPE_PROVINCE) {
                cityList = chinaDao.getCityList(provinceList.get(wv_province.getCurrentItem()).getT1());
                wv_city.setViewAdapter(new LocationAdapter(SelectLocationActivity.this, cityList));
                wv_city.setCurrentItem(0);

                countyList = chinaDao.getCountyList(cityList.get(wv_city.getCurrentItem()).getT1());
                wv_county.setViewAdapter(new LocationAdapter(SelectLocationActivity.this, countyList));
                wv_county.setCurrentItem(0);

                wv_province.setCyclic(false);
            } else if (currentType == TYPE_CITY) {
                countyList = chinaDao.getCountyList(cityList.get(wv_city.getCurrentItem()).getT1());
                wv_county.setViewAdapter(new LocationAdapter(SelectLocationActivity.this, countyList));
                wv_county.setCurrentItem(0);

                wv_city.setCyclic(false);
            }else if(currentType == TYPE_COUNTY){
                wv_county.setCyclic(false);
            }
        }

        @Override
        public void onScrollingStarted(WheelView wheel) {
            if (currentType == TYPE_PROVINCE) {
                wv_province.setCyclic(true);
            } else if (currentType == TYPE_CITY) {
                wv_city.setCyclic(true);
            }else if(currentType == TYPE_COUNTY){
                wv_county.setCyclic(true);
            }
        }
    }

    private class LocationAdapter extends AbstractWheelTextAdapter {
        List<Pair<Integer, String>> list = null;

        protected LocationAdapter(Context context, List<Pair<Integer, String>> list) {
            super(context, R.layout.city_holo_layout, NO_RESOURCE);
            setItemTextResource(R.id.city_name);
            this.list = list;
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            return view;
        }

        @Override
        public int getItemsCount() {
            return list.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            return list.get(index).getT2();
        }
    }

    /**
     * 在3个滚轮上获取当前选中的地理位置信息
     * @return  滚轮上选中的地理位置信息;格式:省,市,区域
     */
    private String getLocationFullString(){
        String province = provinceList.get(wv_province.getCurrentItem()).getT2();
        String city = cityList.get(wv_city.getCurrentItem()).getT2();
        String county = countyList.get(wv_county.getCurrentItem()).getT2();

        return province + "," + city + "," + county;
    }

    /**
     * 设置在Button上,用于保存滚轮上的地理位置信息.
     * 更新显示的当前的位置信息;
     * @param view
     */
    public void saveHandSelectLoaction(View view) {
        if(!(wv_county.isCyclic() || wv_province.isCyclic() || wv_city.isCyclic())){
            Log.i("LOG--", "wv_province:"+wv_province.isCyclic());
            ll_wheel_views.setVisibility(View.INVISIBLE);
            String location = getLocationFullString();
            tv_curr_location.setText(location);

            weatherCode = ""+countyList.get(wv_county.getCurrentItem()).getT1();
        }
    }

    /**
     * 设置在Button上用户返回上一个页面
     * @param view
     */
    public void back(View view) {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        String currLocation = tv_curr_location.getText().toString();
        if(!TextUtils.isEmpty(currLocation)){
            //保存到SharedPreferences中,用于下次启动时获取
            SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("location", currLocation);
            editor.commit();

            bundle.putString("location", currLocation);
            if(weatherCode != null){//并非第一次进入,但是进来了什么也没干,没有改变地理位置信息
                bundle.putString("weatherCode", weatherCode);
                setResult(CODE_LOCATION_RESULT, intent);

                LeftLocationInfo.LocationInfo locationInfo = new LeftLocationInfo.LocationInfo(currLocation, weatherCode);

                //将地理位置信息保存到磁盘上,用于在主页面的left显示
                LeftLocationInfo locationInfoList = new LeftLocationInfo(this);
                locationInfoList.getmLocationInfoSet().add(locationInfo);
                locationInfoList.writeToDisk(this);
            }
            intent.putExtras(bundle);
            finish();
        }else{//第一次进入
            Toast.makeText(this, "请选择您的位置", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 第一次进入App时,如果没有设置地理位置,阻止返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String currLocation = tv_curr_location.getText().toString();
        if(TextUtils.isEmpty(currLocation)){
            return true;
        }else{
            return false;
        }
    }
}
