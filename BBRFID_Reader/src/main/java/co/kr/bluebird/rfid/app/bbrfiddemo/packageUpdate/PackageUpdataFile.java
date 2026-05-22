package co.kr.bluebird.rfid.app.bbrfiddemo.packageUpdate;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public class PackageUpdataFile {
    private static final String TAG = PackageUpdataFile.class.getSimpleName();
    PkgInfo loadedPkgInfo;
    public PackageUpdataFile(){};

    public static String parseMCUVersion(String mcuStr){
        String last8Digits = mcuStr.replaceAll(".*?(\\d{8})$", "$1");
        Log.d(TAG, "parseMCUVersion::last8Digits = " + last8Digits);
        return last8Digits;
    }
    public static String parseRfidVersion(String raw) {

        if (raw == null || raw.trim().isEmpty()) {
            return "";
        }

        raw = raw.trim();

        // 1) dot(.) 없는 경우 → 있는 그대로 반환
        if (!raw.contains(".")) {
            return raw;
        }

        // 2) dot(.)이 2개인데 완전한 "X.X.X" 형태
        String[] parts = raw.split("\\.");

        if (parts.length == 3) {
            // case: 1.2.0 → 그대로
            boolean seg1 = isInteger(parts[0]);
            boolean seg2 = isInteger(parts[1]);
            boolean seg3 = isInteger(parts[2]);

            // 마지막 segment가 3자리 이하이면 그대로 유지
            if (seg1 && seg2 && seg3 && parts[2].length() <= 3) {
                return raw;
            }
        }

        // 3) XX.X.YYMMDD 형태
        String last = parts[parts.length - 1];

        // YYMMDD 패턴
        if (Pattern.matches("\\d{6}", last)) {

            String yy = last.substring(0, 2);
            String mm = last.substring(2, 4);
            String dd = last.substring(4, 6);

            // 20YYMMDD 형태로 변환
            return "20" + yy + mm + dd;
        }

        // 그 외는 원문 그대로
        return raw;
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public PkgInfo parserPackageUpdataFile(Context context, Uri uri, int devHWRevision, String devMCUVer, String devRFIDVer){
        try{
            InputStream is = context.getContentResolver().openInputStream(uri);
            ReadableByteChannel channel = Channels.newChannel(is);
            ByteBuffer buffer = ByteBuffer.allocate(is.available());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            channel.read(buffer);
            buffer.flip();

            //#2
//            FileInputStream fis = new FileInputStream(uri.getPath());
//            FileChannel channel = fis.getChannel();
//            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
//            buffer.order(ByteOrder.LITTLE_ENDIAN);
//            channel.read(buffer);
//            buffer.flip();

            // HEADER
            String magic = readFixedAscii(buffer, 8);
            long headerVer = Integer.toUnsignedLong(buffer.getInt());
            long entryCount = Integer.toUnsignedLong(buffer.getInt());
            String model = readFixedAscii(buffer, 8);

            loadedPkgInfo = new PkgInfo();
            loadedPkgInfo.setModel(model);
            loadedPkgInfo.setHeaderVersion(headerVer);
            loadedPkgInfo.setEntryCount(entryCount);

            Log.d(TAG, "===== PKG HEADER =====");
            Log.d(TAG, "Magic       : " + magic);
            Log.d(TAG, "Model       : " + model);
            Log.d(TAG, "HeaderVer   : " + headerVer);
            Log.d(TAG, "Entry Count : " + entryCount);

            // reserved skip (4 * uint32)
            buffer.getInt();
            buffer.getInt();
            buffer.getInt();
            buffer.getInt();

            loadedPkgInfo.getEntries().clear();

            for (int i = 0; i < entryCount; i++) {
                short fwType = (short) (buffer.get() & 0xFF);
                short hwRev  = (short) (buffer.get() & 0xFF);
                int reserved = buffer.getShort() & 0xFFFF;

                String fwVerStr = readFixedAscii(buffer, 16);

                long offset = Integer.toUnsignedLong(buffer.getInt());
                long size   = Integer.toUnsignedLong(buffer.getInt());

                String fwTypeStr;
                if (fwType == 0) {
                    fwTypeStr = "MCU";
                } else if (fwType == 1) {
                    fwTypeStr = "RFID";
                } else {
                    fwTypeStr = "Unknown(" + fwType + ")";
                }

                if(hwRev == 0 && devHWRevision == 0){
                    if(fwTypeStr.equals("MCU")){
                        if(!fwVerStr.equals(parseMCUVersion(devMCUVer)))
                            loadedPkgInfo.getEntries().add(new PkgEntryInfo(fwTypeStr, hwRev, fwVerStr, offset, size));
                        else
                            Log.d(TAG, "MCU FW same verison");
                    }

                    if(fwTypeStr.equals("RFID")){
                        if(!fwVerStr.equals(parseRfidVersion(devRFIDVer)))
                            loadedPkgInfo.getEntries().add(new PkgEntryInfo(fwTypeStr, hwRev, fwVerStr, offset, size));
                        else
                            Log.d(TAG, "RFID FW same verison");
                    }
                }else if(hwRev == 1 && devHWRevision == 1){
                    if(fwTypeStr.equals("MCU")){
                        if(!fwVerStr.equals(parseMCUVersion(devMCUVer)))
                            loadedPkgInfo.getEntries().add(new PkgEntryInfo(fwTypeStr, hwRev, fwVerStr, offset, size));
                        else
                            Log.d(TAG, "MCU FW same verison");
                    }

                    if(fwTypeStr.equals("RFID")){
                        if(!fwVerStr.equals(parseRfidVersion(devRFIDVer)))
                            loadedPkgInfo.getEntries().add(new PkgEntryInfo(fwTypeStr, hwRev, fwVerStr, offset, size));
                        else
                            Log.d(TAG, "RFID FW same verison");
                    }
                }else if(hwRev == 2 && devHWRevision == 2){
                    if(fwTypeStr.equals("MCU")){
                        if(!fwVerStr.equals(parseMCUVersion(devMCUVer)))
                            loadedPkgInfo.getEntries().add(new PkgEntryInfo(fwTypeStr, hwRev, fwVerStr, offset, size));
                        else
                            Log.d(TAG, "MCU FW same verison");
                    }

                    if(fwTypeStr.equals("RFID")){
                        if(!fwVerStr.equals(parseRfidVersion(devRFIDVer)))
                            loadedPkgInfo.getEntries().add(new PkgEntryInfo(fwTypeStr, hwRev, fwVerStr, offset, size));
                        else
                            Log.d(TAG, "RFID FW same verison");
                    }
                }

                Log.d(TAG, "--- Entry " + i + " ---");
                Log.d(TAG, "FW Type     : " + fwTypeStr);
                Log.d(TAG, "HW Revision : " + hwRev);
                Log.d(TAG, "FW Ver Str  : " + fwVerStr);
                Log.d(TAG, "Offset      : " + offset);
                Log.d(TAG, "Size        : " + size);
            }
            return loadedPkgInfo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readFixedAscii(ByteBuffer buffer, int length) {
        byte[] bytes = new byte[length];
        buffer.get(bytes);

        int end = 0;
        while (end < bytes.length && bytes[end] != 0) {
            end++;
        }
        return new String(bytes, 0, end, StandardCharsets.US_ASCII).trim();
    }


}
