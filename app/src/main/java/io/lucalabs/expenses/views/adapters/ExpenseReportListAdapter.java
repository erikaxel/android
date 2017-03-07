package io.lucalabs.expenses.views.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.views.presenters.ExpenseReportPresenter;

public class ExpenseReportListAdapter extends FirebaseListAdapter<ExpenseReportPresenter> {

    public ExpenseReportListAdapter(Activity activity, Query query) {
        super(activity, ExpenseReportPresenter.class, R.layout.expense_report_list_item, query);
    }

    @Override
    protected void populateView(View view, final ExpenseReportPresenter expenseReport, int position) {
        ((TextView) view.findViewById(R.id.expense_report_text)).setText(expenseReport.getNameString(mActivity));
        ((TextView) view.findViewById(R.id.expense_report_subtitle)).setText(expenseReport.getSubtitleString());

        if (expenseReport.isFinalized())
            ((ImageView) view.findViewById(R.id.expense_report_icon)).setImageResource(R.drawable.ic_lock);
    }

    @Override
    public ExpenseReportPresenter getItem(int position) {
        return super.getItem(super.getCount() - position - 1);
    }

    @Override
    public DatabaseReference getRef(int position) {
        return super.getRef(super.getCount() - position - 1);
    }
}
