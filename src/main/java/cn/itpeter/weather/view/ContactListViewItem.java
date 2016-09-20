package cn.itpeter.weather.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import cn.itpeter.weather.R;

/**
 * Created by annpeter on 11/16/15.
 */
public class ContactListViewItem extends LinearLayout {

    View view = null;
    boolean hasUnderLine = false;   //默认为无

    CheckBox cb_checked = null;

    public ContactListViewItem(Context context) {
        super(context);
        initUi();
    }

    public ContactListViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListViewItem);
        hasUnderLine = typedArray.getBoolean(R.styleable.ListViewItem_hasUnderLine, false);
        initUi();
    }

    public ContactListViewItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initUi();
    }

    private void initUi(){

        view = View.inflate(getContext(), R.layout.view_contact_listview_item, this);
        cb_checked = (CheckBox)view.findViewById(R.id.cb_checked);

        if(hasUnderLine) {
            View iv_under_line = view.findViewById(R.id.iv_under_line);
            iv_under_line.setVisibility(GONE);
        }
    }

    public void setChecked(boolean checked){
        cb_checked.setChecked(checked);
    }

    public boolean isChecked(){
        return cb_checked.isChecked();
    }

}
