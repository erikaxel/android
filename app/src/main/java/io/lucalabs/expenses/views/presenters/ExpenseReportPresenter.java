package io.lucalabs.expenses.views.presenters;

import android.content.Context;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.ExpenseReport;

public class ExpenseReportPresenter extends ExpenseReport {
    public String getNameString(Context context) {
        return getNameString(context, this);
    }

    public String getSubtitleString(Context context) {
        StringBuilder sb = new StringBuilder("");

        if (getName() != null && !getName().isEmpty() && getReference() != null)
            sb.append(getReference()).append(" ");

        if(isFinalized())
            sb.append(context.getString(R.string.expense_report_finalized));

        return sb.toString();
    }

    public static String getNameString(Context context, ExpenseReport object){
        if (object.getName() != null && !object.getName().isEmpty())
            return object.getName();
        else if (object.getReference() != null && !object.getReference().isEmpty())
            return object.getReference();
        else return context.getString(R.string.new_report);
    }
}
