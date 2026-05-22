package co.kr.bluebird.rfid.app.bbrfiddemo.packageUpdate;

import android.os.WorkSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PkgInfo {
    private String model;
    private long headerVersion;
    private long entryCount;
    List<PkgEntryInfo> entries = new ArrayList<>();

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setHeaderVersion(long headerVersion) {
        this.headerVersion = headerVersion;
    }

    public long getHeaderVersion() {
        return headerVersion;
    }

    public void setEntryCount(long entryCount) {
        this.entryCount = entryCount;
    }

    public long getEntryCount() {
        return entryCount;
    }

    public List<PkgEntryInfo>  getEntries() {
        return entries;
    }
}
