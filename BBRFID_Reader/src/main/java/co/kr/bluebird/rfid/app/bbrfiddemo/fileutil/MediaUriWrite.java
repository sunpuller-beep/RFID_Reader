package co.kr.bluebird.rfid.app.bbrfiddemo.fileutil;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.rfid.app.bbrfiddemo.control.ListItem;
import co.kr.bluebird.rfid.app.bbrfiddemo.control.TagListAdapter;

public class MediaUriWrite {
    private static final String TAG = MediaUriWrite.class.getSimpleName();
    private static final boolean D = Constants.SD_D;

    private static final String mLogFileName = "/Inventory-";
    private static final String mExtentionName = ".csv";

    private Context mContext;
    private CopyOnWriteArrayList<String> mTagList;
    private ParcelFileDescriptor file;

    private boolean mDoLoop;
    private boolean forceStop;
    private Thread mWriteThread;

    private ContentValues values;
    private ContentResolver contentResolver;
    private Uri item;

    public MediaUriWrite(Context ctx) {
        mContext = ctx;
    }

    //+20231011 change save routine
    public void saveFile(TagListAdapter adapter) {
        closeFile();

        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = mLogFileName + date + mExtentionName;

        values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "text/csv");
        // 파일을 write중이라면 다른곳에서 데이터요구를 무시하겠다는 의미입니다.
        //values.put(MediaStore.Images.Media.IS_PENDING, 1);

        contentResolver = mContext.getContentResolver();
        Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        item = contentResolver.insert(collection, values);

        Log.d(TAG, "file data path: " + item.getPath());

        try {
            // Uri(item)의 위치에 파일을 생성해준다.
            file = contentResolver.openFileDescriptor(item, "w", null);
            if (file != null) {
                mTagList = new CopyOnWriteArrayList<>();
                mDoLoop = true;
                forceStop = false;
                mWriteThread = new Thread() {
                    @Override
                    public void run() {
                        if (adapter.getTotalCount() > 0) {
                            for (int i = 0; i < adapter.getTotalCount(); i++) {
                                try {
                                    ListItem item = (ListItem) adapter.getItem(i);
                                    String tag = item.mUt + " " + item.mDt + " " + item.mTimeStamp;//[20251210]Add RSSI,TIMESTAMP
                                    Log.d(TAG, "TotalCount = " + adapter.getTotalCount() + "tag = " + tag + " ,item.mDupCount = " + item.mDupCount);
                                    InputStream inputStream = getInputStream(tag + ",");
                                    byte[] strToByte = getBytes(inputStream);
                                    FileOutputStream fos = new FileOutputStream(file.getFileDescriptor());
                                    fos.write(strToByte);
                                    fos.close();
                                    inputStream.close();
                                } catch (Exception e) {
                                    Log.e(TAG, "Exception");
                                    forceStop = true;
                                    break;
                                }
                            }
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "InterruptedException");
                        }
                    }
                };
                mWriteThread.setName("FileWriteThread");
                mWriteThread.start();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //20231011 change save routine+


    public void openFile() {
        closeFile();

        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = mLogFileName + date + mExtentionName;

        values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "text/csv");
        // 파일을 write중이라면 다른곳에서 데이터요구를 무시하겠다는 의미입니다.
        //values.put(MediaStore.Images.Media.IS_PENDING, 1);

        contentResolver = mContext.getContentResolver();
        Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        item = contentResolver.insert(collection, values);

        Log.d(TAG, "file data path: " + item.getPath());

        try {
            // Uri(item)의 위치에 파일을 생성해준다.
            file = contentResolver.openFileDescriptor(item, "w", null);
            if (file != null) {
                mTagList = new CopyOnWriteArrayList<>();
                mDoLoop = true;
                forceStop = false;
                mWriteThread = new Thread() {
                    @Override
                    public void run() {
                        while (!forceStop && (mDoLoop || (mTagList != null && mTagList.size() != 0))) {
                            if (mTagList.size() > 0) {
                                for (String tag : mTagList) {
                                    try {
                                        Log.d(TAG, "file data write: " + tag + ",");
                                        InputStream inputStream = getInputStream(tag + ",");
                                        byte[] strToByte = getBytes(inputStream);
                                        FileOutputStream fos = new FileOutputStream(file.getFileDescriptor());
                                        fos.write(strToByte);
                                        fos.close();
                                        inputStream.close();
                                        mTagList.remove(tag);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Exception");
                                        forceStop = true;
                                        break;
                                    }
                                }
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
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String msg) {
        if (msg != null) {           
            mTagList.add(msg);
        }
    }

    private InputStream getInputStream(String mData) {
        ByteArrayOutputStream bOutSt = new ByteArrayOutputStream();
        try {
            bOutSt.write(mData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] data = bOutSt.toByteArray();
        ByteArrayInputStream bInputSt = new ByteArrayInputStream(data);
        return bInputSt;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void closeFile() {
        mDoLoop = false;

        try {
            if (file != null) {
                file.close();
                file = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        clearTagList();

        if (values != null) {
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            contentResolver.update(item, values, null, null);
        }
    }

    private void clearTagList() {
        if (mTagList != null) {
            mTagList.clear();
            mTagList = null;
        }
    }
}
