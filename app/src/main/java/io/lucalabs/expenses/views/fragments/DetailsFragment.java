package io.lucalabs.expenses.views.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.network.webapi.PostExpenseReportTask;
import io.lucalabs.expenses.utils.DateFormatter;

/**
 * Fragment containing expense report details/form.
 */
public class DetailsFragment extends Fragment implements CalendarDatePickerDialogFragment.OnDateSetListener, View.OnClickListener {
    private final static String FIREBASE_REF = "firebase_ref";
    private final static String TAG = "DetailsFragment";

    private static final String ARRIVAL_PICKER_TAG = "arrival_tag";
    private static final String DEPARTURE_PICKER_TAG = "departure_tag";

    private ExpenseReport mExpenseReport;
    private TextInputEditText mEditName;
    private TextInputEditText mEditProjectCode;
    private CheckBox mEditBillable;
    private CheckBox mEditTravel;
    private TextInputEditText mEditSource;
    private TextInputEditText mEditDestination;
    private EditText mEditDepartureAt;
    private EditText mEditArrivalAt;
    private TextInputEditText mEditComment;

    private String mDepartureAtStamp;
    private String mArrivalAtStamp;

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
        mEditSource = (TextInputEditText) rootView.findViewById(R.id.edit_report_source);
        mEditDestination = (TextInputEditText) rootView.findViewById(R.id.edit_report_destination);
        mEditDepartureAt = (EditText) rootView.findViewById(R.id.edit_report_departure_at);
        mEditArrivalAt = (EditText) rootView.findViewById(R.id.edit_report_arrival_at);
        mEditComment = (TextInputEditText) rootView.findViewById(R.id.edit_report_comment);

        mEditDepartureAt.setOnClickListener(this);
        mEditArrivalAt.setOnClickListener(this);

        Inbox.findExpenseReport(getContext(), getArguments().getString(FIREBASE_REF)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mExpenseReport = dataSnapshot.getValue(ExpenseReport.class);
                mEditName.setText(mExpenseReport.getName());
                mEditProjectCode.setText(mExpenseReport.getProject_code());
                mEditBillable.setChecked(mExpenseReport.isBillable());
                mEditTravel.setChecked(mExpenseReport.isTravel());
                mEditSource.setText(mExpenseReport.getSource());
                mEditDestination.setText(mExpenseReport.getDestination());
                mDepartureAtStamp = mExpenseReport.getDeparture_at();
                mArrivalAtStamp = mExpenseReport.getArrival_at();
                mEditDepartureAt.setText(DateFormatter.formatToLocale(getContext(), mDepartureAtStamp));
                mEditArrivalAt.setText(DateFormatter.formatToLocale(getContext(), mArrivalAtStamp));
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

        if (mExpenseReport != null && !formExpenseReport.equals(mExpenseReport)) {
            Inbox.findExpenseReport(getContext(), getArguments().getString(FIREBASE_REF)).setValue(formExpenseReport);
            new PostExpenseReportTask(getContext(), formExpenseReport).execute();
            formExpenseReport.setFirebase_ref(getArguments().getString(FIREBASE_REF));
        }

        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(DetailsFragment.this)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setDoneText(getString(R.string.affirm_action))
                .setCancelText(getString(R.string.cancel_action));

        if (view.getId() == R.id.edit_report_arrival_at)
            cdp.show(getFragmentManager(), ARRIVAL_PICKER_TAG);
        else
            cdp.show(getFragmentManager(), DEPARTURE_PICKER_TAG);
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int month, int day) {
        if (dialog.getTag() == DEPARTURE_PICKER_TAG) {
            mDepartureAtStamp = DateFormatter.toDateString(year, month, day);
            mEditDepartureAt.setText(DateFormatter.formatToLocale(getContext(), mDepartureAtStamp));
        } else {
            mArrivalAtStamp = DateFormatter.toDateString(year, month, day);
            mEditArrivalAt.setText(DateFormatter.formatToLocale(getContext(), mArrivalAtStamp));
        }
    }

    private ExpenseReport getExpenseReportFromForm() {
        ExpenseReport formExpenseReport = new ExpenseReport();
        formExpenseReport.setName(mEditName.getText().toString());
        formExpenseReport.setProject_code(mEditProjectCode.getText().toString());
        formExpenseReport.setBillable(mEditBillable.isChecked());
        formExpenseReport.setTravel(mEditTravel.isChecked());
        formExpenseReport.setSource(mEditSource.getText().toString());
        formExpenseReport.setDestination(mEditDestination.getText().toString());
        formExpenseReport.setDeparture_at(mDepartureAtStamp);
        formExpenseReport.setArrival_at(mArrivalAtStamp);
        formExpenseReport.setComment(mEditComment.getText().toString());
        return formExpenseReport;
    }
}
