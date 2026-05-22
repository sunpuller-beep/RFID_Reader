/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class ConnectivityFragment extends Fragment {
    private static final String TAG = ConnectivityFragment.class.getSimpleName();

    private static final boolean D = Constants.CON_D;

    private static final int LOADING_DIALOG = 0;

    private static final int PROGRESS_DIALOG = 1;

    private TextView mMessageTextView;

    private TextView mConnectStateTextView;

    private Button mConnectBt;
    private Button mDisconectBt;
    private Button mGetConnectStateBt;

    private Button mGetMCUSleepStateBt;

    private Reader mReader;

    private ProgressDialog mDialog;

    private Context mContext;
    
    private Handler mOptionHandler;
    
    private final ConnectivityHandler mConnectivityHandler = new ConnectivityHandler(this);
    
    public static ConnectivityFragment newInstance() {
        return new ConnectivityFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.connectivity_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mMessageTextView = (TextView)v.findViewById(R.id.message_textview);

        mConnectStateTextView = (TextView)v.findViewById(R.id.connect_state_textview); 
        
        mConnectBt = (Button)v.findViewById(R.id.bt_connect);
        mConnectBt.setOnClickListener(buttonListener);
        mDisconectBt = (Button)v.findViewById(R.id.bt_disconnect);
        mDisconectBt.setOnClickListener(buttonListener);
        mGetConnectStateBt = (Button)v.findViewById(R.id.bt_getstate);
        mGetConnectStateBt.setOnClickListener(buttonListener);
        //<-[20250526]Check MCU sleep status
        mGetMCUSleepStateBt = (Button)v.findViewById(R.id.bt_get_mcu_sleep_status);
        mGetMCUSleepStateBt.setOnClickListener(buttonListener);
        mGetMCUSleepStateBt.setVisibility(View.GONE);
        //[20250526]Check MCU sleep status->

        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mConnectivityHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED)
            updateConnectStateTextView(true);
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
        closeDialog();
        super.onStop();
    }

    private OnClickListener buttonListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int ret = -100;
            String retString = null;

            int id = v.getId();
            if (id == R.id.bt_connect) {
                retString = "SD_Wakeup ";
                ret = mReader.SD_Wakeup(); //origin
//                  ret = mReader.SD_WakeupFaster();
                if (ret == SDConsts.SDResult.SUCCESS) {
                    Activity activity = getActivity();
                    if (activity != null)
                        createDialog(LOADING_DIALOG, activity.getString(R.string.connecting_str));
                }
                if (D) Log.d(TAG, "wakeup result = " + ret);
            }else if(id == R.id.bt_disconnect) {
                retString = "SD_Disconnect ";
                ret = mReader.SD_Disconnect();
                if (D) Log.d(TAG, "disconnect result = " + ret);
                if (ret == SDConsts.SDConnectState.DISCONNECTED || ret == SDConsts.SDConnectState.ALREADY_DISCONNECTED ||
                        ret == SDConsts.SDConnectState.ACCESS_TIMEOUT) {
                    updateConnectStateTextView(false);
                }
            }else if(id == R.id.bt_getstate) {
                retString = "SD_GetConnectState ";
                ret = mReader.SD_GetConnectState();
                if (ret == SDConsts.SDConnectState.CONNECTED) {
                    if (D) Log.d(TAG, "connected");
                    updateConnectStateTextView(true);
                }
                else if (ret == SDConsts.SDConnectState.DISCONNECTED) {
                    if (D) Log.d(TAG, "disconnected");
                    updateConnectStateTextView(false);
                }
                else {
                    if (D) Log.d(TAG, "other state");
                    updateConnectStateTextView(false);
                }
                if (D) Log.d(TAG, "connect state = " + ret);
            }else {
                if (ret != -100) {
                    retString += ret;
                }
            }
            mMessageTextView.setText(" " + retString);
        }
    };
    
    private static class ConnectivityHandler extends Handler {
        private final WeakReference<ConnectivityFragment> mExecutor;
        public ConnectivityHandler(ConnectivityFragment f) {
            mExecutor = new WeakReference<>(f);
        }
        
        @Override
        public void handleMessage(Message msg) {
            ConnectivityFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mConnectivityHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
        
        switch (m.what) {
        case SDConsts.Msg.SDMsg:
            if (m.arg1 == SDConsts.SDCmdMsg.SLED_WAKEUP) {
                mMessageTextView.setText(" " + "SLED_WAKEUP " + m.arg2);
                closeDialog();
                if (m.arg2 == SDConsts.SDResult.SUCCESS) {
                    int ret = mReader.SD_Connect();
                    mMessageTextView.setText(" " + "SD_Connect " + ret);
                    if (ret == SDConsts.SDResult.SUCCESS || ret == SDConsts.SDResult.ALREADY_CONNECTED) {
                        updateConnectStateTextView(true);
                    }
                }
                else
                    Toast.makeText(mContext, "Wakeup failed!", Toast.LENGTH_SHORT).show();
            }
            else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                mMessageTextView.setText(" " + "SLED_UNKNOWN_DISCONNECTED");
                updateConnectStateTextView(false);
            }
            //+Always be display Battery
            else if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
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
    
    private void updateConnectStateTextView(boolean b) {
        if (b) {
            mConnectStateTextView.setText(R.string.connected_str);
            if (mOptionHandler != null)
                mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_CONNECTED).sendToTarget();
        }
        else {
            mConnectStateTextView.setText(R.string.disconnected_str);
            if (mOptionHandler != null)
                mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
        }
    }
    
    private void createDialog(int type, String message) {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);

        mDialog.setTitle(message);
        if (type == PROGRESS_DIALOG) {
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        mDialog.show();
    }

    private void closeDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }       
    }
}