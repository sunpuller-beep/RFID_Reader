/*
 * Copyright (C) 2015 - 2020 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 *
 * Author : JongWon  Lee
 *
 * Date : 2020.03.09
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

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
import android.provider.Settings;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.lang.ref.WeakReference;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;

import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.fileutil.FileSelectorDialog;
import co.kr.bluebird.rfid.app.bbrfiddemo.permission.PermissionHelper;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class RFAccessFragment extends Fragment {

    private static final String TAG = RFAccessFragment.class.getSimpleName();

    private static final boolean D = Constants.ACC_D;

    private static final String FILE_DIR_PRE = "//DIR//";

    private static final String FILE_EXT = ".bin";

    private static final int PROGRESS_DIALOG = 1;

    private ListView mRfidList;

    private Reader mReader;

    private Context mContext;

    private ProgressDialog mDialog;

    private ArrayAdapter<CharSequence> mItemChar;

    private Handler mOptionHandler;

    private FileSelectorDialog mFileSelecter;

    private AccessHandler mAccessHandler = new AccessHandler(this);

    private final static int READ_DIRECTORY_REQUEST_CODE = 1000;

    //+change UI input/output
    private EditText mStartPosText;
    private EditText mLengthText;
    private EditText mDataText1;
    private EditText mDataText2;
    private EditText mDataText3;
    private EditText mAccessPwText;
    private TextView mResultText;

    private Spinner mMemtypeSpin;
    private Spinner mSelectionSpin;
    private ArrayAdapter<CharSequence> mMemtypeChar;
    private ArrayAdapter<CharSequence> mSelectionChar;
    //change UI input/output+

    public static RFAccessFragment newInstance() {
        return new RFAccessFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.rf_access_frag, container, false);
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        //+change UI input/output
        mMemtypeSpin = (Spinner) v.findViewById(R.id.memtype_spin);
        mMemtypeChar = ArrayAdapter.createFromResource(mContext, R.array.access_memtype_array,
                android.R.layout.simple_spinner_dropdown_item);
        mMemtypeSpin.setAdapter(mMemtypeChar);
        mMemtypeSpin.setSelection(1);

        mStartPosText = (EditText) v.findViewById(R.id.startPos_edit);
        mLengthText = (EditText) v.findViewById(R.id.len_edit);
        mDataText1 = (EditText) v.findViewById(R.id.data1_edit);
        mDataText2 = (EditText) v.findViewById(R.id.data2_edit);
        mAccessPwText = (EditText) v.findViewById(R.id.accessPw_edit);
        mResultText = (TextView) v.findViewById(R.id.result_text);

        mSelectionSpin = (Spinner) v.findViewById(R.id.selection_spin);
        mSelectionChar = ArrayAdapter.createFromResource(mContext, R.array.selection_array,
                android.R.layout.simple_spinner_dropdown_item);
        mSelectionSpin.setAdapter(mSelectionChar);
        //change UI input/output+

        mRfidList = (ListView) v.findViewById(R.id.access_list);
        mRfidList.setOnItemClickListener(listListener);

        mItemChar = ArrayAdapter.createFromResource(mContext, R.array.items_array,
                android.R.layout.simple_list_item_1);

        mRfidList.setAdapter(mItemChar);
        return v;
    }

    private OnItemClickListener listListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // TODO Auto-generated method stub
            int pos = arg2;
            int result = -1000;
            String sResult = null;
            //+change UI input/output
            int startPos = 0;
            String startPosStr = null;
            String lenStr = null;
            String data1 = null;
            String data2 = null;
            String data3 = null;
            String accessPW = "00000000";
            int length = 0;
            int memTye = 0;
            int offset = 0;
            int cnt = 0;
            boolean setSelect = false;
            //change UI input/output+
            StringBuilder message = new StringBuilder();
            switch (pos) {
                case 0:
                    //read
                    //+change UI input/output
                    memTye = mMemtypeSpin.getSelectedItemPosition();
                    setSelect = mSelectionSpin.getSelectedItemPosition() == 1 ? true : false;
                    startPosStr = mStartPosText.getText().toString();
                    lenStr = mLengthText.getText().toString();
                    accessPW = mAccessPwText.getText().toString();

                    if (startPosStr == null || lenStr == null || accessPW == null
                            || startPosStr.equals("") || lenStr.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    startPos = Integer.parseInt(startPosStr);
                    length = Integer.parseInt(lenStr);
                    Log.d(TAG, "### RF_READ:  memTye = " + memTye + " ,startPos = " + startPos + " ,length = " + length + " ,accessPW = " + accessPW + " ,selection = " + setSelect);
                    result = mReader.RF_READ(memTye, startPos, length, accessPW, setSelect);
                    //change UI input/output+

                    //read bank type tid
                    //result = mReader.RF_READ(SDConsts.RFMemType.TID, 0, 3, "00000000", false);

                    //read bank type reserved (0, 2) 8byte
                    // Address : 0,1 kill password, Address : 2,3 access password
                    //result = mReader.RF_READ(SDConsts.RFMemType.RESERVED, 0, 2, "00000000", false);
                    //result = mReader.RF_READ(SDConsts.RFMemType.RESERVED, 2, 2, "00000000", false);

                    //read bank type user
                    //result = mReader.RF_READ(SDConsts.RFMemType.USER, 0, 4, "00000000", false);
                    //result = mReader.RF_READ(SDConsts.RFMemType.USER, 7, 1, "00000000", false);

                    //read bank type epc
                    //result = mReader.RF_READ(SDConsts.RFMemType.EPC, 0, 8, "00000000", false);//origin
                    //result = mReader.RF_READ(SDConsts.RFMemType.EPC, 2, 6, "00000000", false);
                    //result = mReader.RF_READ(SDConsts.RFMemType.EPC, 1, 7, "00000000", false);
                    //result = mReader.RF_NXP_DNA_KeyVerification(1);
                    //result = mReader.RF_SetRegionISO("MO"); //add new ISO code

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_READ");
                    else
                        message.append("RF_READ failed " + result);
                    break;

                case 1:
                    //write epc or user
                    //+change UI input/output
                    memTye = mMemtypeSpin.getSelectedItemPosition();
                    setSelect = mSelectionSpin.getSelectedItemPosition() == 1 ? true : false;
                    startPosStr = mStartPosText.getText().toString();
                    data1 = mDataText1.getText().toString();
                    accessPW = mAccessPwText.getText().toString();

                    if (startPosStr == null || data1 == null || accessPW == null
                            || startPosStr.equals("") || data1.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startPos = Integer.parseInt(startPosStr);
                    Log.d(TAG, "### RF_WRITE:  memTye = " + memTye + " ,startPos = " + startPos + " ,data1 = " + data1 + " ,accessPW = " + accessPW + " ,selection = " + setSelect);
                    result = mReader.RF_WRITE(memTye, startPos, data1, accessPW, setSelect);
                    //change UI input/output+

                    //result = mReader.RF_WRITE(SDConsts.RFMemType.USER, 0, "0000000000000000", "00000000", false);
                    //result = mReader.RF_WRITE(SDConsts.RFMemType.USER, 0, "1111222233334444", "00000000", false);
                    //result = mReader.RF_WRITE(SDConsts.RFMemType.EPC, 2, "1111222233334444", "00000000",  false);
                    //result = mReader.RF_NXP_DNA_Authenticate_TAM1();
                    //result = mReader.RF_WRITE(SDConsts.RFMemType.EPC, 2, "303556022843A3C00000000D", "00000000",  false);
                    //result = mReader.RF_WRITE(SDConsts.RFMemType.EPC, 2, "303400000000000254181901", "00000000", false);//origin

                    //+add new ISO code
                    //String[] channels = mReader.RF_GetEnableChannels();
                    //for(int i =0; i<channels.length; i++){
                    //message.append(channels[i]+"/");
                    //Log.d(TAG, "channel: "+message.toString());
                    //}
                    //add new ISO code+

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_WRITE");
                    else
                        message.append("RF_WRITE failed " + result);
                    break;

                case 2:
                    // write epc
                    //+change UI input/output
                    startPosStr = mStartPosText.getText().toString();
                    data1 = mDataText1.getText().toString();
                    accessPW = mAccessPwText.getText().toString();
                    setSelect = mSelectionSpin.getSelectedItemPosition() == 1 ? true : false;

                    if (startPosStr == null || data1 == null || accessPW == null
                            || startPosStr.equals("") || data1.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    startPos = Integer.parseInt(startPosStr);
                    Log.d(TAG, "### RF_WriteTagID: startPos = " + startPos + " ,data1 = " + data1 + " ,accessPW = " + accessPW + " ,selection = " + setSelect);
                    result = mReader.RF_WriteTagID(1, data1, accessPW, setSelect);
                    //change UI input/output+

                    //result = mReader.RF_WRITE(SDConsts.RFMemType.EPC, 2, "303400000000000254181902", "00000000", false);//test
                    //result = mReader.RF_WriteTagID(1, "3000666655554444333322221111", "00000000", false);
                    //result = mReader.RF_WriteTagID(1, "3000666655554444333322221111", "00000000", false);//origin
                    //result = mReader.RF_NXP_DNA_KeyVerification(2);
                    //result = mReader.RF_SetRegionISO("NI"); //add new ISO code

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_WriteTagID");
                    else
                        message.append("RF_WriteTagID failed " + result);
                    break;

                case 3:
                    // write Access PW(new password , old password)
                    //+change UI input/output
                    data1 = mDataText1.getText().toString();
                    accessPW = mAccessPwText.getText().toString();
                    setSelect = mSelectionSpin.getSelectedItemPosition() == 1 ? true : false;

                    if (data1 == null || accessPW == null
                            || data1.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "### RF_WriteAccessPassword: data1 = " + data1 + " ,accessPW = " + accessPW + " ,selection = " + setSelect);
                    result = mReader.RF_WriteAccessPassword(data1, accessPW, setSelect);
                    //change UI input/output+

                    //result = mReader.RF_WriteAccessPassword("00000000", "00000000", false); //origin
                    //result = mReader.RF_NXP_DNA_Authenticate_TAM2(1,0,0);

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_WriteAccessPassword");
                    else
                        message.append("RF_WriteAccessPassword failed " + result);
                    break;

                case 4:
                    //write kill PW
                    //+change UI input/output
                    data1 = mDataText1.getText().toString();
                    accessPW = mAccessPwText.getText().toString();
                    setSelect = mSelectionSpin.getSelectedItemPosition() == 1 ? true : false;

                    if (data1 == null || accessPW == null
                            || data1.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "### RF_WriteKillPassword: data1 = " + data1 + " ,accessPW = " + accessPW + " ,selection = " + setSelect);
                    result = mReader.RF_WriteKillPassword(data1, accessPW, setSelect);
                    //change UI input/output+

                    //result = mReader.RF_WriteKillPassword("00000000", "00000000", false); //origin
                    //result = mReader.RF_NXP_DNA_Authenticate_TAM2(1,1,0);

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_WriteKillPassword");
                    else
                        message.append("RF_WriteKillPassword failed " + result);
                    break;

                case 5:
                    //lock
                    //+change UI input/output
                    data1 = mDataText1.getText().toString(); //lockMask
                    data2 = mDataText2.getText().toString(); //action
                    accessPW = mAccessPwText.getText().toString();
                    setSelect = mSelectionSpin.getSelectedItemPosition() == 1 ? true : false;

                    if (data1 == null || data2 == null || accessPW == null
                            || data1.equals("") || data2.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "### RF_LOCK: data1 = " + data1 + " ,data2 = " + data2 + " ,accessPW = " + accessPW + " ,selection = " + setSelect);
                    result = mReader.RF_LOCK(data1, data2, accessPW, setSelect); // epc lock(0020)
                    //change UI input/output+

                    //1. EPC lock by password
                    //result = mReader.RF_LOCK("0030", "0020", "00000000", false); // epc lock(0020) //origin
                    //result = mReader.RF_NXP_DNA_Authenticate_TAM2(1,1,1);

                    //2. EPC unlock by password
                    //result = mReader.RF_LOCK("0030", "0000", "00000000", false); // epc unlock(0000)
                    //3. EPC permallock
                    //result = mReader.RF_LOCK("0030", "0030", "00000000", false); // epc permal lock(0030)

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_LOCK");
                    else
                        message.append("RF_LOCK failed " + result);
                    break;

                case 6:
                    //WARNING !! This command kill your tag.
                    //Temporary blocked. If you want to use this api, remove the comment block
                    //+change UI input/output
                    data1 = mDataText1.getText().toString(); //kill pw
                    accessPW = mAccessPwText.getText().toString();
                    setSelect = mSelectionSpin.getSelectedItemPosition() == 1 ? true : false;

                    if (data1 == null || accessPW == null
                            || data1.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "### RF_KILL: data1 = " + data1 + " ,accessPW = " + accessPW + " ,selection = " + setSelect);
                    result = mReader.RF_KILL(data1, accessPW, setSelect);
                    //change UI input/output+

                    //result = mReader.RF_KILL("12345678", "00000000", false);//origin
                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_KILL");
                    else
                        message.append("RF_KILL is blocked");
                    break;

                case 7:
                    //write block
                    //+change UI input/output
                    memTye = mMemtypeSpin.getSelectedItemPosition();
                    data1 = mDataText1.getText().toString();
                    data2 = mDataText2.getText().toString(); //block data to write
                    accessPW = mAccessPwText.getText().toString();

                   
                    if (data1 == null || data2 == null || accessPW == null
                            || data1.equals("") || data2.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    offset = Integer.parseInt(data1); //offset
                    Log.d(TAG, "### RF_BlockWrite: memTye = " + memTye + " ,offset = " + offset + " ,data = " + data2 + " ,accessPW = " + accessPW);
                    result = mReader.RF_BlockWrite(memTye, offset, data2, accessPW);
                    //+change UI input/outputl

//                    result = mReader.RF_BlockWrite(SDConsts.RFMemType.USER, 7, "1234", "00000000");//origin

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_BlockWrite");
                    else
                        message.append("RF_BlockWrite failed " + result);
                    break;

                case 8:
                    //permal block lock
                    //+change UI input/output
                    data1 = mDataText1.getText().toString();
                    accessPW = mAccessPwText.getText().toString();
                    memTye = mMemtypeSpin.getSelectedItemPosition();

                    if(memTye != SDConsts.RFMemType.USER){
                        Toast.makeText(mContext, "you select only USER type", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (data1 == null || accessPW == null || data1.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int action = Integer.parseInt(data1, 16);
                    Log.d(TAG, "### RF_BlockPermalock" + " ,action = " + action + " ,accessPW = " + accessPW);
                    result = mReader.RF_BlockPermalock(0, 1, action, accessPW);//TEST
                    //+change UI input/output

                    //origin
                    //first block 0x8000, second 0x4000, 3rd 0x2000, 4th 0x1000
                    //result = mReader.RF_BlockPermalock(0, 1, 0x8000, "87654321");
                    //result = mReader.RF_BlockPermalock(0, 1, 0x8000, "00000000");
                    //result = mReader.RF_BlockPermalock(0, 1, 0x4100, "00000000");//?
                    //result = mReader.RF_BlockPermalock(0, 1, 0x0100, "00000000");//?

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_BlockPermalock");
                    else
                        message.append("RF_BlockPermalock failed " + result);
                    break;

                case 9:
                    //block erase
                    //+change UI input/output
                    memTye = mMemtypeSpin.getSelectedItemPosition();
                    data1 = mDataText1.getText().toString();
                    data2 = mDataText2.getText().toString();
                    accessPW = mAccessPwText.getText().toString();

                    
                    if (data1 == null || data2 == null || accessPW == null
                            || data1.equals("") || data2.equals("") || accessPW.equals("")) {
                        Toast.makeText(mContext, "input param", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    offset = Integer.parseInt(data1); //offset
                    cnt = Integer.parseInt(data2); //count
                    Log.d(TAG, "### RF_BlockErase: memTye = " + memTye + " ,offset = " + offset + " ,cnt = " + cnt + " ,accessPW = " + accessPW);
                    result = mReader.RF_BlockErase(memTye, offset, cnt, accessPW);
                    //+change UI input/output

                    //origin
                    //result = mReader.RF_BlockErase(SDConsts.RFMemType.USER, 0, 2, "00000000");
                    //result = mReader.RF_BlockErase(SDConsts.RFMemType.USER, 7, 1, "00000000");

                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_BlockErase");
                    else
                        message.append("RF_BlockErase failed " + result);
                    break;

                case 10:
                    sResult = mReader.RF_GetLibVersion();
                    message.append("RF_GetLibVersion = " + sResult);
                    break;

                case 11:
                    result = mReader.RF_ResetConfigToFactoryDefaults();
                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_ResetConfigToFactoryDefaults");
                    else
                        message.append("RF_ResetConfigToFactoryDefaults failed " + result);
                    break;

                case 12:
                    result = mReader.RF_ModuleReboot();
                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_ModuleReboot");
                    else
                        message.append("RF_ModuleReboot failed " + result);
                    break;

                case 13:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mStoragePerms[0]);
                        if (b) {
                            result = 0;
                            createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_rfid_str), true);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                requestFileAccessPermission();
                            else
                                PermissionHelper.requestPermission(getActivity(), PermissionHelper.mStoragePerms);
                        }
                    }else{
                        createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_sled_str), false);
                    }
                    if (result == SDConsts.RFResult.SUCCESS)
                        message.append("RF_UpdateRFIDFirmware");
                    break;

                case 14:
                    sResult = mReader.RF_GetRFIDVersion();
                    message.append("RF_GetRFIDVersion = " + sResult);
                    break;
            }

            if (sResult == null)
                Log.d(TAG, "Result = " + result);
            else
                Log.d(TAG, "Result = " + sResult);
            if (message.length() > 0)
                Toast.makeText(mContext, message.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        if(!((MainActivity) getActivity()).mSledUpdate)
            mReader = Reader.getReader(mContext, mAccessHandler);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
                                createAlertDialog(getString(R.string.warning_str), getString(R.string.update_warning_rfid_str), true);
                        }
                    }
                    break;
            }
        }
    }

    private static class AccessHandler extends Handler {
        private final WeakReference<RFAccessFragment> mExecutor;

        public AccessHandler(RFAccessFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            RFAccessFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mAccessHandler m arg1 = " + m.arg1 + " arg2 = " + m.arg2);
        int result = m.arg2;
        String data = "";
        if (m.obj != null  && m.obj instanceof String)
            data = (String)m.obj;
        String messageStr = null;
        switch (m.what) {
            case SDConsts.Msg.RFMsg:
                switch (m.arg1) {
                    //RF_Read callback message
                    case SDConsts.RFCmdMsg.READ:
                        //+change UI input/output
                        mResultText.setText(data);
                        //change UI input/output+
                        messageStr = "READ result = " + result + "\n" + data;
                        Log.d(TAG, "READ result = " + data);
                        break;
                    case SDConsts.RFCmdMsg.WRITE:
                        messageStr = "WRITE result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.WRITE_TAG_ID:
                        messageStr = "WRITE_TAG_ID result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.WRITE_ACCESS_PASSWORD:
                        messageStr = "WRITE_ACCESS_PASSWORD result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.WRITE_KILL_PASSWORD:
                        messageStr = "WRITE_KILL_PASSWORD result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.LOCK:
                        messageStr = "LOCK result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.KILL:
                        messageStr = "KILL result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.BLOCK_WRITE:
                        messageStr = "BLOCK_WRITE result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.BLOCK_PERMALOCK:
                        messageStr = "BLOCK_PERMALOCK result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.BLOCK_ERASE:
                        messageStr = "BLOCK_ERASE result = " + result + " " + data;
                        break;
                    case SDConsts.RFCmdMsg.UPDATE_RF_FW_START:
                        messageStr = "UPDATE_RF_FW_START " + result + " " + data;
                        if (result == SDConsts.RFResult.SUCCESS) {
                            Activity activity = getActivity();
                            if (activity != null)
                                createDialog(PROGRESS_DIALOG, activity.getString(R.string.update_rf_firm_str));
                        }
                        break;
                    case SDConsts.RFCmdMsg.UPDATE_RF_FW:
                        setProgressState(result);
                        break;
                    case SDConsts.RFCmdMsg.UPDATE_RF_FW_END:
                        //<-[20250625]Delay 3s for FW complete
                        try{
                          Thread.sleep(3000);
                        }catch (Exception e){
                        }
                        //[20250625]Delay 3s for FW complete->
                        closeDialog();
                        ((MainActivity) getActivity()).mSledUpdate = false;
                        messageStr = "UPDATE_RF_FW_END " + result + " " + data;
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
        if (messageStr != null)
            Toast.makeText(mContext, messageStr, Toast.LENGTH_SHORT).show();
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

    private void selectFileReadAccess(){
        ((MainActivity) getActivity()).mSledUpdate = true;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, READ_DIRECTORY_REQUEST_CODE);
    }

    private void selectFile() {
        File path = new File(Environment.getExternalStorageDirectory() + FILE_DIR_PRE);
        mFileSelecter = new FileSelectorDialog(mContext, path, FILE_EXT);
        mFileSelecter.addFileListener(new FileSelectorDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                boolean b;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) b = Environment.isExternalStorageManager();
                else b = PermissionHelper.checkPermission(mContext, PermissionHelper.mStoragePerms[0]);
                if (b) {
                    int ret = mReader.RF_UpdateRFIDFirmware(file.toString());
                    Toast.makeText(mContext, getString(R.string.update_rf_firm_str) + "result = " +
                            ret, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, getString(R.string.update_firm_str) +
                            " failed because of permission", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mFileSelecter.showDialog();
    }

    private void requestFileAccessPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", mContext.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, PermissionHelper.REQ_FILE_ACCESS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == PermissionHelper.REQ_FILE_ACCESS) {
            if(Environment.isExternalStorageManager()) {
                selectFile();
            }
            else {
                Toast.makeText(mContext, getString(R.string.permission_not_allowed), Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == READ_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null && resultData.getData() != null) {
                if (D) Log.d(TAG, "RF_UpdateRFIDFirmware  app start");
                @SuppressLint("MissingPermission") int ret = mReader.RF_UpdateRFIDFirmware(resultData.getData());

                Toast.makeText(mContext, getString(R.string.update_rf_firm_str) + "result = " +
                        ret, Toast.LENGTH_SHORT).show();
            } else {
                if (D) Log.d(TAG, "File uri not found {}");
                ((MainActivity) getActivity()).mSledUpdate = false;
            }
        }
    }

}