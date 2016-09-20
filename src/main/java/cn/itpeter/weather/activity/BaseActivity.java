package cn.itpeter.weather.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import cn.itpeter.weather.R;


public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在initData,initUi,initEvent中进行页面跳转,本Activity的所有生命方法依然会被执行,包括onCreate, onStart, onResume
        initData();
        initUi();
        initEvent();
    }

    /**
     * 用于读取网络或者文件数据库等数据
     */
    abstract void initData();

    /**
     * 用于初始化界面,获取界面控件
     */
    abstract void initUi();

    /**
     * 用于给控件添加响应事件
     */
    abstract void initEvent();
}
