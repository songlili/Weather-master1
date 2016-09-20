package cn.itpeter.weather.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.itpeter.weather.R;
import cn.itpeter.weather.utils.OtherUtils;

/**
 * Created by annpeter on 11/16/15.
 */
public class SelectPhonePopupWindow extends PopupWindow {
    private View view;
    private Button btn_confirm;
    private ListView lv_more_phone;
    private LinearLayout ll_popup;
    private Context context;

    public SelectPhonePopupWindow(Context context, ViewGroup rootView) {
        super(context);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.select_phone_popup_window, rootView);
        btn_confirm = (Button)view.findViewById(R.id.btn_confirm);

        this.setContentView(view);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        this.setFocusable(false);

        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);

        ll_popup = (LinearLayout)view.findViewById(R.id.ll_popup);
        lv_more_phone = (ListView)ll_popup.findViewById(R.id.lv_more_phone);

        getContentView().measure(0, 0);//调用此函数用于强制计算控价宽高,在下面方便调用ll_popup.getMeasuredHeight()计算pop出来的高度,用于计算动画移动距离

        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_popup, "translationY", ll_popup.getMeasuredHeight(), 0);
        animator.setDuration(300);
        animator.start();
    }

    public void setMorePhoneListAdapter(BaseAdapter adapter){
        lv_more_phone.setAdapter(adapter);
    }

    /**
     * @param confirmOnClickListener 设置确定按钮监听事件,可以为空
     * @param itemClickListener 设置每个item的监听事件,不能为空
     */
    public void setConfirmOnClickListener(View.OnClickListener confirmOnClickListener, AdapterView.OnItemClickListener itemClickListener){
        if(confirmOnClickListener != null){
            btn_confirm.setOnClickListener(confirmOnClickListener);
        }
        lv_more_phone.setOnItemClickListener(itemClickListener);
    }

}
