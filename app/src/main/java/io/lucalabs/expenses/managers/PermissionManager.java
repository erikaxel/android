package io.lucalabs.expenses.managers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionManager {

    public static final int CAMERA_AND_STORAGE = 2;

    public static boolean hasAll(Context context, String ... permissions){
        for(String permission : permissions)
            if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

    public static boolean allPermissionsWereGranted(int[] results){
        for(int result : results)
            if(result == -1)
                return false;
        return true;
    }
}
