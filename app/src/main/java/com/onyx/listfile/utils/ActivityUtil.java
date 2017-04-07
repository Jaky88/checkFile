package com.onyx.listfile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


public class ActivityUtil {
    public static void startActivity(Activity activity, Class clazz) {
        Intent intent = new Intent(activity, clazz);
        activity.startActivity(intent);
        activity.finish();
    }


    public static void startActivity(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent != null) {
            context.startActivity(intent);
        }
    }
}
