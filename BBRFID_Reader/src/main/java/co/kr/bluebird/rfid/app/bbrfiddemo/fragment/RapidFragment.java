/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.stopwatch.StopwatchService;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class RapidFragment extends Fragment {

    private static final String TAG = RapidFragment.class.getSimpleName();

    private static final boolean D = Constants.INV_D;

    private StopwatchService mStopwatchSvc;

    private TextView mBatteryText;

    private TextView mTimerText;

    private TextView mCountText;

    private TextView mAvrSpeedCountTest;

    private Button mClearButton;

    private Button mInvenButton;

    private Button mStopInvenButton;

    private ProgressBar mProgressBar;

    private Reader mReader;

    private Context mContext;

    private boolean mInventory = false;

    private Handler mOptionHandler;

    private SoundPool mSoundPool;

    private int mSoundId;

    private float mSoundVolume;

    private boolean mSoundFileLoadState;

    private ProcessReadTask mProcessReadTask;

    private double mOldTotalCount = 0;

    private double mOldSec = 0;

    private double mReadCount = 0;

    private int mLastLength = 0;
    
    private int mTickCount = 0;
    
    private UpdateStopwatchHandler mUpdateStopwatchHandler = new UpdateStopwatchHandler(this);
    
    private InventoryHandler mInventoryHandler = new InventoryHandler(this);
    
    public static RapidFragment newInstance() {
        return new RapidFragment();
    }

    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.rapid_frag, container, false);

        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity)getActivity()).mUpdateConnectHandler;
        mTimerText = (TextView)v.findViewById(R.id.timer_text);

        mCountText = (TextView)v.findViewById(R.id.count_text);

        mAvrSpeedCountTest = (TextView)v.findViewById(R.id.speed_avr_count_text);

        Activity activity = getActivity();

        if (activity != null) {
            String speedCountStr = activity.getString(R.string.speed_count_str) + activity.getString(R.string.speed_postfix_str);
            mAvrSpeedCountTest.setText(speedCountStr);
            String timeStr = activity.getString(R.string.timer_str) + activity.getString(R.string.time_postfix_str);
            mTimerText.setText(timeStr);
        }

        mBatteryText = (TextView)v.findViewById(R.id.battery_text);

        mClearButton = (Button)v.findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(clearButtonListener);

        mInvenButton = (Button)v.findViewById(R.id.inven_button);
        mInvenButton.setOnClickListener(sledListener);

        mStopInvenButton = (Button)v.findViewById(R.id.stop_inven_button);
        mStopInvenButton.setOnClickListener(sledListener);

        mProgressBar = (ProgressBar)v.findViewById(R.id.timer_progress);
        mProgressBar.setVisibility(View.INVISIBLE);
        bindStopwatchSvc();
        return v;
    }

    private void createSoundPool() {
        boolean b = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            b = createNewSoundPool();
        else
            b = createOldSoundPool();
        if (b) {
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mSoundVolume = actVolume / maxVolume;
            SoundLoadListener();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(5).build();
        if (mSoundPool != null)
            return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean createOldSoundPool(){
        mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        if (mSoundPool != null)
            return true;
        return false;
    }

    private void SoundLoadListener() {
        if (mSoundPool != null) {
            mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    // TODO Auto-generated method stub
                    mSoundFileLoadState = true;
                }
            });
            mSoundId = mSoundPool.load(mContext, R.raw.beep, 1);
        }
    }

    private class ProcessReadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            updateCountText();
            super.onPreExecute();
        }
    
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                if (mSoundFileLoadState) {
                    if (mSoundPool != null) {
                        mSoundPool.play(mSoundId, mSoundVolume, mSoundVolume, 0, 0, (48000.0f / 44100.0f));
                        try {
                            Thread.sleep(25);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch (NullPointerException e) {
            }
            return null;
        }
    
        @Override
        protected void onPostExecute(Void aVoid) {
            if (!mInventory) {
                updateCountText();
                updateAvrSpeedCountText();
            }
            super.onPostExecute(aVoid);
        }
    };

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mSoundFileLoadState = false;
        createSoundPool();
        mOldTotalCount = 0;

        mOldSec = 0;

        mReadCount = 0;
        mReader = Reader.getReader(mContext, mInventoryHandler);
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            mReader.RF_SetToggle(SDConsts.RFToggle.ON);
            //mReader.RF_SetRFMode(SDConsts.RFMode.DSB_ASK_2);
            updateBatteryState();
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
        mReader.RF_StopInventory();
        pauseStopwatch();
        mInventory = false;
        if (mSoundPool != null)
            mSoundPool.release();
        mSoundFileLoadState = false;
        stopStopwatch();

        unbindStopwatchSvc();
        super.onStop();
    }

    private OnClickListener clearButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "clearButtonListener");
            clearAll();
        }
    };

    private void clearAll() {
        if (!mInventory) {
            updateCountText();

            stopStopwatch();

            mOldTotalCount = 0;

            mOldSec = 0;

            mReadCount = 0;

            updateAvrSpeedCountText();
        }
    }


    private OnClickListener sledListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "stopwatchListener");

            int id = v.getId();
            int ret;            
            if (id == R.id.inven_button) {
                clearAll();
                ret = mReader.RF_PerformInventory(true, false, false);
                if (ret == SDConsts.RFResult.SUCCESS) {
                    startStopwatch();
                    mInventory = true;
                }
                else if (ret == SDConsts.RFResult.MODE_ERROR)
                    Toast.makeText(mContext, "Start Inventory failed, Please check RFR901 MODE", Toast.LENGTH_SHORT).show();
                else if (ret == SDConsts.RFResult.LOW_BATTERY)
                    Toast.makeText(mContext, "Start Inventory failed, LOW_BATTERY", Toast.LENGTH_SHORT).show();
                else
                if (D) Log.d(TAG, "Start Inventory failed");
            }else if(id == R.id.stop_inven_button){
                ret = mReader.RF_StopInventory();
                if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
                    mInventory = false;
                    pauseStopwatch();
                } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
                    Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void startStopwatch() {
        if (D) Log.d(TAG, "startStopwatch");
        
        if (mStopwatchSvc != null && !mStopwatchSvc.isRunning())
            mStopwatchSvc.start();
        
        mProgressBar.setVisibility(View.VISIBLE);
    }
    
    private void pauseStopwatch() {
        if (D) Log.d(TAG, "pauseStopwatch");
        
        if (mStopwatchSvc != null && mStopwatchSvc.isRunning())
            mStopwatchSvc.pause();
        
        updateCountText();
        
        updateTimerText();
        
        updateAvrSpeedCountText();
        
        mProgressBar.setVisibility(View.INVISIBLE);
    }
    
    private void stopStopwatch() {
        if (D) Log.d(TAG, "stopStopwatch");
        
        if (mStopwatchSvc != null && mStopwatchSvc.isRunning())
            mStopwatchSvc.pause();
        
        if (mStopwatchSvc != null)
            mStopwatchSvc.reset();
        
        updateTimerText();
        
        updateAvrSpeedCountText();
        
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void updateCountText() {
        if (D) Log.d(TAG, "updateCountText");
        String text = String.format("%.0f", mReadCount);
        int textLength = text.length();
        if (textLength == 1) {
            mLastLength = 1;
            mCountText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150 - ((textLength - 1) * 12));
        }
        else {
            if (textLength != mLastLength) {
                mCountText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 150 - ((textLength - 1) * 12));
                mLastLength = textLength;
            }
        }
        mCountText.setText(text);
    }
    
    private void updateTimerText() {
        if (D) Log.d(TAG, "updateTimerText");
        if (mStopwatchSvc != null) {
            Activity activity = getActivity();
            if (activity != null)
                mTimerText.setText(mStopwatchSvc.getFormattedElapsedTime() +
                        activity.getString(R.string.time_postfix_str));
        }
    }
    
    private void updateAvrSpeedCountText() {
        if (D) Log.d(TAG, "updateTimerText");
        String speedStr = "";
        double value = 0;
        double totalCount = 0;
        double sec = 0;
        if (mStopwatchSvc != null) {
            sec = ((double)((int)(mStopwatchSvc.getElapsedTime() / 100))) / 10;
            totalCount = mReadCount;
            if (totalCount > 0 && sec >= 1)
                value = (double)((int)(((double)totalCount / sec) * 10)) / 10;
            
            Activity activity = getActivity();
            if (activity != null)
                speedStr = Double.toString(value) + activity.getString(R.string.speed_postfix_str);
            mAvrSpeedCountTest.setText(speedStr);
        }
    }

    private ServiceConnection mStopwatchSvcConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "onServiceConnected");
            
            mStopwatchSvc = ((StopwatchService.LocalBinder)arg1).getService(mUpdateStopwatchHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "onServiceDisconnected");
            
            mStopwatchSvc = null;
        }
    };
    
    private void bindStopwatchSvc() {
        if (D) Log.d(TAG, "bindStopwatchSvc");
        mContext.bindService(new Intent(mContext, StopwatchService.class), mStopwatchSvcConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void unbindStopwatchSvc() {
        if (D) Log.d(TAG, "unbindStopwatchSvc");
        try {
            if (mStopwatchSvc != null)
                mContext.unbindService(mStopwatchSvcConnection);
        }
        catch (java.lang.IllegalArgumentException iae) {
            return;
        }
    }
    
    private static class UpdateStopwatchHandler extends Handler {
        private final WeakReference<RapidFragment> mExecutor;
        public UpdateStopwatchHandler(RapidFragment f) {
            mExecutor = new WeakReference<>(f);
        }
        
        @Override
        public void handleMessage(Message msg) {
            RapidFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleUpdateStopwatchHandler(msg);
            }
        }
    }
    
    public void handleUpdateStopwatchHandler(Message m) {
        if (D) Log.d(TAG, "mUpdateStopwatchHandler");
        if (m.what == StopwatchService.TICK_WHAT) {
            if (D) Log.d(TAG, "received stopwatch message");

            mTickCount++;
            
            updateCountText();
            
            if (mTickCount == 10) {
                updateAvrSpeedCountText();
                mTickCount = 0;
            }
            updateTimerText();
            
            mStopwatchSvc.update();
        }
    }
    
    private static class InventoryHandler extends Handler {
        private final WeakReference<RapidFragment> mExecutor;
        public InventoryHandler(RapidFragment f) {
            mExecutor = new WeakReference<>(f);
        }
        
        @Override
        public void handleMessage(Message msg) {
            RapidFragment executor = mExecutor.get();
            if (executor != null) {
                executor.handleInventoryHandler(msg);
            }
        }
    }
    
    public void handleInventoryHandler(Message m) {
        if (D) Log.d(TAG, "mInventoryHandler");

        if (D) Log.d(TAG, "m arg1 = " + m.arg1 + " arg2 = " + m.arg2);
        switch (m.what) {
        case SDConsts.Msg.SDMsg:
            switch(m.arg1) {
                //+Hotswap feature
                case SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED:
                    if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE)
                        Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                    else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE)
                        Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(mFragment).attach(mFragment).commit();
                    break;
                //Hotswap feature+

            case SDConsts.SDCmdMsg.TRIGGER_PRESSED:
                clearAll();
                //+++NTNS
                int ret = mReader.RF_PerformInventory(true, false, false);
//                    int ret = mReader.RF_PerformInventoryCustom(SDConsts.RFMemType.USER, 0, 10, "00000000", false);
                //---NTNS
                if (ret == SDConsts.RFResult.SUCCESS) {
                    startStopwatch();
                    mInventory = true;
                }
                else if (ret == SDConsts.RFResult.MODE_ERROR)
                    Toast.makeText(mContext, "Start Inventory failed, Please check RFR901 MODE", Toast.LENGTH_SHORT).show();
                else if (ret == SDConsts.RFResult.LOW_BATTERY)
                    Toast.makeText(mContext, "Start Inventory failed, LOW_BATTERY", Toast.LENGTH_SHORT).show();
                else
                    if (D) Log.d(TAG, "Start Inventory failed");
                break;
                
            case SDConsts.SDCmdMsg.SLED_INVENTORY_STATE_CHANGED:
                mInventory = false;
                pauseStopwatch();
                // In case of low battery on inventory, reason value is LOW_BATTERY
                Toast.makeText(mContext, "Inventory Stopped reason : " + m.arg2, Toast.LENGTH_SHORT).show();
                break;
                
            case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                if (mReader.RF_StopInventory() == SDConsts.SDResult.SUCCESS) {
                    mInventory = false;
                }
                pauseStopwatch();
                break;
                
            case SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED:
                //This message contain DETACHED event.
                if (mInventory) {
                    pauseStopwatch();
                    mInventory = false;
                }
                if (mOptionHandler != null)
                    mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                break;
                
            case SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED:
                //Toast.makeText(mContext, "Battery state = " + m.arg2, Toast.LENGTH_SHORT).show();
                if (D) Log.d(TAG, "Battery state = " + m.arg2);
                mBatteryText.setText("" + m.arg2 + "%");

                //+smart batter -critical temper
                if(m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE) {
                    if (mInventory) {
                        pauseStopwatch();
                        mInventory = false;
                    }
                    Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                }
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
                break;
            }
            break;
            
        case SDConsts.Msg.RFMsg:
            switch(m.arg1) {
                //+++NTNS
//                    case SDConsts.RFCmdMsg.INVENTORY_CUSTOM_READ:
//                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
//                            String data = (String)m.obj;
//                            if (data != null)
//                                processReadDataCustom(data);                            
//                        }
//                        break;
                //---NTNS
                case SDConsts.RFCmdMsg.INVENTORY:
                case SDConsts.RFCmdMsg.READ:
                    if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                        if (m.obj != null  && m.obj instanceof String) {
                            String data = (String) m.obj;
                            if (data != null) {
                                processReadData(data);
                            }
                        }
                    }
                    break;
            }
            break;
        }
    }

    private void processReadData(String data) {
        mReadCount++;
        if (mProcessReadTask == null) {
            mProcessReadTask = new ProcessReadTask();
            mProcessReadTask.execute();
        }
        else {
            if (mProcessReadTask.getStatus() == AsyncTask.Status.FINISHED) {
                mProcessReadTask.cancel(true);
                mProcessReadTask = null;
                mProcessReadTask = new ProcessReadTask();
                mProcessReadTask.execute();
            }
        }
    }

    //+++NTNS
//    private void processReadDataCustom(String data) {
//        updateCountText();
//        StringBuilder tagSb = new StringBuilder(); 
//        tagSb.setLength(0);
//        String rssi = ""; 
//        String customData = "";
//        if (data.contains(";")) {
//            if (D) Log.d(TAG, "full tag = " + data);
//            //full tag example = "3000123456783333444455556666;rssi:-54.8^custom=2348920384"
//            int customdDataPoint = data.indexOf('^');
//            customData = data.substring(customdDataPoint, data.length());
//            int customPoint = customData.indexOf('=') + 1;
//            customData = customData.substring(customPoint, customData.length());
//            if (D) Log.d(TAG, "custom data = " + customData);
//            data = data.substring(0, customdDataPoint);
//            
//            int rssiTagPoint = data.indexOf(';');
//            rssi = data.substring(rssiTagPoint, data.length());
//            int rssiPoint = rssi.indexOf(':') + 1;
//            rssi = rssi.substring(rssiPoint, rssi.length());
//            if (D) Log.d(TAG, "rssi tag = " + rssi);
//            data = data.substring(0, rssiTagPoint);
//            
//            if (D) Log.d(TAG, "data tag = " + data);
//            data = data + "\n" + customData;
//        }
//        if (rssi != "") {
//            Activity activity = getActivity();
//            if (activity != null)
//                rssi = activity.getString(R.string.rssi_str) + rssi;
//        }
//        mAdapter.addItem(-1, data, rssi, mTagFilter);
//        if (mSoundPlay) {
//            if (mSoundTask == null) {
//                mSoundTask = new SoundTask();
//                mSoundTask.execute();
//            }
//            else {
//                if (mSoundTask.getStatus() == Status.FINISHED) {
//                    mSoundTask.cancel(true);
//                    mSoundTask = null;
//                    mSoundTask = new SoundTask();
//                    mSoundTask.execute();                    
//                }
//            }
//        }
//        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
//        if (!mInventory) {
//            updateCountText();
//            updateSpeedCountText();
//            updateAvrSpeedCountText();
//        }
//    }
    //---NTNS

    private void updateBatteryState() {
        if (mReader != null) {
            int battery = mReader.SD_GetBatteryStatus();
            if (!(battery < 0 || battery > 100))
                mBatteryText.setText("" + battery + "%");
        }
    }
}