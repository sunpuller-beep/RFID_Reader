/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.fileutil.FileSelectorDialog;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.permission.PermissionHelper;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class SDFragment extends Fragment {
    private static final String TAG = SDFragment.class.getSimpleName();

    private static final boolean D = Constants.SD_D;

    private static final String FILE_DIR_PRE = "//DIR//";

    private String FILE_EXT = ".bin"; //900(".bin"), 901(".fmw")

    private static final int PROGRESS_DIALOG = 1;

    private final static int READ_DIRECTORY_REQUEST_CODE = 1000;

    private Button mSetRfidBt;
    private Button mSetBarcodeBt;
    private Button mGetTriggerBt;

    private Spinner mSleepSpin;
    private ArrayAdapter<CharSequence> mSleepChar;
    private Button mSetSleepBt;
    private Button mGetSleepBt;

    private Spinner mBuzzerLVSpin;
    private ArrayAdapter<CharSequence> mBuzzerLvChar;
    private Button mGetBuzzerBt;
    private Button mSetBuzzerBt;

    private Button mSetBuzzerNoisy;
    private Button mSetBuzzerMute;
    private Button mGetBuzzerMute;

    private Button mSetModeKeyEnable;
    private Button mSetModeKeyDisable;
    private Button mGetModeKeyEnableState;

    private Button mSetTriggerKeyEnable;
    private Button mSetTriggerKeyDisable;
    private Button mGetTriggerKeyEnableState;

    private Button mSetTagBuzzerEnable;
    private Button mSetTagBuzzerDisable;
    private Button mGetTagBuzzerEnableState;
    private Button mSetTagBuzzerSound;

    private Button mSetLEDEnable;
    private Button mSetLEDDisable;
    private Button mGetLEDEnableState;

    private Button mGetChargeBt;

    private Button mGetBatBt;

    private Button mGetFirmBt;

    private Button mGetSerialNumber;

    private Button mUpdateSled;

    private TextView mMessageTextView;

    private Button mSetSDDefaultBt;

    //<-[20251222][Package update]Add RFID FW msg
    private Button mUpdatePkgFWBt;

    private boolean mIsPackgeFWUpdate = false;
    //[20251222][Package update]Add RFID FW msg->

    private Reader mReader;

    private ProgressDialog mDialog;

    private Context mContext;

    private Handler mOptionHandler;

    private FileSelectorDialog mFileSelecter;

    private SDHandler mSDHandler = new SDHandler(this);

    public static SDFragment newInstance() {
        return new SDFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.sd_frag, container, false);
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mMessageTextView = (TextView)v.findViewById(R.id.message_textview);

        mSetRfidBt = (Button)v.findViewById(R.id.bt_settigger_rfid);
        mSetRfidBt.setOnClickListener(buttonListener);
        mSetBarcodeBt = (Button)v.findViewById(R.id.bt_settigger_barcode);
        mSetBarcodeBt.setOnClickListener(buttonListener);
        mGetTriggerBt = (Button)v.findViewById(R.id.bt_trigger);
        mGetTriggerBt.setOnClickListener(buttonListener);


        mSetSleepBt = (Button)v.findViewById(R.id.bt_sleep);
        mSetSleepBt.setOnClickListener(buttonListener);
        mGetSleepBt = (Button)v.findViewById(R.id.bt_get_sleep);
        mGetSleepBt.setOnClickListener(buttonListener);
        mSleepSpin = (Spinner)v.findViewById(R.id.sleep_spin);
        mSleepChar = ArrayAdapter.createFromResource(mContext, R.array.sleep_array,
                android.R.layout.simple_spinner_dropdown_item);
        mSleepSpin.setAdapter(mSleepChar);

        mSetBuzzerBt = (Button)v.findViewById(R.id.bt_buzzer);
        mSetBuzzerBt.setOnClickListener(buttonListener);
        mGetBuzzerBt = (Button)v.findViewById(R.id.bt_get_buzzer);
        mGetBuzzerBt.setOnClickListener(buttonListener);
        mBuzzerLVSpin = (Spinner)v.findViewById(R.id.buzzer_lv_spin);
        mBuzzerLvChar = ArrayAdapter.createFromResource(mContext, R.array.buzzerlevel_array,
                android.R.layout.simple_spinner_dropdown_item);
        mBuzzerLVSpin.setAdapter(mBuzzerLvChar);

        mSetBuzzerMute = (Button)v.findViewById(R.id.bt_setbuzzermute);
        mSetBuzzerMute.setOnClickListener(buttonListener);
        mSetBuzzerNoisy = (Button)v.findViewById(R.id.bt_setbuzzernoisy);
        mSetBuzzerNoisy.setOnClickListener(buttonListener);
        mGetBuzzerMute = (Button)v.findViewById(R.id.bt_getbuzzermute);
        mGetBuzzerMute.setOnClickListener(buttonListener);


        mSetModeKeyEnable = (Button)v.findViewById(R.id.bt_mode_key_enable);
        mSetModeKeyEnable.setOnClickListener(buttonListener);
        mSetModeKeyDisable = (Button)v.findViewById(R.id.bt_mode_key_disable);
        mSetModeKeyDisable.setOnClickListener(buttonListener);
        mGetModeKeyEnableState = (Button)v.findViewById(R.id.bt_get_mode_key_state);
        mGetModeKeyEnableState.setOnClickListener(buttonListener);

        mSetTriggerKeyEnable = (Button)v.findViewById(R.id.bt_trg_key_enable);
        mSetTriggerKeyEnable.setOnClickListener(buttonListener);
        mSetTriggerKeyDisable = (Button)v.findViewById(R.id.bt_trg_key_disable);
        mSetTriggerKeyDisable.setOnClickListener(buttonListener);
        mGetTriggerKeyEnableState = (Button)v.findViewById(R.id.bt_get_trg_key_state);
        mGetTriggerKeyEnableState.setOnClickListener(buttonListener);

        mSetTagBuzzerEnable = (Button) v.findViewById(R.id.bt_tag_buzzer_enable);
        mSetTagBuzzerEnable.setOnClickListener(buttonListener);
        mSetTagBuzzerDisable = (Button) v.findViewById(R.id.bt_tag_buzzer_disable);
        mSetTagBuzzerDisable.setOnClickListener(buttonListener);
        mGetTagBuzzerEnableState = (Button) v.findViewById(R.id.bt_get_tag_buzzer_state);
        mGetTagBuzzerEnableState.setOnClickListener(buttonListener);
        mSetTagBuzzerSound = (Button) v.findViewById(R.id.bt_set_tag_buzzer_sound);
        mSetTagBuzzerSound.setOnClickListener(buttonListener);

        mSetLEDEnable = (Button) v.findViewById(R.id.bt_set_led_enable);
        mSetLEDEnable.setOnClickListener(buttonListener);
        mSetLEDDisable = (Button) v.findViewById(R.id.bt_set_led_disable);
        mSetLEDDisable.setOnClickListener(buttonListener);
        mGetLEDEnableState = (Button) v.findViewById(R.id.bt_get_led_state);
        mGetLEDEnableState.setOnClickListener(buttonListener);

        mGetChargeBt = (Button) v.findViewById(R.id.bt_charge);
        mGetChargeBt.setOnClickListener(buttonListener);

        mGetBatBt = (Button)v.findViewById(R.id.bt_bat);
        mGetBatBt.setOnClickListener(buttonListener);

        mGetFirmBt = (Button)v.findViewById(R.id.bt_firm);
        mGetFirmBt.setOnClickListener(buttonListener);

        mGetSerialNumber = (Button)v.findViewById(R.id.bt_get_serial);
        mGetSerialNumber.setOnClickListener(buttonListener);

        mUpdateSled = (Button)v.findViewById(R.id.bt_update_firm);
        mUpdateSled.setOnClickListener(buttonListener);

        mSetSDDefaultBt = (Button)v.findViewById(R.id.bt_set_sd_default_all);
        mSetSDDefaultBt.setOnClickListener(buttonListener);

        //<-[20251222][Package update]Add RFID FW msg
        mUpdatePkgFWBt = (Button) v.findViewById(R.id.bt_package_fw_update);
        mUpdatePkgFWBt.setOnClickListener(buttonListener);
        //[20251222][Package update]Add RFID FW msg->

        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mSDHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {

            int v = mReader.SD_GetAutoSleepTimeout();
            if (v < SDConsts.SDSleepTimeout.NO_SLEEP || v > SDConsts.SDSleepTimeout.MIN_10)
                mSleepSpin.setSelection(0);
            else
                mSleepSpin.setSelection(v);

            v = mReader.SD_GetBuzzerLevel();
            if (v < SDConsts.SDBuzzerLevel.LOW || v > SDConsts.SDBuzzerLevel.HIGH)
                mBuzzerLVSpin.setSelection(0);
            else
                mBuzzerLVSpin.setSelection(v);

            FILE_EXT = (mReader.SD_GetModel() == SDConsts.MODEL.RFR900) ? ".bin" : ".fmw";
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
        if(!((MainActivity) getActivity()).mSledUpdate)
            closeDialog();
        super.onStop();
    }

    private OnClickListener buttonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int ret = -100;
            String retString = null;
            int mode, spinPos, state;
            int id = v.getId();
            
            if(id == R.id.bt_settigger_rfid){
                retString = "SD_RFID_Mode ";
                mode = mReader.SD_GetTriggerMode();
                if (mode == SDConsts.SDTriggerMode.BARCODE)
                    ret = mReader.SD_SetTriggerMode(SDConsts.SDTriggerMode.RFID);
                if (D) Log.d(TAG, "set rfid cur mode = " + mode + " ret = " + ret);
            }else if(id == R.id.bt_settigger_barcode){
                retString = "SD_BARCODE_Mode ";
                mode = mReader.SD_GetTriggerMode();
                if (mode == SDConsts.SDTriggerMode.RFID)
                    ret = mReader.SD_SetTriggerMode(SDConsts.SDTriggerMode.BARCODE);
                if (D) Log.d(TAG, "set barcode cur mode = " + mode + " ret = " + ret);
            }else if(id == R.id.bt_trigger){
                retString = "SD_GetTriggerMode ";
                ret = mReader.SD_GetTriggerMode();
                if (ret == SDConsts.SDTriggerMode.BARCODE)
                    if (D) Log.d(TAG, "barcode");
                    else if (ret == SDConsts.SDTriggerMode.RFID)
                        if (D) Log.d(TAG, "rfid");
                        else if (D) Log.d(TAG, "other state");
                if (D) Log.d(TAG, "get trigger mode = " + ret);
            }else if(id == R.id.bt_sleep){
                retString = "SD_SetAutoSleepTimeout ";
                spinPos = mSleepSpin.getSelectedItemPosition();
                // ex> SDConsts.SDSleepTimeout.SEC_30;
                ret = mReader.SD_SetAutoSleepTimeout(spinPos);
                if (D) Log.d(TAG, "set sleep timeout = " + ret);
            }else if(id == R.id.bt_get_sleep){
                retString = "SD_GetAutoSleepTimeout ";
                ret = mReader.SD_GetAutoSleepTimeout();
                if (D) Log.d(TAG, "get sleep timeout = " + ret);
            }else if(id == R.id.bt_buzzer){
                retString = "SD_SetBuzzerLevel ";
                spinPos = mBuzzerLVSpin.getSelectedItemPosition();
                // ex> SDConsts.SDBuzzerLevel.LOW
                ret = mReader.SD_SetBuzzerLevel(spinPos);
                if (D) Log.d(TAG, "set buzzer = " + ret);
            }else if(id == R.id.bt_get_buzzer){
                retString = "SD_GetBuzzerLevel ";
                ret = mReader.SD_GetBuzzerLevel();
                if (D) Log.d(TAG, "get buzzer = " + ret);
            }else if(id == R.id.bt_setbuzzermute){
                retString = "SD_SetBuzzerMute ";
                state = mReader.SD_GetBuzzerState();
                if (state >= SDConsts.SDBuzzerState.NOISY)
                    ret = mReader.SD_SetBuzzerEnable(SDConsts.SDBuzzerState.MUTE);
                if (D) Log.d(TAG, "set buzzer mute = " + ret);
            }else if(id == R.id.bt_setbuzzernoisy){
                retString = "SD_SetBuzzerNoisy ";
                state = mReader.SD_GetBuzzerState();
                if (state <= SDConsts.SDBuzzerState.MUTE)
                    ret = mReader.SD_SetBuzzerEnable(SDConsts.SDBuzzerState.NOISY);
                if (D) Log.d(TAG, "set buzzer noisy = " + ret);
            }else if(id == R.id.bt_getbuzzermute){
                retString = "SD_GetBuzzerMute ";
                ret = mReader.SD_GetBuzzerState();
                if (D) Log.d(TAG, "get buzzer mute state = " + ret);
            }else if(id == R.id.bt_tag_buzzer_enable){
                retString = "SD_SetTagBuzzerEnable ";
                ret = mReader.SD_SetTagBuzzerEnable(SDConsts.SDTagBuzzerState.ENABLE);
                if (D) Log.d(TAG, "set tag buzzer enable = " + ret);
            }else if(id == R.id.bt_tag_buzzer_disable){
                retString = "SD_SetTagBuzzerDisable ";
                ret = mReader.SD_SetTagBuzzerEnable(SDConsts.SDTagBuzzerState.DISABLE);
                if (D) Log.d(TAG, "set tag buzzer disable = " + ret);
            }else if(id == R.id.bt_get_tag_buzzer_state){
                retString = "SD_GetTagBuzzerState ";
                ret = mReader.SD_GetTagBuzzerState();
                if (D) Log.d(TAG, "get triggerkey enable state = " + ret);
            }else if(id == R.id.bt_set_tag_buzzer_sound){
                retString = "SD_SetTagBuzzerSound ";
                ret = mReader.SD_SetTagBuzzerSound();
                if (D) Log.d(TAG, "set tag sound = " + ret);
            }else if(id == R.id.bt_set_led_enable){
                retString = "SD_SetLEDEnable ";
                ret = mReader.SD_SetLEDEnable(SDConsts.SDLEDState.ENABLE);
                if (D) Log.d(TAG, "set led enable = " + ret);
            }else if(id == R.id.bt_set_led_disable){
                retString = "SD_SetLEDDisable ";
                ret = mReader.SD_SetLEDEnable(SDConsts.SDLEDState.DISABLE);
                if (D) Log.d(TAG, "set led disable = " + ret);
            }else if(id == R.id.bt_get_led_state){
                retString = "SD_GetLEDEnableState ";
                ret = mReader.SD_GetLEDEnableState();
                if (D) Log.d(TAG, "get led enable state = " + ret);
            }else if(id == R.id.bt_charge){
                retString = "SD_GetChargeState ";
                ret = mReader.SD_GetChargeState();
                if (D) Log.d(TAG, "get charge state = " + ret);
            }else if(id == R.id.bt_bat){
                retString = "SD_GetBatteryStatus ";
                ret = mReader.SD_GetBatteryStatus();
                if (D) Log.d(TAG, "bat = " + ret);
            }else if(id == R.id.bt_firm){
                retString = "SD_GetVersion ";
                retString += mReader.SD_GetVersion();
                if (D) Log.d(TAG, "sd firm version= " + retString);
            }else if(id == R.id.bt_get_serial){
                retString = "SD_GetSerialNumber ";
                retString += mReader.SD_GetSerialNumber();
                if (D) Log.d(TAG, "sd firm version= " + retString);
            }else if(id == R.id.bt_update_firm){
                retString = "SD_UpdateSDFirmware ";
                ret = 0;
                mIsPackgeFWUpdate = false;//[20251222][Package update]Add RFID FW msg
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mStoragePerms[0]);
                    if (b) {
                        createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_sled_str), true);
                    } else {
                        PermissionHelper.requestPermission(getActivity(), PermissionHelper.mStoragePerms);
                    }
                }else{
                    createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_sled_str), false);
                }
            }else if(id == R.id.bt_mode_key_enable){
                retString = "SD_SetModeKeyEnable ";
                ret = mReader.SD_SetModeKeyEnable(SDConsts.SDModeKeyState.ENABLE);
                if (D) Log.d(TAG, "set modekey enable = " + ret);
            }else if(id == R.id.bt_mode_key_disable){
                retString = "SD_SetModeKeyEnable ";
                ret = mReader.SD_SetModeKeyEnable(SDConsts.SDModeKeyState.DISABLE);
                if (D) Log.d(TAG, "set modekey disable = " + ret);
            }else if(id == R.id.bt_get_mode_key_state){
                retString = "SD_GetModeKeyEnableState ";
                ret = mReader.SD_GetModeKeyEnableState();
                if (D) Log.d(TAG, "get modekey enable state = " + ret);
            }else if(id == R.id.bt_trg_key_enable){
                retString = "SD_SetTriggerKeyEnable ";
                ret = mReader.SD_SetTriggerKeyEnable(SDConsts.SDTriggerKeyState.ENABLE);
                if (D) Log.d(TAG, "set triggerkey enable = " + ret);
            }else if(id == R.id.bt_trg_key_disable){
                retString = "SD_SetTriggerKeyEnable ";
                ret = mReader.SD_SetTriggerKeyEnable(SDConsts.SDTriggerKeyState.DISABLE);
                if (D) Log.d(TAG, "set triggerkey disable = " + ret);
            }else if(id == R.id.bt_get_trg_key_state){
                retString = "SD_GetTriggerKeyEnableState ";
                ret = mReader.SD_GetTriggerKeyEnableState();
                if (D) Log.d(TAG, "get triggerkey enable state = " + ret);
            }else if(id == R.id.bt_set_sd_default_all){
                retString = "SD_ResetConfiguration ";
                ret = mReader.SD_ResetConfiguration();
                if (D) Log.d(TAG, "reset sled to default = " + ret);
            }
            //<-[20251222][Package update]Add RFID FW msg
            else if (id == R.id.bt_package_fw_update) {
                retString = "SD_UpdatePackageFirmware ";
                ret = 0;
                mIsPackgeFWUpdate = true;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mStoragePerms[0]);
                    if (b) {
                        createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_sled_str), true);
                    } else {
                        PermissionHelper.requestPermission(getActivity(), PermissionHelper.mStoragePerms);
                    }
                } else {
                    createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_sled_str), false);
                }
            }
            //[20251222][Package update]Add RFID FW msg->

            if (ret != -100) {
                retString += ret;
            }
            mMessageTextView.setText(" " + retString);
        }
    };

    private void selectFileReadAccess(){
        ((MainActivity) getActivity()).mSledUpdate = true;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_DIRECTORY_REQUEST_CODE);
    }

    private static class SDHandler extends Handler {
        private final WeakReference<SDFragment> mExecutor;
        public SDHandler(SDFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            SDFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mSDHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            //<-[20251222][Package update]Add RFID FW msg
            case SDConsts.Msg.RFMsg:
                switch (m.arg1) {
                    case SDConsts.RFCmdMsg.UPDATE_RF_FW_START:
                        mMessageTextView.setText(" " + "UPDATE_RF_FW_START " + m.arg2);
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            Activity activity = getActivity();
                            if (activity != null)
                                createDialog(PROGRESS_DIALOG, activity.getString(R.string.update_rf_firm_str));
                        }
                        break;
                    case SDConsts.RFCmdMsg.UPDATE_RF_FW:
                        setProgressState(m.arg2);
                        break;
                    case SDConsts.RFCmdMsg.UPDATE_RF_FW_END:
                        closeDialog();
                        mMessageTextView.setText(" " + "UPDATE_RF_FW_END " + m.arg2);
                        ((MainActivity) getActivity()).mSledUpdate = false;
                }
                break;
            //[20251222][Package update]Add RFID FW msg->
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_PRESSED)
                    mMessageTextView.setText(" " + "TRIGGER_PRESSED");
                else if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_RELEASED)
                    mMessageTextView.setText(" " + "TRIGGER_RELEASED");
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_MODE_CHANGED)
                    mMessageTextView.setText(" " + "SLED_MODE_CHANGED " + m.arg2);
                else if (m.arg1 == SDConsts.SDCmdMsg.UPDATE_SD_FW_START) {
                    mMessageTextView.setText(" " + "UPDATE_SD_FW_START " + m.arg2);
                    if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                        Activity activity = getActivity();
                        if (activity != null)
                            createDialog(PROGRESS_DIALOG, activity.getString(R.string.update_sled_firmware_str));
                    }
                } else if (m.arg1 == SDConsts.SDCmdMsg.UPDATE_SD_FW) {
                    setProgressState(m.arg2);
                } else if (m.arg1 == SDConsts.SDCmdMsg.UPDATE_SD_FW_END) {
                    closeDialog();
                    mMessageTextView.setText(" " + "UPDATE_SD_FW_END " + m.arg2);
                    ((MainActivity) getActivity()).mSledUpdate = false;
                } else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                } else if (m.arg1 == SDConsts.SDCmdMsg.UPDATE_SD_BOOT_START) {
                    mMessageTextView.setText(" " + "UPDATE_SD_BOOT_START " + m.arg2);
                    if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                        Activity activity = getActivity();
                        if (activity != null)
                            createDialog(PROGRESS_DIALOG, activity.getString(R.string.update_sled_bootloader_str));
                    }
                } else if (m.arg1 == SDConsts.SDCmdMsg.UPDATE_SD_BOOT) {
                    setProgressState(m.arg2);
                } else if (m.arg1 == SDConsts.SDCmdMsg.UPDATE_SD_BOOT_END) {
                    closeDialog();
                    mMessageTextView.setText(" " + "UPDATE_SD_BOOT_START " + m.arg2);
                } else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    if (mOptionHandler != null)
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                }
                //+Always be display Battery
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    //+smart batter -critical temper
                    if (m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
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
                //+SLED firmware update re-connect
//                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_WAKEUP) {
//                    if (m.arg2 == SDConsts.SDResult.SUCCESS) {
//                        int ret = mReader.SD_Connect();
//                    }
//                    else
//                        Toast.makeText(mContext, "Wakeup failed!", Toast.LENGTH_SHORT).show();
//                }
                //SLED firmware update re-connect+
                break;

            case SDConsts.Msg.BCMsg:
                StringBuilder readData = new StringBuilder();
                if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_PRESSED)
                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_PRESSED");
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_TRIGGER_RELEASED)
                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_RELEASED");
                else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_READ) {
                    if (D) Log.d(TAG, "BC_MSG_BARCODE_READ");
                    if (m.arg2 == SDConsts.BCResult.SUCCESS)
                        readData.append(" " + "BC_MSG_BARCODE_READ");
                    else if (m.arg2 == SDConsts.BCResult.ACCESS_TIMEOUT)
                        readData.append(" " + "BC_MSG_BARCODE_ACCESS_TIMEOUT");
                    if (m.obj != null && m.obj instanceof String)
                        readData.append("\n" + (String) m.obj);
                    mMessageTextView.setText(" " + readData.toString());
                }
                if (D) Log.d(TAG, "data = " + readData.toString());
                break;

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
        if (type == PROGRESS_DIALOG){
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        mDialog.show();
    }

    private void setProgressState(int percent) {
        if (mDialog != null) {
            if (mDialog.isShowing()) {
                mDialog.setProgress(percent);
            }
        }
    }

    private void closeDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
    }

    private void createAlertDialog(String title, String message, boolean isSdkLowVersion) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                if(isSdkLowVersion) {
                    selectFile();
                }else{
                    selectFileReadAccess();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_str), null);
        builder.show();
    }

    private void selectFile() {
        File path = new File(Environment.getExternalStorageDirectory() + FILE_DIR_PRE);

        mFileSelecter = new FileSelectorDialog(mContext, path, FILE_EXT);
        mFileSelecter.addFileListener(new FileSelectorDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mStoragePerms[0]);

                if (b) {
                    //<-[20251222][Package update]Add RFID FW msg
                    int ret;
                    String str = "";
                    if(mIsPackgeFWUpdate){
                        ret = mReader.SD_UpdatePackageFirmware(file.toString());
                        str = getString(R.string.update_package_fw_str);
                    }else {
                        ret = mReader.SD_UpdateSLEDFirmware(file.toString());
                        str = getString(R.string.update_firm_str);
                    }
                    Toast.makeText(mContext,str + "result = " +
                            ret, Toast.LENGTH_SHORT).show();
                    //[20251222][Package update]Add RFID FW msg->
                } else {
                    Toast.makeText(mContext, getString(R.string.update_firm_str) +
                            " failed because of permission", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mFileSelecter.showDialog();
    }

    @RequiresPermission(allOf = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    private void firmwareUpdate(Uri mUri) {
        @SuppressLint("MissingPermission")
        //<-[20251222][Package update]Add RFID FW msg
        int ret = -100;
        String str = "";
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(mIsPackgeFWUpdate){
                ret = mReader.SD_UpdatePackageFirmware(mUri);
                str = getString(R.string.update_package_fw_str);
            }else{
                ret = mReader.SD_UpdateSLEDFirmware(mUri);
                str = getString(R.string.update_firm_str);
            }

            Toast.makeText(mContext, str + "result = " + ret, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFilenameFromUri(Uri mUri){
        String[] segments = mUri.getPath().split("/");
        String idStr = segments[segments.length-1];

        try {
            idStr = (String) idStr.subSequence(0, idStr.lastIndexOf('.'));
        }catch (StringIndexOutOfBoundsException e){
            return idStr;
        }

        return idStr;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, /*@NonNull */String[] permissions, /*@NonNull */int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (requestCode) {
                case PermissionHelper.REQ_PERMISSION_CODE:
                    if (permissions != null) {
                        boolean hasResult = false;
                        for (String p : permissions) {
                            if (p.equals(PermissionHelper.mStoragePerms[0])) {
                                hasResult = true;
                                break;
                            }
                        }
                        if (hasResult) {
                            if (grantResults != null && grantResults.length != 0 &&
                                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                                createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_sled_str), true);
                        }
                    }
                    break;
            }
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PermissionHelper.REQ_FILE_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(Environment.isExternalStorageManager()) {
                    selectFile();
                } else {
                    Toast.makeText(mContext, getString(R.string.permission_not_allowed), Toast.LENGTH_SHORT).show();
                }
            }
        }else if (requestCode == READ_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null && resultData.getData() != null) {
                firmwareUpdate(resultData.getData());
            } else {
                if (D) Log.d(TAG, "File uri not found {}");
                ((MainActivity) getActivity()).mSledUpdate = false;
            }
        }
    }
}