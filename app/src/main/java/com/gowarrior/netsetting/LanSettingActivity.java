package com.gowarrior.netsetting;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gowarrior.settinglibrary.UnityEditText;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lib.widget.wheel.adapters.NumericWheelAdapter;
import lib.widget.wheel.OnWheelChangedListener;
import lib.widget.wheel.OnWheelScrollListener;
import lib.widget.wheel.WheelView;

/**
 * Created by GoWarrior on 2015/7/5.
 */
public class LanSettingActivity extends Activity {
    private final static String LOGTAG = LanSettingActivity.class.getSimpleName();

    private final static int ID_ADDRESS_IP = 0;
    private final static int ID_ADDRESS_MASK = 1;
    private final static int ID_ADDRESS_GATEWAY = 2;
    private final static int ID_ADDRESS_DNS = 3;
    private final static int NUM_OF_ID_ADDRESS = 4;
    private InetAddress[] mAddress = new InetAddress[NUM_OF_ID_ADDRESS];

    private Context mContext;
    private MainScene mMainScene;
    private ManualScene mManualScene;
    private DialScene mDialScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.net_setting_lan);

        mContext = this;
        mMainScene = new MainScene();
        mManualScene = new ManualScene();
        mDialScene = new DialScene();
        mMainScene.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(View.VISIBLE == mManualScene.getVisibility()) {
                    mManualScene.hide();
                    mMainScene.show();
                    return true;
                } else if (View.VISIBLE == mDialScene.getVisibility()) {
                    mDialScene.hide();
                    mMainScene.show();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class Scene {
        private View mContainer;

        public Scene(int id) {
            mContainer = findViewById(id);
        }

        public void show() {
            mContainer.setVisibility(View.VISIBLE);
        }

        public void hide() {
            mContainer.setVisibility(View.INVISIBLE);
        }

        public int getVisibility() {
            return mContainer.getVisibility();
        }
    }

    private class MainScene extends Scene implements View.OnClickListener {
        private final static String LOGTAG = "MainScene";

        private Button mFocusButton;
        private int[] mTextIds = {
                R.id.net_setting_lan_ip,
                R.id.net_setting_lan_mask,
                R.id.net_setting_lan_gateway,
                R.id.net_setting_lan_dns
        };

        public MainScene() {
            super(R.id.net_setting_lan_view);

            Button button;
            button = (Button)findViewById(R.id.net_setting_lan_towlan);
            button.setOnClickListener(this);
            button = (Button)findViewById(R.id.net_setting_lan_dial);
            button.setOnClickListener(this);
            button = (Button)findViewById(R.id.net_setting_lan_manual);
            button.setOnClickListener(this);
            button = (Button)findViewById(R.id.net_setting_lan_auto);
            button.setOnClickListener(this);
            mFocusButton = button;

            getIpInfo();
            updateIpInfo();
        }

        @Override
        public void onClick(View v) {
            mFocusButton = (Button)v;
            switch (v.getId()) {
                case R.id.net_setting_lan_auto:
                    break;
                case R.id.net_setting_lan_manual:
                    mMainScene.hide();
                    mManualScene.show();
                    break;
                case R.id.net_setting_lan_dial:
                    mMainScene.hide();
                    mDialScene.show();
                    break;
                case R.id.net_setting_lan_towlan:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void show() {
            super.show();
            mFocusButton.requestFocus();
        }

        public void updateIpInfo() {
            for(int i = 0; i < mTextIds.length; i++) {
                TextView tv = (TextView)findViewById(mTextIds[i]);
                if (null != mAddress[i]) {
                    tv.setText(mAddress[i].getHostAddress());
                } else {
                    tv.setText("");
                }
            }
        }
    }

    private class ManualScene extends Scene implements
            View.OnFocusChangeListener, View.OnClickListener {
        private final static String LOGTAG = "ManualScene";

        private final int ID_DIGIT_HUNDREDS = 0;
        private final int ID_DIGIT_TENS = 1;
        private final int ID_DIGIT_ONES = 2;
        private final int NUM_OF_ID_DIGIT = 3;
        private int mValueId;
        private int mFocusIndex;
        private ImageView mFocusFrame;
        private Button mButton;
        private WheelView mFocusWheel;
        private TextView mTitle;
        private InetAddress[] mDefaultAddress;
        private int[] mTitleIds = {
                R.string.label_net_setting_ip,
                R.string.label_net_setting_mask,
                R.string.label_net_setting_gateway,
                R.string.label_net_setting_dns
        };
        private int[] mWheelIds = {
                R.id.digit_1,
                R.id.digit_2,
                R.id.digit_3,
                R.id.digit_4,
                R.id.digit_5,
                R.id.digit_6,
                R.id.digit_7,
                R.id.digit_8,
                R.id.digit_9,
                R.id.digit_10,
                R.id.digit_11,
                R.id.digit_12
        };

        public ManualScene() {
            super(R.id.net_setting_lan_manual_view);

            String ip = "192.168.1.101";
            String mask = "255.255.255.0";
            String route = "192.168.1.1";
            String dns = "192.168.1.1";
            mDefaultAddress = new InetAddress[NUM_OF_ID_ADDRESS];

            try {
                mDefaultAddress[ID_ADDRESS_IP] = InetAddress.getByName(ip);
                mDefaultAddress[ID_ADDRESS_MASK] = InetAddress.getByName(mask);
                mDefaultAddress[ID_ADDRESS_GATEWAY] = InetAddress.getByName(route);
                mDefaultAddress[ID_ADDRESS_DNS] = InetAddress.getByName(dns);
            } catch (Exception e) {
                Log.v(LOGTAG, "InetAddress ERROR!");
            }

            for (int i = 0; i < mWheelIds.length; i++) {
                initWheel(mWheelIds[i]);
            }

            mFocusFrame = (ImageView) findViewById(R.id.net_setting_lan_focus_frame);
            mButton = (Button) findViewById(R.id.net_setting_lan_button_next);
            mButton.setOnFocusChangeListener(this);
            mButton.setOnClickListener(this);
            mTitle = (TextView) findViewById(R.id.net_setting_lan_manual_title);
        }

        @Override
        public void show() {
            super.show();
            mValueId = ID_ADDRESS_IP;
            Log.v(LOGTAG, "show: ip=" + mAddress[ID_ADDRESS_IP]);
            initValue(mValueId);

            mTitle.setText(mTitleIds[mValueId]);
            mButton.setText(R.string.label_net_setting_action_next);

            mFocusIndex = 0;
            mFocusWheel = getWheel(mWheelIds[mFocusIndex]);
            mFocusWheel.requestFocus();
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            Log.v(LOGTAG, "onFocusChange: v=" + v + " focus=" + hasFocus);
            if (!hasFocus)
                return;
            int id = v.getId();

            if (id == R.id.net_setting_lan_button_next) {
                mFocusFrame.setVisibility(View.INVISIBLE);
                mFocusWheel = null;
            } else if (v instanceof WheelView) {
                if (mFocusWheel == null) {
                    setFocusWheel((WheelView) v, false);
                } else {
                    setFocusWheel((WheelView) v, true);
                }
            }
        }

        @Override
        public void onClick(View v) {
            try {
                mAddress[mValueId] = InetAddress.getByAddress(getAddressValue());
            } catch (UnknownHostException e) {
                Log.e(LOGTAG, ""+e.getMessage());
            }

            mValueId++;
            if (mValueId < NUM_OF_ID_ADDRESS) {
                initValue(mValueId);
                mTitle.setText(mTitleIds[mValueId]);

                if (mValueId == (NUM_OF_ID_ADDRESS - 1)) {
                    mButton.setText(R.string.label_net_setting_action_connect);
                }
            } else {
                super.hide();
                mMainScene.show();
            }
        }

        /**
         * Initializes wheel
         */
        private void initWheel(int id) {
            WheelView wheel = getWheel(id);
            NumericWheelAdapter adapter = new NumericWheelAdapter(mContext, 0, 9);
            adapter.setItemResource(R.layout.net_setting_ip_wheel_text);
            adapter.setItemTextResource(R.id.text);
            wheel.setViewAdapter(adapter);
            wheel.setCurrentItem((int) (Math.random() * 10));

            wheel.setFocusable(true);
            wheel.setFocusableInTouchMode(true);
            wheel.setOnFocusChangeListener(this);

            wheel.setDrawShadows(false);
            wheel.setVisibleItems(1);
            wheel.setWheelBackground(R.drawable.net_setting_ip_wheel_bg);
            wheel.setWheelForeground(R.color.transparent);

            wheel.setCyclic(true);
            wheel.setInterpolator(new AnticipateOvershootInterpolator());
        }

        /**
         * get wheel by id
         */
        private WheelView getWheel(int id) {
            return (WheelView)findViewById(id);
        }

        /**
         * init value of wheel
         */
        private void initValue(int id) {
            byte[] values;

            if(null == mAddress[id]) {
                mAddress[id] = mDefaultAddress[id];
            }
            values = mAddress[id].getAddress();
            Log.v(LOGTAG, "initValue: values=" + values);

            for (int j = 0; j < values.length; j++) {
                int number = values[j] & 0xFF;
                Log.v(LOGTAG, "initValue: value[" + j + "]=" + number);
                setWheelRange(j*NUM_OF_ID_DIGIT, number);
            }
        }

        private void setFocusWheel(WheelView wheel, boolean showAnimation) {
            if (wheel == mFocusWheel)
                return;
            mFocusWheel = wheel;
            updateRange(wheel);
            mFocusFrame.setVisibility(View.VISIBLE);

            float x = wheel.getX()
                    - (mFocusFrame.getWidth() - wheel.getWidth()) / 2;
            Log.v(LOGTAG, "setWheelFocus: old_x=" + mFocusFrame.getX()
                    + " new_x=" + x);
            if (showAnimation) {
                ViewPropertyAnimator vpa = mFocusFrame.animate();
                vpa.x(x);
                vpa.setDuration(250);
                vpa.setInterpolator(new AccelerateDecelerateInterpolator());
            } else {
                mFocusFrame.setX(x);
            }
        }

        private void updateRange(WheelView wheel) {
            int id = wheel.getId();
            // find id index
            int i;
            for (i = 0; i < mWheelIds.length; i++) {
                if (id == mWheelIds[i])
                    break;
            }
            if (i >= mWheelIds.length) {
                Log.v(LOGTAG, "updateRange: can't found id=" + id);
                return;
            }
            int startIndex = (i / NUM_OF_ID_DIGIT) * NUM_OF_ID_DIGIT;
            int value = getWheelValue(startIndex);
            Log.v(LOGTAG, "updateRange: value=" + value);
            setWheelRange(startIndex, value);
        }

        private byte[] getAddressValue() {
            byte[] values = new byte[4];
            for(int j = 0; j < values.length; j++) {
                values[j] = (byte)getWheelValue(j*NUM_OF_ID_DIGIT);
            }
            return values;
        }

        private int getWheelValue(int idStartIndex) {
            int value = 0;
            for (int i = ID_DIGIT_HUNDREDS; i < NUM_OF_ID_DIGIT; i++) {
                WheelView wheel = getWheel(mWheelIds[idStartIndex + i]);
                value = value * 10 + wheel.getCurrentItem();
            }
            return value;
        }

        private void setWheelRange(int idStartIndex, int value) {
            int digit = value;
            for (int i = ID_DIGIT_HUNDREDS, divider = 100; i < NUM_OF_ID_DIGIT; i++) {

                WheelView wheel = getWheel(mWheelIds[idStartIndex + i]);
                NumericWheelAdapter adapter = new NumericWheelAdapter(mContext,
                        0, getDigitMax(value, i));
                adapter.setItemResource(R.layout.net_setting_ip_wheel_text);
                adapter.setItemTextResource(R.id.text);
                wheel.setViewAdapter(adapter);
                wheel.setCurrentItem(digit / divider);

                digit %= divider;
                divider /= 10;
            }
        }

        private int getDigitMax(int value, int digitId) {
            int max = 9;
            if (ID_DIGIT_HUNDREDS == digitId) {
                max = getHundredsMax(value);
            } else if (ID_DIGIT_TENS == digitId) {
                max = getTensMax(value);
            } else if (ID_DIGIT_ONES == digitId) {
                max = getOnesMax(value);
            }
            return max;
        }

        private int getHundredsMax(int value) {
            int max = 2;
            if((value%100) > 55) {
                max = 1;
            }
            return max;
        }

        private int getTensMax(int value) {
            int max = 9;
            if (value >= 200) {
                int ones = value % 10;
                if (ones > 5) {
                    max = 4;
                } else {
                    max = 5;
                }
            }
            return max;
        }

        private int getOnesMax(int value) {
            int max = 9;
            if (value >= 250) {
                max = 5;
            }
            return max;
        }
    }

    private class DialScene extends Scene {
        private UnityEditText mAccount;
        private UnityEditText mPassword;
        private Button mButton;
        private TextView mStateTextView;

        public DialScene() {
            super(R.id.net_setting_lan_dial_view);

            mAccount = (UnityEditText) findViewById(R.id.net_setting_lan_dial_account);
            mPassword = (UnityEditText) findViewById(R.id.net_setting_lan_dial_password);
            mStateTextView = (TextView) findViewById(R.id.net_setting_lan_pppoe_state);
            mButton = (Button) findViewById(R.id.net_setting_lan_pppoe_button);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mPassword.setOnEditorActionListener(mOnEditorActionListener);
        }

        UnityEditText.OnEditorActionListener mOnEditorActionListener = new UnityEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(UnityEditText view, int actionId, KeyEvent event) {
                Log.v(LOGTAG, "onEditorAction: id=" + view.getId() + " action=" + actionId);
                return false;
            }
        };

        @Override
        public void show() {
            super.show();
            mStateTextView.setText("");
            if(mPassword.getText().isEmpty()){
                mAccount.showKeyboard();
            } else {
                mButton.requestFocus();
            }
        }
    }

    /**
     * get ip info of lan
     */
    private void getIpInfo() {
        String ip = "192.168.1.101";
        String mask = "255.255.255.0";
        String route = "192.168.1.1";
        String dns = "192.168.1.1";

        mAddress[ID_ADDRESS_IP] = null;
        mAddress[ID_ADDRESS_MASK] = null;
        mAddress[ID_ADDRESS_GATEWAY] = null;
        mAddress[ID_ADDRESS_DNS] = null;

        Log.i(LOGTAG, "ip: " + ip);
        Log.i(LOGTAG, "mask: " + mask);
        Log.i(LOGTAG, "route: " + route);
        Log.i(LOGTAG, "dns: " + dns);

        try {
            mAddress[ID_ADDRESS_IP] = InetAddress.getByName(ip);
            mAddress[ID_ADDRESS_MASK] = InetAddress.getByName(mask);
            mAddress[ID_ADDRESS_GATEWAY] = InetAddress.getByName(route);
            mAddress[ID_ADDRESS_DNS] = InetAddress.getByName(dns);
        } catch (UnknownHostException e) {
            Log.v(LOGTAG, "Lan UnknownHost Exception.");
            return;
        }
    }
}
