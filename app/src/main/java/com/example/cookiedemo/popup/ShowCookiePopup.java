package com.example.cookiedemo.popup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookiedemo.R;
import com.example.cookiedemo.utils.GsonUtil;

import razerdp.basepopup.BasePopupWindow;

/**
 * Create on 2020/3/10 14:14
 * author revstar
 * Email 1967919189@qq.com
 */
public class ShowCookiePopup extends BasePopupWindow {
    private String cookie;
    private TextView tv_cookie;
    public ShowCookiePopup(Context context,String cookie) {
        super(context);
        tv_cookie=findViewById(R.id.tv_cookie);
        this.cookie= GsonUtil.toFormat(cookie,true,false);
        tv_cookie.setText(this.cookie);
        tv_cookie.setMovementMethod(ScrollingMovementMethod.getInstance());
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clipData = ClipData.newPlainText("Label", this.cookie);
            cm.setPrimaryClip(clipData);
            Toast.makeText(context, "已复制到剪切板", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowCookiePopup.this.dismiss();
            }
        });
    }


    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.cookie_pop_item);
    }
}
