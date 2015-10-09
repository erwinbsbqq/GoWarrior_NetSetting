package com.gowarrior.settinglibrary;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jim.Yu on 2015/6/2.
 */
public class CommonDialog extends DialogFragment implements View.OnClickListener{

    private final static String LOGTAG = CommonDialog.class.getSimpleName();

    public final static int ID_BUTTON_1 = R.id.dialog_button_1;
    public final static int ID_BUTTON_2 = R.id.dialog_button_2;
    public final static int ID_BUTTON_3 = R.id.dialog_button_3;

    private final static String ARG_MESSAGE = "message";
    private final static String ARG_BUTTON_NAMES = "button";
    private final static String ARG_STYLE_ID = "style";
    private final static String ARG_MESSAGE_ID = "message_id";
    private final static String ARG_BUTTON_NAME_ARRAY_ID = "button_id";
    private final static String ARG_FOCUS_BUTTON = "focus_button";

    private final int[] mButtonIds = { R.id.dialog_button_1,
            R.id.dialog_button_2, R.id.dialog_button_3 };

    private ViewGroup mButtonGroup;
    private TextView mText;
    private Timer mTimer;
    private Handler mHandler;
    private int mLifeTime;
    private int mTick;
    private boolean mButtonInited = false;
    private int mButtonCount;


    public static CommonDialog showDialog(int msgId, int btnId, int focusIndex,
                                          String tag, Activity activity) {
        return showDialog(msgId, btnId, 0, focusIndex, 0, 0, tag, activity);
    }

    public static CommonDialog showDialog(int msgId, int btnId, int focusIndex, int styleId,
                                          String tag, Activity activity) {
        return showDialog(msgId, btnId, styleId, focusIndex, 0, 0, tag, activity);
    }

    public static CommonDialog showDialog(int msgId, int btnId, int focusIndex, int styleId,
                                          int lifetime, int tick, String tag, Activity activity) {
        CommonDialog dialog = CommonDialog.newInstance(msgId, btnId, styleId, focusIndex);
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        dialog.show(ft, tag);
        dialog.setLifeTime(lifetime, tick);
        return dialog;
    }

    public static CommonDialog newInstance(int messageId, int buttonArrayId,
                                           int styleId, int focusButtonIndex) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_MESSAGE_ID, messageId);
        bundle.putInt(ARG_BUTTON_NAME_ARRAY_ID, buttonArrayId);
        bundle.putInt(ARG_STYLE_ID, styleId);
        bundle.putInt(ARG_FOCUS_BUTTON, focusButtonIndex);
        return createInstance(bundle);
    }

    public static CommonDialog newInstance(String message, String[] buttonNames,
                                           int styleId, int focusButtonIndex) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_MESSAGE, message);
        bundle.putStringArray(ARG_BUTTON_NAMES, buttonNames);
        bundle.putInt(ARG_STYLE_ID, styleId);
        bundle.putInt(ARG_FOCUS_BUTTON, focusButtonIndex);
        return createInstance(bundle);
    }

    private static CommonDialog createInstance(Bundle bundle) {
        CommonDialog dialog = new CommonDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(Activity act) {
        // If the activity we're being attached to has
        // not implemented the OnDialogDoneListener
        // interface, the following line will throw a
        // ClassCastException. This is the earliest we
        // can test if we have a well-behaved activity.

        try {
            OnDialogDoneListener test = (OnDialogDoneListener) act;
        } catch (ClassCastException cce) {
            // Here is where we fail gracefully.
            Log.e(LOGTAG, "Activity is not listening");
        }
        super.onAttach(act);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setCancelable(true);
        int style = DialogFragment.STYLE_NO_FRAME, theme = 0;
        setStyle(style, R.style.DialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return createView(inflater, container);
    }

    @Override
    public void onSaveInstanceState(Bundle icicle) {
        super.onPause();
    }

    @Override
    public void onCancel(DialogInterface di) {
        Log.v(LOGTAG, "onCancel");
        notifyDialogDone(-1, null);
        super.onCancel(di);
    }

    @Override
    public void onDismiss(DialogInterface di) {
        Log.v(LOGTAG, "onDismiss");
        cancelTimer();
        super.onDismiss(di);
    }

    public void onClick(View v) {
        if (R.id.dialog_button_1 == v.getId()) {
            notifyDialogDone(0, null);
            dismiss();
        } else if (R.id.dialog_button_2 == v.getId()) {
            notifyDialogDone(1, null);
            dismiss();
        }
    }

    public void setLifeTime(int lifetime, int tick) {

        if (0 == lifetime)
            return;
        if (tick < 100)
            tick = 100;

        if (null != mTimer)
            mTimer.cancel();

        mLifeTime = lifetime;
        mTick = tick;
        mTimer = new Timer();
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

                if (mLifeTime <= mTick) {
                    mLifeTime = 0;
                    mTick = 0;
                    closeDialog();
                }

                mLifeTime -= mTick;
                notifyDialogCountDown();
                super.handleMessage(msg);
            }
        };

        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mHandler.sendEmptyMessage(0);
            }
        }, 0, tick);
    }

    public void setMessage(String message) {
        mText.setText(message);
    }

    private void cancelTimer() {
        if (null != mTimer) {
            Log.v(LOGTAG, "closeDialog: cancel Timer");
            mTimer.cancel();
        }

        if (null != mHandler) {
            Log.v(LOGTAG, "closeDialog: remove messages");
            mHandler.removeMessages(0);
        }
    }

    private void closeDialog() {
        cancelTimer();
        OnDialogDoneListener act = (OnDialogDoneListener) getActivity();
        act.onDialogDone(this.getTag(), -1, null);
        dismiss();
    }

    private void notifyDialogDone(int id, String message) {
        Activity act = getActivity();
        if (act instanceof OnDialogDoneListener) {
            ((OnDialogDoneListener)act).onDialogDone(getTag(), id, message);
        }
    }

    private void notifyDialogCountDown() {
        Activity act = getActivity();
        if (act instanceof OnDialogCountDownListener) {
            ((OnDialogCountDownListener)act).onDialogCountDown(getTag(), mLifeTime, this);
        }
    }

    private View createView(LayoutInflater inflater, ViewGroup container) {
        Bundle bundle = getArguments();
        View v;

        if (R.style.SmallDialog == bundle.getInt(ARG_STYLE_ID)) {
            v = inflater.inflate(R.layout.common_small_dialog, container, false);
        } else {
            v = inflater.inflate(R.layout.common_big_dialog, container, false);
        }

        Context context = v.getContext();
        String message = bundle.getString(ARG_MESSAGE);

        if (null == message) {
            message = context.getResources().getString(bundle.getInt(ARG_MESSAGE_ID));
        }

        mText = (TextView) v.findViewById(R.id.dialog_message);
        mText.setText(message);

        ViewGroup group = (ViewGroup) v.findViewById(R.id.dialog_button_container);
        ViewTreeObserver vto = group.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub

                if (!mButtonInited) {
                    initButtons();
                    mButtonInited = true;
                }
            }
        });

        ViewGroup.LayoutParams params = group.getLayoutParams();

        return v;
    }

    private void initButtons() {
        View v = getView();
        Bundle bundle = getArguments();
        String[] buttonNames = bundle.getStringArray(ARG_BUTTON_NAMES);

        if (null == buttonNames) {
            try {
                buttonNames = v.getResources().getStringArray(
                        bundle.getInt(ARG_BUTTON_NAME_ARRAY_ID));
            } catch (Resources.NotFoundException e) {
                buttonNames = new String[0];
            }
        }

        int focusIndex = bundle.getInt(ARG_FOCUS_BUTTON);
        ViewGroup group = (ViewGroup) v.findViewById(R.id.dialog_button_container);
        int buttonWidth = 0;

        if (0 != buttonNames.length) {
            buttonWidth = group.getWidth() / buttonNames.length; // - group.getPaddingLeft()*(buttonNames.length - 1)
            Log.v(LOGTAG, "initView: buttonWidth=" + buttonWidth + " layoutWidth=" + group.getWidth());
            Log.v(LOGTAG, "initView: paddingLeft=" + group.getPaddingLeft() + " paddingRight=" + group.getPaddingRight());
        } else {
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode
                            || KeyEvent.KEYCODE_VOLUME_UP  == keyCode
                            || KeyEvent.KEYCODE_VOLUME_MUTE  == keyCode) {
                        return true;
                    }

                    if (KeyEvent.ACTION_UP == event.getAction()) {
                        if (KeyEvent.KEYCODE_DPAD_CENTER == keyCode
                                || KeyEvent.KEYCODE_ENTER == keyCode) {
                            notifyDialogDone(-1, null);
                            dismiss();
                            return true;
                        }
                    }
                    if (KeyEvent.KEYCODE_BACK == keyCode) {
                        closeDialog();
                        return true;
                    }
                    return false;
                }
            });
        }

        for (int i = 0; i < mButtonIds.length; i++) {
            Button button = (Button) v.findViewById(mButtonIds[i]);
            ViewGroup.LayoutParams lp = button.getLayoutParams();

            if (i >= buttonNames.length) {
                button.setVisibility(View.INVISIBLE);
                lp.width = 0;
                button.setLayoutParams(lp);
                continue;
            }

            lp.width = buttonWidth;
            button.setLayoutParams(lp);
            button.setText(buttonNames[i]);
            button.setOnClickListener(this);
            button.setOnFocusChangeListener(mOnFocusChangeListener);

            if (i == focusIndex) {
                button.requestFocus();
            }

            button.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode
                            || KeyEvent.KEYCODE_VOLUME_UP == keyCode
                            || KeyEvent.KEYCODE_VOLUME_MUTE == keyCode) {
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            int animId = R.anim.button_focus_out;

            if (hasFocus) {
                animId = R.anim.button_focus_in;
                ViewParent parent = v.getParent();
                parent.bringChildToFront(v);
                parent.requestLayout();
            }

            Animator set = (AnimatorSet) AnimatorInflater.loadAnimator(
                    getActivity().getBaseContext(), animId);
            set.setTarget(v);
            set.start();
        }
    };
}
