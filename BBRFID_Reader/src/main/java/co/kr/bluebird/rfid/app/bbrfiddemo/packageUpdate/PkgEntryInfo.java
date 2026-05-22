package co.kr.bluebird.rfid.app.bbrfiddemo.packageUpdate;

public class PkgEntryInfo {
    String fwTypeStr;
    int hwRevision;
    String fwVersionStr;
    long offset;
    long size;

    public PkgEntryInfo(String fwTypeStr, int hwRevision,
                        String fwVersionStr, long offset, long size) {
        this.fwTypeStr = fwTypeStr;
        this.hwRevision = hwRevision;
        this.fwVersionStr = fwVersionStr;
        this.offset = offset;
        this.size = size;
    }

    public String getFWType(){
        return this.fwTypeStr;
    }
    public int getHWRevision(){
        return this.hwRevision;
    }
    public String getFWVerion(){
        return this.fwVersionStr;
    }
    public long getFWStratOffset(){
        return this.offset;
    }
    public long getFWSize(){
        return this.size;
    }

    @Override
    public String toString() {
        return "PkgEntryInfo{" +
                "fwTypeStr ='" + getFWType() + '\'' +
                ", hwRevision ='" + getHWRevision() + '\'' +
                ", fwVersionStr ='" + getFWVerion() + '\'' +
                ", offset = '" + getFWStratOffset() + '\'' +
                "  size = " + getFWSize() +
                '}';
    }

}
