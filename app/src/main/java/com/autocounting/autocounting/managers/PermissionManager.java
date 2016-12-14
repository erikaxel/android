package com.autocounting.autocounting.managers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionManager {

//    public static final int STORAGE_READ_WRITE = 0;
//    public static final int CAMERA = 1;
    public static final int CAMERA_AND_STORAGE = 2;

    public static boolean hasAll(Context context, String ... permissions){
        for(String permission : permissions)
            if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }
}
