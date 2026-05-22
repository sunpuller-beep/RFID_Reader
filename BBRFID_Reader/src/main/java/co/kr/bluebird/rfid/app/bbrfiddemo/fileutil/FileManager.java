/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 *
 * Author : Bogon Jun
 *
 * Date : 2017.03.28
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fileutil;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import co.kr.bluebird.rfid.app.bbrfiddemo.control.ListItem;
import co.kr.bluebird.rfid.app.bbrfiddemo.control.TagListAdapter;


public class FileManager {

    private static final String TAG = FileManager.class.getSimpleName();

    private static final String mLogDir = "/BB/SLED";

    private static final String mLogFileName = "Inventory-";

    private static final String mLogFileName1 = "Inventory-";

    private static final String mExtentionName = ".csv";

    private static final int MINIMUM_SPACE_MB_SIZE = 100;

    private static final long SIZE_KB = 1024L;

    private static final long SIZE_MB = SIZE_KB * SIZE_KB;

    private File mDir;

    private File mFile;

    private PrintWriter mWriter;

    private Context mContext;

    private CopyOnWriteArrayList<String> mTagList;

    private Thread mWriteThread;

    private boolean mDoLoop;

    private boolean forceStop;

    private long mAvailableSpace;

    private StatFs mStatFs;

    FileOutputStream outFs;

    public FileManager(Context ctx) {
        mContext = ctx;
    }

    //+20231011 change save routine
    public boolean saveFile(TagListAdapter adapter) {
        closeFile();
        mStatFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        if (!isWriteAvailable()) {
            Toast.makeText(mContext, "File Write : Your storage is running out of space.\nNeed " +
                    "over than 100MB free space.", Toast.LENGTH_SHORT).show();
            return false;
        }
        makeDir();

        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        mFile = new File(mDir, mLogFileName + date + mExtentionName);

        if (readyToWrite()) {
            clearTagList();
            mTagList = new CopyOnWriteArrayList<>();
            mDoLoop = true;
            forceStop = false;
            mWriter.println(CsvTagFormatter.HEADER);
            mWriteThread = new Thread() {
                @Override
                public void run() {
                    if (adapter.getTotalCount() > 0) {
                        for (int i = 0; i < adapter.getTotalCount(); i++) {
                            ListItem item = (ListItem) adapter.getItem(i);
                            try {
                                mWriter.println(CsvTagFormatter.format(item));
                            } catch (java.lang.Exception e) {
                                Log.e(TAG, "Exception");
                                forceStop = true;
                                break;
                            }
                            if (!isWriteAvailable()) {
                                forceStop = true;
                                Log.e(TAG, "File Write : Your storage is running out of space.\nNeed " +
                                        "over than 100MB free space.");
                                break;
                            }
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "InterruptedException");
                            }
                        }
                    }
                    closeFile();
                }
            };
            mWriteThread.setName("FileWriteThread");
            mWriteThread.start();
            return true;
        }
        return false;
    }
    //20231011 change save routine+

    public boolean openFile() {
        closeFile();
        mStatFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        if (!isWriteAvailable()) {
            Toast.makeText(mContext, "File Write : Your storage is running out of space.\nNeed " +
                    "over than 100MB free space.", Toast.LENGTH_SHORT).show();
            return false;
        }
        makeDir();
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        mFile = new File(mDir, mLogFileName + date + mExtentionName);
        if (readyToWrite()) {
            clearTagList();
            mTagList = new CopyOnWriteArrayList<>();
            mDoLoop = true;
            forceStop = false;
            mWriter.println(CsvTagFormatter.HEADER);
            mWriteThread = new Thread() {
                @Override
                public void run() {
                    while (!forceStop && (mDoLoop || (mTagList != null && mTagList.size() != 0))) {
                        if (mTagList.size() > 0) {
                            for (String tag : mTagList) {
                                try {
                                    mWriter.println(CsvTagFormatter.escape(tag));
                                    mTagList.remove(tag);
                                } catch (java.lang.Exception e) {
                                    Log.e(TAG, "Exception");
                                    forceStop = true;
                                    break;
                                }
                            }
                        }
                        if (!isWriteAvailable()) {
                            forceStop = true;
                            Log.e(TAG, "File Write : Your storage is running out of space.\nNeed " +
                                    "over than 100MB free space.");
                            break;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "InterruptedException");
                        }
                    }
                }
            };
            mWriteThread.setName("FileWriteThread");
            mWriteThread.start();
            return true;
        }
        return false;
    }

    private void clearTagList() {
        if (mTagList != null) {
            mTagList.clear();
            mTagList = null;
        }
    }

    public void writeToFile(String msg) {
        if (mFile != null && mWriter != null) {
            mTagList.add(msg);
        }
    }

    public void closeFile() {
        mDoLoop = false;
        if (mWriter != null) {
            mWriter.flush();
            mWriter.close();
            mWriter = null;
        }
        if (mFile != null) {
            FileScanning scanning = new FileScanning(mContext, mFile);
            mFile = null;
        }
        clearTagList();
    }

    private void makeDir() {
        String storage = Environment.getExternalStorageDirectory().getPath();
        mDir = new File(storage + mLogDir);
        if (!mDir.exists())
            mDir.mkdirs();
    }

    private boolean readyToWrite() {
        try {
            mWriter = new PrintWriter(new BufferedWriter(new FileWriter(mFile, true), 1024), true);
        } catch (Exception e) {
            Log.e(TAG, "Exception = " + e.toString());
            return false;
        }
        return true;
    }

    private boolean isWriteAvailable() {
        if (getAvailableSpaceInMB() > MINIMUM_SPACE_MB_SIZE)
            return true;
        return false;
    }

    private long getAvailableSpaceInMB() {
        mAvailableSpace = mStatFs.getAvailableBlocksLong() * mStatFs.getBlockSizeLong();
        return mAvailableSpace / SIZE_MB;
    }
}
