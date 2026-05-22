/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.legacy.app.ActionBarDrawerToggle;

import java.util.ArrayList;
import java.lang.ref.WeakReference;
import co.kr.bluebird.rfid.app.bbrfiddemo.fragment.*;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.BatteryPollingHandler;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final boolean D = Constants.MAIN_D;

    public static final int MSG_OPTION_CONNECT_STATE_CHANGED = 0;

    public static final int MSG_OPTION_DISCONNECTED = 0;

    public static final int MSG_OPTION_CONNECTED = 1;

    public static final int MSG_BACK_PRESSED = 2;

    public static final int MSG_BATT_NOTI = 3; //Always be display Battery

    private String[] mFunctionsString;
    private ArrayAdapter<String> mDrawerAdapter;
    private boolean mAdminMenuVisible;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private Reader mReader;

    private Context mContext;

    private FragmentManager mFragmentManager;

    private boolean mIsConnected;

    public boolean mSledUpdate = false;

    private ConnectivityFragment mConnectivityFragment;
    private SDFragment mSDFragment;
    private RFAccessFragment mRFAccessFragment;
    private RFConfigFragment mRFConfigFragment;
    private RFSelectionFragment mRFSelectionFragment;
    private ScanFragment mScanFragment;
    private RapidFragment mRapidFragment;
    private InventoryFragment mInventoryFragment;
    private BarcodeFragment mBarcodeFragment;
    private SBBarcodeFragment mSBBarcodeFragment;
    private InfoFragment mInfoFragment;
    private BatteryFragment mBatteryFragment;
    private TestFragment mTestFragment;

    private LinearLayout mUILayout;

    private Fragment mCurrentFragment;

    private ImageButton mConnectButton;
    private ImageButton mSDFunctionButton;
    private ImageButton mRFConfigButton;
    private ImageButton mRFAccessButton;
    private ImageButton mRFSelectButton;
    private ImageButton mRapidButton;
    private ImageButton mInventoryButton;
    private ImageButton mBarcodeButton;
    private ImageButton mSBBarcodeButton;
    private ImageButton mBatteryButton;
    private ImageButton mInformationButton;

    Menu mMenu; //Always be display Battery

    private String modelIDStr;

    private final MainHandler mMainHandler = new MainHandler(this);

    public final UpdateConnectHandler mUpdateConnectHandler = new UpdateConnectHandler(this);

    //+for internal rfid battery status tracking (<-Defect: affects the inventory performance)
//    private HandlerThread mWorkerThread;
//    private BatteryPollingHandler mBatteryPollingHandler;
    //for internal rfid battery status tracking+

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D) Log.d(TAG, " onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mUILayout = (LinearLayout) findViewById(R.id.ui_layout);

        mCurrentFragment = null;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int buttonHeight = size.x / 3;

        mConnectButton = (ImageButton) findViewById(R.id.connect_bt);
        mConnectButton.setMinimumHeight(buttonHeight);

        mSDFunctionButton = (ImageButton) findViewById(R.id.sdfunc_bt);
        mSDFunctionButton.setMinimumHeight(buttonHeight);

        mRFConfigButton = (ImageButton) findViewById(R.id.rfconf_bt);
        mRFConfigButton.setMinimumHeight(buttonHeight);

        mRFAccessButton = (ImageButton) findViewById(R.id.rfacc_bt);
        mRFAccessButton.setMinimumHeight(buttonHeight);

        mRFSelectButton = (ImageButton) findViewById(R.id.rfsel_bt);
        mRFSelectButton.setMinimumHeight(buttonHeight);

        mRapidButton = (ImageButton) findViewById(R.id.rapid_bt);
        mRapidButton.setMinimumHeight(buttonHeight);

        mInventoryButton = (ImageButton) findViewById(R.id.inv_bt);
        mInventoryButton.setMinimumHeight(buttonHeight);

        //+add bc
        mBarcodeButton = (ImageButton) findViewById(R.id.bar_bt);
        mBarcodeButton.setMinimumHeight(buttonHeight);
        //add bc+

        mSBBarcodeButton = (ImageButton) findViewById(R.id.bar_sb_bt);
        mSBBarcodeButton.setMinimumHeight(buttonHeight);

        mBatteryButton = (ImageButton) findViewById(R.id.bat_bt);
        mBatteryButton.setMinimumHeight(buttonHeight);

        mInformationButton = (ImageButton) findViewById(R.id.info_bt);
        mInformationButton.setMinimumHeight(buttonHeight);

        ImageButton testButton = (ImageButton) findViewById(R.id.test_bt);
        testButton.setMinimumHeight(buttonHeight);

        mConnectButton.setOnClickListener(buttonListener);
        mSDFunctionButton.setOnClickListener(buttonListener);
        mRFConfigButton.setOnClickListener(buttonListener);
        mRFAccessButton.setOnClickListener(buttonListener);
        mRFSelectButton.setOnClickListener(buttonListener);
        mRapidButton.setOnClickListener(buttonListener);
        mInventoryButton.setOnClickListener(buttonListener);
        mBarcodeButton.setOnClickListener(buttonListener);
        mSBBarcodeButton.setOnClickListener(buttonListener);
        mBatteryButton.setOnClickListener(buttonListener);
        mInformationButton.setOnClickListener(buttonListener);
        mInformationButton.setOnLongClickListener(adminMenuLongClickListener);
        testButton.setOnClickListener(buttonListener);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.drawable.ic_launcher, R.string.drawer_open, R.string.drawer_close) {
            String mDrawerTitle = "Functions";

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        mDrawerList.setAdapter(mDrawerAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        updateDrawerMenu(false);

        mFragmentManager = getFragmentManager();

        mIsConnected = false;
    }

    public View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = 0;
            int resId = v.getId();
            if(resId == R.id.test_bt){
                return;
            }else if(resId == R.id.connect_bt){
                id = 0;
            }else if(resId == R.id.sdfunc_bt){
                id = 6;
            }else if(resId == R.id.rfconf_bt){
                id = 4;
            }else if(resId == R.id.rfacc_bt){
                return;
            }else if(resId == R.id.rfsel_bt){
                id = 5;
            }else if(resId == R.id.rapid_bt){
                id = 1;
            }else if(resId == R.id.inv_bt){
                id = 7;
            }else if(resId == R.id.bar_bt){
                return;
            }else if(resId == R.id.bar_sb_bt){
                return;
            }else if(resId == R.id.bat_bt){
                id = 2;
            }else if(resId == R.id.info_bt){
                id = 3;
            }
            selectItem(id);
        }
    };

    private final View.OnLongClickListener adminMenuLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            updateDrawerMenu(!mAdminMenuVisible);
            Toast.makeText(mContext, mAdminMenuVisible ? "Admin menu enabled" : "Admin menu hidden", Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private void updateDrawerMenu(boolean showAdminMenu) {
        mAdminMenuVisible = showAdminMenu;
        int arrayId = mAdminMenuVisible ? R.array.functions_array_admin : R.array.functions_array;
        mFunctionsString = getResources().getStringArray(arrayId);
        mDrawerAdapter.clear();
        for (String functionName : mFunctionsString) {
            mDrawerAdapter.add(functionName);
        }
        mDrawerAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //+Always be display Battery
        mMenu = menu;
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            int value = mReader.SD_GetBatteryStatus();
            menu.getItem(2).setVisible(true);
            menu.getItem(2).setTitle(Integer.toString(value) + "%");
        }
        //Always be display Battery+

        if (mIsConnected)
            menu.getItem(0).setVisible(true);
        else
            menu.getItem(0).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connected) {
            Toast.makeText(this, modelIDStr + " " + getString(R.string.sled_connected_str), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_home) {
            switchToHome();
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        switch (position) {
            case 0:
                if (mConnectivityFragment == null)
                    mConnectivityFragment = ConnectivityFragment.newInstance();
                mCurrentFragment = mConnectivityFragment;
                break;
            case 1:
                if (mScanFragment == null)
                    mScanFragment = ScanFragment.newInstance();
                mCurrentFragment = mScanFragment;
                break;
            case 2:
                if (mBatteryFragment == null)
                    mBatteryFragment = BatteryFragment.newInstance();
                mCurrentFragment = mBatteryFragment;
                break;
            case 3:
                if (mInfoFragment == null)
                    mInfoFragment = InfoFragment.newInstance();
                mCurrentFragment = mInfoFragment;
                break;
            case 4:
                if (mRFConfigFragment == null)
                    mRFConfigFragment = RFConfigFragment.newInstance();
                mCurrentFragment = mRFConfigFragment;
                break;
            case 5:
                if (mRFSelectionFragment == null)
                    mRFSelectionFragment = RFSelectionFragment.newInstance();
                mCurrentFragment = mRFSelectionFragment;
                break;
            case 6:
                if (mSDFragment == null)
                    mSDFragment = SDFragment.newInstance();
                mCurrentFragment = mSDFragment;
                break;
            case 7:
                if (mInventoryFragment == null)
                    mInventoryFragment = InventoryFragment.newInstance();
                mCurrentFragment = mInventoryFragment;
                break;
            default:
                return;
        }
        if (checkUnavailableFragment()) return;

        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.content, mCurrentFragment);
        ft.commit();

        mDrawerList.setItemChecked(position, true);
        if (mFunctionsString != null && position >= 0 && position < mFunctionsString.length)
            setTitle(mFunctionsString[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        mUILayout.setVisibility(View.GONE);
    }

    //+For Internal RFID
    private boolean checkUnavailableFragment() {
        if (mCurrentFragment == mSBBarcodeFragment && mReader.SD_GetType() == SDConsts.SLED_TYPE.INTERNAL_SLED) {
            Toast.makeText(mContext, "Internal RFID doesn't support (SB)", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    //For Internal RFID+

    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, " onStart");
        if(!mSledUpdate) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            boolean openResult = false;
            boolean isConnected = false;
            mReader = Reader.getReader(mContext, mMainHandler);
            if (mReader != null)
                openResult = mReader.SD_Open();
            if (openResult == SDConsts.SD_OPEN_SUCCESS) {
                Log.i(TAG, "Reader opened");
                modelIDStr = (mReader.SD_GetModel() == SDConsts.MODEL.RFR900) ? getString(R.string.rfr900) : getString(R.string.rfr901);
                if (mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED)
                    isConnected = true;
            } else if (openResult == SDConsts.RF_OPEN_FAIL)
                if (D) Log.e(TAG, "Reader open failed");

            updateConnectState(isConnected);
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, " onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, " onPause");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {}

    @Override
    protected void onStop() {
        if (D) Log.d(TAG, " onStop");
        if(!mSledUpdate) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mReader = Reader.getReader(mContext, mMainHandler);
            if (mReader != null)
                if (mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
                    mReader.SD_Disconnect();
                }
            mReader.SD_Close();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mCurrentFragment != null)
                mCurrentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentFragment != null)
            switchToHome();
        else
            super.onBackPressed();
    }

    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mExecutor;

        public MainHandler(MainActivity ac) {
            mExecutor = new WeakReference<>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity executor = mExecutor.get();
            if (executor != null) {
                executor.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message m) {
        if (D) Log.d(TAG, "handleMessage");
        if (D) Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");

        switch (m.what) {
            case SDConsts.Msg.SDMsg:
                //+Always be display Battery
                try {
                    if (m.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                        //+smart batter -critical temper
                        if (m.arg2 == SDConsts.SDCommonResult.SMARTBATT_CRITICAL_TEMPERATURE)
                            Utils.createAlertDialog(mContext, getString(R.string.smart_critical_temper_str));
                        //smart batter -critical temper+

                        Log.d(TAG, "command = " + m.arg1 + " result = " + m.arg2 + " obj = data");
                        mMenu.getItem(2).setTitle(m.arg2 + "%");
                    }
                    //Always be display Battery+

                    //+Hotswap feature
                    else if (m.arg1 == SDConsts.SDCmdMsg.SLED_HOTSWAP_STATE_CHANGED) {
                        if (m.arg2 == SDConsts.SDHotswapState.HOTSWAP_STATE)
                            Toast.makeText(mContext, "HOTSWAP STATE CHANGED = HOTSWAP_STATE", Toast.LENGTH_SHORT).show();
                        else if (m.arg2 == SDConsts.SDHotswapState.NORMAL_STATE)
                            Toast.makeText(mContext, "HOTSWAP STATE CHANGED = NORMAL_STATE", Toast.LENGTH_SHORT).show();
                    }
                    //Hotswap feature+
                } catch (NullPointerException ignored) {}
                break;
            case SDConsts.Msg.RFMsg:
                break;
            case SDConsts.Msg.BCMsg:
                break;
        }
    }

    private void switchToHome() {
        if (D) Log.d(TAG, "switchToHome");
        try {
            mDrawerLayout.closeDrawer(mDrawerList);
            if (mCurrentFragment != null) {
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.remove(mCurrentFragment);
                ft.commit();
                mCurrentFragment = null;
                mReader = Reader.getReader(mContext, mMainHandler);
            }
            setTitle(getString(R.string.app_name));
            if (mUILayout.getVisibility() != View.VISIBLE) {
                mUILayout.setVisibility(View.VISIBLE);
            }
        } catch (java.lang.IllegalStateException ignored) {}
    }

    private void updateConnectState(boolean b) {
        mIsConnected = b;
        invalidateOptionsMenu();

        //+check connect type
        if (b) {
            int ret = mReader.SD_GetType();
            if (ret == SDConsts.SLED_TYPE.INTERNAL_SLED) {
                modelIDStr = getString(R.string.internal_rfid);

                //+for internal rfid battery status tracking (<-Defect: affects the inventory performance)
//                if (mWorkerThread != null && mWorkerThread.isAlive()) return;
//
//                Log.d(TAG, "BatteryPollingThread start");
//                mWorkerThread = new HandlerThread("BatteryPollingThread");
//                mWorkerThread.start();
//                mBatteryPollingHandler = new BatteryPollingHandler(mWorkerThread.getLooper(), mUpdateConnectHandler, mReader);
//                mBatteryPollingHandler.start();
                //for internal rfid battery status tracking+
            } else if (ret == SDConsts.SLED_TYPE.RFR900_EXTERNAL_SLED) {
                modelIDStr = getString(R.string.rfr900);
            } else if (ret == SDConsts.SLED_TYPE.RFR901_EXTERNAL_SLED) {
                if (mReader.SD_GetSerialNumber().contains("RFR971")) {
                    modelIDStr = getString(R.string.rfr971);
                } else {
                    modelIDStr = getString(R.string.rfr901);
                }
            }
        } else {
            //+for internal rfid battery status tracking (<-Defect: affects the inventory performance)
//            Log.d(TAG, "BatteryPollingThread stop");
//            if (mBatteryPollingHandler != null) {
//                mBatteryPollingHandler.stop();
//            }
//            if (mWorkerThread != null) {
//                mWorkerThread.quitSafely();
//                mWorkerThread = null;
//            }
//            mBatteryPollingHandler = null;
            //for internal rfid battery status tracking+
        }
        //check connect type+
    }

    private static class UpdateConnectHandler extends Handler {
        private final WeakReference<MainActivity> mExecutor;

        public UpdateConnectHandler(MainActivity ac) {
            mExecutor = new WeakReference<>(ac);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity executor = mExecutor.get();
            if (executor != null) {
                executor.handleUpdateConnectHandler(msg);
            }
        }
    }

    public void handleUpdateConnectHandler(Message m) {
        if (m.what == MSG_OPTION_DISCONNECTED) {
            Log.d(TAG, "MSG_OPTION_DISCONNECTED");
            updateConnectState(false);
        } else if (m.what == MSG_OPTION_CONNECTED) {
            Log.d(TAG, "MSG_OPTION_CONNECTED");
            updateConnectState(true);
        } else if (m.what == MSG_BACK_PRESSED)
            switchToHome();
            //+Always be display Battery
        else if (m.what == MSG_BATT_NOTI) {
            Log.d(TAG, "MSG_BATT_NOTI : " + m.arg2 + "%");
            mMenu.getItem(2).setTitle(m.arg2 + "%");
        }
        //Always be display Battery+
    }
}
