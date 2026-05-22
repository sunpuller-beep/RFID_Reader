package co.kr.bluebird.rfid.app.bbrfiddemo.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import co.kr.bluebird.rfid.app.bbrfiddemo.MainActivity;
import co.kr.bluebird.sled.Reader;
import co.kr.bluebird.sled.SDConsts;

public class BatteryPollingHandler extends Handler {
    private static final int MSG_POLL = 100;
    private final Handler mActivityHandler;
    private final Reader mReader;
    private long mInterval = 5000;
    private boolean mIsRunning = false;

    public BatteryPollingHandler(Looper looper, Handler activityHandler, Reader reader) {
        super(looper);
        this.mActivityHandler = activityHandler;
        this.mReader = reader;
    }

    public void start() {
        if (mIsRunning) return;
        mIsRunning = true;
        sendEmptyMessage(MSG_POLL);
    }

    public void stop() {
        mIsRunning = false;
        removeMessages(MSG_POLL);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.what == MSG_POLL && mIsRunning) {
            int result = mReader.SD_GetBatteryStatus();

            if (result != SDConsts.SDBatteryState.OTHER_CMD_RUNNING_ERROR) {
                Message reply = Message.obtain();
                reply.what = MainActivity.MSG_BATT_NOTI;
                reply.arg1 = -1;
                reply.arg2 = result;
                mActivityHandler.sendMessage(reply);
            }

            sendEmptyMessageDelayed(MSG_POLL, mInterval);
        }
    }
}
