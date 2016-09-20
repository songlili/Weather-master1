package cn.itpeter.weather.view;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.itpeter.weather.R;
import cn.itpeter.weather.activity.MainActivity;
import cn.itpeter.weather.bean.LeftLocationInfo;
import cn.itpeter.weather.utils.OtherUtils;

/**
 * Created by annpeter on 11/24/15.
 */
public class MainSliderView extends FrameLayout {

    private View mLeftView;
    private View mRightView;

    private ListView lv_citys;
    private BaseAdapter mCityAdapter;

    List<LeftLocationInfo.LocationInfo> locationInfoList = null;
    LeftLocationInfo mLeftLocationInfo = null;

    private float mLeftViewWidth;
    private float mRightViewWidth = 0;

    private float downX;


    public MainSliderView(Context context) {
        super(context);
    }

    public MainSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mLeftViewWidth  = mLeftView.getMeasuredWidth();
        mRightViewWidth = mRightView.getMeasuredWidth();

        mLeftView.layout((int)-mLeftViewWidth, 0, 0, b);
        mRightView.layout(l, t, r, b);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeftView = getChildAt(0);
        mRightView = getChildAt(1);
        lv_citys = (ListView)mLeftView.findViewById(R.id.lv_citys);


        mLeftLocationInfo = new LeftLocationInfo(getContext());
        locationInfoList = mLeftLocationInfo.getLastLocationInfo(getContext());
        mCityAdapter = new CityListAdapter();
        lv_citys.setAdapter(mCityAdapter);

        lv_citys.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity mainActivity = (MainActivity)getContext();
                LeftLocationInfo.LocationInfo info = (LeftLocationInfo.LocationInfo)locationInfoList.get(position);
                mainActivity.refreshTodayAndFutureData(info.getmFullLocation(), info.getmWeatherCode());

                scrollTo(0, 0);
            }
        });
    }


    private float downScrolledX;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downScrolledX = getScrollX();

                if(downScrolledX == 0 ){
                    //刷新ListView
                    locationInfoList = mLeftLocationInfo.getLastLocationInfo(getContext());
                    mCityAdapter.notifyDataSetChanged();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - downX;

                int newScrollX = (int)(-deltaX+downScrolledX);
                if(-newScrollX > mLeftViewWidth){
                    newScrollX = (int)-mLeftViewWidth;
                }
                if(newScrollX >0)
                {
                    newScrollX = 0;
                }
                scrollTo(newScrollX, 0);
                break;
            case MotionEvent.ACTION_UP:
                int upScrolledX = getScrollX();
                if(-upScrolledX > mLeftViewWidth/2){
                    scrollTo((int)-mLeftViewWidth, 0);
                }else{
                    scrollTo(0, 0);
                }

                downScrolledX = 0;
                break;
        }

        return true;
    }

    private class CityListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return locationInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return locationInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = View.inflate(getContext(), R.layout.left_city_item, null);
            TextView tv_city_name = (TextView)view.findViewById(R.id.tv_city_name);

            String fullLocationName = locationInfoList.get(position).getmFullLocation();
            String city = OtherUtils.getLowestLocation(fullLocationName);
            tv_city_name.setText(city);

            ImageView iv_delete = (ImageView)view.findViewById(R.id.iv_delete);
            iv_delete.setOnClickListener(new CityDeleteClickListener(fullLocationName));
            return view;
        }
    }

    class CityDeleteClickListener implements OnClickListener{
        String fullLocationName ;

        CityDeleteClickListener(String fullLocationName){
            this.fullLocationName = fullLocationName;
        }

        @Override
        public void onClick(View v) {
            mLeftLocationInfo.removeCity(getContext(), fullLocationName);
            locationInfoList = mLeftLocationInfo.getLastLocationInfo(getContext());
            mCityAdapter.notifyDataSetChanged();
        }
    }
}
