/*
 * Copyright (C) 2015 - 2017 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 * 
 * Author : Bogon Jun
 *
 * Date : 2016.03.03
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;
import co.kr.bluebird.sled.SDConsts;

public class RFIDReceiver extends BroadcastReceiver {
    private static final String TAG = RFIDReceiver.class.getSimpleName();
    
    private static final boolean D = Constants.RECV_D;
    
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // TODO Auto-generated method stub
        String action = arg1.getAction();
        if (SDConsts.ACTION_SLED_ATTACHED.equals(action)) {
            Toast.makeText(arg0, "SLED_ATTACHED", Toast.LENGTH_SHORT).show();
            if (D) Log.d(TAG, "SLED_ATTACHED");
        }
        else if (SDConsts.ACTION_SLED_DETACHED.equals(action)) {
            Toast.makeText(arg0, "SLED_DETACHED", Toast.LENGTH_SHORT).show();
            if (D) Log.d(TAG, "SLED_DETACHED");
        }
    }
}
