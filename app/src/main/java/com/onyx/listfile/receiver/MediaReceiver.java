package com.onyx.listfile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.onyx.listfile.utils.ActivityUtil;



/**
 * Created by 12345 on 2017/4/7.
 */

public class MediaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if("android.intent.action.MEDIA_MOUNTED".equals(action) ||
                "android.intent.action.BOOT_COMPLETED".equals(action) ||
                "android.net.conn.CONNECTIVITY_CHANGE".equals(action)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                 ActivityUtil.startActivity(context,"com.onyx.listfile");
                }
            }, 3*1000);
        }
    }
}
