package cn.itpeter.weather.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import cn.itpeter.weather.R;

public class SplashActivity extends BaseActivity {

    private static MessageHandler handler = null;

    private static final int CODE_HAD_SLEEP_FIVE_SECONDS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }

    @Override
    void initData() {
        handler = new MessageHandler(this);

        copyDatabases();
    }

    @Override
    void initEvent() {
        //Intent intent = new Intent(SplashActivity.this, EditMsgActivity.class);
        //startActivity(intent);
        goMainActivity();
    }


    private void goMainActivity(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(5);

                    Message message = Message.obtain();
                    message.what = CODE_HAD_SLEEP_FIVE_SECONDS;
                    handler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    @Override
    void initUi() {

    }

    /**
     * 拷贝省份,城市,区域的信息数据库到databases目录
     */
    private void copyDatabases() {
        String DATABASES_DIR = "/data/data/cn.itpeter.weather/databases/";
        File databasesDir = new File(DATABASES_DIR);
        if (!databasesDir.isDirectory()) {
            databasesDir.mkdir();
        }
        File provinceCityCountyDB = new File(DATABASES_DIR + "china.db");
        if (!provinceCityCountyDB.exists()) {
            try {
                InputStream fis = getAssets().open("china.db");
                FileOutputStream fos = new FileOutputStream(provinceCityCountyDB);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = fis.read(buffer)) != -1) {
                    fos.write(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class MessageHandler extends Handler {

        WeakReference reference = null;

        MessageHandler(SplashActivity activity) {
            reference = new WeakReference<SplashActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CODE_HAD_SLEEP_FIVE_SECONDS:
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

}
