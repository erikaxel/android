package io.lucalabs.expenses.views.presenters;

import android.content.Context;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.ExpenseReport;

public class ExpenseReportPresenter extends ExpenseReport {
    public static String getSubtitleString(Context context, ExpenseReport expenseReport) {
        StringBuilder sb = new StringBuilder("");

        if (expenseReport.getName() != null && !expenseReport.getName().isEmpty() && expenseReport.getReference() != null)
            sb.append(expenseReport.getReference()).append(" ");

        if(expenseReport.isFinalized())
            sb.append(context.getString(R.string.expense_report_finalized));

        return sb.toString();
    }

    public static String getNameString(Context context, ExpenseReport expensereport){
        if (expensereport.getName() != null && !expensereport.getName().isEmpty())
            return expensereport.getName();
        else if (expensereport.getReference() != null && !expensereport.getReference().isEmpty())
            return expensereport.getReference();
        else return context.getString(R.string.new_report);
    }
}
