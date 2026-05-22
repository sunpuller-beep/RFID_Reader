/*
 * Field scan workflow for collecting EPC, TID, and USER memory.
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;
import co.kr.bluebird.sled.SelectionCriterias;

public class ScanFragment extends Fragment {

    private static final int TID_START_WORD = 0;
    private static final int TID_WORD_LENGTH = 6;

    private static final int PHASE_NONE = 0;
    private static final int PHASE_TID = 1;
    private static final int PHASE_USER = 2;
    private static final int DEFAULT_USER_START_WORD = 0;
    private static final int DEFAULT_USER_WORD_LENGTH = 16;
    private static final String DEFAULT_ACCESS_PASSWORD = "00000000";
    private static final long READ_TIMEOUT_MS = 5000L;

    private Context mContext;
    private Reader mReader;
    private Handler mOptionHandler;

    private TextView mStatusText;
    private TextView mFoundCountText;
    private TextView mReadCountText;
    private TextView mFailedCountText;
    private TextView mFilePathText;
    private TextView mCurrentPlaceText;
    private EditText mPlaceNameEdit;
    private EditText mUserStartEdit;
    private EditText mUserLengthEdit;
    private EditText mAccessPasswordEdit;
    private CheckBox mReadMemoryCheck;
    private Button mSetPlaceButton;
    private Button mStartButton;
    private Button mStopButton;
    private Button mReadButton;
    private Button mSaveButton;
    private Button mClearButton;
    private ArrayAdapter<String> mAdapter;

    private boolean mInventoryRunning;
    private boolean mReadingMemory;
    private int mReadPhase = PHASE_NONE;
    private int mReadIndex;
    private int mReadRequestToken;
    private String mPlaceName = "";
    private ScanTag mCurrentTag;
    private ScanReadConfig mActiveReadConfig;

    private final LinkedHashMap<String, ScanTag> mTags = new LinkedHashMap<>();
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final Handler mUiHandler = new Handler();
    private Runnable mReadTimeoutRunnable;

    public static ScanFragment newInstance() {
        return new ScanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.scan_frag, container, false);
        mContext = inflater.getContext();
        mOptionHandler = ((MainActivity) getActivity()).mUpdateConnectHandler;

        mStatusText = (TextView) v.findViewById(R.id.scan_status_text);
        mFoundCountText = (TextView) v.findViewById(R.id.found_count_text);
        mReadCountText = (TextView) v.findViewById(R.id.read_count_text);
        mFailedCountText = (TextView) v.findViewById(R.id.failed_count_text);
        mFilePathText = (TextView) v.findViewById(R.id.file_path_text);
        mCurrentPlaceText = (TextView) v.findViewById(R.id.current_place_text);
        mPlaceNameEdit = (EditText) v.findViewById(R.id.place_name_edit);
        mUserStartEdit = (EditText) v.findViewById(R.id.user_start_edit);
        mUserLengthEdit = (EditText) v.findViewById(R.id.user_length_edit);
        mAccessPasswordEdit = (EditText) v.findViewById(R.id.access_password_edit);
        mReadMemoryCheck = (CheckBox) v.findViewById(R.id.read_memory_check);

        mSetPlaceButton = (Button) v.findViewById(R.id.set_place_button);
        mStartButton = (Button) v.findViewById(R.id.start_scan_button);
        mStopButton = (Button) v.findViewById(R.id.stop_scan_button);
        mReadButton = (Button) v.findViewById(R.id.read_data_button);
        mSaveButton = (Button) v.findViewById(R.id.save_scan_button);
        mClearButton = (Button) v.findViewById(R.id.clear_scan_button);

        mAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, new ArrayList<String>());
        ListView listView = (ListView) v.findViewById(R.id.scan_tag_list);
        listView.setAdapter(mAdapter);

        mSetPlaceButton.setOnClickListener(mClickListener);
        mStartButton.setOnClickListener(mClickListener);
        mStopButton.setOnClickListener(mClickListener);
        mReadButton.setOnClickListener(mClickListener);
        mSaveButton.setOnClickListener(mClickListener);
        mClearButton.setOnClickListener(mClickListener);

        updateUi("Ready to collect tags");
        updatePlaceLabel();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mReader = Reader.getReader(mContext, mScanHandler);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onStop() {
        stopInventory();
        super.onStop();
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.set_place_button) {
                clearInputFocus();
                setPlaceName();
            } else if (id == R.id.start_scan_button) {
                clearInputFocus();
                startInventory();
            } else if (id == R.id.stop_scan_button) {
                stopInventory();
            } else if (id == R.id.read_data_button) {
                startMemoryRead();
            } else if (id == R.id.save_scan_button) {
                saveCsv();
            } else if (id == R.id.clear_scan_button) {
                clearScan();
            }
        }
    };

    private void startInventory() {
        updatePlaceNameFromInput();
        if (!isReaderConnected()) {
            Toast.makeText(mContext, "Reader is not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mReadingMemory) {
            Toast.makeText(mContext, "Reading data", Toast.LENGTH_SHORT).show();
            return;
        }
        int ret = mReader.RF_PerformInventory(true, false, false);
        if (ret == SDConsts.RFResult.SUCCESS) {
            mInventoryRunning = true;
            updateUi("Collecting tags in range");
            Toast.makeText(mContext, "Scan started. Walk the target area, then stop scan.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Start scan failed: " + ret, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopInventory() {
        if (mReader != null && mInventoryRunning) {
            mReader.RF_StopInventory();
        }
        boolean wasRunning = mInventoryRunning;
        mInventoryRunning = false;
        if (!mReadingMemory) {
            updateUi("Tag collection stopped");
        }
        if (wasRunning && !mReadingMemory) {
            Toast.makeText(mContext, "Scan stopped. " + mTags.size() + " tags collected. Now read tag data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearScan() {
        if (mInventoryRunning || mReadingMemory) {
            Toast.makeText(mContext, "Stop current work first", Toast.LENGTH_SHORT).show();
            return;
        }
        int clearedCount = mTags.size();
        mTags.clear();
        mPlaceName = "";
        mPlaceNameEdit.setText("");
        updatePlaceLabel();
        mFilePathText.setText("");
        updateUi("Ready to collect tags");
        Toast.makeText(mContext, "Cleared " + clearedCount + " tags", Toast.LENGTH_SHORT).show();
    }

    private void setPlaceName() {
        String placeName = readPlaceNameInput();
        if (placeName.length() == 0) {
            mPlaceName = "";
            mPlaceNameEdit.setText("");
            updatePlaceLabel();
            Toast.makeText(mContext, "Place cleared. Default file name will be used.", Toast.LENGTH_SHORT).show();
        } else {
            mPlaceName = placeName;
            mPlaceNameEdit.setText("");
            updatePlaceLabel();
            Toast.makeText(mContext, "Place set: " + mPlaceName, Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePlaceNameFromInput() {
        String placeName = readPlaceNameInput();
        if (placeName.length() > 0) {
            mPlaceName = placeName;
        }
        updatePlaceLabel();
    }

    private String readPlaceNameInput() {
        if (mPlaceNameEdit == null) {
            return "";
        }
        return mPlaceNameEdit.getText().toString().trim();
    }

    private void clearInputFocus() {
        if (mPlaceNameEdit != null) {
            mPlaceNameEdit.clearFocus();
        }
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        View focusedView = activity.getCurrentFocus();
        if (focusedView == null) {
            focusedView = mPlaceNameEdit;
        }
        if (focusedView == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void updatePlaceLabel() {
        if (mCurrentPlaceText == null) {
            return;
        }
        if (mPlaceName == null || mPlaceName.length() == 0) {
            mCurrentPlaceText.setText("Place: Not set");
        } else {
            mCurrentPlaceText.setText("Place: " + mPlaceName);
        }
    }

    private void startMemoryRead() {
        if (!isReaderConnected()) {
            Toast.makeText(mContext, "Reader is not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mReadMemoryCheck.isChecked()) {
            Toast.makeText(mContext, "Enable TID/USER memory read first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mTags.isEmpty()) {
            Toast.makeText(mContext, "No tags found", Toast.LENGTH_SHORT).show();
            return;
        }
        stopInventory();
        mActiveReadConfig = getReadConfig();
        if (mActiveReadConfig == null) {
            return;
        }
        mReadingMemory = true;
        mReadIndex = 0;
        for (ScanTag tag : mTags.values()) {
            tag.status = "PENDING";
            tag.error = "";
            tag.tid = "";
            tag.user = "";
        }
        updateUi("Reading stored data for collected tags");
        Toast.makeText(mContext, "Reading TID and USER for " + mTags.size() + " collected tags", Toast.LENGTH_SHORT).show();
        readNextTag();
    }

    private void readNextTag() {
        if (mReadIndex >= mTags.size()) {
            mReadingMemory = false;
            mReadPhase = PHASE_NONE;
            mCurrentTag = null;
            mActiveReadConfig = null;
            cancelReadTimeout();
            if (mReader != null) {
                mReader.RF_RemoveSelection();
            }
            updateUi("Read complete for collected tags");
            Toast.makeText(mContext, buildReadSummaryMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        mCurrentTag = new ArrayList<>(mTags.values()).get(mReadIndex);
        mReadIndex++;
        mCurrentTag.status = "READING_TID";
        mCurrentTag.error = "";
        updateUi("Reading collected tag " + mReadIndex + " / " + mTags.size());

        if (!selectEpc(mCurrentTag.epc)) {
            markCurrentFailed("SELECTION_FAILED");
            readNextTag();
            return;
        }
        mReadPhase = PHASE_TID;
        int ret = mReader.RF_READ(SDConsts.RFMemType.TID, TID_START_WORD, TID_WORD_LENGTH, mActiveReadConfig.accessPassword, true);
        if (ret != SDConsts.RFResult.SUCCESS) {
            markCurrentFailed("TID_READ_START_FAILED:" + ret);
            readNextTag();
        } else {
            scheduleReadTimeout(mReadRequestToken + 1, "TID_READ_TIMEOUT");
        }
    }

    private boolean selectEpc(String epc) {
        if (mReader == null || epc == null || epc.length() == 0) {
            return false;
        }
        SelectionCriterias criteria = new SelectionCriterias();
        int ret = criteria.makeCriteria(SelectionCriterias.SCMemType.EPC, epc, 32,
                epc.length() * 4, SelectionCriterias.SCActionType.ASLINVA_DSLINVB);
        if (ret != SDConsts.RFResult.SUCCESS) {
            return false;
        }
        mReader.RF_RemoveSelection();
        mReader.RF_SetSelection(criteria);
        return true;
    }

    private void handleReadResult(int result, String data) {
        if (mCurrentTag == null) {
            return;
        }
        cancelReadTimeout();
        if (mActiveReadConfig == null) {
            mReadingMemory = false;
            return;
        }
        if (result != SDConsts.RFResult.SUCCESS) {
            String bank = mReadPhase == PHASE_TID ? "TID" : "USER";
            markCurrentFailed(bank + "_READ_FAILED:" + result);
            readNextTag();
            return;
        }

        if (mReadPhase == PHASE_TID) {
            mCurrentTag.tid = data;
            mCurrentTag.status = "READING_USER";
            updateUi("Reading USER data " + mReadIndex + " / " + mTags.size());
            mReadPhase = PHASE_USER;
            int ret = mReader.RF_READ(SDConsts.RFMemType.USER, mActiveReadConfig.userStartWord, mActiveReadConfig.userWordLength, mActiveReadConfig.accessPassword, true);
            if (ret != SDConsts.RFResult.SUCCESS) {
                markCurrentFailed("USER_READ_START_FAILED:" + ret);
                readNextTag();
            } else {
                scheduleReadTimeout(mReadRequestToken + 1, "USER_READ_TIMEOUT");
            }
        } else if (mReadPhase == PHASE_USER) {
            mCurrentTag.user = data;
            mCurrentTag.status = "OK";
            mCurrentTag.error = "";
            updateUi("Reading " + mReadIndex + " / " + mTags.size());
            readNextTag();
        }
    }

    private ScanReadConfig getReadConfig() {
        String userStartStr = mUserStartEdit.getText().toString().trim();
        String userLengthStr = mUserLengthEdit.getText().toString().trim();
        String accessPassword = mAccessPasswordEdit.getText().toString().trim();

        int userStartWord = parsePositiveInt(userStartStr, DEFAULT_USER_START_WORD);
        int userWordLength = parsePositiveInt(userLengthStr, DEFAULT_USER_WORD_LENGTH);

        if (userStartWord < 0) {
            Toast.makeText(mContext, "USER start must be 0 or greater", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (userWordLength <= 0) {
            Toast.makeText(mContext, "USER length must be greater than 0", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (accessPassword.length() == 0) {
            accessPassword = DEFAULT_ACCESS_PASSWORD;
            mAccessPasswordEdit.setText(accessPassword);
        }
        return new ScanReadConfig(userStartWord, userWordLength, accessPassword);
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (value.length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void markCurrentFailed(String error) {
        if (mCurrentTag != null) {
            mCurrentTag.status = "FAILED";
            mCurrentTag.error = error;
        }
    }

    private void scheduleReadTimeout(int token, final String error) {
        cancelReadTimeout();
        mReadRequestToken = token;
        mReadTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mReadingMemory || mCurrentTag == null || token != mReadRequestToken) {
                    return;
                }
                markCurrentFailed(error);
                updateUi("Read timeout. Moving to next tag");
                readNextTag();
            }
        };
        mUiHandler.postDelayed(mReadTimeoutRunnable, READ_TIMEOUT_MS);
    }

    private void cancelReadTimeout() {
        if (mReadTimeoutRunnable != null) {
            mUiHandler.removeCallbacks(mReadTimeoutRunnable);
            mReadTimeoutRunnable = null;
        }
    }

    private void saveCsv() {
        if (mTags.isEmpty()) {
            Toast.makeText(mContext, "No data to save", Toast.LENGTH_SHORT).show();
            return;
        }
        updatePlaceNameFromInput();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = buildCsvFileName(timestamp);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveCsvWithMediaStore(fileName);
            } else {
                saveCsvLegacy(fileName);
            }
            Toast.makeText(mContext, buildSaveSummaryMessage(fileName), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(mContext, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCsvLegacy(String fileName) throws IOException {
        File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (documentsDir == null) {
            throw new IOException("Documents storage is not available");
        }
        File dir = new File(documentsDir, "RFID-Reader");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create Documents/RFID-Reader");
        }
        File file = new File(dir, fileName);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(buildCsvContent());
            writer.close();
            mFilePathText.setText("Saved " + mTags.size() + " tags\n" + file.getAbsolutePath());
        } catch (IOException e) {
            throw e;
        }
    }

    private void saveCsvWithMediaStore(String fileName) throws IOException {
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/RFID-Reader");
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values);
        if (uri == null) {
            throw new IOException("Cannot create CSV file");
        }

        OutputStream outputStream = resolver.openOutputStream(uri);
        if (outputStream == null) {
            throw new IOException("Cannot open CSV file");
        }
        outputStream.write(buildCsvContent().getBytes("UTF-8"));
        outputStream.close();

        values.clear();
        values.put(MediaStore.MediaColumns.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
        mFilePathText.setText("Saved " + mTags.size() + " tags\nDocuments/RFID-Reader/" + fileName);
    }

    private String buildCsvContent() {
        StringBuilder builder = new StringBuilder();
        String scannedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        builder.append("place,epc,epc_valid,tid,user,status,error,scanned_at\n");
        for (ScanTag tag : mTags.values()) {
            builder.append(csv(mPlaceName));
            builder.append(",");
            builder.append(csv(tag.epc));
            builder.append(",");
            builder.append(csv(tag.isValidKedufineEpc() ? "Y" : "N"));
            builder.append(",");
            builder.append(csv(tag.tid));
            builder.append(",");
            builder.append(csv(tag.user));
            builder.append(",");
            builder.append(csv(tag.status));
            builder.append(",");
            builder.append(csv(tag.error));
            builder.append(",");
            builder.append(csv(scannedAt));
            builder.append("\n");
        }
        return builder.toString();
    }

    private String buildCsvFileName(String timestamp) {
        String safePlaceName = sanitizeFileNamePrefix(mPlaceName);
        if (safePlaceName.length() == 0) {
            return "rfid_scan_" + timestamp + ".csv";
        }
        return safePlaceName + "_rfid_scan_" + timestamp + ".csv";
    }

    private String sanitizeFileNamePrefix(String value) {
        if (value == null) {
            return "";
        }
        String safe = value.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        safe = safe.replaceAll("\\s+", "_");
        safe = safe.replaceAll("_+", "_");
        safe = safe.replaceAll("^_+|_+$", "");
        return safe;
    }

    private String csv(String value) {
        if (value == null) {
            value = "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private boolean isReaderConnected() {
        return mReader != null && mReader.SD_GetConnectState() == SDConsts.SDConnectState.CONNECTED;
    }

    private void processInventoryData(String data) {
        String epc = parseEpc(data);
        if (epc.length() == 0) {
            return;
        }
        if (!mTags.containsKey(epc)) {
            mTags.put(epc, new ScanTag(epc));
            updateUi("Collecting tags in range");
        }
    }

    private String parseEpc(String data) {
        if (data == null) {
            return "";
        }
        int idx = data.indexOf(';');
        if (idx >= 0) {
            data = data.substring(0, idx);
        }
        return data.trim();
    }

    private void updateUi(String status) {
        if (mStatusText != null) {
            mStatusText.setText(status);
        }
        int read = getReadSuccessCount();
        int failed = getReadFailedCount();
        ArrayList<String> rows = new ArrayList<>();
        for (ScanTag tag : mTags.values()) {
            rows.add(tag.toDisplayText());
        }
        setCounterText(mFoundCountText, "Found", mTags.size());
        setCounterText(mReadCountText, "Read", read);
        setCounterText(mFailedCountText, "Failed", failed);

        mAdapter.clear();
        mAdapter.addAll(rows);
        mAdapter.notifyDataSetChanged();
    }

    private void setCounterText(TextView textView, String label, int value) {
        String text = label + ": " + value;
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }

    private int getReadSuccessCount() {
        int read = 0;
        for (ScanTag tag : mTags.values()) {
            if ("OK".equals(tag.status)) {
                read++;
            }
        }
        return read;
    }

    private int getReadFailedCount() {
        int failed = 0;
        for (ScanTag tag : mTags.values()) {
            if ("FAILED".equals(tag.status)) {
                failed++;
            }
        }
        return failed;
    }

    private String buildReadSummaryMessage() {
        return "Read complete for " + mTags.size() + " collected tags. Success " + getReadSuccessCount() + ", failed " + getReadFailedCount();
    }

    private String buildSaveSummaryMessage(String fileName) {
        return "Saved " + mTags.size() + " tags to " + fileName + " (success " + getReadSuccessCount() + ", failed " + getReadFailedCount() + ")";
    }

    private static class ScanHandler extends Handler {
        private final WeakReference<ScanFragment> mExecutor;

        ScanHandler(ScanFragment fragment) {
            mExecutor = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            ScanFragment fragment = mExecutor.get();
            if (fragment != null) {
                fragment.handleReaderMessage(msg);
            }
        }
    }

    private void handleReaderMessage(Message msg) {
        switch (msg.what) {
            case SDConsts.Msg.RFMsg:
                if (msg.arg1 == SDConsts.RFCmdMsg.INVENTORY && msg.arg2 == SDConsts.RFResult.SUCCESS) {
                    if (msg.obj instanceof String) {
                        processInventoryData((String) msg.obj);
                    }
                } else if (msg.arg1 == SDConsts.RFCmdMsg.READ) {
                    String data = msg.obj instanceof String ? (String) msg.obj : "";
                    handleReadResult(msg.arg2, data);
                }
                break;
            case SDConsts.Msg.SDMsg:
                if (msg.arg1 == SDConsts.SDCmdMsg.TRIGGER_PRESSED) {
                    startInventory();
                } else if (msg.arg1 == SDConsts.SDCmdMsg.TRIGGER_RELEASED) {
                    stopInventory();
                } else if (msg.arg1 == SDConsts.SDCmdMsg.SLED_INVENTORY_STATE_CHANGED) {
                    mInventoryRunning = false;
                    if (!mReadingMemory) {
                        updateUi("Tag collection stopped");
                    }
                } else if (msg.arg1 == SDConsts.SDCmdMsg.SLED_UNKNOWN_DISCONNECTED) {
                    mInventoryRunning = false;
                    mReadingMemory = false;
                    if (mOptionHandler != null) {
                        mOptionHandler.obtainMessage(MainActivity.MSG_OPTION_DISCONNECTED).sendToTarget();
                    }
                    updateUi("Disconnected");
                } else if (msg.arg1 == SDConsts.SDCmdMsg.SLED_BATTERY_STATE_CHANGED) {
                    if (mOptionHandler != null) {
                        mOptionHandler.obtainMessage(MainActivity.MSG_BATT_NOTI, msg.arg1, msg.arg2).sendToTarget();
                    }
                }
                break;
        }
    }

    private static class ScanTag {
        final String epc;
        String tid = "";
        String user = "";
        String status = "DISCOVERED";
        String error = "";

        ScanTag(String epc) {
            this.epc = epc;
        }

        String toDisplayText() {
            StringBuilder sb = new StringBuilder();
            sb.append(epc);
            if (!isValidKedufineEpc()) {
                sb.append(" (non-96bit EPC)");
            }
            sb.append("\nTID: ");
            sb.append(tid.length() == 0 ? "-" : tid);
            sb.append("\nUSER: ");
            sb.append(user.length() == 0 ? "-" : user);
            sb.append("\n");
            sb.append(status);
            if (error.length() > 0) {
                sb.append(" / ");
                sb.append(error);
            }
            return sb.toString();
        }

        boolean isValidKedufineEpc() {
            return epc != null && epc.matches("(?i)[0-9a-f]{24}");
        }
    }

    private static class ScanReadConfig {
        final int userStartWord;
        final int userWordLength;
        final String accessPassword;

        ScanReadConfig(int userStartWord, int userWordLength, String accessPassword) {
            this.userStartWord = userStartWord;
            this.userWordLength = userWordLength;
            this.accessPassword = accessPassword;
        }
    }
}
