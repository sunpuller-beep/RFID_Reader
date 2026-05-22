/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bluebird.keymapper.KeyMapperManager;

import java.lang.ref.WeakReference;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.BCResumeListener;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class BarcodeFragment extends Fragment {
    private static final String TAG = BarcodeFragment.class.getSimpleName();

    private static final boolean D = Constants.BAR_D;

    private Button mSetRfidBt;
    private Button mSetBarcodeBt;
    private Button mGetTriggerBt;

    private Button mPressBarcode;
    private Button mReleaseBarcode;

    private Button mSetModeKeyEnable;
    private Button mSetModeKeyDisable;
    private Button mGetModeKeyEnableState;

    private Button mSetMultiScanTypeEnable;
    private Button mSetMultiScanTypeDisable;
    private Button mGetMultiScanTypeState;

    private Spinner mBarcodeMultiSpin;
    private ArrayAdapter<CharSequence> mBarcodeMultiChar;
    private Button mSetBarcodeMultiNumberBt;
    private Button mGetBarcodeMultiNumberBt;

    private Spinner mBarcodeTriggerSpin;
    private ArrayAdapter<CharSequence> mBarcodeTriggerChar;
    private Button mSetBarcodeTriggerBt;
    private Button mGetBarcodeTriggerBt;

    private Spinner mBarcodeHWSpin;
    private ArrayAdapter<CharSequence> mBarcodeHWChar;
    private Button mSetBarcodeHWBt;
    private Button mGetBarcodeHWBt;

    private Button mPauseBarcode;
    private Button mResumeBarcode;
    private Button mGetBarcodeState;
    private Button mGetBarcodeCameraInfoBt;
    private Button mEnableMulti;
    private Button mDisableMulti;
    private Button mGetMultiState;

    private TextView mMessageTextView;

    private TextView mDataTextView;

    private Reader mReader;

    private Context mContext;

    private Handler mOptionHandler;

    private int mSledType = SDConsts.SLED_TYPE.SLED_UNKNOWN;

    private final BarcodeHandler mBarcodeHandler = new BarcodeHandler(this);

    public static BarcodeFragment newInstance() {
        return new BarcodeFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.barcode_frag, container, false);
        if (D) Log.d(TAG, "onCreateView");
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        mMessageTextView = (TextView) v.findViewById(R.id.message_textview);

        mDataTextView = (TextView) v.findViewById(R.id.data_textview);

        mSetRfidBt = (Button) v.findViewById(R.id.bt_settigger_rfid);
        mSetRfidBt.setOnClickListener(buttonListener);
        mSetBarcodeBt = (Button) v.findViewById(R.id.bt_settigger_barcode);
        mSetBarcodeBt.setOnClickListener(buttonListener);
        mGetTriggerBt = (Button) v.findViewById(R.id.bt_trigger);
        mGetTriggerBt.setOnClickListener(buttonListener);

        mSetModeKeyEnable = (Button) v.findViewById(R.id.bt_mode_key_enable);
        mSetModeKeyEnable.setOnClickListener(buttonListener);
        mSetModeKeyDisable = (Button) v.findViewById(R.id.bt_mode_key_disable);
        mSetModeKeyDisable.setOnClickListener(buttonListener);
        mGetModeKeyEnableState = (Button) v.findViewById(R.id.bt_get_mode_key_state);
        mGetModeKeyEnableState.setOnClickListener(buttonListener);

        mSetMultiScanTypeEnable = (Button) v.findViewById(R.id.bt_multi_scan_type_enable);
        mSetMultiScanTypeEnable.setOnClickListener(buttonListener);
        mSetMultiScanTypeDisable = (Button) v.findViewById(R.id.bt_multi_scan_type_disable);
        mSetMultiScanTypeDisable.setOnClickListener(buttonListener);
        mGetMultiScanTypeState = (Button) v.findViewById(R.id.bt_get_multi_scan_type_state);
        mGetMultiScanTypeState.setOnClickListener(buttonListener);

        mPressBarcode = (Button) v.findViewById(R.id.bt_barcode_press);
        mPressBarcode.setOnClickListener(buttonListener);
        mReleaseBarcode = (Button) v.findViewById(R.id.bt_barcode_release);
        mReleaseBarcode.setOnClickListener(buttonListener);

        mSetBarcodeMultiNumberBt = (Button) v.findViewById(R.id.bt_set_multi_number);
        mSetBarcodeMultiNumberBt.setOnClickListener(buttonListener);
        mGetBarcodeMultiNumberBt = (Button) v.findViewById(R.id.bt_get_multi_number);
        mGetBarcodeMultiNumberBt.setOnClickListener(buttonListener);
        mBarcodeMultiSpin = (Spinner) v.findViewById(R.id.barcode_multi_number_spin);
        mBarcodeMultiChar = ArrayAdapter.createFromResource(mContext, R.array.multi_number_array,
                android.R.layout.simple_spinner_dropdown_item);
        mBarcodeMultiSpin.setAdapter(mBarcodeMultiChar);

        mSetBarcodeTriggerBt = (Button) v.findViewById(R.id.bt_set_trigger);
        mSetBarcodeTriggerBt.setOnClickListener(buttonListener);
        mGetBarcodeTriggerBt = (Button) v.findViewById(R.id.bt_get_trigger);
        mGetBarcodeTriggerBt.setOnClickListener(buttonListener);
        mBarcodeTriggerSpin = (Spinner) v.findViewById(R.id.barcode_trigger_spin);
        mBarcodeTriggerChar = ArrayAdapter.createFromResource(mContext, R.array.trigger_array,
                android.R.layout.simple_spinner_dropdown_item);
        mBarcodeTriggerSpin.setAdapter(mBarcodeTriggerChar);

        mSetBarcodeHWBt = (Button) v.findViewById(R.id.bt_set_hw_key);
        mSetBarcodeHWBt.setOnClickListener(buttonListener);
        mGetBarcodeHWBt = (Button) v.findViewById(R.id.bt_get_hw_key);
        mGetBarcodeHWBt.setOnClickListener(buttonListener);
        mBarcodeHWSpin = (Spinner) v.findViewById(R.id.barcode_hw_key_spin);
        mBarcodeHWChar = ArrayAdapter.createFromResource(mContext, R.array.barcodehw_array,
                android.R.layout.simple_spinner_dropdown_item);
        mBarcodeHWSpin.setAdapter(mBarcodeHWChar);

        mPauseBarcode = (Button) v.findViewById(R.id.bt_barcode_pause);
        mPauseBarcode.setOnClickListener(buttonListener);
        mResumeBarcode = (Button) v.findViewById(R.id.bt_barcode_resume);
        mResumeBarcode.setOnClickListener(buttonListener);
        mGetBarcodeState = (Button) v.findViewById(R.id.bt_get_barcode_state);
        mGetBarcodeState.setOnClickListener(buttonListener);

        mEnableMulti = (Button) v.findViewById(R.id.bt_enable_multi);
        mEnableMulti.setOnClickListener(buttonListener);
        mDisableMulti = (Button) v.findViewById(R.id.bt_disable_multi);
        mDisableMulti.setOnClickListener(buttonListener);
        mGetMultiState = (Button) v.findViewById(R.id.bt_get_multi_state);
        mGetMultiState.setOnClickListener(buttonListener);

        //<-[20250825]Add barcode/camera info
        mGetBarcodeCameraInfoBt = (Button) v.findViewById(R.id.bt_barcode_get_info);
        mGetBarcodeCameraInfoBt.setOnClickListener(buttonListener);
        //[20250825]Add barcode/camera info->
//        setBuiltInModelKeyEvent(v);//origin - block

        return v;
    }

    private void setBarcodeKeyMapOn(boolean setOn){
        try{
            @SuppressLint("WrongConstant") KeyMapperManager mKeyMapperManager = (KeyMapperManager) mContext.getSystemService("keymapper");
            int sourceKeyCode = KeyEvent.keyCodeFromString("KEYCODE_PTT_SCAN_R");

            mKeyMapperManager.setKeyMapping(sourceKeyCode,setOn);
            if(setOn)
                mKeyMapperManager.removeKeyMapSetting(sourceKeyCode);
        }catch(NullPointerException e0){
            e0.printStackTrace();
            Toast.makeText(mContext, "All barcode func works fine. for your information This device's image is not keymapper supported.", Toast.LENGTH_SHORT).show();
            return;
        }catch (NoSuchMethodError e1){
            e1.printStackTrace();
            if (D) Log.d(TAG, "NoSuchMethodError : unknown0");
//            Toast.makeText(mContext, "unknown0", Toast.LENGTH_SHORT).show();
        }catch (Exception e2){
            e2.printStackTrace();
            return;
        }
    }

    private void setBuiltInModelKeyEvent(View mView){
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();

        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == 503) {  //KEYCODE_PTT_SCAN_R
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && mSledType == SDConsts.SLED_TYPE.INTERNAL_SLED) {
                        int ret = mReader.BC_SetTriggerState(true);
                        return true;
                    } else return false;
                } else return false;
            }
        });
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
//        setBarcodeKeyMapOn(false);
        mReader = Reader.getReader(mContext, mBarcodeHandler);
        mReader.BC_SetBarcodeKeyFormat(Utils.getSelection(mContext));
        if (mReader != null) {
            mBarcodeHWSpin.setSelection(mReader.BC_GetBarcodeKeyFormat());
            mBarcodeTriggerSpin.setSelection(mReader.BC_GetBarcodeTriggerMode());
            mBarcodeMultiSpin.setSelection(mReader.BC_GetBarcodeMultiScanNumber() - 1);
            mSledType = mReader.SD_GetType();
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
//        setBarcodeKeyMapOn(true);
        mReader.BC_SetBarcodeKeyFormat(Utils.getSelection(mContext));
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
//                if (mode == SDConsts.SDTriggerMode.BARCODE) {}
                ret = mReader.SD_SetTriggerMode(SDConsts.SDTriggerMode.RFID);
                //+control HW key
                setBarcodeKeyMapOn(false);
                //control HW key+
                mDataTextView.setText(" SET RFID MODE result : " + ret);
                if (D) Log.d(TAG, "set rfid cur mode = " + mode + " ret = " + ret);
            }
            //<-[20250825]Add barcode/camera info
            else if(id == R.id.bt_barcode_get_info){
                retString = "SD_BarcodeCamera_Info ";
                int info  = mReader.BC_GetSupportedDevicesInfo();
                retString +=" : " + info;
                if (D) Log.d(TAG, "get barcode/camera cur info = " + info);
            }
            //[20250825]Add barcode/camera info->
            else if(id == R.id.bt_settigger_barcode){
                retString = "SD_BARCODE_Mode ";
                mode = mReader.SD_GetTriggerMode();
//                if (mode == SDConsts.SDTriggerMode.RFID) {}
                ret = mReader.SD_SetTriggerMode(SDConsts.SDTriggerMode.BARCODE);
                //+control HW key
                setBarcodeKeyMapOn(true);
                //control HW key+
                mDataTextView.setText(" SET BARCODE MODE result : " + ret);
                if (D) Log.d(TAG, "set barcode cur mode = " + mode + " ret = " + ret);
            }else if(id == R.id.bt_trigger){
                retString = "SD_GetTriggerMode ";
                ret = mReader.SD_GetTriggerMode();
                if (ret == SDConsts.SDTriggerMode.BARCODE)
                    if (D) Log.d(TAG, "barcode");
                    else if (ret == SDConsts.SDTriggerMode.RFID)
                        if (D) Log.d(TAG, "rfid");
                        else if (D) Log.d(TAG, "other state");
                mDataTextView.setText(" GET TRG MODE : " + ret);
                if (D) Log.d(TAG, "get trigger mode = " + ret);
            }else if(id == R.id.bt_barcode_press){
                retString = "BC_SetTriggerState ";
                ret = mReader.BC_SetTriggerState(true);
            }else if(id == R.id.bt_barcode_release){
                retString = "BC_SetTriggerState ";
                ret = mReader.BC_SetTriggerState(false);
            }else if(id == R.id.bt_mode_key_enable){
                retString = "SD_SetModeKeyEnable ";
                ret = mReader.SD_SetModeKeyEnable(SDConsts.SDModeKeyState.ENABLE);
                mDataTextView.setText(" SET MODEKEY ENABLE : " + ret);
                if (D) Log.d(TAG, "set modekey enable = " + ret);
            }else if(id == R.id.bt_mode_key_disable){
                retString = "SD_SetModeKeyEnable ";
                ret = mReader.SD_SetModeKeyEnable(SDConsts.SDModeKeyState.DISABLE);
                mDataTextView.setText(" SET MODEKEY DISABLE : " + ret);
                if (D) Log.d(TAG, "set modekey disable = " + ret);
            }else if(id == R.id.bt_get_mode_key_state){
                retString = "SD_GetModeKeyEnableState ";
                ret = mReader.SD_GetModeKeyEnableState();
                mDataTextView.setText(" GET MODEKEY STATE result : " + ret);
                if (D) Log.d(TAG, "get modekey enable state = " + ret);
            }else if(id == R.id.bt_multi_scan_type_enable){
                retString = "BC_SetBarcodeMultiScanType Enable";
                ret = mReader.BC_SetBarcodeMultiScanType(SDConsts.BCMultiScanType.ENABLE);
                mDataTextView.setText(" SET MULTISCANTYPE ENABLE : " + ret);
                if (D) Log.d(TAG, "set multi scan type enable = " + ret);
            }else if(id == R.id.bt_multi_scan_type_disable){
                retString = "BC_SetBarcodeMultiScanType Disable";
                ret = mReader.BC_SetBarcodeMultiScanType(SDConsts.BCMultiScanType.DISABLE);
                mDataTextView.setText(" SET MULTISCANTYPE DISABLE : " + ret);
                if (D) Log.d(TAG, "set multi scan type disable = " + ret);
            }else if(id == R.id.bt_get_multi_scan_type_state){
                retString = "BC_GetBarcodeMultiScanType ";
                ret = mReader.BC_GetBarcodeMultiScanType();
                mDataTextView.setText(" GET MULTISCANTYPE STATE result : " + ret);
                if (D) Log.d(TAG, "get multi scan type = " + ret);
            }else if(id == R.id.bt_get_multi_number){
                retString = "BC_GetBarcodeMultiScanNumber ";
                ret = mReader.BC_GetBarcodeMultiScanNumber();
                mDataTextView.setText(" GET MULTI NUM result : " + ret);
                if (D) Log.d(TAG, "get barcode multi number = " + ret);
            }else if(id == R.id.bt_set_multi_number){
                retString = "BC_SetBarcodeMultiScanNumber ";
                ret = mReader.BC_SetBarcodeMultiScanNumber(
                        mBarcodeMultiSpin.getSelectedItemPosition() + 1);
                mDataTextView.setText(" SET MULTI NUM : " + ret);
                if (D) Log.d(TAG, "set barcode multi number = " + ret);
            }else if(id == R.id.bt_get_trigger){
                retString = "BC_GetBarcodeTriggerMode ";
                ret = mReader.BC_GetBarcodeTriggerMode();
                mDataTextView.setText(" GET BC TRIGGER : " + ret);
                if (D) Log.d(TAG, "get barcode trigger state = " + ret);
            }else if(id == R.id.bt_set_trigger){
                retString = "BC_SetBarcodeTriggerMode ";
                ret = mReader.BC_SetBarcodeTriggerMode(mBarcodeTriggerSpin.getSelectedItemPosition());
                mDataTextView.setText(" SET BC TRIGGER result : " + ret);
                if (D) Log.d(TAG, "set barcode trigger state = " + ret);
            }else if(id == R.id.bt_get_hw_key){
                retString = "BC_GetBarcodeKeyFormat ";
                ret = mReader.BC_GetBarcodeKeyFormat();
                mDataTextView.setText(" GET BC/HW KEY : " + ret);
                if (D) Log.d(TAG, "get barcode hw state = " + ret);
            }else if(id == R.id.bt_set_hw_key){
                retString = "BC_SetBarcodeKeyFormat ";
                ret = mReader.BC_SetBarcodeKeyFormat(mBarcodeHWSpin.getSelectedItemPosition());
                Utils.saveSelection(mContext, mBarcodeHWSpin.getSelectedItemPosition());
                mDataTextView.setText(" SET BC/HW KEY result : " + ret);
                if (D) Log.d(TAG, "set barcode hw state = " + ret);
            }else if(id == R.id.bt_barcode_pause){
                retString = "BC_PauseBarcode ";
                if (mReader.BC_GetBarcodeState() == SDConsts.BCState.ACTIVE) {
                    ret = mReader.BC_PauseBarcode();
                    mDataTextView.setText(" PAUSE BARCODE : " + ret);
                    if (D) Log.d(TAG, "pause barcode result = " + ret);
                }
            }else if(id == R.id.bt_barcode_resume){
                retString = "BC_ResumeBarcode ";
//                    if (mReader.BC_GetBarcodeState() == SDConsts.BCState.PAUSED) {
//                        ret = mReader.BC_ResumeBarcode();
//                        if (D) Log.d(TAG, "resume barcode result = " + ret);
//                    }
                if (mReader.BC_GetBarcodeState() == SDConsts.BCState.PAUSED) {
                    mReader.BC_ResumeBarcode(new BCResumeListener() {
                        @Override
                        public void onCompleted(int resumeResult) {
                            if (D) Log.d(TAG, "onCompleted = " + resumeResult);
                            mDataTextView.setText(" RESUME BARCODE : " + resumeResult);
                            mMessageTextView.setText("BC_ResumeBarcode " + resumeResult);
                        }
                    });
                }
            }else if(id == R.id.bt_get_barcode_state){
                retString = "BC_GetBarcodeState ";
                ret = mReader.BC_GetBarcodeState();
                mDataTextView.setText(" GET BARCODE STATE result : " + ret);
                if (D) Log.d(TAG, "get barcode state = " + ret);
            }else if(id == R.id.bt_enable_multi){
                retString = "BC_SetBarcodeMultiScan ENABLE";
                ret = mReader.BC_SetBarcodeMultiScan(SDConsts.BCMultiScanState.ENABLE);
                mDataTextView.setText(" ENABLE MULTI : " + ret);
                if (D) Log.d(TAG, "enable multi result = " + ret);
            }else if(id == R.id.bt_disable_multi){
                retString = "BC_SetBarcodeMultiScan DISABLE ";
                ret = mReader.BC_SetBarcodeMultiScan(SDConsts.BCMultiScanState.DISABLE);
                mDataTextView.setText(" DISABLE MULTI : " + ret);
                if (D) Log.d(TAG, "disable multi result = " + ret);
            }else if(id == R.id.bt_get_multi_state){
                retString = "BC_GetBarcodeMultiScanState ";
                ret = mReader.BC_GetBarcodeMultiScanState();
                mDataTextView.setText(" GET MULTI STATE result : " + ret);
                if (D) Log.d(TAG, "get multi state = " + ret);
            }else{
                if (ret != -100)
                    retString += ret;
            }
            mMessageTextView.setText(" " + retString);
        }
    };

    private static class BarcodeHandler extends Handler {
        private final WeakReference<BarcodeFragment> mExecutor;

        public BarcodeHandler(BarcodeFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            BarcodeFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mBarcodeHandler");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_PRESSED)
                    mMessageTextView.setText(" " + "TRIGGER_PRESSED");
                else if (m.arg1 == SDConsts.SDCmdMsg.TRIGGER_RELEASED)
                    mMessageTextView.setText(" " + "TRIGGER_RELEASED");
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_MODE_CHANGED)
                    mMessageTextView.setText(" " + "SLED_MODE_CHANGED " + m.arg2);
                else if (m.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
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
                    if (m.obj != null && m.obj instanceof String) {
                        readData.append("\n" + (String) m.obj);
                        mDataTextView.setText(" " + (String) m.obj);
                    }
                    mMessageTextView.setText(" " + readData.toString());
                } else if (m.arg1 == SDConsts.BCCmdMsg.BARCODE_ERROR) {
                    if (D) Log.d(TAG, "BARCODE_ERROR");
                    if (m.arg2 == SDConsts.BCResult.LOW_BATTERY)
                        readData.append(" " + "BARCODE_ERROR Low battery");
                    else
                        readData.append(" " + "BARCODE_ERROR ");
                    readData.append("barcode pasue");
                }
                if (D) Log.d(TAG, "data = " + readData.toString());
                break;

            //+sb
            case SDConsts.Msg.SBMsg:
                if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_TRIGGER_PRESSED_SLED){
//                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_PRESSED_SLED");
                }
                else if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_TRIGGER_RELEASED_SLED){
//                    mMessageTextView.setText(" " + "BARCODE_TRIGGER_RELEASED_SLED");
                }
                else if (m.arg1 == SDConsts.SBCmdMsg.BARCODE_READ) {
                    StringBuilder SBData = new StringBuilder();
                    if (D) Log.d(TAG, "SB_MSG_BARCODE_READ");
                    if (m.arg2 == SDConsts.SBResult.SUCCESS)
                        SBData.append(" " + "SB_MSG_BARCODE_READ");
                    else if (m.arg2 == SDConsts.SBResult.ACCESS_TIMEOUT)
                        SBData.append(" " + "SB_MSG_BARCODE_ACCESS_TIMEOUT");
                    if (m.obj != null  && m.obj instanceof String) {
                        SBData.append("\n  " + (String)m.obj);
                        mDataTextView.setText(" " + (String)m.obj);
                    }
                    mMessageTextView.setText(" " + SBData.toString());
                }
                break;
            //sb+

        }
    }
}