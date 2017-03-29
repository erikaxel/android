package io.lucalabs.expenses.views.widgets;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

public class CreateReportFab extends FloatingActionButton implements View.OnClickListener {

    public CreateReportFab(Context context) {
        super(context);
    }

    public void setup(){
        setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

    }
}

