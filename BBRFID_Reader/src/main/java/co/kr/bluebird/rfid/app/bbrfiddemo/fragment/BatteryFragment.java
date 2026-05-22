/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class BatteryFragment extends Fragment {
    private static final String TAG = BatteryFragment.class.getSimpleName();

    private static final boolean D = Constants.BAT_D;

    private TextView mBatteryTv;

    private Button mGetChargeBt;

    private Button mGetBatBt;

    //+smart battery
    private Button mGetSmartBattStatus;
    private Button mGetSmartBattVol;
    private Button mGetSmartBattSerial;
    private Button mGetSmartBattPresent;
    private Button mGetSmartBattLevel;
    private Button mGetSmartBattGauge;
    private Button mGetSmartBattHealth;
    private Button mGetSmartBattTemper;
    private Button mGetSmartBattCycleCnt;
    private Button mGetSmartBattCap;
    //smart battery+

    private TextView mMessageTextView;

    private ProgressBar mBatteryProgress;

    private Reader mReader;

    private Context mContext;

    private Handler mOptionHandler;

    private final BatteryHandler mBatteryHandler = new BatteryHandler(this);

    public static BatteryFragment newInstance() {
        return new BatteryFragment();
    }

    private Fragment mFragment;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRepeatingTask = new Runnable() {
        @Override
        public void run() {
            try {
                Log.e(TAG, "check battery status");
                int ret = mReader.SD_GetBatteryStatus();

                //+Always be display Battery
                if (mOptionHandler != null) {
                    mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, -1, ret).sendToTarget();
                }
                //Always be display Battery+
            } finally {
                mHandler.postDelayed(mRepeatingTask, 5000);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.battery_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        mBatteryTv = (TextView) v.findViewById(R.id.battery_state_textview);

        mMessageTextView = (TextView) v.findViewById(R.id.message_textview);

        mGetChargeBt = (Button) v.findViewById(R.id.bt_charge);
        mGetChargeBt.setOnClickListener(buttonListener);

        mGetBatBt = (Button) v.findViewById(R.id.bt_bat);
        mGetBatBt.setOnClickListener(buttonListener);

        mBatteryProgress = (ProgressBar) v.findViewById(R.id.batt_progress);

        //+smart battery
        mGetSmartBattStatus = (Button) v.findViewById(R.id.bt_smart_batt);
        mGetSmartBattStatus.setOnClickListener(buttonListener);

        mGetSmartBattVol = (Button) v.findViewById(R.id.bt_smart_voltage);
        mGetSmartBattVol.setOnClickListener(buttonListener);

        mGetSmartBattSerial = (Button) v.findViewById(R.id.bt_smart_serial);
        mGetSmartBattSerial.setOnClickListener(buttonListener);

        mGetSmartBattPresent = (Button) v.findViewById(R.id.bt_smart_present);
        mGetSmartBattPresent.setOnClickListener(buttonListener);

        mGetSmartBattLevel = (Button) v.findViewById(R.id.bt_smart_level);
        mGetSmartBattLevel.setOnClickListener(buttonListener);

        mGetSmartBattGauge = (Button) v.findViewById(R.id.bt_smart_life_time);
        mGetSmartBattGauge.setOnClickListener(buttonListener);

        mGetSmartBattHealth = (Button) v.findViewById(R.id.bt_smart_health);
        mGetSmartBattHealth.setOnClickListener(buttonListener);

        mGetSmartBattTemper = (Button) v.findViewById(R.id.bt_smart_tmeperature);
        mGetSmartBattTemper.setOnClickListener(buttonListener);

        mGetSmartBattCycleCnt = (Button) v.findViewById(R.id.bt_smart_cycle_cnt);
        mGetSmartBattCycleCnt.setOnClickListener(buttonListener);

        mGetSmartBattCap = (Button) v.findViewById(R.id.bt_smart_capacity);
        mGetSmartBattCap.setOnClickListener(buttonListener);
        //smart battery+
        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mBatteryHandler);

        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            int value = mReader.SD_GetBatteryStatus();
//            Activity activity = getActivity();
//            if (activity != null) {
//                mBatteryTv.setText(activity.getString(R.string.battery_state_str) +
//                        value + " " + activity.getString(R.string.percent_str));
//                mBatteryProgress.setProgress(value);
//            }
            mBatteryTv.setText(value + " %");
            mBatteryProgress.setProgress(value);
            mHandler.post(mRepeatingTask);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "onResume");
        super.onResume();

        // Update Battery Status for Internal RFID [ START ]
        getActivity().invalidateOptionsMenu();
        // Update Battery Status for Internal RFID [ END ]
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        if (D) Log.d(TAG, "onStop");
        super.onStop();

        mHandler.removeCallbacks(mRepeatingTask);
    }

    private OnClickListener buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int ret = -100;
            String retString = null;

            int id = v.getId();
            if (id == R.id.bt_charge) {
                retString = "SD_GetChargeState : ";
                retString += mReader.SD_GetChargeState();
                Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_bat) {
                retString = "SD_GetBatteryStatus : ";
                ret = mReader.SD_GetBatteryStatus();
                retString += (ret + " %");
                mBatteryTv.setText(ret + " %");
                mBatteryProgress.setProgress(ret);
                Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
            //+smart battery
            } else if (id == R.id.bt_smart_serial) {
                String retTemp = mReader.SD_GetSmartBatterySerial();
                if (checkNotSupportedApi(retTemp, -1)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatterySerial : ";
                    retString += retTemp;
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_batt) {
                ret = mReader.SD_GetSmartBatteryStatus();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryStatus : ";
                    retString += ret;
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_voltage) {
                ret = mReader.SD_GetSmartBatteryVoltage();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryVoltage : ";
                    retString += (ret + " mV");
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_present) {
                ret = mReader.SD_GetSmartBatteryPresentStatus();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryPresentStatus : ";
                    retString += ret;
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_level) {
                ret = mReader.SD_GetSmartBatteryLevel();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryLevel : ";
                    retString += ret;
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_life_time) {
                ret = mReader.SD_GetSmartBatteryLifeTime();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryGaugeTime : ";
                    int h = ret / 60;
                    int m = ret % 60;
                    retString += (h + " hour " + m + " min");
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_health) {
                ret = mReader.SD_GetSmartBatteryHealth();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryHealth : ";
                    retString += ret;
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_tmeperature) {
                ret = mReader.SD_GetSmartBatteryTemperature();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryTemperature : ";
                    float d = (float) (ret / 10.0);
                    retString += (d + " degree");
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_cycle_cnt) {
                ret = mReader.SD_GetSmartBatteryCycleCnt();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryCycleCnt : ";
                    retString += ret;
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_smart_capacity) {
                ret = mReader.SD_GetSmartBatteryCapacity();
                if (checkNotSupportedApi(null, ret)) {
                    retString = "Not Supported API";
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                } else {
                    retString = "SD_GetSmartBatteryCapacity : ";
                    retString += (ret + " mV");
                    Toast.makeText(mContext, retString, Toast.LENGTH_SHORT).show();
                }
            //smart battery+
            }

            mMessageTextView.setText(" " + retString);
        }
    };

    private boolean checkNotSupportedApi(String arg1, int arg2) {
        return SDConsts.NOT_SUPPORTED_API_STR.equals(arg1)
                || "\u0016".equals(arg1) // [Exception] Internal RFID
                || arg2 == SDConsts.SDResult.NOT_SUPPORTED_API;
    }

    private static class BatteryHandler extends Handler {
        private final WeakReference<BatteryFragment> mExecutor;

        public BatteryHandler(BatteryFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            BatteryFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mBatteryHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    mBatteryTv.setText(m.arg2 + " %");
                    mBatteryProgress.setProgress(m.arg2);

                    //+smart batter -critical temper
                    if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                        Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                    //smart batter -critical temper+

                    //+Always be display Battery
                    if (mOptionHandler != null) {
                        Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                        mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                    }
                    //Always be display Battery+
                } else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                }
                //+Hotswap feature
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED) {
                    if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE)
                        Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                    else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE)
                        Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(mFragment).attach(mFragment).commit();
                }
                //Hotswap feature+
                break;
        }
    }
}