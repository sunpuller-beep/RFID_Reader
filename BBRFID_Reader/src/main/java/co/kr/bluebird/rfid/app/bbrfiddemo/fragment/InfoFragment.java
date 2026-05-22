/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 * 
 * Author : Bogon Jun
 *
 * Date : 2016.01.18
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class InfoFragment extends Fragment {
    
    private static final String TAG = InfoFragment.class.getSimpleName();

    private static final boolean D = Constants.INFO_D;

    private TextView mOSVersionTv;
    
    private TextView mRFIDLibVersionTv;
    
    private TextView mRFIDModuleVersion;

    private TextView mSDFirmwareVersion;
    
    private TextView mSDBTFirmwareVersion;

    private TextView mSDSerialNumber;
    
    private TextView mSDBootloaderBersion;
    
    private TextView mAppVersion;

    private TextView mSleType;

    private Reader mReader;

    private Context mContext;
    
    private Handler mOptionHandler;
    
    private InfoHandler mInfoHandler = new InfoHandler(this);
    
    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.info_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mOSVersionTv = (TextView)v.findViewById(R.id.tv_os_version);
        mRFIDLibVersionTv = (TextView)v.findViewById(R.id.tv_rf_lib_version);
        mRFIDModuleVersion = (TextView)v.findViewById(R.id.tv_rfid_version);
        mSDFirmwareVersion = (TextView)v.findViewById(R.id.tv_sd_firm_version);
        mSDBTFirmwareVersion = (TextView)v.findViewById(R.id.tv_sd_bt_firm_version);
        mSDSerialNumber = (TextView)v.findViewById(R.id.tv_sd_serial_number);
        mSDBootloaderBersion = (TextView)v.findViewById(R.id.tv_bootloader_version);
        mAppVersion = (TextView)v.findViewById(R.id.tv_app_version);
        mSleType = (TextView)v.findViewById(R.id.tv_sled_type);

        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mInfoHandler);
		
        mOSVersionTv.setText(Build.DISPLAY);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            mRFIDLibVersionTv.setText(mReader.RF_GetLibVersion());
            mRFIDModuleVersion.setText(mReader.RF_GetRFIDVersion());
            mSleType.setText("EXTERNAL_SLED");

            int sledType = mReader.SD_GetType();
            if (mReader.SD_GetConnectionType() == SDConsts.ConnectType.USB) {
                if (sledType == SDConsts.SLED_TYPE.INTERNAL_SLED){ // Internal RFID(USB)
                    mSDFirmwareVersion.setText(mReader.SD_GetVersion());
                    mSDBTFirmwareVersion.setText(mReader.SD_GetBTVersion());
                    mSleType.setText("INTERNAL_SLED");
                    mSDSerialNumber.setText(mReader.SD_GetHostSerialNumber());
                    mSDBootloaderBersion.setText(mReader.SD_GetBootLoaderVersion());
                } else { // DRxxx(USB)
                    mSDFirmwareVersion.setText("Not Supported API");
                    mSDBTFirmwareVersion.setText("Not Supported API");
                    mSDSerialNumber.setText("Not Supported API");
                    mSDBootloaderBersion.setText("Not Supported API");
                }
            } else {
                mSDFirmwareVersion.setText(mReader.SD_GetVersion());
                mSDBTFirmwareVersion.setText(mReader.SD_GetBTVersion());
                if (sledType == SDConsts.SLED_TYPE.INTERNAL_SLED){ // Internal RFID(UART)
                    mSleType.setText("INTERNAL_SLED");
                    mSDSerialNumber.setText(mReader.SD_GetHostSerialNumber());
                } else if (sledType == SDConsts.SLED_TYPE.RFR900_EXTERNAL_SLED
                        || sledType == SDConsts.SLED_TYPE.RFR901_EXTERNAL_SLED
                        || sledType == SDConsts.SLED_TYPE.RFR971_EXTERNAL_SLED){ // External RFID(UART)
                    mSDSerialNumber.setText(mReader.SD_GetSerialNumber());
                }
                mSDBootloaderBersion.setText(mReader.SD_GetBootLoaderVersion());
            }
        }
        mAppVersion.setText(Constants.VERSION);
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
    }
    
    private static class InfoHandler extends Handler {
        private final WeakReference<InfoFragment> mExecutor;
        public InfoHandler(InfoFragment f) {
            mExecutor = new WeakReference<>(f);
        }
        
        @Override
        public void handleMessage(Message msg) {
            InfoFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mInfoHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
        
        switch (m.what) {
        case SDConsts.Msg.SDMsg:
            if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                if (mOptionHandler != null)
                    mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
            }
            //+Always be display Battery
            else if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                //+smart batter -critical temper
                if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                    Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                //smart batter -critical temper+

                Activity activity = getActivity();
                if (activity != null) {
                    if (mOptionHandler != null) {
                        Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                        mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                    }
                }
            }
            //Always be display Battery+
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