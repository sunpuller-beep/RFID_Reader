/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class TestFragment extends Fragment {
    private static final String TAG = TestFragment.class.getSimpleName();

    private static final boolean D = true;

    private Context mContext;

    private Reader mReader;

    private CheckBox rb1, rb2, rb3, rb4, rb5, rb6;
    private Button btn_set_channel, btn_get_channel;
    private TextView tv_current_channel;

    private int[] CHANNEL_JP = {9168, 9180, 9192, 9204, 9206, 9208};

    private int mCurrentRegion = SDConsts.RFRegion.NOT_SETTED;
    private ProgressDialog mDialog;

    private Handler mOptionHandler;
    private final RFConfigHandler mRFConfigHandler = new RFConfigHandler(this);

    public static TestFragment newInstance() {
        return new TestFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.test_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        rb1 = v.findViewById(R.id.rb_1);
        rb2 = v.findViewById(R.id.rb_2);
        rb3 = v.findViewById(R.id.rb_3);
        rb4 = v.findViewById(R.id.rb_4);
        rb5 = v.findViewById(R.id.rb_5);
        rb6 = v.findViewById(R.id.rb_6);

        btn_set_channel = v.findViewById(R.id.btn_set_channel);
        btn_get_channel = v.findViewById(R.id.btn_get_channel);

        tv_current_channel = v.findViewById(R.id.tv_current_channel);

        btn_set_channel.setOnClickListener(sledListener);
        btn_get_channel.setOnClickListener(sledListener);

        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mRFConfigHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            updateRegion(true);
        } else {
            Toast.makeText(mContext, "Please connect RFR900 or RFR901 device", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
        super.onStart();
    }

    private void updateRegion(boolean isUpdateCheckbox) {
        mCurrentRegion = mReader.RF_GetRegion();
        if(mCurrentRegion != SDConsts.RFRegion.JAPAN_1) {
            mReader.RF_SetRegion(SDConsts.RFRegion.JAPAN_1);
        } else {

            rb1.setText(String.valueOf(CHANNEL_JP[0]));
            rb2.setText(String.valueOf(CHANNEL_JP[1]));
            rb3.setText(String.valueOf(CHANNEL_JP[2]));
            rb4.setText(String.valueOf(CHANNEL_JP[3]));
            rb5.setText(String.valueOf(CHANNEL_JP[4]));
            rb6.setText(String.valueOf(CHANNEL_JP[5]));

            updateCheck(isUpdateCheckbox);
        }
    }

    private void updateCheck(boolean isUpdateCheckbox) {
        CheckBox[] radioButtons = {rb1, rb2, rb3, rb4, rb5, rb6};
        String[] channels = mReader.RF_GetEnableChannels();
        if(channels == null) {
            return;
        }

        String curChannels = "";
        for (int i = 0; i < channels.length; i++) {
            String separator = (i == 0) ? "" : ", ";
            curChannels += separator + channels[i];
        }
        tv_current_channel.setText(curChannels);

        if(isUpdateCheckbox) {
            for (int i = 0; i < 6; i++) {
                radioButtons[i].setChecked(false);

                int nChannel = CHANNEL_JP[i];
                for (String strChannel : channels) {
                    if (Integer.parseInt(strChannel) == nChannel) {
                        radioButtons[i].setChecked(true);
                        break;
                    }
                }
            }

//            if (mReader.SD_GetModel() == SDConsts.MODEL.RFR901) {
//                for (int i = 0; i < 6; i++) {
//                    radioButtons[i].setChecked(false);
//
//                    int nChannel = CHANNEL_JP_1[i];
//                    for (String strChannel : channels) {
//                        if (Integer.parseInt(strChannel) == nChannel) {
//                            radioButtons[i].setChecked(true);
//                            break;
//                        }
//                    }
//                }
//            } else {
//                for (int i = 0; i < 6; i++) {
//                    radioButtons[i].setChecked(false);
//
//                    int nChannel = CHANNEL_JP_2[i];
//                    for (String strChannel : channels) {
//                        if (Integer.parseInt(strChannel) == nChannel) {
//                            radioButtons[i].setChecked(true);
//                            break;
//                        }
//                    }
//                }
//            }
        }
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
        if (D) Log.d(TAG, " onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        if (D) Log.d(TAG, " onStop");
        closeDialog();
        super.onStop();
    }

    private OnClickListener sledListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.btn_get_channel){
                updateRegion(false);
            }else if(id == R.id.btn_set_channel){
                ArrayList<String> enableChannel = new ArrayList<>();

                CheckBox[] radioButtons = {rb1, rb2, rb3, rb4, rb5, rb6};
                for(CheckBox rb : radioButtons) {
                    if(rb.isChecked()) {
                        enableChannel.add(rb.getText().toString());
                    }
                }
                int result = 0;
                if(enableChannel.size() > 0) {
                    String[] chList = enableChannel.toArray(new String[enableChannel.size()]);
                    Log.e("hsseo", "region : " + mCurrentRegion);
                    result = mReader.RF_SetEnableChannels(mCurrentRegion, chList);
                    Toast.makeText(mContext, "RF_SetEnableChannels : " + result, Toast.LENGTH_SHORT).show();
                } else {
                    updateRegion(false);
                }
            }
        }
    };

    private static class RFConfigHandler extends Handler {
        private final WeakReference<TestFragment> mExecutor;

        public RFConfigHandler(TestFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            TestFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        Log.d(TAG, "handleMessage : [" + m.what + "] " + m.arg1 + " / " + m.arg2);
        int result = m.arg2;

        switch (m.what) {
            case SDConsts.Msg.RFMsg:
                switch (m.arg1) {
                    case SDConsts.RFCmdMsg.REGION_CHANGE_START:
                        if (result == SDConsts.RFResult.SUCCESS) {
                            createDialog("Region is changing...");
                        }
                        break;
                    case SDConsts.RFCmdMsg.REGION_CHANGE_END:
                        if(result == SDConsts.RFResult.SUCCESS) {

                            closeDialog();
                            updateRegion(false);
                        }
                        break;

                }
                break;
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

    private void createDialog(String message) {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
        mDialog = new ProgressDialog(mContext);
        mDialog.setCancelable(false);
        mDialog.setTitle(message);
        mDialog.show();
    }

    private void createAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.available_regions_str));
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.drawer_close), null);
        builder.show();
    }


    private void closeDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
    }
}