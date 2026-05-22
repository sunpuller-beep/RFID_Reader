/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 *
 * http://www.bluebirdcorp.com/
 *
 * Author : Bogon Jun
 *
 * Date : 2016.04.07
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.fileutil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileSelectorDialog {

    private final String TAG = FileSelectorDialog.class.getSimpleName();

    private static final String PARENT_DIR = "..";

    private String[] mFileList;

    private File mCurrentPath;

    private ListenerList<FileSelectedListener> mFileListenerList = new ListenerList<>();

    private ListenerList<DirectorySelectedListener> mDirListenerList = new ListenerList<>();

    private Context mContext;

    private boolean mSelectDirectoryOption;

    private String mFileEndsWith;

    public interface FileSelectedListener {
        void fileSelected(File file);
    }

    public interface DirectorySelectedListener {
        void directorySelected(File directory);
    }

    public FileSelectorDialog(Context ctx, File path, String extendsionFilter) {
        mContext = ctx;
        setFileEndsWith(extendsionFilter);
        if (!path.exists())
            path = Environment.getExternalStorageDirectory();
        loadFileList(path);
    }

    public Dialog createFileDialog() {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle(mCurrentPath.getPath());
        if (mSelectDirectoryOption) {
            builder.setPositiveButton("Select directory", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    sendDirectorySelectedEvent(mCurrentPath);
                }
            });
        }

        builder.setItems(mFileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String fileChosen = mFileList[which];
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    loadFileList(chosenFile);
                    dialog.cancel();
                    dialog.dismiss();
                    showDialog();
                }
                else
                    sendFileSelectedEvent(chosenFile);
            }
        });
        dialog = builder.show();
        return dialog;
    }

    public void addFileListener(FileSelectedListener listener) {
        mFileListenerList.add(listener);
    }

    public void removeFileListener(FileSelectedListener listener) {
        mFileListenerList.remove(listener);
    }

    public void setSelectDirectoryOption(boolean selectDirectoryOption) {
        this.mSelectDirectoryOption = selectDirectoryOption;
    }

    public void addDirectoryListener(DirectorySelectedListener listener) {
        mDirListenerList.add(listener);
    }

    public void removeDirectoryListener(DirectorySelectedListener listener) {
        mDirListenerList.remove(listener);
    }

    public void showDialog() {
        createFileDialog().show();
    }

    private void sendFileSelectedEvent(final File file) {
        mFileListenerList.sendEvent(new ListenerList.sendHandler<FileSelectedListener>() {
            public void sendEvent(FileSelectedListener listener) {
                listener.fileSelected(file);
            }
        });
    }

    private void sendDirectorySelectedEvent(final File directory) {
        mDirListenerList.sendEvent(new ListenerList.sendHandler<DirectorySelectedListener>() {
            public void sendEvent(DirectorySelectedListener listener) {
                listener.directorySelected(directory);
            }
        });
    }

    private void loadFileList(File path) {
        this.mCurrentPath = path;
        ArrayList<String> r = new ArrayList<>();
        if (path.exists()) {
            if (path.getParentFile() != null)
                r.add(PARENT_DIR);
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    if (!sel.canRead())
                        return false;
                    if (mSelectDirectoryOption)
                        return sel.isDirectory();
                    else {
                        //Modify both bin and fmw files to be visible
                        return filename.endsWith("pkg") || filename.endsWith("bin") || filename.endsWith("fmw") || sel.isDirectory();//[20251217][Package update]Add pkg file type
                    }
                }
            };
            String[] fileList1 = path.list(filter);
            if (fileList1 != null) {
                for (String file : fileList1)
                    r.add(file);
            }
        }
        mFileList = (String[]) r.toArray(new String[]{});
    }


    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR))
            return mCurrentPath.getParentFile();
        else
            return new File(mCurrentPath, fileChosen);
    }

    private void setFileEndsWith(String fileEndsWith) {
        this.mFileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
    }

    static class ListenerList<T> {
        private ArrayList<T> listenerList = new ArrayList<>();

        public interface sendHandler<L> {
            void sendEvent(L listener);
        }

        public void add(T listener) {
            listenerList.add(listener);
        }

        public void sendEvent(sendHandler<T> sendHandler) {
            ArrayList<T> copy = new ArrayList<>(listenerList);
            for (T l : copy) {
                sendHandler.sendEvent(l);
            }
        }

        public void remove(T listener) {
            listenerList.remove(listener);
        }

        public ArrayList<T> getListenerList() {
            return listenerList;
        }
    }
}
