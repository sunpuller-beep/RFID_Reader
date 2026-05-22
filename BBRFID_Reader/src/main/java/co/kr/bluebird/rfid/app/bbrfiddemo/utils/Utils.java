package co.kr.bluebird.rfid.app.bbrfiddemo.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import co.kr.bluebird.rfid.app.bbrfiddemo.R;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    //+smart batter -critical temper
    public static void createAlertDialog(Context ctx, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(ctx.getString(R.string.smart_critical_temper_title));
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton(ctx.getString(R.string.drawer_close), null);
        builder.show();
    }
    //smart batter -critical temper+

    //<-[20260316] barcode hw key
    private static final String PREF_NAME = "BcHwKeyPrefs";
    private static final String KEY_SELECTION = "last_selection";

    public static void saveSelection(Context context, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_SELECTION, value).apply();
    }

    public static int getSelection(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SELECTION, 3); // default value = 3
    }
    //[20260316] barcode hw key->
}
