package co.kr.bluebird.rfid.app.bbrfiddemo.fileutil;

import co.kr.bluebird.rfid.app.bbrfiddemo.control.ListItem;

public class CsvTagFormatter {

    public static final String HEADER = "epc,rssi_or_info,phase,frequency,epc_decode,timestamp,duplicate_count,has_pc";

    private CsvTagFormatter() {
    }

    public static String format(ListItem item) {
        return escape(item.mUt) + ","
                + escape(item.mDt) + ","
                + escape(item.mPha) + ","
                + escape(item.mFrequency) + ","
                + escape(item.mEpcDecode) + ","
                + escape(item.mTimeStamp) + ","
                + item.mDupCount + ","
                + item.mHasPc;
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuote = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuote ? "\"" + escaped + "\"" : escaped;
    }
}
