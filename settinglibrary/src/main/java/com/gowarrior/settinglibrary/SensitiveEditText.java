package com.gowarrior.settinglibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Jim.Yu on 2015/6/2.
 */
public class SensitiveEditText extends EditText {

    private OnImeBackListener mOnImeBackListener;


    public interface OnImeBackListener {
        public abstract void onImeBack(SensitiveEditText view, String text);
    }

    public SensitiveEditText(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public SensitiveEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public SensitiveEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBackListener != null) {
                mOnImeBackListener.onImeBack(this, this.getText().toString());
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnImeBackListener(OnImeBackListener listener) {
        mOnImeBackListener = listener;
    }
}
