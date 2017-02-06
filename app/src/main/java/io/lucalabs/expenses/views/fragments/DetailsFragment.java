package io.lucalabs.expenses.views.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Inbox;


/**
 * Fragment containing expense report details/form.
 */
public class DetailsFragment extends Fragment implements CalendarDatePickerDialogFragment.OnDateSetListener {
    private final static String FIREBASE_REF = "firebase_ref";
    private final static String TAG = "DetailsFragment";

    private ExpenseReport mExpenseReport;
    private TextInputEditText mEditName;
    private TextInputEditText mEditProjectCode;
    private CheckBox mEditBillable;
    private CheckBox mEditTravel;
    private EditText mEditDepartureAt;
    private EditText mEditArrivalAt;
    private TextInputEditText mEditComment;

    public DetailsFragment() {
    }

    public static DetailsFragment newInstance(String firebaseRef) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(FIREBASE_REF, firebaseRef);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        mEditName = (TextInputEditText) rootView.findViewById(R.id.edit_report_name);
        mEditProjectCode = (TextInputEditText) rootView.findViewById(R.id.edit_report_project_code);
        mEditBillable = (CheckBox) rootView.findViewById(R.id.edit_report_billable);
        mEditTravel = (CheckBox) rootView.findViewById(R.id.edit_report_travel);
        mEditDepartureAt = (EditText) rootView.findViewById(R.id.edit_report_departure_at);
        mEditArrivalAt = (EditText) rootView.findViewById(R.id.edit_report_arrival_at);
        mEditComment = (TextInputEditText) rootView.findViewById(R.id.edit_report_comment);

        ((TextView) rootView.findViewById(R.id.edit_report_departure_date)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(DetailsFragment.this)
                        .setFirstDayOfWeek(Calendar.MONDAY)
                        .setDoneText(getString(R.string.affirm_action))
                        .setCancelText(getString(R.string.cancel_action));
                cdp.show(getFragmentManager(), "fragment_date_picker");
            }
        });

        Inbox.findExpenseReport(getContext(), getArguments().getString(FIREBASE_REF)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mExpenseReport = dataSnapshot.getValue(ExpenseReport.class);
                mEditName.setText(mExpenseReport.getName());
                mEditProjectCode.setText(mExpenseReport.getProject_code());
                mEditBillable.setChecked(mExpenseReport.isBillable());
                mEditTravel.setChecked(mExpenseReport.isTravel());
                mEditArrivalAt.setText(mExpenseReport.getArrival_at());

                mEditComment.setText(mExpenseReport.getComment());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Couldn't fetch expense report", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        ExpenseReport formExpenseReport = getExpenseReportFromForm();

        if (mExpenseReport != null && !formExpenseReport.equals(mExpenseReport))
            Inbox.findExpenseReport(getContext(), getArguments().getString(FIREBASE_REF)).setValue(formExpenseReport);

        super.onDestroyView();
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        Toast.makeText(getContext(), "Toast", Toast.LENGTH_SHORT).show();
    }

    private ExpenseReport getExpenseReportFromForm() {
        ExpenseReport formExpenseReport = new ExpenseReport();
        formExpenseReport.setName(mEditName.getText().toString());
        formExpenseReport.setProject_code(mEditProjectCode.getText().toString());
        formExpenseReport.setBillable(mEditBillable.isChecked());
        formExpenseReport.setTravel(mEditTravel.isChecked());
        formExpenseReport.setComment(mEditComment.getText().toString());
        return formExpenseReport;
    }
}
