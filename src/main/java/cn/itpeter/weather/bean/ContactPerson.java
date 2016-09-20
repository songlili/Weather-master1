package cn.itpeter.weather.bean;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by annpeter on 11/16/15.
 */
public class ContactPerson {
    String name = null;
    Uri headImgUri = null;
    List<String> phoneList = null;

    public ContactPerson(){
        phoneList = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhoneList() {
        return phoneList;
    }

    public void addPhone(String phone){
        phoneList.add(phone);
    }

    public Uri getHeadImgUri() {
        return headImgUri;
    }

    public void setHeadImgUri(Uri headImgUri) {
        this.headImgUri = headImgUri;
    }

    @Override
    public String toString() {
        return "name:"+name+  "  headImgUri:"+headImgUri.toString() +"  phoneArray:" + phoneList.toString();
    }

    public static List<ContactPerson> getContacts(Context context) {

        List<ContactPerson> list = new ArrayList<ContactPerson>();

        Uri id_uri = Uri.parse("content://com.android.contacts/contacts");
        ContentResolver resolver = context.getContentResolver();
        Cursor idCursor = resolver.query(id_uri, new String[] { "_id" }, null, null, null);
        while (idCursor.moveToNext()) {
            int contractId = idCursor.getInt(0);
            Uri data_uri = Uri.parse("content://com.android.contacts/contacts/" + contractId + "/data");

            String TYPE_NAME = "vnd.android.cursor.item/name";
            String TYPE_PHONE = "vnd.android.cursor.item/phone_v2";

            Cursor dataCursor = resolver.query(data_uri, new String[]{"data1", "mimetype"}, "mimetype=? or mimetype=?", new String[]{TYPE_NAME, TYPE_PHONE}, null);

            ContactPerson contactPerson = new ContactPerson();

            while (dataCursor.moveToNext()) {
                String data = dataCursor.getString(0);
                String dataType = dataCursor.getString(1);

                //只添加联系人的用户名,头像uri和号码信息
                if(dataType.equals(TYPE_NAME)){
                    contactPerson.setName(data);
                }else if(dataType.equals(TYPE_PHONE)){
                    contactPerson.addPhone(data);
                }
            }

            if((!TextUtils.isEmpty(contactPerson.getName())) && contactPerson.getPhoneList().size() > 0){
                //获取联系人头像
                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contractId);
                contactPerson.setHeadImgUri(uri);
                list.add(contactPerson);
            }
        }
        return list;
    }
}
