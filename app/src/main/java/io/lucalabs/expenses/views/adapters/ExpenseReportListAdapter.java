package io.lucalabs.expenses.views.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.ExpenseReportActivity;
import io.lucalabs.expenses.models.ExpenseReport;

public class ExpenseReportListAdapter extends FirebaseListAdapter<ExpenseReport> {

    public ExpenseReportListAdapter(Activity activity, Query query) {
        super(activity, ExpenseReport.class, R.layout.expense_report_list_item, query);
    }

    @Override
    protected void populateView(View view, final ExpenseReport expenseReport, int position) {
        ((TextView) view.findViewById(R.id.expense_report_text)).setText(expenseReport.getNameString());
    }
}
