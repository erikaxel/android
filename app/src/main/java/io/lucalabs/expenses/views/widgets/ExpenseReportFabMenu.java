package io.lucalabs.expenses.views.widgets;

import android.app.Activity;
import android.content.Context;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import io.lucalabs.expenses.R;

/**
 * This is unused, for now
 */
public class ExpenseReportFabMenu extends FloatingActionsMenu {
    private Activity mActivity;

    public ExpenseReportFabMenu(Context context) {
        super(context);
    }

    public void setup(Activity activity){
        mActivity = activity;
//        CameraFab cameraFab = (CameraFab) findViewById(R.id.camera_button);
//        addButton(cameraFab);
    }
}

