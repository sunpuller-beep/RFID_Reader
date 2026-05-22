/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.fileutil.FileManager;
import co.kr.bluebird.rfid.app.bbrfiddemo.control.ListItem;
import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.fileutil.MediaUriWrite;
import co.kr.bluebird.rfid.app.bbrfiddemo.permission.PermissionHelper;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.control.TagListAdapter;
import co.kr.bluebird.rfid.app.bbrfiddemo.stopwatch.StopwatchService;
import co.kr.bluebird.rfid.app.bbrfiddemo.utils.Utils;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;
import co.kr.bluebird.sled.SelectionCriterias;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.bluebird.keymapper.KeyMapperManager;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

//<-[20250402]Add Bulk encoding
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.concurrent.CopyOnWriteArrayList;
import co.kr.bluebird.rfid.app.bbrfiddemo.Constants.EPCItem;
//[20250402]Add Bulk encoding->
public class InventoryFragment extends Fragment {

    private static final String TAG = InventoryFragment.class.getSimpleName();

    private static final boolean D = Constants.INV_D;

    private StopwatchService mStopwatchSvc;

    private TagListAdapter mAdapter;

    private ListView mRfidList;

    private TextView mBatteryText;

    private TextView mTimerText;

    private TextView mCountText;

    private TextView mSpeedCountText;

    private TextView mAvrSpeedCountTest;

    private Button mClearButton;

    private Button mInvenButton;

    //<-[20250424]Add other inventory api for test
    private Button mInvenWithLocButton;//RF_PerformInventoryWithLocating
    private Button mInvenWithPhaseFreqButton;//RF_PerformInventoryWithPhaseFreq
    private Button mInvenCustomButton;//RF_PerformInventoryCustom
    private Button mInvenRSSIButton;//RF_PerformInventoryWithRssiLimitation
    //[20250424]Add other inventory api for test->

    private Button mStopInvenButton;

    private Switch mTurboSwitch;

    private Switch mRssiSwitch;

    private Switch mFilterSwitch;

    private Switch mSoundSwitch;

    private Switch mMaskSwitch;

    private Switch mToggleSwitch;

    private Switch mPCSwitch;

    private Switch mFileSwitch;

    private ProgressBar mProgressBar;

    private Reader mReader;

    private Context mContext;

    private boolean mTagFilter = false;
	
//    private boolean mTagFilter = true;//[20250402]Add Bulk encoding - if bulk encoding, enable(false->true)

    private boolean mSoundPlay = true;

    private boolean mMask = false;

    private boolean mInventory = false;

    private boolean mIsTurbo = true;

    private boolean mToggle = false;

    private boolean mIgnorePC = false;

    private boolean mRssi = false;

    private boolean mFile = false;

    private Handler mOptionHandler;

    private SoundPool mSoundPool;

    private int mSoundId;

    private float mSoundVolume;

    private boolean mSoundFileLoadState;

    private SoundTask mSoundTask;

    private double mOldTotalCount = 0;

    private double mOldSec = 0;

    private FileManager mFileManager;

    private MediaUriWrite mUriWrite;

    private String mLocateTag;

    private String mLocateEPC;

    private int mLocateStartPos;

    private LinearLayout mLocateLayout;

    private LinearLayout mListLayout;

    private ProgressBar mTagLocateProgress;

    private int mLocateValue;

    private int mSledType = SDConsts.SLED_TYPE.SLED_UNKNOWN;

    private ImageButton mBackButton;

    private TextView mLocateTv;

    private boolean mLocate;

    private TimerTask mLocateTimerTask;

    private Timer mClearLocateTimer;

    private Spinner mSessionSpin;
    private ArrayAdapter<CharSequence> mSessionChar;

    private Spinner mSelFlagSpin;
    private ArrayAdapter<CharSequence> mSelFlagChar;
    //private int mCurrentPower;

    private int mTickCount = 0;

    private UpdateStopwatchHandler mUpdateStopwatchHandler = new UpdateStopwatchHandler(this);

    private InventoryHandler mInventoryHandler = new InventoryHandler(this);

    public static InventoryFragment newInstance() {
        return new InventoryFragment();
    }

    private Fragment mFragment;
	
	//<-[20240710]add rssi limit setting menu
    private EditText mRssiEditText;
    private Button mSetRssiButton;
    private Button mGetRssiButton;
	int mRssiLimitVal = -100;
    //[20240710]add rssi limit setting menu->

    //<-[20250402]Add Bulk encoding
    private Button mEncodingInvenButton;

    private Button mEncodingStopInvenButton;

    RadioGroup mRadioGroup;

    private RadioButton mSKU1Button;

    private RadioButton mSKU2Button;

    private RadioButton mMassButton;

    private RadioButton mPrivateButton;

    private RadioButton mReviveButton;

    String mTagStrSKU;

    String mTagStrPW;

    int mEncodingMode = 0;

    private CopyOnWriteArrayList<EPCItem> mInditexTagINFOList;

    private CopyOnWriteArrayList<String> mInditexTagList;

    private TextView mSuccessTextView;

    private TextView mFailTextView;

    int mSuccessCnt = 0;

    int mFailCnt = 0;
    //[20250402]Add Bulk encoding->

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.inventory_frag, container, false);
        mContext = inflater.getContext();

        mFragment = this;

        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        mRfidList = (ListView) v.findViewById(R.id.rfid_list);

        mRfidList.setOnItemClickListener(listItemClickListener);

        mLocateLayout = (LinearLayout) v.findViewById(R.id.tag_locate_container);

        mListLayout = (LinearLayout) v.findViewById(R.id.tag_list_container);

        mLocateTv = (TextView) v.findViewById(R.id.tag_locate_text);

        mTagLocateProgress = (ProgressBar) v.findViewById(R.id.tag_locate_progress);

        mBackButton = (ImageButton) v.findViewById(R.id.back_button);
        mBackButton.setOnClickListener(sledListener);

        mTimerText = (TextView) v.findViewById(R.id.timer_text);

        mCountText = (TextView) v.findViewById(R.id.count_text);

        mSpeedCountText = (TextView) v.findViewById(R.id.speed_count_text);

        mAvrSpeedCountTest = (TextView) v.findViewById(R.id.speed_avr_count_text);

        Activity activity = getActivity();

        if (activity != null) {
            String speedCountStr = activity.getString(R.string.speed_count_str) + activity.getString(R.string.speed_postfix_str);
            mSpeedCountText.setText(speedCountStr);
            mAvrSpeedCountTest.setText(speedCountStr);
        }

        mBatteryText = (TextView) v.findViewById(R.id.battery_text);

        mTurboSwitch = (Switch) v.findViewById(R.id.turbo_switch);

        mRssiSwitch = (Switch) v.findViewById(R.id.rssi_switch);

        mFilterSwitch = (Switch) v.findViewById(R.id.filter_switch);

        mSoundSwitch = (Switch) v.findViewById(R.id.sound_switch);

        mMaskSwitch = (Switch) v.findViewById(R.id.mask_switch);

        mToggleSwitch = (Switch) v.findViewById(R.id.toggle_switch);

        mPCSwitch = (Switch) v.findViewById(R.id.pc_switch);

        mFileSwitch = (Switch) v.findViewById(R.id.file_switch);

        mClearButton = (Button) v.findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(clearButtonListener);

        mInvenButton = (Button) v.findViewById(R.id.inven_button);
        mInvenButton.setOnClickListener(sledListener);

        mStopInvenButton = (Button) v.findViewById(R.id.stop_inven_button);
        mStopInvenButton.setOnClickListener(sledListener);

        mProgressBar = (ProgressBar) v.findViewById(R.id.timer_progress);
        mProgressBar.setVisibility(View.INVISIBLE);

        mSessionSpin = (Spinner) v.findViewById(R.id.session_spin);
        mSessionChar = ArrayAdapter.createFromResource(mContext, R.array.session_array,
                android.R.layout.simple_spinner_dropdown_item);
        mSessionSpin.setAdapter(mSessionChar);

        mSelFlagSpin = (Spinner) v.findViewById(R.id.sel_flag_spin);
        mSelFlagChar = ArrayAdapter.createFromResource(mContext, R.array.sel_flag_array,
                android.R.layout.simple_spinner_dropdown_item);
        mSelFlagSpin.setAdapter(mSelFlagChar);

        mAdapter = new TagListAdapter(mContext);
        mRfidList.setAdapter(mAdapter);

        bindStopwatchSvc();

        setBuiltInModelKeyEvent(v);

	    //+[20240710]add rssi limit setting menu
        mRssiEditText = (EditText) v.findViewById(R.id.rssi_limit_edit);
        mSetRssiButton = (Button) v.findViewById(R.id.set_rssi_limit_button);
        mSetRssiButton.setOnClickListener(sledListener);
        mGetRssiButton = (Button) v.findViewById(R.id.get_rssi_limit_button);
        mGetRssiButton.setOnClickListener(sledListener);
        //[20240710]add rssi limit setting menu+

        //<-[20250402]Add Bulk encoding
//        mFilterSwitch.setVisibility(View.GONE);//if bulk encoding, enable

        mSuccessTextView = (TextView) v.findViewById(R.id.sucess_count_text);
        mFailTextView = (TextView) v.findViewById(R.id.fail_count_text);

        mEncodingInvenButton = (Button) v.findViewById(R.id.encoding_inven_button);
        mEncodingInvenButton.setOnClickListener(sledListener);

        mEncodingStopInvenButton = (Button) v.findViewById(R.id.encoding_stop_inven_button);
        mEncodingStopInvenButton.setOnClickListener(sledListener);

        mSKU1Button = (RadioButton) v.findViewById(R.id.radioSku1);
        mSKU1Button.setOnClickListener(radioSKUButtonClickListener);
        mSKU2Button = (RadioButton) v.findViewById(R.id.radioSku2);
        mSKU2Button.setOnClickListener(radioSKUButtonClickListener);

        mMassButton = (RadioButton) v.findViewById(R.id.radioMass);
        mMassButton.setOnClickListener(radioEncodingModeButtonClickListener);
        mPrivateButton = (RadioButton) v.findViewById(R.id.radioPrivate);
        mPrivateButton.setOnClickListener(radioEncodingModeButtonClickListener);
        mReviveButton = (RadioButton) v.findViewById(R.id.radioRevive);
        mReviveButton.setOnClickListener(radioEncodingModeButtonClickListener);

        initINDITEXTagData();
        //[20250402]Add Bulk encoding->
		
		//<-[20250424]Add other inventory api for test
        mInvenWithLocButton = (Button) v.findViewById(R.id.inven_withLoc_button);
        mInvenWithLocButton.setOnClickListener(sledListener);

        mInvenWithPhaseFreqButton = (Button) v.findViewById(R.id.inven_withPhaseFreq_button);
        mInvenWithPhaseFreqButton.setOnClickListener(sledListener);

        mInvenCustomButton = (Button) v.findViewById(R.id.inven_custom_button);
        mInvenCustomButton.setOnClickListener(sledListener);

        mInvenRSSIButton = (Button) v.findViewById(R.id.inven_rssi_button);
        mInvenRSSIButton.setOnClickListener(sledListener);
        //[20250424]Add other inventory api for test->

        return v;
    }

    //<-[20250402]Add Bulk encoding
    void initINDITEXTagData(){
        mInditexTagINFOList = new CopyOnWriteArrayList<EPCItem>();
        mInditexTagINFOList.add(new EPCItem("2EF6B77C", "918b65a3"));
        mInditexTagINFOList.add(new EPCItem("2EF6B944", "cdf33df7"));
        mInditexTagINFOList.add(new EPCItem("2EF6B8C1", "316f8d23"));
        mInditexTagINFOList.add(new EPCItem("2EF6B696", "eaa02f70"));
        mInditexTagINFOList.add(new EPCItem("2EF6B8C2", "0da5f744"));
        mInditexTagINFOList.add(new EPCItem("0E980AF4", "36208b15"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA8A", "287bc724"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB10", "f0547ccd"));
        mInditexTagINFOList.add(new EPCItem("2EF6B654", "48f867c8"));
        mInditexTagINFOList.add(new EPCItem("2EF6B640", "22c7eaca"));
        mInditexTagINFOList.add(new EPCItem("2EF6B72A", "31dafe32"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6AE", "462856ca"));
        mInditexTagINFOList.add(new EPCItem("2EF6B746", "c249b487"));
        mInditexTagINFOList.add(new EPCItem("2EF6B736", "046e9861"));
        mInditexTagINFOList.add(new EPCItem("2EF6B9CA", "126e7d62"));
        mInditexTagINFOList.add(new EPCItem("2EF6B69F", "0175d188"));
        mInditexTagINFOList.add(new EPCItem("11E79925", "d9f424dc"));
        mInditexTagINFOList.add(new EPCItem("11E798D4", "0baf2ed0"));
        mInditexTagINFOList.add(new EPCItem("2EF6B686", "630b3c0e"));
        mInditexTagINFOList.add(new EPCItem("2EF6B679", "29a05010"));
        mInditexTagINFOList.add(new EPCItem("2EF6B796", "56c7144c"));
        mInditexTagINFOList.add(new EPCItem("11E798DC", "1b0bbd24"));
        mInditexTagINFOList.add(new EPCItem("11E7965A", "af8db956"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA91", "719b4cf0"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6FD", "8bea3096"));
        mInditexTagINFOList.add(new EPCItem("2EF6B685", "7ff1d34d"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA40", "7711a180"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA8D", "8b503470"));
        mInditexTagINFOList.add(new EPCItem("2EF6B966", "7898536a"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6BD", "3f12f70f"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6CB", "84dd4b8d"));
        mInditexTagINFOList.add(new EPCItem("2EF6B683", "4c25314e"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6FC", "eec2aea6"));
        mInditexTagINFOList.add(new EPCItem("2EF6B65F", "598399dc"));
        mInditexTagINFOList.add(new EPCItem("2EF6B72B", "3591caf3"));
        mInditexTagINFOList.add(new EPCItem("2EF6BAEA", "90c81d55"));
        mInditexTagINFOList.add(new EPCItem("2EF6BAC0", "969fbbe0"));
        mInditexTagINFOList.add(new EPCItem("2EF6B97F", "1d8ed03b"));
        mInditexTagINFOList.add(new EPCItem("2EF6B719", "18b5e032"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB20", "82e58740"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA44", "20842d8b"));
        mInditexTagINFOList.add(new EPCItem("11E798B5", "25594827"));
        mInditexTagINFOList.add(new EPCItem("2EF6B94E", "f46ec13d"));
        mInditexTagINFOList.add(new EPCItem("2EF6B772", "7188a8d1"));
        mInditexTagINFOList.add(new EPCItem("2EF6BC2E", "5e5e56ec"));
        mInditexTagINFOList.add(new EPCItem("2EF6B79D", "0ca1188a"));
        mInditexTagINFOList.add(new EPCItem("2EF6B750", "621be137"));
        mInditexTagINFOList.add(new EPCItem("2EF6B65D", "e1ffbf00"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA3A", "885cac25"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6C6", "1a850852"));
        mInditexTagINFOList.add(new EPCItem("2EF6B667", "e9636d2c"));
        mInditexTagINFOList.add(new EPCItem("2EF6BC17", "1f6b2d23"));
        mInditexTagINFOList.add(new EPCItem("2EF6B653", "02e6adbb"));
        mInditexTagINFOList.add(new EPCItem("2EF6B768", "db6babed"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB5D", "bd7b5826"));
        mInditexTagINFOList.add(new EPCItem("2EF6BBF3", "1a63fb1c"));
        mInditexTagINFOList.add(new EPCItem("2EF6B799", "107ce025"));
        mInditexTagINFOList.add(new EPCItem("11E79481", "4ac4ea76"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB7C", "9e3047f4"));
        mInditexTagINFOList.add(new EPCItem("11E795E4", "cc7679ac"));
        mInditexTagINFOList.add(new EPCItem("2EF6B724", "5023404a"));
        mInditexTagINFOList.add(new EPCItem("2EF6B65C", "a6953d95"));
        mInditexTagINFOList.add(new EPCItem("2EF6B678", "32ddaa9f"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6F5", "3382803b"));
        mInditexTagINFOList.add(new EPCItem("2EF6B737", "361b6fa2"));
        mInditexTagINFOList.add(new EPCItem("2EF6B658", "3ab6a34f"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB18", "11f556d2"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6C8", "7eb71083"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA3B", "3bcd9603"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6CA", "da8b169a"));
        mInditexTagINFOList.add(new EPCItem("11E798E7", "72c1ba41"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB07", "10813a23"));
        mInditexTagINFOList.add(new EPCItem("2EF6B98A", "8e1a012a"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB63", "6e72e0dd"));
        mInditexTagINFOList.add(new EPCItem("2EF6B9C1", "dd8b7a6b"));
        mInditexTagINFOList.add(new EPCItem("11E798E3", "ae32a319"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB7B", "e2e3a654"));
        mInditexTagINFOList.add(new EPCItem("2EF6BBB5", "b873f2b2"));
        mInditexTagINFOList.add(new EPCItem("2EF6B9CB", "83d76987"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA2D", "b07ce17d"));
        mInditexTagINFOList.add(new EPCItem("2EF6B86A", "c85714cf"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6DE", "26250581"));
        mInditexTagINFOList.add(new EPCItem("2EF6B637", "28586098"));
        mInditexTagINFOList.add(new EPCItem("2EF6B656", "74b151be"));
        mInditexTagINFOList.add(new EPCItem("2EF6B8C4", "cb8360d3"));
        mInditexTagINFOList.add(new EPCItem("2EF6B699", "ada1d2a2"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB5C", "d01f7211"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA56", "4bafa1a6"));
        mInditexTagINFOList.add(new EPCItem("2EF6B783", "e183ae8a"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA2C", "ba0b61c6"));
        mInditexTagINFOList.add(new EPCItem("2EF6B9C7", "cb4288ea"));
        mInditexTagINFOList.add(new EPCItem("11E79658", "fd4ce4d8"));
        mInditexTagINFOList.add(new EPCItem("2EF6B885", "957842ae"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA2F", "54eb4607"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6B2", "0b82d887"));
        mInditexTagINFOList.add(new EPCItem("2EF6B7B1", "5e2ca8b3"));
        mInditexTagINFOList.add(new EPCItem("2EF6BAEC", "6a0c4384"));
        mInditexTagINFOList.add(new EPCItem("11E798E5", "f3dd3c1b"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB15", "f78c5305"));
        mInditexTagINFOList.add(new EPCItem("11E798C6", "67807fdb"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6D8", "f65fd1f3"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6C2", "cda092b9"));
        mInditexTagINFOList.add(new EPCItem("2EF6BBB7", "b4f6e1e3"));
        mInditexTagINFOList.add(new EPCItem("2EF6B659", "cd9bd478"));
        mInditexTagINFOList.add(new EPCItem("2EF6B766", "2564651a"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6C0", "d0f9b377"));
        mInditexTagINFOList.add(new EPCItem("2EF6B9BF", "f1121f79"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB62", "a8c65d56"));
        mInditexTagINFOList.add(new EPCItem("2EF6BADF", "c82841ba"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6A7", "3c08705f"));
        mInditexTagINFOList.add(new EPCItem("2EF6B70B", "af17eede"));
        mInditexTagINFOList.add(new EPCItem("2EF6B690", "744a2e51"));
        mInditexTagINFOList.add(new EPCItem("11E79851", "6089a978"));
        mInditexTagINFOList.add(new EPCItem("2EF6B8C0", "ff7c77cd"));
        mInditexTagINFOList.add(new EPCItem("2EF6B630", "d229346a"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6F8", "eb3fc808"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB19", "d207541f"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA2E", "b9a5605f"));
        mInditexTagINFOList.add(new EPCItem("2EF6B79A", "115a3366"));
        mInditexTagINFOList.add(new EPCItem("11E798D7", "2ce04c43"));
        mInditexTagINFOList.add(new EPCItem("2EF6B669", "d0a8f6a7"));
        mInditexTagINFOList.add(new EPCItem("11E798F5", "5aef31e2"));
        mInditexTagINFOList.add(new EPCItem("2EF6BAEE", "e61bdb1b"));
        mInditexTagINFOList.add(new EPCItem("11E798E4", "e34e4ed8"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6C9", "891bf6d3"));
        mInditexTagINFOList.add(new EPCItem("11E798E6", "743279aa"));
        mInditexTagINFOList.add(new EPCItem("11E7972C", "031c6123"));
        mInditexTagINFOList.add(new EPCItem("2EF6BA98", "1fdf3499"));
        mInditexTagINFOList.add(new EPCItem("2EF6B93D", "c859a49c"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB11", "dd08af53"));
        mInditexTagINFOList.add(new EPCItem("2EF6B668", "38842cc9"));
        mInditexTagINFOList.add(new EPCItem("2EF6B691", "fa1b41aa"));
        mInditexTagINFOList.add(new EPCItem("2EF6B697", "87928557"));
        mInditexTagINFOList.add(new EPCItem("2EF6B729", "06bcdb07"));
        mInditexTagINFOList.add(new EPCItem("11E7989D", "f5af6cf4"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB29", "68085ffa"));
        mInditexTagINFOList.add(new EPCItem("2EF6B770", "9ab9f430"));
        mInditexTagINFOList.add(new EPCItem("2EF6B71B", "69a857d5"));
        mInditexTagINFOList.add(new EPCItem("2EF6B90F", "1d8f4ad7"));
        mInditexTagINFOList.add(new EPCItem("2EF6B71E", "88fe2550"));
        mInditexTagINFOList.add(new EPCItem("2EF6B76F", "f6320184"));
        mInditexTagINFOList.add(new EPCItem("2EF6B76B", "035c6d95"));
        mInditexTagINFOList.add(new EPCItem("2EF6B76E", "e8464774"));
        mInditexTagINFOList.add(new EPCItem("2EF6BBC4", "f846866b"));
        mInditexTagINFOList.add(new EPCItem("0F9E3769", "b06d24af"));
        mInditexTagINFOList.add(new EPCItem("0E943F7E", "774f268a"));
        mInditexTagINFOList.add(new EPCItem("2EF6B70C", "842be9f2"));
        mInditexTagINFOList.add(new EPCItem("11E79476", "3eb9d2d6"));
        mInditexTagINFOList.add(new EPCItem("2EF6B6A3", "734e2225"));
        mInditexTagINFOList.add(new EPCItem("2EF6BB13", "64d62550"));
        mInditexTagINFOList.add(new EPCItem("1571C05A", "b026315c"));
        mInditexTagINFOList.add(new EPCItem("1571C2DD", "63c55023"));
        

        mInditexTagList = new CopyOnWriteArrayList<String>();
        mInditexTagList.add("2EF6B77C");
        mInditexTagList.add("2EF6B944");
        mInditexTagList.add("2EF6B8C1");
        mInditexTagList.add("2EF6B696");
        mInditexTagList.add("2EF6B8C2");
        mInditexTagList.add("0E980AF4");
        mInditexTagList.add("2EF6BA8A");
        mInditexTagList.add("2EF6BB10");
        mInditexTagList.add("2EF6B654");
        mInditexTagList.add("2EF6B640");
        mInditexTagList.add("2EF6B72A");
        mInditexTagList.add("2EF6B6AE");
        mInditexTagList.add("2EF6B746");
        mInditexTagList.add("2EF6B736");
        mInditexTagList.add("2EF6B9CA");
        mInditexTagList.add("2EF6B69F");
        mInditexTagList.add("11E79925");
        mInditexTagList.add("11E798D4");
        mInditexTagList.add("2EF6B686");
        mInditexTagList.add("2EF6B679");
        mInditexTagList.add("2EF6B796");
        mInditexTagList.add("11E798DC");
        mInditexTagList.add("11E7965A");
        mInditexTagList.add("2EF6BA91");
        mInditexTagList.add("2EF6B6FD");
        mInditexTagList.add("2EF6B685");
        mInditexTagList.add("2EF6BA40");
        mInditexTagList.add("2EF6BA8D");
        mInditexTagList.add("2EF6B966");
        mInditexTagList.add("2EF6B6BD");
        mInditexTagList.add("2EF6B6CB");
        mInditexTagList.add("2EF6B683");
        mInditexTagList.add("2EF6B6FC");
        mInditexTagList.add("2EF6B65F");
        mInditexTagList.add("2EF6B72B");
        mInditexTagList.add("2EF6BAEA");
        mInditexTagList.add("2EF6BAC0");
        mInditexTagList.add("2EF6B97F");
        mInditexTagList.add("2EF6B719");
        mInditexTagList.add("2EF6BB20");
        mInditexTagList.add("2EF6BA44");
        mInditexTagList.add("11E798B5");
        mInditexTagList.add("2EF6B94E");
        mInditexTagList.add("2EF6B772");
        mInditexTagList.add("2EF6BC2E");
        mInditexTagList.add("2EF6B79D");
        mInditexTagList.add("2EF6B750");
        mInditexTagList.add("2EF6B65D");
        mInditexTagList.add("2EF6BA3A");
        mInditexTagList.add("2EF6B6C6");
        mInditexTagList.add("2EF6B667");
        mInditexTagList.add("2EF6BC17");
        mInditexTagList.add("2EF6B653");
        mInditexTagList.add("2EF6B768");
        mInditexTagList.add("2EF6BB5D");
        mInditexTagList.add("2EF6BBF3");
        mInditexTagList.add("2EF6B799");
        mInditexTagList.add("11E79481");
        mInditexTagList.add("2EF6BB7C");
        mInditexTagList.add("11E795E4");
        mInditexTagList.add("2EF6B724");
        mInditexTagList.add("2EF6B65C");
        mInditexTagList.add("2EF6B678");
        mInditexTagList.add("2EF6B6F5");
        mInditexTagList.add("2EF6B737");
        mInditexTagList.add("2EF6B658");
        mInditexTagList.add("2EF6BB18");
        mInditexTagList.add("2EF6B6C8");
        mInditexTagList.add("2EF6BA3B");
        mInditexTagList.add("2EF6B6CA");
        mInditexTagList.add("11E798E7");
        mInditexTagList.add("2EF6BB07");
        mInditexTagList.add("2EF6B98A");
        mInditexTagList.add("2EF6BB63");
        mInditexTagList.add("2EF6B9C1");
        mInditexTagList.add("11E798E3");
        mInditexTagList.add("2EF6BB7B");
        mInditexTagList.add("2EF6BBB5");
        mInditexTagList.add("2EF6B9CB");
        mInditexTagList.add("2EF6BA2D");
        mInditexTagList.add("2EF6B86A");
        mInditexTagList.add("2EF6B6DE");
        mInditexTagList.add("2EF6B637");
        mInditexTagList.add("2EF6B656");
        mInditexTagList.add("2EF6B8C4");
        mInditexTagList.add("2EF6B699");
        mInditexTagList.add("2EF6BB5C");
        mInditexTagList.add("2EF6BA56");
        mInditexTagList.add("2EF6B783");
        mInditexTagList.add("2EF6BA2C");
        mInditexTagList.add("2EF6B9C7");
        mInditexTagList.add("11E79658");
        mInditexTagList.add("2EF6B885");
        mInditexTagList.add("2EF6BA2F");
        mInditexTagList.add("2EF6B6B2");
        mInditexTagList.add("2EF6B7B1");
        mInditexTagList.add("2EF6BAEC");
        mInditexTagList.add("11E798E5");
        mInditexTagList.add("2EF6BB15");
        mInditexTagList.add("11E798C6");
        mInditexTagList.add("2EF6B6D8");
        mInditexTagList.add("2EF6B6C2");
        mInditexTagList.add("2EF6BBB7");
        mInditexTagList.add("2EF6B659");
        mInditexTagList.add("2EF6B766");
        mInditexTagList.add("2EF6B6C0");
        mInditexTagList.add("2EF6B9BF");
        mInditexTagList.add("2EF6BB62");
        mInditexTagList.add("2EF6BADF");
        mInditexTagList.add("2EF6B6A7");
        mInditexTagList.add("2EF6B70B");
        mInditexTagList.add("2EF6B690");
        mInditexTagList.add("11E79851");
        mInditexTagList.add("2EF6B8C0");
        mInditexTagList.add("2EF6B630");
        mInditexTagList.add("2EF6B6F8");
        mInditexTagList.add("2EF6BB19");
        mInditexTagList.add("2EF6BA2E");
        mInditexTagList.add("2EF6B79A");
        mInditexTagList.add("11E798D7");
        mInditexTagList.add("2EF6B669");
        mInditexTagList.add("11E798F5");
        mInditexTagList.add("2EF6BAEE");
        mInditexTagList.add("11E798E4");
        mInditexTagList.add("2EF6B6C9");
        mInditexTagList.add("11E798E6");
        mInditexTagList.add("11E7972C");
        mInditexTagList.add("2EF6BA98");
        mInditexTagList.add("2EF6B93D");
        mInditexTagList.add("2EF6BB11");
        mInditexTagList.add("2EF6B668");
        mInditexTagList.add("2EF6B691");
        mInditexTagList.add("2EF6B697");
        mInditexTagList.add("2EF6B729");
        mInditexTagList.add("11E7989D");
        mInditexTagList.add("2EF6BB29");
        mInditexTagList.add("2EF6B770");
        mInditexTagList.add("2EF6B71B");
        mInditexTagList.add("2EF6B90F");
        mInditexTagList.add("2EF6B71E");
        mInditexTagList.add("2EF6B76F");
        mInditexTagList.add("2EF6B76B");
        mInditexTagList.add("2EF6B76E");
        mInditexTagList.add("2EF6BBC4");
        mInditexTagList.add("0F9E3769");
        mInditexTagList.add("0E943F7E");
        mInditexTagList.add("2EF6B70C");
        mInditexTagList.add("11E79476");
        mInditexTagList.add("2EF6B6A3");
        mInditexTagList.add("2EF6BB13");
        mInditexTagList.add("1571C05A");
        mInditexTagList.add("1571C2DD");       
    }

    private void startEncodingInventory() {
        if (!mInventory) {
            clearAll();
            int ret = SDConsts.RFResult.OTHER_ERROR;
            switch(mEncodingMode){
                case Constants.EncodeMode.MASS:
                    mReader.RF_RemoveSelection();
                    SelectionCriterias s = new SelectionCriterias();
                    s.makeCriteria(SelectionCriterias.SCMemType.EPC, mTagStrSKU,
                            12, mTagStrSKU.length() * 4,
                            SelectionCriterias.SCActionType.DSLINVB_ASLINVA);//0->4
                    mReader.RF_SetSelection(s);
                    ret = mReader.RF_PerformInventoryEncoding(mIsTurbo, true, mIgnorePC);
                    break;
                case Constants.EncodeMode.PRIVATE:
                    mReader.RF_RemoveSelection();
                    ret = mReader.RF_PerformInventoryEncoding(mIsTurbo, false, mIgnorePC);
                    break;
                case Constants.EncodeMode.REVIVE:
                    mReader.RF_RemoveSelection();
                    SelectionCriterias s1 = new SelectionCriterias();
                    s1.makeCriteria(SelectionCriterias.SCMemType.USER, Constants.BB_PW,
                            0, 32,
                            SelectionCriterias.SCActionType.ASLINVA_DSLINVB);
                    mReader.RF_SetSelection(s1);
                    ret = mReader.RF_PerformInventoryEncoding(mIsTurbo, true, mIgnorePC);
                    break;
            }
            if (ret == SDConsts.RFResult.SUCCESS) {
                startStopwatch();
                mInventory = true;
                enableControl(!mInventory);
            } else if (ret == SDConsts.RFResult.MODE_ERROR)
                Toast.makeText(mContext, "Start Inventory failed, Please check RFR MODE", Toast.LENGTH_SHORT).show();
            else if (ret == SDConsts.RFResult.LOW_BATTERY)
                Toast.makeText(mContext, "Start Inventory failed, LOW_BATTERY", Toast.LENGTH_SHORT).show();
            else if (D) Log.d(TAG, "Start Inventory failed");
        }
    }

    private void stopEncodingInventory(){
        mReader.RF_RemoveSelection();
        int ret = mReader.RF_StopInventoryEncoding();
        if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
            mInventory = false;
            saveFile();//20231011 change save routine
            enableControl(!mInventory);
            pauseStopwatch();
        } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
            Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();

    }

    RadioButton.OnClickListener radioSKUButtonClickListener = new RadioButton.OnClickListener(){
        @Override
        public void onClick(View view) {
            if (mSKU1Button.isChecked()) {
                mTagStrSKU = "0002";//chaging SKU1
                mSKU2Button.setChecked(false);
            }else if(mSKU2Button.isChecked()){
                mTagStrSKU = "0004";//chaging SKU2
                mSKU1Button.setChecked(false);
            }
            Toast.makeText(mContext, "To write SKU = " + (mTagStrSKU.equals("0002")?"other->0002":"other->0004"), Toast.LENGTH_SHORT).show();
        }
    };

    RadioButton.OnClickListener radioEncodingModeButtonClickListener = new RadioButton.OnClickListener(){
        @Override
        public void onClick(View view) {
            if (mMassButton.isChecked()) {
                mEncodingMode = Constants.EncodeMode.MASS;
                mPrivateButton.setChecked(false);
                mReviveButton.setChecked(false);
            }else if (mPrivateButton.isChecked()) {
                mEncodingMode = Constants.EncodeMode.PRIVATE;
                mReviveButton.setChecked(false);
                mMassButton.setChecked(false);
            }else if (mReviveButton.isChecked()) {
                mEncodingMode = Constants.EncodeMode.REVIVE;
                mPrivateButton.setChecked(false);
                mMassButton.setChecked(false);
            }
            Toast.makeText(mContext, "Encoding Mode = " + mEncodingMode, Toast.LENGTH_SHORT).show();
        }
    };

    private void updateSuccessText() {
        Log.d(TAG, "updateSuccessText");
        String text = Integer.toString(mSuccessCnt);
        mSuccessTextView.setText(text);
        text = Integer.toString(  mAdapter.getCount() - mSuccessCnt);
        mFailTextView.setText(text);
    }

    private String generateNewEpc(String oldEpc)
    {
        String newEpc = "";
        int position = 8;
        int length = 4;
        newEpc = oldEpc.substring(0, position) + mTagStrSKU + oldEpc.substring(position + length);
        return newEpc;
    }

    private void processEncodingReadData(String data) {        
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String info = "";
        String epcDecode = "";
        String pha = "";
        String freq = "";
        String antId = "";
        String originalData = data;
        if (originalData.contains(";")) {
            if (D) Log.d(TAG, "full tag = " + data);
            data = "";
            String[] splitData = originalData.split(";");
            Activity activity = getActivity();
            String prefix = "";
            for (String dt : splitData) {
                if(dt.startsWith("rssi:")) {
                    int type = -1;
                    String[] splitInfo = dt.split(":");
                    for(String str : splitInfo) {
                        if(str.equals("rssi")) type = 0;
                        else if(str.equals("pha")) type = 1;
                        else if(str.equals("freq")) type = 2;
                        else if(str.equals("antid"))type = 3;
                        else if(type != -1) {
                            switch (type) {
                                case 0:
                                    if (activity != null)
                                        prefix = activity.getString(R.string.rssi_str);
                                    info = prefix + str.replace("rssi:", "");
                                    break;
                                case 1: pha = "PHA : " + str;
                                    break;
                                case 2: freq = "Freq : " + str;
                                    break;
                                case 3: antId = "AntId : " + str;
                                    break;
                            }
                        }
                    }
                    if (D) Log.d(TAG, "rssi tag = " + info);
                } else if (dt.startsWith("loc:")) {
                    if (activity != null)
                        prefix = activity.getString(R.string.loc_str);
                    info = prefix + dt.replace("loc:", "");
                    if (D) Log.d(TAG, "loc tag = " + info);
                } else if (dt.startsWith("epcdc:")) {
                    if (activity != null)
                        prefix = activity.getString(R.string.epc_decode_str);
                    epcDecode = prefix + dt.replace("epcdc:", "");
                    if (D) Log.d(TAG, "epc_decode tag = " + info);
                } else if (TextUtils.isEmpty(data)) {
                    data = dt;
                    if (D) Log.d(TAG, "data tag = " + data);
                }
            }
        }

        mAdapter.addItem(-1, data, info, pha, freq, epcDecode, 0, mStopwatchSvc.getFormattedElapsedTime(), !mIgnorePC, mTagFilter);
        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        if (!mInventory) {
            updateCountText();
            updateSpeedCountText();
            updateAvrSpeedCountText();
        }

        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }
	//[20250402]Add Bulk encoding->


    private void setBarcodeKeyMapOn(boolean setOn) {
        try {
            @SuppressLint("WrongConstant") KeyMapperManager mKeyMapperManager = (KeyMapperManager) mContext.getSystemService("keymapper");
            int sourceKeyCode = KeyEvent.keyCodeFromString("KEYCODE_PTT_SCAN_R");

            mKeyMapperManager.setKeyMapping(sourceKeyCode, setOn);
            if (setOn)
                mKeyMapperManager.removeKeyMapSetting(sourceKeyCode);
        } catch (NullPointerException e0) {
            e0.printStackTrace();
            Toast.makeText(mContext, "All inventory func works fine. for your information This device's image is not keymapper supported.", Toast.LENGTH_SHORT).show();
            return;
        }catch (NoSuchMethodError e1){
            e1.printStackTrace();
            if (D) Log.d(TAG, "NoSuchMethodError : unknown0");
//            Toast.makeText(mContext, "unknown0", Toast.LENGTH_SHORT).show();
        }catch (Exception e2) {
            e2.printStackTrace();
            return;
        }
    }

    private void setBuiltInModelKeyEvent(View mView) {
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();

        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == 503) {  //KEYCODE_PTT_SCAN_R
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getRepeatCount() == 0 && mSledType == SDConsts.SLED_TYPE.INTERNAL_SLED) { // 260227
                        if (D) Log.d(TAG, "mInventory check: " + mInventory);
                        if (!mInventory) {
                            startInventory(Constants.InventoryType.NORAML);
                        } else {
                            stopInventory();
                        }
                        return true;
                    } else return false;
                } else return false;
            }
        });
    }

    private void startInventory(int type) {
        if (!mInventory) {
            clearAll();
//            openFile();//20231011 change save routine

            int ret = 0;
            if (mLocate) {
                //+force optimize config for find tag - RF Mode:1,Session:S0,Toggole:On,Singulation:5
//                mReader.RF_SetRFMode(1);
//                mReader.RF_SetSession(SDConsts.RFSession.SESSION_S0);
//                mReader.RF_SetToggle(SDConsts.RFToggle.ON);
//                mReader.RF_SetSingulationControl(5, SDConsts.RFSingulation.MIN_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
                //force optimize config for find tag - RF Mode:1,Session:S0,Toggole:On,Singulation:5+
                ret = mReader.RF_PerformInventoryForLocating(mLocateEPC);
            } else {
                //+force optimize config for unique tag - RF Mode:1,Session:S1,Toggole:Off,Singulation:10
//                mReader.RF_SetRFMode(1);
//                mReader.RF_SetSession(SDConsts.RFSession.SESSION_S1);
//                mReader.RF_SetToggle(SDConsts.RFToggle.OFF);
//                mReader.RF_SetSingulationControl(10, SDConsts.RFSingulation.MIN_SINGULATION, SDConsts.RFSingulation.MAX_SINGULATION);
                //force optimize config for unique tag - RF Mode:1,Session:S1,Toggole:Off,Singulation:10+
//                ret = mReader.RF_PerformInventory(mIsTurbo, mMask, mIgnorePC, false);
                //<-[20250424]Add other inventory api for test
                switch (type){
                    case Constants.InventoryType.NORAML:
                        ret = mReader.RF_PerformInventory(mIsTurbo, mMask, mIgnorePC);
                        break;
                    case Constants.InventoryType.RSSI_TO_LOCATE:
                        ret = mReader.RF_PerformInventoryWithLocating(mIsTurbo, mMask, mIgnorePC);
                        break;
                    case Constants.InventoryType.CUSTOM:
                        mReader.RF_PerformInventoryCustom(SelectionCriterias.SCMemType.TID, 0, 2, "00000000", mMask);
                        break;
                    case Constants.InventoryType.RSSI_LIMIT:
                        ret = mReader.RF_PerformInventoryWithRssiLimitation(mIsTurbo, mMask, mIgnorePC, mRssiLimitVal);
                        break;
                    case Constants.InventoryType.WITH_PAHSE_FREQ:
                        ret = mReader.RF_PerformInventoryWithPhaseFreq(mIsTurbo, mMask, mIgnorePC);
                        break;
                }
                //[20250424]Add other inventory api for test->
//                ret = mReader.RF_PerformInventory(mIsTurbo, mMask, mIgnorePC);

                //+additional inventory feature
                //Check the API below to use inventory with limitation.
//                ret = mReader.RF_PerformInventoryWithRssiLimitation(mIsTurbo, mMask, mIgnorePC, mRssiLimitVal);

                //Check the API below to use inventory with information other than EPC
                //ret = mReader.RF_PerformInventoryCustom(SDConsts.RFMemType.TID, 0 ,2, "00000000", mMask);

                //Check the API below to use inventory with locating
                //ret = mReader.RF_PerformInventoryWithLocating(mIsTurbo, mMask, mIgnorePC);

                //Check the API below to use inventory with phase freq
//                ret = mReader.RF_PerformInventoryWithPhaseFreq(mIsTurbo, mMask, mIgnorePC);

                //Check the API below to use inventory custom
                //ret = mReader.RF_PerformInventoryCustom(SelectionCriterias.SCMemType.TID, 0, 4, "00000000",mMask);
                //additional inventory feature+
            }

            if (ret == SDConsts.RFResult.SUCCESS) {
                startStopwatch();
                mInventory = true;
                enableControl(!mInventory);
            } else if (ret == SDConsts.RFResult.MODE_ERROR)
                Toast.makeText(mContext, "Start Inventory failed, Please check RFR MODE", Toast.LENGTH_SHORT).show();
            else if (ret == SDConsts.RFResult.LOW_BATTERY)
                Toast.makeText(mContext, "Start Inventory failed, LOW_BATTERY", Toast.LENGTH_SHORT).show();
            else if (D) Log.d(TAG, "Start Inventory failed");
        }
    }

    private void stopInventory(){
        int ret = mReader.RF_StopInventory();
        if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
            mInventory = false;
            saveFile();//20231011 change save routine
            enableControl(!mInventory);
            pauseStopwatch();
        } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
            Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();

    }

    private OnItemSelectedListener sessionListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position > 0)
                Toast.makeText(mContext, "If you want to use session 1 ~ 3 value, toggle off", Toast.LENGTH_SHORT).show();
            mReader.RF_SetSession(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private OnItemSelectedListener selFlagListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mReader.RF_SetSelectionFlag(position + 1);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private AdapterView.OnItemClickListener listItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListItem i = (ListItem) mRfidList.getItemAtPosition(position);
            mLocateTag = i.mUt;
            mLocateStartPos = (i.mHasPc ? 0 : 4);
            if (i.mHasPc)
                mLocateEPC = mLocateTag.substring(4, mLocateTag.length());
            else
                mLocateEPC = mLocateTag;

            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(getString(R.string.locating_str));
            alert.setMessage(getString(R.string.want_tracking_str));

            alert.setPositiveButton(getString(R.string.yes_str), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    SelectionCriterias s = new SelectionCriterias();
                    s.makeCriteria(SelectionCriterias.SCMemType.EPC, mLocateTag,
                            mLocateStartPos, mLocateTag.length() * 4,
                            SelectionCriterias.SCActionType.ASLINVA_DSLINVB);
                    mReader.RF_SetSelection(s);
                    switchLayout(false);
                    mLocateTv.setText(mLocateTag);
                    //enableControl(false);
                }
            });
            alert.setNegativeButton(getString(R.string.no_str), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        }
    };

    private void switchLayout(boolean showList) {
        mLocate = !showList;
//        if (mLocate)
//            mReader.RF_SetRssiTrackingState(SDConsts.RFRssi.ON);
        if (showList) {
            mListLayout.setVisibility(View.VISIBLE);
            mLocateLayout.setVisibility(View.GONE);
            mInvenButton.setText(R.string.inven_str);
            mStopInvenButton.setText(R.string.stop_inven_str);
        } else {
            mTagLocateProgress.setProgress(0);
            mListLayout.setVisibility(View.GONE);
            mLocateLayout.setVisibility(View.VISIBLE);
            mInvenButton.setText(R.string.track_str);
            mStopInvenButton.setText(R.string.stop_track_str);
        }
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
    private boolean createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(5).build();
        if (mSoundPool != null)
            return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean createOldSoundPool() {
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

    private class SoundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            if (mLocate)
                mTagLocateProgress.setProgress(mLocateValue);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            if (mSoundPlay) {
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
                } catch (java.lang.NullPointerException e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    };

    @Override
    public void onStart() {
        if (D) Log.d(TAG, "onStart");
        mSoundFileLoadState = false;

//        setBarcodeKeyMapOn(false);
        createSoundPool();

        mOldTotalCount = 0;

        mOldSec = 0;

        mReader = Reader.getReader(mContext, mInventoryHandler);
        if (mReader.SD_GetTriggerMode() == SDConsts.SDTriggerMode.RFID) {
            mReader.BC_SetBarcodeKeyFormat(0);
            setBarcodeKeyMapOn(false);
        }
        if (mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED) {
            enableControl(true);
            updateButtonState();
            mSledType = mReader.SD_GetType();
        } else
            enableControl(false);

        mLocate = false;

        mInventory = false;

        addCheckListener();

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
        setBarcodeKeyMapOn(true);
        mReader.BC_SetBarcodeKeyFormat(Utils.getSelection(mContext));
        mReader.RF_StopInventory();
        pauseStopwatch();
        mInventory = false;
        if (mSoundPool != null)
            mSoundPool.release();
        mSoundFileLoadState = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            if (mFileManager != null) {
                mFileManager.closeFile();
                mFileManager = null;
            }
        }else{
            if (mUriWrite != null) {
                mUriWrite.closeFile();
                mUriWrite = null;
            }
        }
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
            mAdapter.removeAllItem();

            updateCountText();

            stopStopwatch();

            mOldTotalCount = 0;

            mOldSec = 0;

            //<-[20250402]Add Bulk encoding
            mSuccessCnt = 0;
            updateSuccessText();
            //[20250402]Add Bulk encoding->

            updateSpeedCountText();

            updateAvrSpeedCountText();

            Activity activity = getActivity();
            if (activity != null)
                mSpeedCountText.setText("0" + activity.getString(R.string.speed_postfix_str));
        }
    }


    private OnClickListener sledListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "stopwatchListener");
            int ret;
            int id = v.getId();
            if (id == R.id.set_rssi_limit_button) {
                String value = mRssiEditText.getText().toString();
                if (value != null) {
                    value.replaceAll(" ","");
                    if (D) Log.e(TAG, "vaule = " + value);
                    if (value != "") {
                        try {
                            mRssiLimitVal = Integer.parseInt(value);
                            Toast.makeText(mContext, "Set Rssi limt = " + mRssiLimitVal, Toast.LENGTH_SHORT).show();
                            return;
                        } catch (java.lang.NumberFormatException e) {
                            if (D) Log.e(TAG, e.toString());
                        }
                    }else
                        mRssiLimitVal = -100;
                }else
                    Toast.makeText(mContext, "Set Rssi limit!", Toast.LENGTH_SHORT).show();
            }else if (id == R.id.get_rssi_limit_button){
                Toast.makeText(mContext, "Get Rssi limit = " + mRssiLimitVal, Toast.LENGTH_SHORT).show();
            }else if(id == R.id.inven_withLoc_button){
                startInventory(Constants.InventoryType.RSSI_TO_LOCATE);
            }else if(id == R.id.inven_withPhaseFreq_button){
                startInventory(Constants.InventoryType.WITH_PAHSE_FREQ);
            }else if(id == R.id.inven_custom_button){
                startInventory(Constants.InventoryType.CUSTOM);
            }else if(id == R.id.inven_rssi_button){
                startInventory(Constants.InventoryType.RSSI_LIMIT);
            } else if(id == R.id.inven_button){
                startInventory(Constants.InventoryType.NORAML);
            }else if(id == R.id.back_button){
                ret = mReader.RF_StopInventory();
                if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
                    mInventory = false;
                    enableControl(!mInventory);
                    pauseStopwatch();
                } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
                    Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();

                switchLayout(true);
                mLocateTv.setText("");
                mLocateTag = null;
                clearAll();
            }else if(id == R.id.stop_inven_button){
                ret = mReader.RF_StopInventory();
                if (ret == SDConsts.RFResult.SUCCESS || ret == SDConsts.RFResult.NOT_INVENTORY_STATE) {
                    mInventory = false;
                    saveFile();//20231011 change save routine
                    enableControl(!mInventory);
                    pauseStopwatch();
                } else if (ret == SDConsts.RFResult.STOP_FAILED_TRY_AGAIN)
                    Toast.makeText(mContext, "Stop Inventory failed", Toast.LENGTH_SHORT).show();
            }else if(id == R.id.encoding_inven_button){
                startEncodingInventory();
            }else if(id == R.id.encoding_stop_inven_button){
                stopEncodingInventory();
            }
        }
    };

    //+20231011 change save routine
    @SuppressLint("SuspiciousIndentation")
    private void saveFile() {
        if (mFile && !mInventory && !mLocate) {
            mFileSwitch.setChecked(mFile);
        }
        if(mFile){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                if (mFileManager == null)
                    mFileManager = new FileManager(mContext);
                mFileManager.saveFile(mAdapter);
            }else{
                if(mUriWrite == null)
                    mUriWrite = new MediaUriWrite(mContext);
                mUriWrite.saveFile(mAdapter);
            }
        }
    }
    //20231011 change save routine+


    @SuppressLint("SuspiciousIndentation")
    private void openFile() {
        if (mFile && !mInventory && !mLocate) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                if (mFileManager == null)
                    mFileManager = new FileManager(mContext);
                mFileManager.openFile();
            }else{
                if(mUriWrite == null)
                    mUriWrite = new MediaUriWrite(mContext);
                mUriWrite.openFile();
            }
            mFileSwitch.setChecked(mFile);
        }
    }

    private OnCheckedChangeListener sledcheckListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            int id = buttonView.getId();

            if (id == R.id.turbo_switch) {
                if (isChecked)
                    mIsTurbo = true;
                else
                    mIsTurbo = false;
            }else if (id == R.id.file_switch){
                if (isChecked) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        boolean b = PermissionHelper.checkPermission(mContext, PermissionHelper.mStoragePerms[0]);   //file write
                        if (!b) {
                            PermissionHelper.requestPermission(getActivity(), PermissionHelper.mStoragePerms);
                        } else
                            mFile = true;
                    }else{
                        mFile = true;
                    }
                } else {
                    mFile = false;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        if (mFileManager != null) {
                            mFileManager.closeFile();
                            mFileManager = null;
                        }
                    }else{
                        if (mUriWrite != null) {
                            mUriWrite.closeFile();
                            mUriWrite = null;
                        }
                    }
                }
            }else if (id == R.id.rssi_switch){
                if (isChecked) {
                    if (mReader.RF_SetRssiTrackingState(SDConsts.RFRssi.ON) == SDConsts.RFResult.SUCCESS)
                        mRssi = true;
                } else {
                    if (mReader.RF_SetRssiTrackingState(SDConsts.RFRssi.OFF) == SDConsts.RFResult.SUCCESS)
                        mRssi = false;
                }
            }else if (id == R.id.filter_switch){
                clearAll();
                if (isChecked)
                    mTagFilter = true;
                else
                    mTagFilter = false;
            }else if (id == R.id.sound_switch){
                if (isChecked)
                    mSoundPlay = true;
                else
                    mSoundPlay = false;
            }else if (id == R.id.mask_switch){
                if (isChecked)
                    mMask = true;
                else
                    mMask = false;
            }else if (id == R.id.toggle_switch){
                if (isChecked) {
                    if (mReader.RF_SetToggle(SDConsts.RFToggle.ON) == SDConsts.RFResult.SUCCESS)
                        mToggle = true;
                } else {
                    if (mReader.RF_SetToggle(SDConsts.RFToggle.OFF) == SDConsts.RFResult.SUCCESS)
                        mToggle = false;
                }
            }else if (id == R.id.pc_switch){
                if (isChecked)
                    mIgnorePC = true;
                else
                    mIgnorePC = false;
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

        updateSpeedCountText();

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
        String text = Integer.toString(mAdapter.getCount());
        mCountText.setText(text);
    }

    private void updateTimerText() {
        if (D) Log.d(TAG, "updateTimerText");
        if (mStopwatchSvc != null)
            mTimerText.setText(mStopwatchSvc.getFormattedElapsedTime());
    }

    private void updateSpeedCountText() {
        if (D) Log.d(TAG, "updateSpeedCountText");
        String speedStr = "";
        double value = 0;
        double totalCount = 0;
        double sec = 0;
        if (mStopwatchSvc != null) {
            sec = ((double) ((int) (mStopwatchSvc.getElapsedTime() / 100))) / 10;

            if (!mTagFilter)
                totalCount = mAdapter.getTotalCount();
            else {
                totalCount = mAdapter.getTotalCount();
                for (int i = 0; i < mAdapter.getCount(); i++)
                    totalCount += mAdapter.getItemDupCount(i);
            }
            if (totalCount > 0 && sec - mOldSec >= 1) {
                value = (double) ((int) (((totalCount - mOldTotalCount) / (sec - mOldSec)) * 10)) / 10;

                mOldTotalCount = totalCount;

                mOldSec = sec;
                Activity activity = getActivity();
                if (activity != null)
                    speedStr = Double.toString(value) + activity.getString(R.string.speed_postfix_str);
                mSpeedCountText.setText(speedStr);
            }
        }
    }

    private void updateAvrSpeedCountText() {
        if (D) Log.d(TAG, "updateAvrSpeedCountText");
        String speedStr = "";
        double value = 0;
        int totalCount = 0;
        double sec = 0;
        if (mStopwatchSvc != null) {
            sec = ((double) ((int) (mStopwatchSvc.getElapsedTime() / 100))) / 10;

            if (!mTagFilter)
                totalCount = mAdapter.getTotalCount();
            else {
                totalCount = mAdapter.getTotalCount();
                for (int i = 0; i < mAdapter.getCount(); i++)
                    totalCount += mAdapter.getItemDupCount(i);
            }
            if (totalCount > 0 && sec >= 1)
                value = (double) ((int) (((double) totalCount / sec) * 10)) / 10;

            Activity activity = getActivity();
            if (activity != null)
                speedStr = Double.toString(value) + activity.getString(R.string.speed_postfix_str);
            mAvrSpeedCountTest.setText(speedStr);
        }
    }

    private void enableControl(boolean b) {
        if (b)
            mRfidList.setOnItemClickListener(listItemClickListener);
        else
            mRfidList.setOnItemClickListener(null);
        mTurboSwitch.setEnabled(b);
        mRssiSwitch.setEnabled(b);
        mFilterSwitch.setEnabled(b);
        mSoundSwitch.setEnabled(b);
        mMaskSwitch.setEnabled(b);
        mToggleSwitch.setEnabled(b);
        mPCSwitch.setEnabled(b);
        mFileSwitch.setEnabled(b);
        mSessionSpin.setEnabled(b);
        mSelFlagSpin.setEnabled(b);
        mBackButton.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    private ServiceConnection mStopwatchSvcConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            // TODO Auto-generated method stub
            if (D) Log.d(TAG, "onServiceConnected");

            mStopwatchSvc = ((StopwatchService.LocalBinder) arg1).getService(mUpdateStopwatchHandler);
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
        } catch (java.lang.IllegalArgumentException iae) {
            return;
        }
    }

    private static class UpdateStopwatchHandler extends Handler {
        private final WeakReference<InventoryFragment> mExecutor;

        public UpdateStopwatchHandler(InventoryFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            InventoryFragment executor = mExecutor.get();
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

            updateSpeedCountText();

            if (mTickCount == 10) {
                updateAvrSpeedCountText();
                mTickCount = 0;
            }
            updateTimerText();

            mStopwatchSvc.update();

            mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        }
    }

    private static class InventoryHandler extends Handler {
        private final WeakReference<InventoryFragment> mExecutor;

        public InventoryHandler(InventoryFragment f) {
            mExecutor = new WeakReference<>(f);
        }

        @Override
        public void handleMessage(Message msg) {
            InventoryFragment executor = mExecutor.get();
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
                switch (m.arg1) {
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
                        startInventory(Constants.InventoryType.NORAML);
                        break;

                    case SDConsts.SDCmdMsg.SLED_INVENTORY_STATE_CHANGED:
                        mInventory = false;
                        enableControl(!mInventory);
                        pauseStopwatch();
                        // In case of low battery on inventory, reason value is LOW_BATTERY
                        Toast.makeText(mContext, "Inventory Stopped reason : " + m.arg2, Toast.LENGTH_SHORT).show();

                        mAdapter.addItem(-1, "Inventory Stopped reason : " + m.arg2, Integer.toString(m.arg2), !mIgnorePC, mTagFilter);
                        break;

                    case SDConsts.SDCmdMsg.TRIGGER_RELEASED:
                        if (mReader.RF_StopInventory() == SDConsts.SDResult.SUCCESS) {
                            mInventory = false;
                            saveFile();//20231011 change save routine
                            enableControl(!mInventory);
                        }
                        pauseStopwatch();
                        break;

                    case SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED:
                        //This message contain DETACHED event.
                        if (mInventory) {
                            pauseStopwatch();
                            mInventory = false;
                        }
                        enableControl(false);
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
                                enableControl(!mInventory);
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
                switch (m.arg1) {
                    //+RF_PerformInventoryCustom
                    case SDConsts.RFCmdMsg.INVENTORY_CUSTOM_READ:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            String data = (String) m.obj;
                            if (data != null)
                                processReadDataCustom(data);
                        }
                        break;
                    //RF_PerformInventoryCustom+
                    //---NTNS
                    case SDConsts.RFCmdMsg.INVENTORY:
                    case SDConsts.RFCmdMsg.READ:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null && m.obj instanceof String) {
                                String data = (String) m.obj;
                                if (data != null)
                                    processReadData(data);
                            }
                        }
                        break;
                    //<-[20250402]Add Bulk encoding
                    case SDConsts.RFCmdMsg.WRITE_BULK_ENCODING_INVENTORY:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null && m.obj instanceof String) {
                                String data = (String) m.obj;
                                if (data != null){
                                    String[] i = data.split(";");
                                    String targetPcEPC = i[0];
                                    String last8EPC = targetPcEPC.substring(targetPcEPC.length() - 8, targetPcEPC.length());
                                    String targetEPC = targetPcEPC.substring(4);
                                    if(mInditexTagList.contains(last8EPC)){//check inditex tag
                                        processEncodingReadData(data);
                                        int idx = mInditexTagList.indexOf(last8EPC);//To find matching EPC's index
                                        EPCItem eItem = mInditexTagINFOList.get(idx);//To find matching access pw
                                        String epcToWrite = generateNewEpc(targetEPC);

                                        CopyOnWriteArrayList<String> addedTagList = (CopyOnWriteArrayList<String>) mAdapter.getTagList();
                                        int seqID = addedTagList.indexOf(targetPcEPC);

                                        Log.d(TAG, "WRITE_BULK_ENCODING_INVENTORY::SUCCESS::seqID = " + seqID + " ,targetEPC = " + targetEPC + " ,last8EPC = " + last8EPC);
                                        Log.d(TAG, "WRITE_BULK_ENCODING_INVENTORY::SUCCESS::idx(INDITEX List) = " + idx +" ,newAccessPW = " + eItem.mAccessPW + " ,newEPC = " + epcToWrite);

                                        switch (mEncodingMode){
                                            case Constants.EncodeMode.MASS:
                                                mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                        eItem.mAccessPW, SDConsts.RFMemType.EPC, 2, (epcToWrite.length()/4), epcToWrite);
                                                break;
                                            case Constants.EncodeMode.PRIVATE:
                                                mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                        eItem.mAccessPW, SDConsts.RFMemType.RESERVED, 2, 3, (Constants.BB_PW + Constants.PRIVATE_SUFFIX_PW));
                                                break;
                                            case Constants.EncodeMode.REVIVE:
                                                mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                        Constants.BB_PW, SDConsts.RFMemType.RESERVED, 2, 3, (eItem.mAccessPW + Constants.REVIVE_SUFFIX_PW));
                                                break;
                                        }
                                    }
                                }
                           }
                        }
                        break;
                    case SDConsts.RFCmdMsg.WRITE_BULK_ENCODING_REPORT:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            try{
                                if (m.obj != null && m.obj instanceof String) {
                                    String strID = (String) m.obj;
                                    int seqID = Integer.parseInt(strID);
                                    CopyOnWriteArrayList<String> addedTagList = (CopyOnWriteArrayList<String>) mAdapter.getTagList();
                                    String targetPcEPC = addedTagList.get(seqID);
                                    String targetEPC = targetPcEPC.substring(4);

                                    String last8EPC = targetPcEPC.substring(targetPcEPC.length() - 8, targetPcEPC.length());
                                    int idx = mInditexTagList.indexOf(last8EPC);//To find matching EPC's index
                                    EPCItem eItem = mInditexTagINFOList.get(idx);//To find matching access pw
                                    String epcToWrite = generateNewEpc(targetEPC);

                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::SUCCESS::seqID = " + seqID + " ,targetEPC(seqID) = " + targetEPC + " ,last8EPC = " + last8EPC);
                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::SUCCESS::idx(INDITEX List) = " + idx +" ,newAccessPW = " + eItem.mAccessPW + " ,newEPC = " + epcToWrite);

                                    mSuccessCnt++;

                                    if(mSuccessCnt >= mInditexTagList.size()) {
                                        Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::SUCCESS::All counted,Stop Encoding");
                                        stopEncodingInventory();
                                    }
                                    updateSuccessText();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        } else {
                            try{
                                int errorCode = m.arg2;
                                if (m.obj != null && m.obj instanceof String) {
                                    String strID = (String) m.obj;
                                    int seqID = Integer.parseInt(strID);
                                    CopyOnWriteArrayList<String> addedTagList = (CopyOnWriteArrayList<String>) mAdapter.getTagList();
                                    String targetPcEPC = addedTagList.get(seqID);
                                    String targetEPC = targetPcEPC.substring(4);

                                    String last8EPC = targetPcEPC.substring(targetPcEPC.length() - 8, targetPcEPC.length());
                                    int idx = mInditexTagList.indexOf(last8EPC);//To find matching EPC's index
                                    EPCItem eItem = mInditexTagINFOList.get(idx);//To find matching access pw
                                    String epcToWrite = generateNewEpc(targetEPC);

                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::FAIL::errorCode = " + errorCode + " ,seqID = " + seqID + " ,targetEPC(seqID) = " + targetEPC + " ,last8EPC = " + last8EPC);
                                    Log.d(TAG, "WRITE_BULK_ENCODING_REPORT::FAIL::idx(INDITEX List idx) = " + idx +" ,newAccessPW = " + eItem.mAccessPW + " ,newEPC = " + epcToWrite);

                                    switch (mEncodingMode){
                                        case Constants.EncodeMode.MASS:
                                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                    eItem.mAccessPW, SDConsts.RFMemType.EPC, 2, (epcToWrite.length()/4), epcToWrite);
                                            break;
                                        case Constants.EncodeMode.PRIVATE:
                                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                    eItem.mAccessPW, SDConsts.RFMemType.RESERVED, 2, 3, (Constants.BB_PW + Constants.PRIVATE_SUFFIX_PW));
                                            break;
                                        case Constants.EncodeMode.REVIVE:
                                            mReader.RF_SetEncodeInformation(seqID, (targetEPC.length()/2), targetEPC,
                                                    eItem.mAccessPW, SDConsts.RFMemType.RESERVED, 2, 3, (Constants.BB_PW + Constants.REVIVE_SUFFIX_PW));
                                            break;
                                    }
                                }
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }
                        }
                        break;
                    //[20250402]Add Bulk encoding->

                    case SDConsts.RFCmdMsg.LOCATE:
                        if (m.arg2 == SDConsts.RFResult.SUCCESS) {
                            if (m.obj != null && m.obj instanceof Integer) {
                                processLocateData((int) m.obj);
                            }
                        }
                        break;
                }
                break;
        }
    }

    private void processLocateData(int data) {
        startLocateTimer();
        mLocateValue = data;
        //mTagLocateProgress.setProgress(data);
        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }

    private void processReadData(String data) {
        //updateCountText();
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String info = "";
        String epcDecode = "";
        String pha = "";
        String freq = "";

        String originalData = data;
        if (originalData.contains(";")) {
            if (D) Log.d(TAG, "full tag = " + data);
            //full tag example(with optional value)
            //1) RF_PerformInventory => "3000123456783333444455556666;rssi:-54.8"
            //2) RF_PerformInventoryWithLocating => "3000123456783333444455556666;loc:64"
            //3) RF_PerformInventoryWithEPCDecoder => "3000123456783333444455556666;rssi:-54.2;ed:(01)02432042280962(21) 3735552"
//            int infoTagPoint = data.indexOf(';');
//            info = data.substring(infoTagPoint, data.length());
//            int infoPoint = info.indexOf(':') + 1;
//            info = info.substring(infoPoint, info.length());
//            if (D) Log.d(TAG, "info tag = " + info);
//            data = data.substring(0, infoTagPoint);
//            if (D) Log.d(TAG, "data tag = " + data);
            data = "";
            String[] splitData = originalData.split(";");
            Activity activity = getActivity();
            String prefix = "";

            for (String dt : splitData) {
                if(dt.startsWith("rssi:")) {
                    int type = -1;
                    String[] splitInfo = dt.split(":");
                    for(String str : splitInfo) {
                        if(str.equals("rssi")) {
                            type = 0;
                        } else if(str.equals("pha")) {
                            type = 1;
                        } else if(str.equals("freq")) {
                            type = 2;
                        } else if(type != -1) {
                            switch (type) {
                                case 0: {
                                    if (activity != null)
                                        prefix = activity.getString(R.string.rssi_str);
                                    info = prefix + str.replace("rssi:", "");
                                    break;
                                }
                                case 1: {
                                    pha = "PHA : " + str;
                                    break;
                                }
                                case 2: {
                                    freq = "Freq : " + str;
                                    break;
                                }
                            }
                        }
                    }
                    if (D) Log.d(TAG, "rssi tag = " + info);
                } /*else if (dt.startsWith("pha:")) {
                    if (activity != null)
                        prefix = activity.getString(R.string.rssi_str);
                    pha = prefix + dt.replace("pha:", "");
                    if (D) Log.d(TAG, "rssi tag = " + info);
                } */else if (dt.startsWith("loc:")) {
                    if (activity != null)
                        prefix = activity.getString(R.string.loc_str);
                    info = prefix + dt.replace("loc:", "");
                    if (D) Log.d(TAG, "loc tag = " + info);
                } else if (dt.startsWith("epcdc:")) {
                    if (activity != null)
                        prefix = activity.getString(R.string.epc_decode_str);
                    epcDecode = prefix + dt.replace("epcdc:", "");
                    if (D) Log.d(TAG, "epc_decode tag = " + info);
                } else if (TextUtils.isEmpty(data)) {
                    data = dt;
                    if (D) Log.d(TAG, "data tag = " + data);
                }
            }
        }
        //<-[20251210]Add RSSI,TIMESTAMP
//        mAdapter.addItem(-1, data, info, pha, freq, epcDecode, !mIgnorePC, mTagFilter);
        mAdapter.addItem(-1, data, info, pha, freq, epcDecode, 0, mStopwatchSvc.getFormattedElapsedTime(), !mIgnorePC, mTagFilter);
        //[20251210]Add RSSI,TIMESTAMP->

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (mFileManager != null && mFile)
                mFileManager.writeToFile(data);
        } else {
            if (mUriWrite != null && mFile)
                mUriWrite.writeToFile(data);
        }


        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        if (!mInventory) {
            updateCountText();
            updateSpeedCountText();
            updateAvrSpeedCountText();
        }

        if (mSoundTask == null) {
            mSoundTask = new SoundTask();
            mSoundTask.execute();
        } else {
            if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSoundTask.cancel(true);
                mSoundTask = null;
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            }
        }
    }

    private void addCheckListener() {
        if (mTurboSwitch != null)
            mTurboSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mRssiSwitch != null)
            mRssiSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mFilterSwitch != null)
            mFilterSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mSoundSwitch != null)
            mSoundSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mMaskSwitch != null)
            mMaskSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mToggleSwitch != null)
            mToggleSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mPCSwitch != null)
            mPCSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mFileSwitch != null)
            mFileSwitch.setOnCheckedChangeListener(sledcheckListener);

        if (mSessionSpin != null)
            mSessionSpin.setOnItemSelectedListener(sessionListener);

        if (mSelFlagSpin != null)
            mSelFlagSpin.setOnItemSelectedListener(selFlagListener);
    }

    private void updateButtonState() {
        mPCSwitch.setChecked(mIgnorePC);

        mFileSwitch.setChecked(mFile);

        mFilterSwitch.setChecked(mTagFilter);

        mSoundSwitch.setChecked(mSoundPlay);

        mMaskSwitch.setChecked(mMask);

        mTurboSwitch.setChecked(mIsTurbo);


        if (mReader != null) {
            int toggle = mReader.RF_GetToggle();
            if (toggle == SDConsts.RFToggle.ON)
                mToggle = true;
            else
                mToggle = false;
            mToggleSwitch.setChecked(mToggle);

            int session = mReader.RF_GetSession();
            if (session != mSessionSpin.getSelectedItemPosition())
                mSessionSpin.setSelection(session);

            int flag = mReader.RF_GetSelectionFlag();
            if (flag != mSelFlagSpin.getSelectedItemPosition() + 1)
                mSelFlagSpin.setSelection(flag - 1);

            int rssi = mReader.RF_GetRssiTrackingState();
            if (rssi == SDConsts.RFRssi.ON)
                mRssi = true;
            else
                mRssi = false;
            mRssiSwitch.setChecked(mRssi);

            int battery = mReader.SD_GetBatteryStatus();
            if (!(battery < 0 || battery > 100))
                mBatteryText.setText("" + battery + "%");

        }
    }

    private void startLocateTimer() {
        stopLocateTimer();

        mLocateTimerTask = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                locateTimeout();
            }
        };
        mClearLocateTimer = new Timer();
        mClearLocateTimer.schedule(mLocateTimerTask, 500);
    }

    private void stopLocateTimer() {
        if (mClearLocateTimer != null) {
            mClearLocateTimer.cancel();
            mClearLocateTimer = null;
        }
    }

    private void locateTimeout() {
        mTagLocateProgress.setProgress(0);
    }

    //+RF_PerformInventoryCustom
    private void processReadDataCustom(String data) {
        updateCountText();
        StringBuilder tagSb = new StringBuilder();
        tagSb.setLength(0);
        String rssi = "";
        String customData = "";
        if (data.contains(";")) {
            if (D) Log.d(TAG, "processReadDataCustom::full tag = " + data);
            //full tag example = "3000123456783333444455556666;rssi:-54.8^custom=2348920384"
            int customdDataPoint = data.indexOf('^');
            customData = data.substring(customdDataPoint, data.length());
            int customPoint = customData.indexOf('=') + 1;
            customData = customData.substring(customPoint, customData.length());
            if (D) Log.d(TAG, "custom data = " + customData);
            data = data.substring(0, customdDataPoint);

            int rssiTagPoint = data.indexOf(';');
            rssi = data.substring(rssiTagPoint, data.length());
            int rssiPoint = rssi.indexOf(':') + 1;
            rssi = rssi.substring(rssiPoint, rssi.length());
            if (D) Log.d(TAG, "rssi tag = " + rssi);
            data = data.substring(0, rssiTagPoint);

            if (D) Log.d(TAG, "data tag = " + data);
            data = data + "\n" + customData;
        }
        if (rssi != "") {
            Activity activity = getActivity();
            if (activity != null)
                rssi = activity.getString(R.string.rssi_str) + rssi;
        }
        mAdapter.addItem(-1, data, rssi, false, /*mTagFilter*/false);

        if (mSoundPlay) {
            if (mSoundTask == null) {
                mSoundTask = new SoundTask();
                mSoundTask.execute();
            } else {
                if (mSoundTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mSoundTask.cancel(true);
                    mSoundTask = null;
                    mSoundTask = new SoundTask();
                    mSoundTask.execute();
                }
            }
        }
        mRfidList.setSelection(mRfidList.getAdapter().getCount() - 1);
        if (!mInventory) {
            updateCountText();
            updateSpeedCountText();
            updateAvrSpeedCountText();
        }
    }
    //RF_PerformInventoryCustom+

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (D) Log.d(TAG, "onRequestPermissionsResult");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            switch (requestCode) {
                case PermissionHelper.REQ_PERMISSION_CODE:
                    if (permissions != null) {
                        boolean hasResult = true;
                        for (int result : grantResults) {
                            if (result != PackageManager.PERMISSION_GRANTED) {
                                hasResult = false;
                                break;
                            }
                        }
                        if (hasResult) {
                            mFile = true;
                        }else {
                            mFile = false;
                        }
                    }
                    break;
            }
            mFileSwitch.setChecked(mFile);
        }else{
            mFile = true;
            mFileSwitch.setChecked(mFile);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_FILE_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(Environment.isExternalStorageManager()) {
                    mFile = true;
                } else {
                    Toast.makeText(mContext, getString(R.string.permission_not_allowed), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}