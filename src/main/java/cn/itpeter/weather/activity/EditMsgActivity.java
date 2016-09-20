package cn.itpeter.weather.activity;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.itpeter.weather.R;

public class EditMsgActivity extends BaseActivity {

    List<String> phoneList; //所有需要发送短信的手机号列表
    List<String> nameList;  //所有被发送短信的联系人表

    @ViewInject(R.id.et_name_list)
    EditText et_name_list;
    @ViewInject(R.id.et_text)
    EditText et_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_edit_msg);

        ViewUtils.inject(this);
        super.onCreate(savedInstanceState);

    }

    @Override
    void initData() {
        phoneList = getIntent().getStringArrayListExtra("phonelist");
        nameList = getIntent().getStringArrayListExtra("namelist");
    }

    @Override
    void initEvent() {

    }

    @Override
    void initUi() {
        et_name_list.setText(et_name_list.getText().toString() + getNameListString());
    }

    /**
     * @return 返回所有联系人姓名列表,中间用 ', ' 隔开
     */
    private String getNameListString(){
        if(nameList == null){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for(String name : nameList){
            if(isFirst){
                sb.append(name);
                isFirst = false;
            }else{
                sb.append(", "+name);
            }
        }
        return sb.toString();
    }

    /**
     * 设置在Button上,遍历所有手机号发送短信
     * @param view
     */
    public void sendMsgs(View view){

        String text = et_text.getText().toString();

        //如果发送文字为空,提示并返回
        if(TextUtils.isEmpty(text)){
            Toast.makeText(this, "分享内容不能为空哦", Toast.LENGTH_SHORT).show();
            return;
        }

        SmsManager sm = SmsManager.getDefault();
        String regEx = "[^0-9]";
        Pattern pattern = Pattern.compile(regEx);

        for(String phoneNum : phoneList){
            Matcher matcher = pattern.matcher(phoneNum);
            String num = matcher.replaceAll("").trim();
            sm.sendTextMessage(num, null, text, null, null);
        }

        Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        et_text.setText("");
    }

    //设置给Button,用于页面返回
    public void back(View view){
        finish();
    }
}
