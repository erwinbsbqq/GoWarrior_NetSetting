package com.gowarrior.settinglibrary;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Jim.Yu on 2015/6/2.
 */
public class UnityEditText extends LinearLayout {

    private final static String LOGTAG = UnityEditText.class.getSimpleName();

    private TextView mLabel;
    private SensitiveEditText mEdit;
    private String mHint;
    private ColorStateList mLabelColor;
    private ColorStateList mHintColor;
    private UnityEditText mInstance = this;
    private OnImeBackListener mOnImeBackListener;
    private OnEditorActionListener mOnEditorActionListener;

    public interface OnImeBackListener {
        public abstract void onImeBack(UnityEditText view, String text);
    }

    public interface OnEditorActionListener {

        public abstract boolean onEditorAction(UnityEditText view, int actionId, KeyEvent event);

    }

    public UnityEditText(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initWidget(context, null);
    }

    public UnityEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initWidget(context, attrs);
    }

    public UnityEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initWidget(context, attrs);
    }

    public void setOnEditorActionListener(OnEditorActionListener listener) {
        mOnEditorActionListener = listener;
    }

    public void setOnImeBackListener(OnImeBackListener listener) {
        mOnImeBackListener = listener;
    }

    public void setLabel(String label) {
        mLabel.setText(label);
    }

    public void setHint(String hint) {
        mHint = hint;
        if (mEdit.isFocused()) {
            mEdit.setHint(hint);
        }
    }

    public void setText(String text) {
        mEdit.setText(text);
    }

    public String getText() {
        return mEdit.getText().toString();
    }

    public void setSelection(int index) {
        mEdit.setSelection(index);
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) mEdit.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.showSoftInput(mEdit, 0);
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mEdit.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
    }

    private void initWidget(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UnityEditText, 0, 0);
        boolean password = a.getBoolean(R.styleable.UnityEditText_password, false);
        boolean singleLine = a.getBoolean(R.styleable.UnityEditText_singleLine, true);
        String text = a.getString(R.styleable.UnityEditText_text);
        String label = a.getString(R.styleable.UnityEditText_label);
        String hint = a.getString(R.styleable.UnityEditText_hint);
        ColorStateList textColor = a.getColorStateList(R.styleable.UnityEditText_textColor);
        ColorStateList labelTextColor = a.getColorStateList(
                R.styleable.UnityEditText_labelTextColor);

        if (labelTextColor == null) {
            labelTextColor = textColor;
        }

        int hintTextColor = a.getColor(R.styleable.UnityEditText_hintTextColor, 0);
        float textSize = a.getDimension(R.styleable.UnityEditText_textSize, -1f);
        a.recycle();
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.unity_edit_text, this, true);

        mLabel = (TextView)findViewById(R.id.label);
        mEdit = (SensitiveEditText)findViewById(R.id.edit);

        if (textSize > 0) {
            mLabel.setTextSize(textSize);
            mEdit.setTextSize(textSize);
        }

        if (labelTextColor != null) {
            mLabel.setTextColor(labelTextColor);
            mLabelColor = labelTextColor;
        }

        if (hintTextColor != 0) {
            mEdit.setHintTextColor(hintTextColor);
        }

        if (textColor != null) {
            mEdit.setTextColor(textColor);
        }

        if (label != null) {
            mLabel.setText(label);
        }

        if (hint != null) {
//            mEdit.setHint(hint);
            mHint = hint;
        }

        if (text != null) {
            mEdit.setText(text);
        }

        mEdit.setSingleLine(singleLine);

        if (password) {
            mEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }

        mEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                // TODO Auto-generated method stub
                Log.v(LOGTAG, "view=" + view + " focus=" + hasFocus + " hint=" + mHint);

                if (view != mEdit)
                    return;

                setSelected(hasFocus);
//                mLabel.setSelected(hasFocus);

                if (hasFocus) {
                    mEdit.setHint(mHint);
//                    showKeyboard();
                } else {
                    mEdit.setHint(null);
//                    hideKeyboard();
                }

                if (mLabelColor != null) {
                    int color = mLabelColor.getColorForState(hasFocus?FOCUSED_STATE_SET:EMPTY_STATE_SET, 0);
                    if (color != 0) {
                        mLabel.setTextColor(color);
                    }
                }
            }
        });

        mEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int actionId,
                                          KeyEvent event) {
                // TODO Auto-generated method stub
                if (mOnEditorActionListener != null) {
                    return mOnEditorActionListener.onEditorAction(mInstance, actionId, event);
                }
                return false;
            }
        });

        mEdit.setOnImeBackListener(new SensitiveEditText.OnImeBackListener() {

            @Override
            public void onImeBack(SensitiveEditText view, String text) {
                // TODO Auto-generated method stub
                if (mOnImeBackListener != null) {
                    mOnImeBackListener.onImeBack(mInstance, text);
                }
            }
        });
    }
}
