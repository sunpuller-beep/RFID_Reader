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
import co.kr.bluebird.sled.SelectionCriterias;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import java.lang.ref.WeakReference;

public class RFSelectionFragment extends Fragment {
    private static final String TAG = RFSelectionFragment.class.getSimpleName(); 
    
    private static final boolean D = Constants.SEL_D;
    
    private Context mContext;
    
    private TextView mMonitorText;
    
    private Spinner mMemtypeSpin;
    private ArrayAdapter<CharSequence> mMemtypeChar;
    
    private Spinner mActionSpin;
    private ArrayAdapter<CharSequence> mActionChar;
    
    private EditText mMaskEdit;
    
    private EditText mStartPosEdit;
    
    private EditText mMaskLengthEdit;
    
    private Button mSetCriteriaBt;
    
    private Button mRemoveCriteriaBt;

    private Button mReadBt;//SELECTION TEST
    
    private Button mSetSelectionBt;
    
    private Button mGetSelectionBt;
    
    private Button mRemoveSelectionBt;
    
    private Reader mReader;
    
    private SelectionCriterias mCurrentSelectionCriterias;
    
    private Handler mOptionHandler;
    
    private RFSelectionHandler mRFSelectionHandler = new RFSelectionHandler(this);
    
    public static RFSelectionFragment newInstance() {
        return new RFSelectionFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.rf_selection_frag, container,false);
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;

        mMonitorText = (TextView)v.findViewById(R.id.text_criteria);
  
        mMemtypeSpin = (Spinner)v.findViewById(R.id.memtype_spin);
        mMemtypeChar = ArrayAdapter.createFromResource(mContext, R.array.selection_memtype_array, 
                android.R.layout.simple_spinner_dropdown_item);
        mMemtypeSpin.setAdapter(mMemtypeChar);

        mActionSpin = (Spinner)v.findViewById(R.id.action_spin);
        mActionChar = ArrayAdapter.createFromResource(mContext, R.array.selection_action_array, 
                android.R.layout.simple_spinner_dropdown_item);
        mActionSpin.setAdapter(mActionChar);
  
        mMaskEdit = (EditText)v.findViewById(R.id.mask_edit);

        mStartPosEdit = (EditText)v.findViewById(R.id.start_pos_edit);
  
        mMaskLengthEdit = (EditText)v.findViewById(R.id.mask_length_edit);
  
        mSetCriteriaBt = (Button)v.findViewById(R.id.set_criteria);
        mSetCriteriaBt.setOnClickListener(sledListener);
  
        mRemoveCriteriaBt = (Button)v.findViewById(R.id.remove_criteria);
        mRemoveCriteriaBt.setOnClickListener(sledListener);

        //+SELECTION TEST
        mReadBt = (Button)v.findViewById(R.id.read);
        mReadBt.setOnClickListener(sledListener);
        mReadBt.setVisibility(View.VISIBLE);
        //SELECTION TEST+

        mSetSelectionBt = (Button)v.findViewById(R.id.set_selection);
        mSetSelectionBt.setOnClickListener(sledListener);
  
        mGetSelectionBt = (Button)v.findViewById(R.id.get_selection);
        mGetSelectionBt.setOnClickListener(sledListener);
  
        mRemoveSelectionBt = (Button)v.findViewById(R.id.remove_selection);
        mRemoveSelectionBt.setOnClickListener(sledListener);
        
        mCurrentSelectionCriterias = new SelectionCriterias();
        return v;
    }

    @Override
    public void onStart() {
        if (D) Log.d(TAG, " onStart");
        mReader = Reader.getReader(mContext, mRFSelectionHandler);
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
        super.onStop();
    }

    private OnClickListener sledListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "sledListener");
            String value = null;
            int memPos = 0;
            int actionPos = 0;
            int id = v.getId();
            String mask;
            int selectStartPosByte = 0;
            int selectMaskLengthBit = 0;
            //+SELECTION TEST            
            if(id == R.id.read){
                mReader = Reader.getReader(mContext, mRFSelectionHandler);
                int result = mReader.RF_READ(memPos + 1, 1, 6, "00000000", true);
            //SELECTION TEST+
            }else if(id == R.id.set_criteria){
                memPos = mMemtypeSpin.getSelectedItemPosition();
                actionPos = mActionSpin.getSelectedItemPosition();
                value = mStartPosEdit.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            selectStartPosByte = Integer.parseInt(value);
                        } catch(java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                value = mMaskLengthEdit.getText().toString();
                if (value != null) {
                    if (value != "") {
                        try {
                            selectMaskLengthBit = Integer.parseInt(value);
                        } catch(java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }
                }
                mask = mMaskEdit.getText().toString();
                int ret = mCurrentSelectionCriterias.makeCriteria(memPos + 1, mask, selectStartPosByte, selectMaskLengthBit, actionPos);
            }else if(id == R.id.remove_criteria){
                if (mCurrentSelectionCriterias != null && mCurrentSelectionCriterias.getCriteria() != null &&
                        mCurrentSelectionCriterias.getCriteria().size() > 0) {
                    mCurrentSelectionCriterias.getCriteria().remove(mCurrentSelectionCriterias.getCriteria().size() - 1);
                }
            }else if(id == R.id.set_selection){
                if (mCurrentSelectionCriterias != null && mCurrentSelectionCriterias.getCriteria() != null &&
                        mCurrentSelectionCriterias.getCriteria().size() > 0) {
                    mReader.RF_SetSelection(mCurrentSelectionCriterias);
                    mCurrentSelectionCriterias.getCriteria().clear();
                }
            }else if(id == R.id.get_selection){
                SelectionCriterias sc = mReader.RF_GetSelection();
                getSelectionCriterias(sc);
            }else if(id == R.id.remove_selection){
                mReader.RF_RemoveSelection();
            }
            updateCurrentSelectionCriterias();
        }
    }; 
    
    private void getSelectionCriterias(SelectionCriterias sc) {
        StringBuilder sb = new StringBuilder();
        if (sc != null && sc.getCriteria() != null && sc.getCriteria().size() > 0) {
            for (SelectionCriterias.Criteria c : sc.getCriteria()) {
                sb.append("memtype = " + c.getSelectMemType());
                sb.append(" mask = " + c.getSelectMask());
                sb.append(" startPosByte = " + c.getSelectStartPosByte());
                sb.append(" maskLengthBit = " + c.getSelectMaskLengthBit());
                sb.append(" action = " + c.getSelectAction());
                sb.append("\n");
            }
            String str = sb.toString();
            if (str != null) {
                createDialog(str);
            }
        }
        return;
    }
    
    private void updateCurrentSelectionCriterias() {
        StringBuilder sb = new StringBuilder();
        if (mCurrentSelectionCriterias != null && mCurrentSelectionCriterias.getCriteria() != null && mCurrentSelectionCriterias.getCriteria().size() > 0) {
            for (SelectionCriterias.Criteria c : mCurrentSelectionCriterias.getCriteria()) {
                sb.append("memtype = " + c.getSelectMemType());
                sb.append(" mask = " + c.getSelectMask());
                sb.append(" startPosByte = " + c.getSelectStartPosByte());
                sb.append(" maskLengthBit = " + c.getSelectMaskLengthBit());
                sb.append(" action = " + c.getSelectAction());
                sb.append("\n");
            }
            String str = sb.toString();
            if (str != null) {
                mMonitorText.setText(str);
                return;
            }
        }
        mMonitorText.setText("");
    }
    
    private static class RFSelectionHandler extends Handler {
        private final WeakReference<RFSelectionFragment> mExecutor;
        public RFSelectionHandler(RFSelectionFragment f) {
            mExecutor = new WeakReference<>(f);
        }
        
        @Override
        public void handleMessage(Message msg) {
            RFSelectionFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    public void handleMessage(Message m) {
        if (D) Log.d(TAG, "mRFSelectionHandler");
        if (D) Log.d(TAG, "m arg1 = " + m.arg1 + " arg2 = " + m.arg2);

        //+SELECTION TEST
        String data = "";
        if (m.obj != null  && m.obj instanceof String)
            data = (String)m.obj;
        //SELECTION TEST+

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
        //+SELECTION TEST
        case SDConsts.Msg.RFMsg:
            switch (m.arg1) {
                case SDConsts.RFCmdMsg.READ:
                    Toast.makeText(mContext, "READ: "+data, Toast.LENGTH_SHORT).show();
                    break;
            }
            break;
        }
        //SELECTION TEST+
    }
    
    private void createDialog(String message) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
        Activity activity = getActivity();
        if (activity != null)
            dlg.setTitle(activity.getString(R.string.selection_str));
        dlg.setMessage(message);
        dlg.setCancelable(false);
        
        dlg.setNeutralButton("Close", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        });
        dlg.show();
    }
}