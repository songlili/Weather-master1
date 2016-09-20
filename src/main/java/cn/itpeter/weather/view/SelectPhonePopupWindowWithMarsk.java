package cn.itpeter.weather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by annpeter on 11/16/15.
 */
public class SelectPhonePopupWindowWithMarsk extends RelativeLayout {
    private SelectPhonePopupWindow popupWindow = null;
    Context context = null;
    boolean popupWindowShowing;

    public SelectPhonePopupWindowWithMarsk(Context context) {
        super(context);
        initUi(context);
    }

    public SelectPhonePopupWindowWithMarsk(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUi(context);
    }

    public SelectPhonePopupWindowWithMarsk(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initUi(context);
    }

    private void initUi(Context context){
        this.context = context;
        popupWindow = new SelectPhonePopupWindow(getContext(), null);
        popupWindowShowing = true;
        popupWindow.showAtLocation(this, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    public void setMorePhoneListAdapter(BaseAdapter adapter){
        popupWindow.setMorePhoneListAdapter(adapter);
    }




    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(getVisibility() == VISIBLE && popupWindowShowing){
            this.setBackgroundColor(0xFFFFFF);
            popupWindowShowing = false;
            popupWindow.dismiss();
            setVisibility(GONE);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setConfirmOnClickListener(View.OnClickListener confirmOnClickListener, AdapterView.OnItemClickListener itemClickListener){
        popupWindow.setConfirmOnClickListener(confirmOnClickListener, itemClickListener);
    }

    public void dismiss(){
        this.setBackgroundColor(0xFFFFFF);
        popupWindowShowing = false;
        popupWindow.dismiss();
        setVisibility(GONE);
    }
}
