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
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class SBBarcodeFragment extends Fragment {
    private static final String TAG = SBBarcodeFragment.class.getSimpleName();

    private static final boolean D = Constants.SB_BAR_D;

    private Button mSetRfidBt;
    private Button mSetBarcodeBt;
    private Button mGetTriggerBt;
    private Button mResetConfigurationBt;
    
    private Button mPressBarcodeOfSLED;
    private Button mReleaseBarcodeOfSLED;

    private Button mSoundEnable;
    private Button mSoundDisable;
    private Button mSoundState;
    
    private Button mScannerEnable;
    private Button mscannerDisable;
    private Button mGetRevision;

    private Button mIlluminationOn;
    private Button mIlluminationOff;
    private Button mAimOn;
    private Button mAimOff;

    private Spinner mParamSpin;
    private ArrayAdapter<CharSequence> mParamChar;
    private Button mGetParamBt;

    private EditText mParamValueEt;
    private Button mSetParamBt;
    
    private Spinner mPreTextSpin;
    private ArrayAdapter<CharSequence> mPreTextChar;
    private EditText mPreTextEt;
    private Button mGetPreTextBt;
    private Button mSetPreTextBt;

    private Button mGetBarcodeInfoBt;
    
    private Spinner mScannerModeSpin;
    private ArrayAdapter<CharSequence> mScannerModeChar;
    private Button mSetScannerModeBt;
    private Button mGetScannerModeBt;

    private TextView mMessageTextView;

    private TextView mDataTextView;
    
    private Reader mReader;

    private Context mContext;

    private ProgressDialog mDialog;
    
    private Handler mOptionHandler;

    private Fragment mFragment;
    
    private SBBarcodeHandler mSBBarcodeHandler = new SBBarcodeHandler(this);
    
    public static SBBarcodeFragment newInstance() {
        return new SBBarcodeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sb_barcode_frag, container, false);
        if (D) Log.d(TAG, "onCreateView");
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mMessageTextView = (TextView)v.findViewById(R.id.message_textview);

        mDataTextView = (TextView)v.findViewById(R.id.data_textview);
                
        mSetRfidBt = (Button)v.findViewById(R.id.bt_settigger_rfid);
        mSetRfidBt.setOnClickListener(buttonListener);
        mSetBarcodeBt = (Button)v.findViewById(R.id.bt_settigger_barcode);
        mSetBarcodeBt.setOnClickListener(buttonListener);
        mGetTriggerBt = (Button)v.findViewById(R.id.bt_trigger);
        mGetTriggerBt.setOnClickListener(buttonListener);
        mResetConfigurationBt = (Button)v.findViewById(R.id.bt_reset);
        mResetConfigurationBt.setOnClickListener(buttonListener);
        
        mPressBarcodeOfSLED = (Button)v.findViewById(R.id.bt_barcode_press_of_sled);
        mPressBarcodeOfSLED.setOnClickListener(buttonListener);
        mReleaseBarcodeOfSLED = (Button)v.findViewById(R.id.bt_barcode_release_of_sled);
        mReleaseBarcodeOfSLED.setOnClickListener(buttonListener);

        mSoundEnable = (Button)v.findViewById(R.id.bt_sound_enable);
        mSoundEnable.setOnClickListener(buttonListener);
        mSoundDisable = (Button)v.findViewById(R.id.bt_sound_disable);
        mSoundDisable.setOnClickListener(buttonListener);
        mSoundState = (Button)v.findViewById(R.id.bt_get_sound_state);
        mSoundState.setOnClickListener(buttonListener);

        mScannerEnable = (Button)v.findViewById(R.id.bt_enable_scanner);
        mScannerEnable.setOnClickListener(buttonListener);
        mscannerDisable = (Button)v.findViewById(R.id.bt_disable_scanner);
        mscannerDisable.setOnClickListener(buttonListener);
        mGetRevision = (Button)v.findViewById(R.id.bt_get_revision);
        mGetRevision.setOnClickListener(buttonListener);

        mIlluminationOn = (Button)v.findViewById(R.id.bt_illum_on);
        mIlluminationOn.setOnClickListener(buttonListener);
        mIlluminationOff = (Button)v.findViewById(R.id.bt_illum_off);
        mIlluminationOff.setOnClickListener(buttonListener);
        mAimOn = (Button)v.findViewById(R.id.bt_aim_on);
        mAimOn.setOnClickListener(buttonListener);
        mAimOff = (Button)v.findViewById(R.id.bt_aim_off);
        mAimOff.setOnClickListener(buttonListener);

        mParamSpin = (Spinner)v.findViewById(R.id.param_spin);
        mParamChar = ArrayAdapter.createFromResource(mContext, R.array.param_array, 
                android.R.layout.simple_spinner_dropdown_item);
        mParamSpin.setAdapter(mParamChar);
        
        mGetParamBt = (Button)v.findViewById(R.id.bt_get_param);
        mGetParamBt.setOnClickListener(buttonListener);
        
        mParamValueEt = (EditText)v.findViewById(R.id.et_param_value);
        mSetParamBt = (Button)v.findViewById(R.id.bt_set_param);
        mSetParamBt.setOnClickListener(buttonListener);

        mPreTextSpin = (Spinner)v.findViewById(R.id.bar_text_spin);
        mPreTextChar = ArrayAdapter.createFromResource(mContext, R.array.bar_pre_text_array, 
                android.R.layout.simple_spinner_dropdown_item);
        mPreTextSpin.setAdapter(mPreTextChar);
        mPreTextEt = (EditText)v.findViewById(R.id.et_text_value);
        
        mGetPreTextBt = (Button)v.findViewById(R.id.bt_get_func);
        mGetPreTextBt.setOnClickListener(buttonListener);
        
        mSetPreTextBt = (Button)v.findViewById(R.id.bt_set_func);
        mSetPreTextBt.setOnClickListener(buttonListener);

        mSetScannerModeBt = (Button)v.findViewById(R.id.bt_set_scan_trigger_mode);
        mSetScannerModeBt.setOnClickListener(buttonListener);
        mGetScannerModeBt = (Button)v.findViewById(R.id.bt_get_scan_trigger_mode);
        mGetScannerModeBt.setOnClickListener(buttonListener);
        mScannerModeSpin = (Spinner)v.findViewById(R.id.scanner_trigger_spin);
        mScannerModeChar = ArrayAdapter.createFromResource(mContext, R.array.scanner_mode_array, 
                android.R.layout.simple_spinner_dropdown_item);
        mScannerModeSpin.setAdapter(mScannerModeChar);

        //<-[20250825]Add barcode/camera info
        mGetBarcodeInfoBt = (Button) v.findViewById(R.id.bt_barcode_get_info);
        mGetBarcodeInfoBt.setOnClickListener(buttonListener);
        //[20250825]Add barcode/camera info->
        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mReader = Reader.getReader(mContext, mSBBarcodeHandler);

        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.BTConnectState.CONNECTED) {
            int v = mReader.SB_GetBarcodeTriggerMode();
            if (v < 0 || v > 3)
                mScannerModeSpin.setSelection(0);
            else
                mScannerModeSpin.setSelection(v);
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
        closeDialog();
        super.onStop();
    }

    private OnClickListener buttonListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int ret = -100;
            String retString = null;
            int mode;
            int id = v.getId();
            if(id == R.id.bt_settigger_rfid){
                retString = "SD_RFID_Mode ";
                mode = mReader.SD_GetTriggerMode();
                if (mode == SDConsts.SDTriggerMode.BARCODE)
                    ret = mReader.SD_SetTriggerMode(SDConsts.SDTriggerMode.RFID);
                if (D) Log.d(TAG, "set rfid cur mode = " + mode + " ret = " + ret);
            }
            //<-[20250825]Add barcode/camera info
            else if(id == R.id.bt_barcode_get_info){
                retString = "SD_Barcode_Info ";
                int info  = mReader.SB_GetSupportedDevicesInfo();
                retString +=" : " + info;
                if (D) Log.d(TAG, "get barcode/camera cur info = " + info);
            }
            //[20250825]Add barcode/camera info->
            else if(id == R.id.bt_settigger_barcode){
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
                else
                    if (D) Log.d(TAG, "other state");
                if (D) Log.d(TAG, "get trigger mode = " + ret);
            }else if(id == R.id.bt_reset){
                retString = "SB_ResetBarcodeConfiguration ";
                ret = mReader.SB_ResetBarcodeConfiguration();
                if (D) Log.d(TAG, "SB_ResetBarcodeConfiguration = " + ret);
            }else if(id == R.id.bt_barcode_press_of_sled){
                retString = "SB_StartScan ";
                ret = mReader.SB_StartScan(true);
                if (D) Log.d(TAG, "SB_StartScan = " + ret);
            }else if(id == R.id.bt_barcode_release_of_sled){
                retString = "SB_StartScan ";
                ret = mReader.SB_StartScan(false);
                if (D) Log.d(TAG, "SB_StartScan = " + ret);
            }else if(id == R.id.bt_sound_enable){
                retString = "SB_EnableBarcodeSound ";
                ret = mReader.SB_EnableBarcodeSound(true);
                if (D) Log.d(TAG, "SB_EnableBarcodeSound = " + ret);
            }else if(id == R.id.bt_sound_disable){
                retString = "SB_EnableBarcodeSound ";
                ret = mReader.SB_EnableBarcodeSound(false);
                if (D) Log.d(TAG, "SB_EnableBarcodeSound = " + ret);
            }else if(id == R.id.bt_get_sound_state){
                retString = "SB_GetBarcodeSoundState ";
                ret = mReader.SB_GetBarcodeSoundState();
                if (D) Log.d(TAG, "SB_GetBarcodeSoundState = " + ret);
            }else if(id == R.id.bt_enable_scanner){
                retString = "SB_EnableScanner ";
                ret = mReader.SB_EnableBarcodeScanner(true);
                if (D) Log.d(TAG, "SB_EnableScanner = " + ret);
            }else if(id == R.id.bt_disable_scanner){
                retString = "SB_EnableScanner ";
                ret = mReader.SB_EnableBarcodeScanner(false);
                if (D) Log.d(TAG, "SB_EnableScanner = " + ret);
            }else if(id == R.id.bt_get_revision){
                retString = "SB_GetRevision ";
                retString += mReader.SB_GetRevision();
                if (D) Log.d(TAG, "SB_GetRevision = " + retString);
            }else if(id == R.id.bt_illum_on){
                retString = "SB_Illumination ";
                ret = mReader.SB_EnableIllumination(true);
                if (D) Log.d(TAG, "SB_Illumination = " + ret);
            }else if(id == R.id.bt_illum_off){
                retString = "SB_Illumination ";
                ret = mReader.SB_EnableIllumination(false);
                if (D) Log.d(TAG, "SB_Illumination = " + ret);
            }else if(id == R.id.bt_aim_on){
                retString = "SB_SetAimState ";
                ret = mReader.SB_EnableAim(true);
                if (D) Log.d(TAG, "SB_SetAimState = " + ret);
            }else if(id == R.id.bt_aim_off){
                retString = "SB_SetAimState ";
                ret = mReader.SB_EnableAim(false);
                if (D) Log.d(TAG, "SB_SetAimState = " + ret);
            }else if(id == R.id.bt_get_param){
                retString = "SB_GetParamValue ";
                ret = mReader.SB_GetParamValue(getParamValue(mParamSpin.getSelectedItemPosition()));
                if (D) Log.d(TAG, "SB_GetParamValue = " + ret);
            }else if(id == R.id.bt_set_param){
                retString = "SB_SetParamValue ";
                int value = 0;
                try {
                    value = Integer.parseInt(mParamValueEt.getText().toString());
                    if (value >= 0 && value <= 255)
                        ret = mReader.SB_SetParamValue(getParamValue(mParamSpin.getSelectedItemPosition()), value);
                    else
                        ret = -1;
                } 
                catch (NumberFormatException e) {
                    ret = -1;
                }
                if (D) Log.d(TAG, "SB_SetParamValue = " + ret);
            }else if(id == R.id.bt_set_scan_trigger_mode){
                retString = "SB_SetBarcodeTriggerMode ";
                ret = mReader.SB_SetBarcodeTriggerMode(mScannerModeSpin.getSelectedItemPosition());
                if (D) Log.d(TAG, "SB_SetBarcodeTriggerMode = " + ret);
            }else if(id == R.id.bt_get_scan_trigger_mode){
                retString = "SB_GetBarcodeTriggerMode ";
                ret = mReader.SB_GetBarcodeTriggerMode();
                if (D) Log.d(TAG, "SB_GetBarcodeTriggerMode = " + ret);
            }else if(id == R.id.bt_get_func){
                retString = "SB_GetBarcodePresetValue ";
                retString += mReader.SB_GetBarcodePresetValue(mPreTextSpin.getSelectedItemPosition());
                if (D) Log.d(TAG, "SB_GetBarcodePresetValue = " + retString);
            }else if(id == R.id.bt_set_func){
                retString = "SB_SetBarcodePresetValue ";
                Editable edit = mPreTextEt.getText();
                String str = null;
                if (edit != null)
                    str = edit.toString();
                ret = mReader.SB_SetBarcodePresetValue(mPreTextSpin.getSelectedItemPosition(),
                        str);
                if (D) Log.d(TAG, "SB_SetBarcodePresetValue = " + ret);

            }
            if (ret != -100) {
                retString += ret;
            }
            mMessageTextView.setText(" " + retString);
        }
    };
    
    private static class SBBarcodeHandler extends Handler {
        private final WeakReference<SBBarcodeFragment> mExecutor;
        public SBBarcodeHandler(SBBarcodeFragment f) {
            mExecutor = new WeakReference<>(f);
        }
        
        @Override
        public void handleMessage(Message msg) {
            SBBarcodeFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mSBBarcodeHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_PRESSED)
                    mMessageTextView.setText(" " + "TRIGGER_PRESSED");
                else if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_RELEASED)
                    mMessageTextView.setText(" " + "TRIGGER_RELEASED");
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_MODE_CHANGED)
                    mMessageTextView.setText(" " + "SLED_MODE_CHANGED " + m.arg2);
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED)
                    mMessageTextView.setText(" " + "SLED_UNKNOWN_DISCONNECTED");
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
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    //+smart batter -critical temper
                    if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                        Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                    //smart batter -critical temper+

                    //+Always be display Battery
                    Activity activity = getActivity();
                    if (activity != null) {
                        if (mOptionHandler != null) {
                            Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                            mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, m.arg1, m.arg2).sendToTarget();
                        }
                    }
                    //Always be display Battery+
                }
                break;
            case SDConsts.Msg.SBMsg:
                if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_TRIGGER_PRESSED_SLED){
                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_PRESSED_SLED");
                }
                else if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_TRIGGER_RELEASED_SLED){
                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_RELEASED_SLED");
                }
                else if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_READ) {
                    StringBuilder readData = new StringBuilder();
                    if (D) Log.d(TAG, "SB_MSG_BARCODE_READ");
                    if (m.arg2 == SDConsts.SBResult.SUCCESS)
                        readData.append(" " + "SB_MSG_BARCODE_READ");
                    else if (m.arg2 == SDConsts.SBResult.ACCESS_TIMEOUT)
                        readData.append(" " + "SB_MSG_BARCODE_ACCESS_TIMEOUT");
                    if (m.obj != null  && m.obj instanceof String) {
                        readData.append("\n  " + (String)m.obj);
                        mDataTextView.setText(" " + (String)m.obj);
                    }
                    mMessageTextView.setText(" " + readData.toString());
                }
                else if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_RESET_CONFIG_START) {
                    createDialog("Reset Configuration...");
                }
                else if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_RESET_CONFIG_END) {
                    closeDialog();
                }
                break;
//            case SDConsts.Msg.BTMsg:
//                if (m.arg1 == SDConsts.BTCmdMsg.SLED_BT_CONNECTION_STATE_CHANGED) {
//                    if (D) Log.d(TAG, "SLED_BT_CONNECTION_STATE_CHANGED = " + m.arg2);
//                    if (mOptionHandler != null)
//                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_CONNECT_STATE_CHANGED).sendToTarget();
//                }
//                break;
        }
    }

    private void createDialog(String message) {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }
        mDialog = new ProgressDialog(mContext);
        mDialog.setTitle(message);
        mDialog.setCancelable(false);
        mDialog.show();
    }
    
    private void closeDialog() {
        if (mDialog != null) {
            if (mDialog.isShowing())
                mDialog.cancel();
            mDialog = null;
        }       
    }

    private int getParamValue(int position) {
        int value = -1;
        switch (position) {
            case 0:
                value = SDConsts.SBParam.UPC_A;
                break;
            case 1:
                value = SDConsts.SBParam.UPC_E;
                break;
            case 2:
                value = SDConsts.SBParam.UPC_E1;
                break;
            case 3:
                value = SDConsts.SBParam.EAN8;
                break;
            case 4:
                value = SDConsts.SBParam.EAN13;
                break;
            case 5:
                value = SDConsts.SBParam.BOOKLAND_EAN;
                break;
            case 6:
                value = SDConsts.SBParam.BOOKLAND_ISBN_FORMAT;
                break;
            case 7:
                value = SDConsts.SBParam.DECODE_UPC_EAN_SUPPLEMENTAL;
                break;
            case 8:
                value = SDConsts.SBParam.UPC_EAN_SUPPLEMENTAL_REDUNDANCY;
                break;
            case 9:
                value = SDConsts.SBParam.DECODE_UPC_EAN_SUPPLEMENTAL_AIM_ID;
                break;
            case 10:
                value = SDConsts.SBParam.TRANSMIT_UPC_A_CHK_DIGIT;
                break;
            case 11:
                value = SDConsts.SBParam.TRANSMIT_UPC_E_CHK_DIGIT;
                break;
            case 12:
                value = SDConsts.SBParam.TRANSMIT_UPC_E1_CHK_DIGIT;
                break;
            case 13:
                value = SDConsts.SBParam.UPC_A_PREAMBLE;
                break;
            case 14:
                value = SDConsts.SBParam.UPC_E_PREAMBLE;
                break;
            case 15:
                value = SDConsts.SBParam.UPC_E1_PREAMBLE;
                break;
            case 16:
                value = SDConsts.SBParam.CONVERT_UPC_E_TO_A;
                break;
            case 17:
                value = SDConsts.SBParam.CONVERT_UPC_E1_TO_A;
                break;
            case 18:
                value = SDConsts.SBParam.EAN8_EXTEND;
                break;
            case 19:
                value = SDConsts.SBParam.COUPON_REPORT;
                break;
            case 20:
                value = SDConsts.SBParam.ISSN_EAN;
                break;
            case 21:
                value = SDConsts.SBParam.CODE128;
                break;
            case 22:
                value = SDConsts.SBParam.CODE128_LEN_MIN;
                break;
            case 23:
                value = SDConsts.SBParam.CODE128_LEN_MAX;
                break;
            case 24:
                value = SDConsts.SBParam.GS1_128;
                break;
            case 25:
                value = SDConsts.SBParam.ISBT128;
                break;
            case 26:
                value = SDConsts.SBParam.ISBT128_CONCATENATION;
                break;
            case 27:
                value = SDConsts.SBParam.ISBT128_CHECK_TABLE;
                break;
            case 28:
                value = SDConsts.SBParam.ISBT128_CONCATENATION_REDUNDANCY;
                break;
            case 29:
                value = SDConsts.SBParam.CODE39;
                break;
            case 30:
                value = SDConsts.SBParam.TRIOPTIC_CODE39;
                break;
            case 31:
                value = SDConsts.SBParam.CONVERT_CODE39_32;
                break;
            case 32:
                value = SDConsts.SBParam.CODE32_PREFIX;
                break;
            case 33:
                value = SDConsts.SBParam.CODE39_LEN_MIN;
                break;
            case 34:
                value = SDConsts.SBParam.CODE39_LEN_MAX;
                break;
            case 35:
                value = SDConsts.SBParam.CODE39_CHK_DIGIT_VERIFICATION;
                break;
            case 36:
                value = SDConsts.SBParam.TRANSMIT_CODE39_CHK_DIGIT;
                break;
            case 37:
                value = SDConsts.SBParam.CODE39_FULL_ASCII;
                break;
            case 38:
                value = SDConsts.SBParam.CODE93;
                break;
            case 39:
                value = SDConsts.SBParam.CODE93_LEN_MIN;
                break;
            case 40:
                value = SDConsts.SBParam.CODE93_LEN_MAX;
                break;
            case 41:
                value = SDConsts.SBParam.CODE11;
                break;
            case 42:
                value = SDConsts.SBParam.CODE11_LEN_MIN;
                break;
            case 43:
                value = SDConsts.SBParam.CODE11_LEN_MAX;
                break;
            case 44:
                value = SDConsts.SBParam.CODE11_CHK_DIGIT_VERIFICATION;
                break;
            case 45:
                value = SDConsts.SBParam.TRANSMIT_CODE11_CHK_DIGIT;
                break;
            case 46:
                value = SDConsts.SBParam.INTERLEAVED2OF5;
                break;
            case 47:
                value = SDConsts.SBParam.INTERLEAVED2OF5_LEN_MIN;
                break;
            case 48:
                value = SDConsts.SBParam.INTERLEAVED2OF5_LEN_MAX;
                break;
            case 49:
                value = SDConsts.SBParam.INTERLEAVED2OF5_CHK_DIGIT;
                break;
            case 50:
                value = SDConsts.SBParam.TRANSMIT_INTERLEAVED2OF5_CHK_DIGIT;
                break;
            case 51:
                value = SDConsts.SBParam.CONVERT_INTERLEAVED_EAN13;
                break;
            case 52:
                value = SDConsts.SBParam.DISCRETE2OF5;
                break;
            case 53:
                value = SDConsts.SBParam.DISCRETE2OF5_LEN_MIN;
                break;
            case 54:
                value = SDConsts.SBParam.DISCRETE2OF5_LEN_MAX;
                break;
            case 55:
                value = SDConsts.SBParam.CODABAR;
                break;
            case 56:
                value = SDConsts.SBParam.CODABAR_LEN_MIN;
                break;
            case 57:
                value = SDConsts.SBParam.CODABAR_LEN_MAX;
                break;
            case 58:
                value = SDConsts.SBParam.CODABAR_CLSI_EDIT;
                break;
            case 59:
                value = SDConsts.SBParam.CODABAR_NOTIS_EDIT;
                break;
            case 60:
                value = SDConsts.SBParam.MSI;
                break;
            case 61:
                value = SDConsts.SBParam.MSI_LEN_MIN;
                break;
            case 62:
                value = SDConsts.SBParam.MSI_LEN_MAX;
                break;
            case 63:
                value = SDConsts.SBParam.MSI_CHK_DIGIT;
                break;
            case 64:
                value = SDConsts.SBParam.TRANSMIT_MSI_CHK_DIGIT;
                break;
            case 65:
                value = SDConsts.SBParam.MSI_CHK_DIGIT_ALGORITHM;
                break;
            case 66:
                value = SDConsts.SBParam.CHINESE_POST;
                break;
            case 67:
                value = SDConsts.SBParam.MATRIX2OF5;
                break;
            case 68:
                value = SDConsts.SBParam.MATRIX2OF5_LEN_MIN;
                break;
            case 69:
                value = SDConsts.SBParam.MATRIX2OF5_LEN_MAX;
                break;
            case 70:
                value = SDConsts.SBParam.MATRIX2OF5_CHK_DIGIT;
                break;
            case 71:
                value = SDConsts.SBParam.TRANSMIT_MATRIX2OF5_CHK_DIGIT;
                break;
            case 72:
                value = SDConsts.SBParam.KOREA_POST;
                break;
            case 73:
                value = SDConsts.SBParam.INVERSE_1D;
                break;
            case 74:
                value = SDConsts.SBParam.US_POSTNET;
                break;
            case 75:
                value = SDConsts.SBParam.US_PLANET;
                break;
            case 76:
                value = SDConsts.SBParam.TRANSMIT_US_POSTNET_CHK_DIGIT;
                break;
            case 77:
                value = SDConsts.SBParam.UK_POST;
                break;
            case 78:
                value = SDConsts.SBParam.TRANSMIT_UK_POST_CHK_DIGIT;
                break;
            case 79:
                value = SDConsts.SBParam.JAPANESE_POST;
                break;
            case 80:
                value = SDConsts.SBParam.AUSTRAILA_POST;
                break;
            case 81:
                value = SDConsts.SBParam.AUSTRAILA_POST_FORMAT;
                break;
            case 82:
                value = SDConsts.SBParam.NETHELANS_POST;
                break;
            case 83:
                value = SDConsts.SBParam.USPS_4;
                break;
            case 84:
                value = SDConsts.SBParam.UPI_FICS_POST;
                break;
            case 85:
                value = SDConsts.SBParam.GS1_DATABAR;
                break;
            case 86:
                value = SDConsts.SBParam.GS1_LIMIT;
                break;
            case 87:
                value = SDConsts.SBParam.GS1_LIMIT_SECURITY;
                break;
            case 88:
                value = SDConsts.SBParam.GS1_EXPAND;
                break;
            case 89:
                value = SDConsts.SBParam.CONVERT_GS1_UPCEAN;
                break;
            case 90:
                value = SDConsts.SBParam.COMPOSIT_CC_C;
                break;
            case 91:
                value = SDConsts.SBParam.COMPOSIT_CC_AB;
                break;
            case 92:
                value = SDConsts.SBParam.COMPOSIT_TCL39;
                break;
            case 93:
                value = SDConsts.SBParam.UPC_COMPOSIT_MODE;
                break;
            case 94:
                value = SDConsts.SBParam.GS1128_EMULATION_MODE;
                break;
            case 95:
                value = SDConsts.SBParam.PDF417;
                break;
            case 96:
                value = SDConsts.SBParam.MICRO_PDF;
                break;
            case 97:
                value = SDConsts.SBParam.DATA_MATRIX;
                break;
            case 98:
                value = SDConsts.SBParam.DATA_MATRIX_INVERSE;
                break;
            case 99:
                value = SDConsts.SBParam.DATA_MATRIX_DECODE_MIRROR;
                break;
            case 100:
                value = SDConsts.SBParam.MAXICODE;
                break;
            case 101:
                value = SDConsts.SBParam.QR_CODE;
                break;
            case 102:
                value = SDConsts.SBParam.MICRO_QR;
                break;
            case 103:
                value = SDConsts.SBParam.AZTECCODE;
                break;
            case 104:
                value = SDConsts.SBParam.AZTECCODE_INVERSE;
                break;
            case 105:
                value = SDConsts.SBParam.HANXIN;
                break;
            case 106:
                value = SDConsts.SBParam.HANXIN_INVERSE;
                break;
            case 107:
                value = SDConsts.SBParam.REDUNDANCY_LEVEL;
                break;
            case 108:
                value = SDConsts.SBParam.SECURITY_LEVEL;
                break;
            case 109:
                value = SDConsts.SBParam.INTERCHARATER_GAP_SIZE;
                break;
            case 110:
                value = SDConsts.SBParam.DECODE_TIMEOUT;
                break;
            case 111:
                value = SDConsts.SBParam.TIMEOUT_SAME_SYMBOL;
                break;
            case 112:
                value = SDConsts.SBParam.AIMER_MODE;
                break;
            case 113:
                value = SDConsts.SBParam.ILLUMINATION_MODE;
                break;
            case 114:
                value = SDConsts.SBParam.PICKLIST_MODE;
                break;
        }
        return value;
    }
}