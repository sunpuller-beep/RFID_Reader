/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.fileutil.FileSelectorDialog;
import co.kr.bluebird.rfid.app.bbrfiddemo.permission.PermissionHelper;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class RFConfigFragment extends Fragment {
    private static final String TAG = RFConfigFragment.class.getSimpleName();

    private static final boolean D = Constants.CFG_D;

    private Context mContext;

    private Spinner mRegionSpin;
    private ArrayAdapter<CharSequence> mRegionChar;
    private Button mSetRegionBt;
    private Button mGetRegionBt;
    private Button mGetAvailableRegionBt;

    private Spinner mTargetSpin;
    private ArrayAdapter<CharSequence> mTargetChar;
    private Button mSetTargetBt;
    private Button mGetTargetBt;

    private EditText mDutyEditText;
    private Button mSetDutyButton;
    private Button mGetDutyButton;

    private EditText mAccessTimeoutEditText;
    private Button mSetAccessTimeoutButton;
    private Button mGetAccessTimeoutButton;

    private EditText mPowerEditText;
    private Button mSetPowerButton;
    private Button mGetPowerButton;

    //<-[20251120]Add Singulation API

    private EditText mMinSingulationEditText;
    private EditText mMaxSingulationEditText;
    //[20251120]Add Singulation API->

    //<-[20251120]Add RF_ALL_MODE
    private EditText mRFExpansionModeEditText;
    private Button mSetRFExpansionModeButton;
    private Button mGetRFExpansionModeButton;
    //[20251120]Add RF_ALL_MODE->

    private EditText mSingulationEditText;
    private Button mSetSingulationButton;
    private Button mGetSingulationButton;

    private EditText mRFmodeEditText;
    private Button mSetRFmodeButton;
    private Button mGetRFmodeButton;

    private EditText mDwellEditText;
    private Button mSetDwellButton;
    private Button mGetDwellButton;

    private EditText mLBTEditText;
    private Button mSetLBTButton;
    private Button mGetLBTButton;

    //<-[20250425]Add ISO API
    private EditText mISOEditText;
    private Button mSetISOButton;
    private Button mGetISOButton;

    //[20250425]Add ISO API->
	
	//<-[20250210]Add Gen2x API
    private EditText mGen2xEditText;
    private Button mSetGen2xButton;
    private Button mGetGen2xButton;

    //[20250210]Add Gen2x API->

    private Spinner mToggleSpin;
    private ArrayAdapter<CharSequence> mToggleChar;
    private Button mSetToggleBt;
    private Button mGetToggleBt;

    private Spinner mRssiSpin;
    private ArrayAdapter<CharSequence> mRssiChar;
    private Button mSetRssiBt;
    private Button mGetRssiBt;

    private Spinner mChannelsSpin;
    private ArrayAdapter<CharSequence> mChannelsChar;
    private Button mSetChannelsBt;
    private Button mGetChannelsBt;

    private Button mGetDefaultChannelsBt;//[20250416]get default channel
	
	// Set/Get Current Antenna [ START ]
    private Spinner mAntennaSpin;
    private ArrayAdapter<CharSequence> mAntennaChar;
    private Button mSetAntennaBt;
    private Button mGetAntennaBt;
    // Set/Get Current Antenna [ END ]

    // Set/Get Tag Focus [ START ]
    private Spinner mTagFocusSpin;
    private ArrayAdapter<CharSequence> mTagFocusChar;
    private Button mSetTagFocusBt;
    private Button mGetTagFocusBt;
    // Set/Get Tag Focus [ END ]

    private Reader mReader;

    private ProgressDialog mDialog;

    private Handler mOptionHandler;

    private final RFConfigHandler mRFConfigHandler = new RFConfigHandler(this);

    public static RFConfigFragment newInstance() {
        return new RFConfigFragment();
    }

    private Fragment mFragment;

    View mToastlayout;//[20250417]SQA:0010362

    //<-[20250514]Add Antenna port/status API
    private EditText mAntEditText;
    private Button mSetAntPortBt;
    private Button mGetAntPortBt;
    private Button mGetAntStatusViaGPIOBt;
    private Button mGetAntStatusViaRampUpBt;
    //[20250514]Add Antenna port/status API->

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.rf_config_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        mSetRegionBt = (Button) v.findViewById(R.id.bt_set_region);
        mSetRegionBt.setOnClickListener(sledListener);
        mGetRegionBt = (Button) v.findViewById(R.id.bt_get_region);
        mGetRegionBt.setOnClickListener(sledListener);
        mGetAvailableRegionBt = (Button) v.findViewById(R.id.bt_get_available_region);
        mGetAvailableRegionBt.setOnClickListener(sledListener);
        mRegionSpin = (Spinner) v.findViewById(R.id.region_spin);
        mRegionChar = ArrayAdapter.createFromResource(mContext, R.array.region_array,
                android.R.layout.simple_spinner_dropdown_item);
        mRegionSpin.setAdapter(mRegionChar);


        mSetTargetBt = (Button) v.findViewById(R.id.bt_set_target);
        mSetTargetBt.setOnClickListener(sledListener);
        mGetTargetBt = (Button) v.findViewById(R.id.bt_get_target);
        mGetTargetBt.setOnClickListener(sledListener);
        mTargetSpin = (Spinner) v.findViewById(R.id.target_spin);
        mTargetChar = ArrayAdapter.createFromResource(mContext, R.array.target_array,
                android.R.layout.simple_spinner_dropdown_item);
        mTargetSpin.setAdapter(mTargetChar);

        mDutyEditText = (EditText) v.findViewById(R.id.duty_edit);
        mSetDutyButton = (Button) v.findViewById(R.id.set_duty_button);
        mSetDutyButton.setOnClickListener(sledListener);
        mGetDutyButton = (Button) v.findViewById(R.id.get_duty_button);
        mGetDutyButton.setOnClickListener(sledListener);

        mAccessTimeoutEditText = (EditText) v.findViewById(R.id.accesstime_edit);
        mSetAccessTimeoutButton = (Button) v.findViewById(R.id.set_accesstime_button);
        mSetAccessTimeoutButton.setOnClickListener(sledListener);
        mGetAccessTimeoutButton = (Button) v.findViewById(R.id.get_accesstime_button);
        mGetAccessTimeoutButton.setOnClickListener(sledListener);

        mPowerEditText = (EditText) v.findViewById(R.id.power_edit);
        mSetPowerButton = (Button) v.findViewById(R.id.set_power_button);
        mSetPowerButton.setOnClickListener(sledListener);
        mGetPowerButton = (Button) v.findViewById(R.id.get_power_button);
        mGetPowerButton.setOnClickListener(sledListener);

        //<-[20251120]Add Singulation API
        mMinSingulationEditText = (EditText) v.findViewById(R.id.min_singulation_edit);
        mMaxSingulationEditText = (EditText) v.findViewById(R.id.max_singulation_edit);
        //[20251120]Add Singulation API->
        //<-[20251120]Add RF expansion mode API
        mRFExpansionModeEditText = (EditText) v.findViewById(R.id.rfmode_expansion_edit);
        mRFExpansionModeEditText.setVisibility(View.GONE);
        mSetRFExpansionModeButton = (Button) v.findViewById(R.id.set_rfmode_expansion_button);
        mSetRFExpansionModeButton.setOnClickListener(sledListener);
        mSetRFExpansionModeButton.setVisibility(View.GONE);
        mGetRFExpansionModeButton = (Button) v.findViewById(R.id.get_rfmode_expansion_button);
        mGetRFExpansionModeButton.setOnClickListener(sledListener);
        mGetRFExpansionModeButton.setVisibility(View.GONE);
        //[20251120]Add RF expansion mode API->

        mSingulationEditText = (EditText) v.findViewById(R.id.singulation_edit);
        mSetSingulationButton = (Button) v.findViewById(R.id.set_singulation_button);
        mSetSingulationButton.setOnClickListener(sledListener);
        mGetSingulationButton = (Button) v.findViewById(R.id.get_singulation_button);
        mGetSingulationButton.setOnClickListener(sledListener);

        mRFmodeEditText = (EditText) v.findViewById(R.id.rfmode_edit);
        mSetRFmodeButton = (Button) v.findViewById(R.id.set_rfmode_button);
        mSetRFmodeButton.setOnClickListener(sledListener);
        mGetRFmodeButton = (Button) v.findViewById(R.id.get_rfmode_button);
        mGetRFmodeButton.setOnClickListener(sledListener);

        mDwellEditText = (EditText) v.findViewById(R.id.dwell_edit);
        mSetDwellButton = (Button) v.findViewById(R.id.set_dwell_button);
        mSetDwellButton.setOnClickListener(sledListener);
        mGetDwellButton = (Button) v.findViewById(R.id.get_dwell_button);
        mGetDwellButton.setOnClickListener(sledListener);

        mLBTEditText = (EditText) v.findViewById(R.id.lbt_edit);
        mSetLBTButton = (Button) v.findViewById(R.id.set_lbt_button);
        mSetLBTButton.setOnClickListener(sledListener);
        mGetLBTButton = (Button) v.findViewById(R.id.get_lbt_button);
        mGetLBTButton.setOnClickListener(sledListener);

        //<-[20250425]Add ISO API
        mISOEditText = (EditText) v.findViewById(R.id.iso_edit);
        mSetISOButton = (Button) v.findViewById(R.id.set_iso_button);
        mSetISOButton.setOnClickListener(sledListener);
        mGetISOButton = (Button) v.findViewById(R.id.get_iso_button);
        mGetISOButton.setOnClickListener(sledListener);
        //[20250425]Add ISO API->

		//<-[20250210]Add Gen2x API	
        mGen2xEditText = (EditText) v.findViewById(R.id.gen2x_edit);
        mGen2xEditText.setVisibility(View.GONE);
        mSetGen2xButton = (Button) v.findViewById(R.id.set_gen2x_button);
        mSetGen2xButton.setVisibility(View.GONE);
        mSetGen2xButton.setOnClickListener(sledListener);
        mGetGen2xButton = (Button) v.findViewById(R.id.get_gen2x_button);
        mGetGen2xButton.setVisibility(View.GONE);
        mGetGen2xButton.setOnClickListener(sledListener);
        //[20250210]Add Gen2x API->

        mSetToggleBt = (Button) v.findViewById(R.id.bt_set_toggle);
        mSetToggleBt.setOnClickListener(sledListener);
        mGetToggleBt = (Button) v.findViewById(R.id.bt_get_toggle);
        mGetToggleBt.setOnClickListener(sledListener);
        mToggleSpin = (Spinner) v.findViewById(R.id.toggle_spin);
        mToggleChar = ArrayAdapter.createFromResource(mContext, R.array.on_off_array,
                android.R.layout.simple_spinner_dropdown_item);
        mToggleSpin.setAdapter(mToggleChar);

        mSetRssiBt = (Button) v.findViewById(R.id.bt_set_rssi);
        mSetRssiBt.setOnClickListener(sledListener);
        mGetRssiBt = (Button) v.findViewById(R.id.bt_get_rssi);
        mGetRssiBt.setOnClickListener(sledListener);
        mRssiSpin = (Spinner) v.findViewById(R.id.rssi_spin);
        mRssiChar = ArrayAdapter.createFromResource(mContext, R.array.on_off_array,
                android.R.layout.simple_spinner_dropdown_item);
        mRssiSpin.setAdapter(mRssiChar);

        mSetChannelsBt = (Button) v.findViewById(R.id.set_enable_channels);
        mSetChannelsBt.setOnClickListener(sledListener);
        mGetChannelsBt = (Button) v.findViewById(R.id.get_enable_channels);
        mGetChannelsBt.setOnClickListener(sledListener);
        mChannelsSpin = (Spinner) v.findViewById(R.id.enable_channels_spin);
        mChannelsChar = ArrayAdapter.createFromResource(mContext, R.array.region_array_for_enable_channels,
                android.R.layout.simple_spinner_dropdown_item);
        mChannelsSpin.setAdapter(mChannelsChar);

        //<-[20250416]get default channel
        mGetDefaultChannelsBt = (Button) v.findViewById(R.id.get_default_channels);
        mGetDefaultChannelsBt.setOnClickListener(sledListener);
        mGetDefaultChannelsBt.setVisibility(View.GONE);
        mToastlayout = inflater.inflate(R.layout.custom_toast, null);//[20250417]SQA:0010362
        //[20250416]get default channel->

        //<-[20250514]Add Antenna port/status API
        mAntEditText = (EditText) v.findViewById(R.id.ant_port_edit);
        mSetAntPortBt = (Button) v.findViewById(R.id.set_antenna_port);
        mSetAntPortBt.setOnClickListener(sledListener);
        mGetAntPortBt = (Button) v.findViewById(R.id.get_antenna_port);
        mGetAntPortBt.setOnClickListener(sledListener);
        mGetAntStatusViaGPIOBt = (Button) v.findViewById(R.id.get_antenna_status_gpio);
        mGetAntStatusViaGPIOBt.setOnClickListener(sledListener);
        mGetAntStatusViaRampUpBt = (Button) v.findViewById(R.id.get_antenna_status_rampup);
        mGetAntStatusViaRampUpBt.setOnClickListener(sledListener);
        //[20250514]Add Antenna port/status API->
		
		// Set/Get Current Antenna [ START ]
        mSetAntennaBt = (Button) v.findViewById(R.id.set_current_antenna);
        mSetAntennaBt.setOnClickListener(sledListener);
        mGetAntennaBt = (Button) v.findViewById(R.id.get_current_antenna);
        mGetAntennaBt.setOnClickListener(sledListener);
        mAntennaSpin = (Spinner) v.findViewById(R.id.antenna_spin);
        mAntennaChar = ArrayAdapter.createFromResource(mContext, R.array.sel_antenna_array,
                android.R.layout.simple_spinner_dropdown_item);
        mAntennaSpin.setAdapter(mAntennaChar);
        // Set/Get Current Antenna [ END ]

        // Set/Get Tag Focus [ START ]
        mSetTagFocusBt = (Button) v.findViewById(R.id.set_tag_focus);
        mSetTagFocusBt.setOnClickListener(sledListener);
        mGetTagFocusBt = (Button) v.findViewById(R.id.get_tag_focus);
        mGetTagFocusBt.setOnClickListener(sledListener);
        mTagFocusSpin = (Spinner) v.findViewById(R.id.tag_focus_spin);
        mTagFocusChar = ArrayAdapter.createFromResource(mContext, R.array.sel_tag_focus_array,
                android.R.layout.simple_spinner_dropdown_item);
        mTagFocusSpin.setAdapter(mTagFocusChar);
        // Set/Get Tag Focus [ END ]

        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mRFConfigHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            if(!(mReader.SD_GetSerialNumber().contains("RFR900J1") || mReader.SD_GetSerialNumber().contains("RFR900SJ1")
                || mReader.SD_GetSerialNumber().contains("RFR900J2") || mReader.SD_GetSerialNumber().contains("RFR900SJ2")
                || mReader.SD_GetSerialNumber().contains("RFR901J1") || mReader.SD_GetSerialNumber().contains("RFR901J2")
                || mReader.SD_GetSerialNumber().contains("RFR901SJ1") || mReader.SD_GetSerialNumber().contains("RFR901J2")
                || mReader.SD_GetSerialNumber().contains("HF550XRJ1") || mReader.SD_GetSerialNumber().contains("HF550XRJ1"))){
                mLBTEditText.setVisibility(View.GONE);// = (EditText) v.findViewById(R.id.lbt_edit);
                mSetLBTButton.setVisibility(View.GONE);// = (Button) v.findViewById(R.id.set_lbt_button);
                mGetLBTButton.setVisibility(View.GONE);// = (Button) v.findViewById(R.id.get_lbt_button);
            }
            mDutyEditText.setText(Integer.toString(mReader.RF_GetDutyCycle()));
            mAccessTimeoutEditText.setText(Integer.toString(mReader.RF_GetAccessTimeout()));
            mPowerEditText.setText(Integer.toString(mReader.RF_GetRadioPowerState()));
            mSingulationEditText.setText(Integer.toString(mReader.RF_GetSingulationControl()));
            mRFmodeEditText.setText(Integer.toString(mReader.RF_GetRFMode()));
            mDwellEditText.setText(Integer.toString(mReader.RF_GetDwelltime()));
            //<-[20250225]Add Gen2x API
            mGen2xEditText.setText(Integer.toString(mReader.RF_GetRFIDProtocolType()));
            //[20250225]Add Gen2x API->

            int v = mReader.RF_GetRegion();
            if (v == SDConsts.RFRegion.NOT_SETTED)
                mRegionSpin.setSelection(mRegionChar.getCount() - 1);
            else if (v < SDConsts.RFRegion.KOREA || v > SDConsts.RFRegion.ETSI_UPPER)//add new ISO code
                mRegionSpin.setSelection(0);
            else
                mRegionSpin.setSelection(v);

            v = mReader.RF_GetInventorySessionTarget();
            if (v < SDConsts.RFInvSessionTarget.TARGET_A || v > SDConsts.RFInvSessionTarget.TARGET_B)
                mTargetSpin.setSelection(0);
            else
                mTargetSpin.setSelection(v);

            v = mReader.RF_GetToggle();
            if (v < SDConsts.RFToggle.OFF || v > SDConsts.RFToggle.ON)
                mToggleSpin.setSelection(0);
            else
                mToggleSpin.setSelection(v);

            v = mReader.RF_GetRssiTrackingState();
            if (v < SDConsts.RFRssi.OFF || v > SDConsts.RFRssi.ON)
                mRssiSpin.setSelection(0);
            else
                mRssiSpin.setSelection(v);

            // Set/Get Current Antenna [ START ]
            v = mReader.RF_GetEnableAntennaPort();
            if (v <= 0 || v > 3)
                mAntennaSpin.setSelection(0);
            else
                mAntennaSpin.setSelection(v - 1);
            // Set/Get Current Antenna [ END ]

            // Set/Get Tag Focus [ START ]
            v = mReader.RF_GetTagFocus();
            if (v == 0 || v == 1)
                mTagFocusSpin.setSelection(v);
            // Set/Get Tag Focus [ END ]

            mLBTEditText.setText(Integer.toString(mReader.RF_GetLBTSValue()));
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
        if (D) Log.d(TAG, " onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        if (D) Log.d(TAG, " onStop");
        closeDialog();
        super.onStop();
    }

    //+RFR901
//    private AdapterView.OnItemSelectedListener sessionListener = new AdapterView.OnItemSelectedListener() {
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            if (position > 0)
//                Toast.makeText(mContext, "If you want to use session 1 ~ 3 value, toggle off", Toast.LENGTH_SHORT).show();
//            mReader.RF_SetSession(position);
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> parent) {
//        }
//    };
//
//    private AdapterView.OnItemSelectedListener selFlagListener = new AdapterView.OnItemSelectedListener() {
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            mReader.RF_SetSelectionFlag(position + 1);
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> parent) {
//
//        }
//    };
    //RFR901+

    private OnClickListener sledListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "sledListener");
            String value;
            int spinPos = 0;
            int id = v.getId();
            int ret = -1;

            if (id == R.id.set_gen2x_button) {
                value = mGen2xEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int val = Integer.parseInt(value);
//                                if (val != 0 && val != 1){
//                                    Toast.makeText(mContext, "set Gen2x range error", Toast.LENGTH_SHORT).show();
//                                    break;
//                                }
                            mReader.RF_SetRFIDProtocolType(val);
                            Toast.makeText(mContext, "set Gen2x", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "failed set", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_gen2x_button) {
                ret = mReader.RF_GetRFIDProtocolType();
                Toast.makeText(mContext, "get Gen2x value = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_set_target) {
                spinPos = mTargetSpin.getSelectedItemPosition();
                ret = mReader.RF_SetInventorySessionTarget(spinPos);
                Toast.makeText(mContext, "Set inv session target result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_get_target) {
                ret = mReader.RF_GetInventorySessionTarget();
                Toast.makeText(mContext, "Get inv session target result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_set_region) {
                spinPos = mRegionSpin.getSelectedItemPosition();
                if (spinPos == mRegionChar.getCount() - 1)
                    Toast.makeText(mContext, "Can't set \"NOT_SETTED\"", Toast.LENGTH_SHORT).show();
                else {
                    ret = mReader.RF_SetRegion(spinPos);
                    Toast.makeText(mContext, "Set region result = " + ret, Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.bt_get_region) {
                ret = mReader.RF_GetRegion();
                Toast.makeText(mContext, "Get region result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_get_available_region) {
                value = mReader.RF_GetAvailableRegionAtThisDevice();
                createAlertDialog(value);
            } else if (id == R.id.set_duty_button) {
                value = mDutyEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int var = Integer.parseInt(value);
                            if (var < SDConsts.RFDutyCycle.MIN_DUTY || var > SDConsts.RFDutyCycle.MAX_DUTY) {
                                Toast.makeText(mContext, "Set duty cycle range = " + SDConsts.RFDutyCycle.MIN_DUTY + " ~ " +
                                        SDConsts.RFDutyCycle.MAX_DUTY, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mReader.RF_SetDutyCycle(var);
                            Toast.makeText(mContext, "Set duty cycle", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "Failed set duty cycle", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_duty_button) {
                ret = mReader.RF_GetDutyCycle();
                Toast.makeText(mContext, "Get Duty result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.set_accesstime_button) {
                value = mAccessTimeoutEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int var = Integer.parseInt(value);
                            if (var < SDConsts.RFAccessTimeout.MIN_ACCESS_TIMEOUT || var > SDConsts.RFAccessTimeout.MAX_ACCESS_TIMEOUT) {
                                Toast.makeText(mContext, "Set access timeout = " + SDConsts.RFAccessTimeout.MIN_ACCESS_TIMEOUT + " ~ " +
                                        SDConsts.RFAccessTimeout.MAX_ACCESS_TIMEOUT, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mReader.RF_SetAccessTimeout(var);
                            Toast.makeText(mContext, "Set access timeout", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "Failed set access timeout", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_accesstime_button) {
                ret = mReader.RF_GetAccessTimeout();
                Toast.makeText(mContext, "Get access timeout result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.set_power_button) {
                value = mPowerEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int var = Integer.parseInt(value);
                            if (var < SDConsts.RFPower.MIN_POWER || var > SDConsts.RFPower.MAX_POWER) {
                                Toast.makeText(mContext, "set power range = " + SDConsts.RFPower.MIN_POWER + " ~ " +
                                        SDConsts.RFPower.MAX_POWER, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mReader.RF_SetRadioPowerState(var);
                            Toast.makeText(mContext, "set power", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "failed set power", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_power_button) {
                ret = mReader.RF_GetRadioPowerState();
                Toast.makeText(mContext, "Get power result = " + ret, Toast.LENGTH_SHORT).show();
            }
            //<-[20251120]Add RF_ALL_MODE
            else if (id == R.id.set_rfmode_expansion_button) {
                value = mRFExpansionModeEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int var = Integer.parseInt(value);
                            mReader.RF_SetRFExpansionMode(var);
                            Toast.makeText(mContext, "Set RF expansion mode", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "Failed set rfmode", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_rfmode_expansion_button) {
                ret = mReader.RF_GetRFExpansionMode();
                Toast.makeText(mContext, "Get RF expansion mode result = " + ret, Toast.LENGTH_SHORT).show();
            }
            //[20251120]Add RF_ALL_MODE->
            else if (id == R.id.set_rfmode_button) {
                value = mRFmodeEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int var = Integer.parseInt(value);
                            if (var < SDConsts.RFMode.DSB_ASK_1 || var > SDConsts.RFMode.AUTO_SET) {//20230906  Impinj E710 FW/SDK 2.0 auto set mode
                                Toast.makeText(mContext, "Set rfmode range = " + SDConsts.RFMode.DSB_ASK_1 + " ~ "
                                        + SDConsts.RFMode.DSB_ASK_2, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mReader.RF_SetRFMode(var);
                            Toast.makeText(mContext, "Set rfmode", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "Failed set rfmode", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_rfmode_button) {
                ret = mReader.RF_GetRFMode();
                Toast.makeText(mContext, "Get rfmode result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.set_dwell_button) {
                value = mDwellEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int var = Integer.parseInt(value);
                            if (var < SDConsts.RFDwell.MIN_DWELL || var > SDConsts.RFDwell.MAX_DWELL) {
                                Toast.makeText(mContext, "set dwell range = " + SDConsts.RFDwell.MIN_DWELL + " ~ " +
                                        SDConsts.RFDwell.MAX_DWELL, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            mReader.RF_SetDwelltime(var);
                            Toast.makeText(mContext, "set dwelltime", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "failed set dwelltime", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_dwell_button) {
                ret = mReader.RF_GetDwelltime();
                Toast.makeText(mContext, "Get dwelltime result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.set_singulation_button) {
                //<-[20251120]Add Singulation API
                String min = mMinSingulationEditText.getText().toString();
                value = mSingulationEditText.getText().toString();
                String max = mMaxSingulationEditText.getText().toString();
                //[20251120]Add Singulation API->
                if (value != null) {
                    if (value != "") {
                        try {
                            int var = Integer.parseInt(value);
                            if (var < SDConsts.RFSingulation.MIN_SINGULATION || var > SDConsts.RFSingulation.MAX_SINGULATION) {
                                Toast.makeText(mContext, "set singulation range = " + SDConsts.RFSingulation.MIN_SINGULATION + " ~ " +
                                        SDConsts.RFSingulation.MAX_SINGULATION, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //<-[20251120]Add Singulation API
                            mReader.RF_SetSingulationControl(var, Integer.parseInt(min), Integer.parseInt(max));
//                            mReader.RF_SetSingulationControl(var, SDConsts.RFSingulation.MIN_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
                            //[20251120]Add Singulation API->
                            Toast.makeText(mContext, "set singulation", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "failed set singulation", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_singulation_button) {
                ret = mReader.RF_GetSingulationControl();
                Toast.makeText(mContext, "Get singulation min = " + mReader.RF_GetMinSingulationControl() +
                                " start = " + ret + " max = " + mReader.RF_GetMaxSingulationControl(),
                        Toast.LENGTH_SHORT).show();
            } else if (id == R.id.set_lbt_button) {
                value = mLBTEditText.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            int val = Integer.parseInt(value);
//                                if (val != SDConsts.RFLBTMode.OFF && val != SDConsts.RFLBTMode.LBT_ON && val != SDConsts.RFLBTMode.LBT_SCAN_ON) {
                            if (val != 0 && val != 1 && val != 3) {
                                Toast.makeText(mContext, "set LBT range error", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mReader.RF_SetLBTValue(val);
                            Toast.makeText(mContext, "set LBT", Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                Toast.makeText(mContext, "failed set", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_lbt_button) {
                ret = mReader.RF_GetLBTSValue();
                Toast.makeText(mContext, "get LBT value = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.set_iso_button) {
                value = mISOEditText.getText().toString();
                if (value != "" && value != null) {
                    try {
                        mReader.RF_SetRegionISO(value);
                        Toast.makeText(mContext, "set Region by ISO code", Toast.LENGTH_SHORT).show();
                        return;
                    } catch (java.lang.NumberFormatException e) {
                        if (D) Log.e(TAG, e.toString());
                    }
                }
                Toast.makeText(mContext, "failed Region by ISO code", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_iso_button) {
                value = mISOEditText.getText().toString();
                boolean r = false;
                if (!value.equals("") && value != null) {
                    r = mReader.RF_checkRegionISO(value);
                    if (r)
                        Toast.makeText(mContext, " reagion and ISO is matching", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(mContext, " reagion and ISO is not matching", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(mContext, "input ISO code", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_set_toggle) {
                spinPos = mToggleSpin.getSelectedItemPosition();
                ret = mReader.RF_SetToggle(spinPos);
                Toast.makeText(mContext, "Set toggle result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_get_toggle) {
                ret = mReader.RF_GetToggle();
                Toast.makeText(mContext, "Get toggle result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_set_rssi) {
                spinPos = mRssiSpin.getSelectedItemPosition();
                ret = mReader.RF_SetRssiTrackingState(spinPos);
                Toast.makeText(mContext, "Set rssi result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.bt_get_rssi) {
                ret = mReader.RF_GetRssiTrackingState();
                Toast.makeText(mContext, "Get rssi result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.set_enable_channels) {
                spinPos = mChannelsSpin.getSelectedItemPosition();
                //test EU & JP
                int region = -1;
                String[] chList = null;
                if (spinPos == 0) { //EU
                    region = SDConsts.RFRegion.ETSI;
                    chList = new String[]{"865700", "866300", "866900", "867500"};
                } else if (spinPos == 1) { //JP
                    region = SDConsts.RFRegion.JAPAN_1;
                    if (mReader.SD_GetModel() == SDConsts.MODEL.RFR900) {
                        chList = new String[]{"9168", "9180", "9192", "9204", "9206", "9208"};
                    } else {
                        chList = new String[]{"916800", "918000", "919200", "920400", "920600", "920800"};
                    }
                }
                ret = mReader.RF_SetEnableChannels(region, chList);
                Toast.makeText(mContext, "SetEnableChannels result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_enable_channels) {
                String[] ch = mReader.RF_GetEnableChannels();
                String channels = "";
                if (ch == null) {
                    Toast.makeText(mContext, "GetEnableChannels result is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < ch.length; i++) {
                    String separator = (i == 0) ? "" : ", ";
                    channels += separator + ch[i];
                }
                //<-[20250417]SQA:0010362
                TextView text = mToastlayout.findViewById(R.id.toast_text);
                text.setText(channels);

                Toast toast = new Toast(mContext.getApplicationContext());
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(mToastlayout);
                toast.show();
                //[20250417]SQA:0010362->
                Log.d(TAG, "GetEnableChannels result = " + channels);
//                    Toast.makeText(mContext, "GetEnableChannels result = " + channels, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_default_channels) {
                String[] ch1 = mReader.RF_GetDefaultChannels();
                if (ch1 == null) {
                    Toast.makeText(mContext, "GetDefaultChannels result is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] channelsSort1 = new String[ch1.length];
                String channels1 = "";
                for (int i = 0; i < ch1.length; i++) {
                    String separator = (i == 0) ? "" : ", ";
                    channels1 += separator + ch1[i];
                    channelsSort1[i] = ch1[i];
                }
                Arrays.sort(channelsSort1);
                Log.d(TAG, "GetDefaultChannels = " + Arrays.toString(channelsSort1));

                //<-[20250417]SQA:0010362
                TextView text0 = mToastlayout.findViewById(R.id.toast_text);
                text0.setText(channels1);

                Toast toast0 = new Toast(mContext.getApplicationContext());
                toast0.setDuration(Toast.LENGTH_LONG);
                toast0.setView(mToastlayout);
                toast0.show();
                //[20250417]SQA:0010362->
//                    Toast.makeText(mContext, "GetDefaultChannels result = " + channels1, Toast.LENGTH_SHORT).show();
                // Set/Get Current Antenna [ START ]
            } else if (id == R.id.set_current_antenna) {
                spinPos = mAntennaSpin.getSelectedItemPosition();
                ret = mReader.RF_SetEnableAntennaPort(spinPos + 1);
                Toast.makeText(mContext, "Set Current Antenna result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_current_antenna) {
                ret = mReader.RF_GetEnableAntennaPort();
                Toast.makeText(mContext, "Get Current Anenna result = " + ret, Toast.LENGTH_SHORT).show();
            }
            // Set/Get Current Antenna [ END ]
            // Set/Get Tag Focus [ START ]
            else if (id == R.id.set_tag_focus) {
                spinPos = mTagFocusSpin.getSelectedItemPosition();
                Log.d(TAG, "spinPos = " + spinPos);
                ret = mReader.RF_SetTagFocus(spinPos);
                Toast.makeText(mContext, "Set Tag Focus result = " + ret, Toast.LENGTH_SHORT).show();
            } else if (id == R.id.get_tag_focus) {
                ret = mReader.RF_GetTagFocus();
                Toast.makeText(mContext, "Get Tag Focus result = " + ret, Toast.LENGTH_SHORT).show();
            }
            // Set/Get Tag Focus [ END ]
        }
    };

    private static class RFConfigHandler extends Handler {
        private final WeakReference<RFConfigFragment> mExecutor;

        public RFConfigHandler(RFConfigFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            RFConfigFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mRFConfigHandler");
        if (D) Log.d(TAG, "m arg1 = " + m.arg1 + " arg2 = " + m.arg2);
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
                        closeDialog();
                        int v = mReader.RF_GetRegion();
                        if (v == SDConsts.RFRegion.NOT_SETTED)
                            mRegionSpin.setSelection(mRegionChar.getCount() - 1);
                        else if (v < SDConsts.RFRegion.KOREA || v > SDConsts.RFRegion.ETSI_UPPER)//add new ISO code
                            mRegionSpin.setSelection(0);
                        else
                            mRegionSpin.setSelection(v);
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