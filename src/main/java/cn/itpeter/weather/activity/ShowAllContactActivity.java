package cn.itpeter.weather.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.db.annotation.Check;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.itpeter.weather.R;
import cn.itpeter.weather.bean.ContactPerson;
import cn.itpeter.weather.view.ContactListViewItem;
import cn.itpeter.weather.view.SelectPhonePopupWindowWithMarsk;

public class ShowAllContactActivity extends BaseActivity {

    @ViewInject(R.id.lv_contact)
    ListView lv_contact = null;

    List<ContactPerson> contactPersonList = null;

    private Set<String> allSelectPhoneNumSet;  //所有被选中要发分享的手机号
    private Set<String> thisSelectPhoneNumSet;  //当前被选中要分享的手机号

    private CheckBox cb_checked_all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_show_all_contact);

        ViewUtils.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    void initData() {
        allSelectPhoneNumSet = new HashSet<String>();
        contactPersonList = ContactPerson.getContacts(this);
    }

    @Override
    void initUi() {
        lv_contact.setAdapter(new ContactAdapter());
        cb_checked_all = (CheckBox)findViewById(R.id.cb_checked_all);
    }

    @Override
    void initEvent() {

        cb_checked_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(ContactPerson person: contactPersonList){
                    if(isChecked){
                        allSelectPhoneNumSet.addAll(person.getPhoneList());
                    }else{
                        allSelectPhoneNumSet.clear();
                    }

                    for(int i =0; i < lv_contact.getChildCount(); i++){
                        CheckBox cb_checked = (CheckBox)lv_contact.getChildAt(i).findViewById(R.id.cb_checked);
                        cb_checked.setChecked(isChecked);
                    }
                }
            }
        });

        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<String> phoneList = contactPersonList.get(position).getPhoneList();

                if (phoneList.size() > 1) {//有多个手机号码的联系人
                    showMorePhone(view, phoneList);
                } else {//只有一个号码的联系人
                    ContactListViewItem item = (ContactListViewItem) view;
                    item.setChecked(!item.isChecked());

                    String phoneNum = contactPersonList.get(position).getPhoneList().get(0);//获取该联系人的手机号码
                    if(item.isChecked()){
                        allSelectPhoneNumSet.add(phoneNum);
                    }else{
                        allSelectPhoneNumSet.remove(phoneNum);
                    }
                }
            }
        });
    }

    /**
     * 显示多个手机号,并设置他们的监听事件
     * @param viewParentItem 上一级中被点击的item
     * @param phoneList 多个手机号的手机号list
     */
    private void showMorePhone(final View viewParentItem, final List<String> phoneList) {

        thisSelectPhoneNumSet = new HashSet<String>();

        final SelectPhonePopupWindowWithMarsk popupWindowWithMarsk = (SelectPhonePopupWindowWithMarsk) View.inflate(this, R.layout.select_phone_popup_windown_with_marsk, null);
        this.addContentView(popupWindowWithMarsk, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final MorePhoneAdapter adapter = new MorePhoneAdapter(this, phoneList);
        popupWindowWithMarsk.setMorePhoneListAdapter(adapter);

        //more_phone每个item的监听事件
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CheckBox ch_checked = (CheckBox)view.findViewById(R.id.cb_checked);

                ch_checked.setChecked(!ch_checked.isChecked());

                if(ch_checked.isChecked()){
                    thisSelectPhoneNumSet.add(phoneList.get(position));
                }else{
                    thisSelectPhoneNumSet.remove(phoneList.get(position));
                }
            }
        };

        //确认按钮监听事件
        View.OnClickListener confirmListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //联系人多个号码弹框清除
                popupWindowWithMarsk.dismiss();

                //如果该联系人有一个或一个以上号码被选中,则checkbox为选中状态
                for(String phoneNum : phoneList){
                    allSelectPhoneNumSet.removeAll(phoneList);  //删除原来添加的phone,添加现有的phone
                }
                allSelectPhoneNumSet.addAll(thisSelectPhoneNumSet);
                CheckBox cb_checked = (CheckBox)viewParentItem.findViewById(R.id.cb_checked);
                if(thisSelectPhoneNumSet.size() > 0){
                    cb_checked.setChecked(true);
                }else {
                    cb_checked.setChecked(false);
                }

            }
        };

        //添加监听
        popupWindowWithMarsk.setConfirmOnClickListener(confirmListener, itemClickListener);
    }

    private class MorePhoneAdapter extends BaseAdapter {

        Context context = null;
        List<String> list = null;

        private Set<Integer> checkedSet;

        MorePhoneAdapter(Context context, List list) {
            this.list = list;
            this.context = context;

            checkedSet = new HashSet<Integer>();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = View.inflate(context, R.layout.select_phone_item, null);
            TextView tv_phone = (TextView)view.findViewById(R.id.tv_phone);
            tv_phone.setText(list.get(position));

            CheckBox ch_checked = (CheckBox)view.findViewById(R.id.cb_checked);

            //如果不是第一次进入,并且该号码加入了分享列表
            String phoneNum = list.get(position);
            boolean hasAdded = allSelectPhoneNumSet.contains(phoneNum);
            if(hasAdded){
                ch_checked.setChecked(true);
                thisSelectPhoneNumSet.add(phoneNum);
            }
            return view;
        }
    }

    private class ContactAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contactPersonList.size();
        }

        @Override
        public Object getItem(int position) {
            return contactPersonList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = View.inflate(ShowAllContactActivity.this, R.layout.contact_listview_item, null);
            ContactPerson contactPerson = contactPersonList.get(position);

            try {
                InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), contactPerson.getHeadImgUri());
                Bitmap headImgBitmap = BitmapFactory.decodeStream(is);
                ImageView iv_head_img = (ImageView) view.findViewById(R.id.iv_head_img);
                iv_head_img.setImageBitmap(headImgBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_name.setText(contactPerson.getName());

            if (position == contactPersonList.size() - 1) {
                ImageView iv_under_line = (ImageView) view.findViewById(R.id.iv_under_line);
                iv_under_line.setVisibility(View.GONE);

            }
            if (contactPersonList.get(position).getPhoneList().size() == 1) {
                TextView tv_more_phone = (TextView) view.findViewById(R.id.tv_more_phone);
                tv_more_phone.setVisibility(View.GONE);
            }

            CheckBox ch_checked = (CheckBox)view.findViewById(R.id.cb_checked);

            return view;
        }
    }

    /**
     * 从ListView选择所有选中的人的名字加入到list中, 用于编辑发送消息页面显示
     * @return 所有选中人名列表
     */
    public List<String> getAllContactName(){
        List<String> list = new ArrayList<String>();

        for(int i =0; i < lv_contact.getChildCount(); i++){
            CheckBox cb_checked = (CheckBox)lv_contact.getChildAt(i).findViewById(R.id.cb_checked);
           if(cb_checked.isChecked()){
               String name = ((TextView)lv_contact.getChildAt(i).findViewById(R.id.tv_name)).getText().toString();
               list.add(name);
           }
        }
        return list;
    }

    /**
     * 设置在Button上,用于跳转到信息编辑页面
     * @param view
     */
    public void addContactName(View view){

        if(allSelectPhoneNumSet.size() == 0){
            Toast.makeText(this, "至少要添加一个联系人哦", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EditMsgActivity.class);
        ArrayList<String> nameList = (ArrayList)getAllContactName();
        intent.putStringArrayListExtra("namelist", nameList);
        ArrayList<String> phoneList = new ArrayList<String>();
        phoneList.addAll(allSelectPhoneNumSet);
        intent.putStringArrayListExtra("phonelist", phoneList);
        startActivity(intent);
    }

    /**
     * 设置在Button上,用于返回
     * @param view
     */
    public void back(View view){
        finish();
    }
}
