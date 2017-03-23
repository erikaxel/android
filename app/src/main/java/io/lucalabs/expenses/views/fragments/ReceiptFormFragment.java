package io.lucalabs.expenses.views.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.ExpenseReportActivity;
import io.lucalabs.expenses.activities.ReceiptActivity;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.Task;
import io.lucalabs.expenses.utils.ArgumentComparator;
import io.lucalabs.expenses.utils.DateFormatter;
import io.lucalabs.expenses.utils.NumberFormatter;

public class ReceiptFormFragment extends Fragment implements CalendarDatePickerDialogFragment.OnDateSetListener, View.OnClickListener {
    private TextInputEditText mEditMerchantName;
    private TextInputEditText mEditAmount;
    private TextInputEditText mEditCurrency;
    private TextInputEditText mEditUsedDate;
    private CheckBox mEditReimbursable;
    private TextInputEditText mEditComment;

    private String mUsedDateStamp;

    private String mFirebaseRef;
    private String mExpenseReportRef;
    private Receipt mReceipt;
    private ExpenseReport mExpenseReport;

    private View mView;

    public static ReceiptFormFragment newInstance() {
        return new ReceiptFormFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt_form, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditMerchantName = (TextInputEditText) view.findViewById(R.id.edit_receipt_merchant_name);
        mEditAmount = (TextInputEditText) view.findViewById(R.id.edit_receipt_amount);
        mEditCurrency = (TextInputEditText) view.findViewById(R.id.edit_receipt_currency);
        mEditUsedDate = (TextInputEditText) view.findViewById(R.id.edit_receipt_used_date);
        mEditReimbursable = (CheckBox) view.findViewById(R.id.edit_receipt_reimbursable);
        mEditComment = (TextInputEditText) view.findViewById(R.id.edit_receipt_comment);
        mFirebaseRef = getActivity().getIntent().getStringExtra("firebase_ref");
        mExpenseReportRef = getActivity().getIntent().getStringExtra("expense_report_ref");
        mEditUsedDate.setOnClickListener(this);

        mView = view;

        setReceipt();
        setExpenseReport();
    }

    private void setReceipt() {
        Inbox.findReceipt(getActivity(), mFirebaseRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mReceipt = dataSnapshot.getValue(Receipt.class);

                mEditMerchantName.setText(mReceipt.getMerchant_name());
                mEditAmount.setText(mReceipt.getAmountString());
                mEditCurrency.setText(mReceipt.getCurrency());
                mEditUsedDate.setText(mReceipt.getUsedDateString(getActivity()));
                mUsedDateStamp = mReceipt.getUsed_date();
                mEditReimbursable.setChecked(mReceipt.isReimbursable());
                mEditComment.setText(mReceipt.getComment());

                final ImageView imageView = (ImageView) mView.findViewById(R.id.receipt_image);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ReceiptActivity) getActivity()).showPreview(
                                mReceipt.getImage(getActivity()),
                                Inbox.receiptImage(getActivity(), mReceipt, "original"));
                    }
                });
                if (mReceipt.getFilename() != null) {
                    Glide.with(getActivity())
                            .load(mReceipt.getImage(getActivity()))
                            .asBitmap()
                            .into(imageView);
                } else {
                    StorageReference ref = Inbox.receiptImage(getActivity(), mReceipt, "medium");
                    Glide.with(getActivity())
                            .using(new FirebaseImageLoader())
                            .load(ref)
                            .into(imageView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Couldn't fetch receipt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setExpenseReport() {
        Inbox.findExpenseReport(getActivity(), mExpenseReportRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mExpenseReport = dataSnapshot.getValue(ExpenseReport.class);

                if (mExpenseReport.isFinalized()) {
                    getActivity().setTitle(R.string.title_activity_receipt_finalized);
                    mView.findViewById(R.id.edit_receipt_name_wrapper).setEnabled(false);
                    mEditMerchantName.setEnabled(false);
                    mEditAmount.setEnabled(false);
                    mEditCurrency.setEnabled(false);
                    mEditUsedDate.setEnabled(false);
                    mEditReimbursable.setEnabled(false);
                    mEditComment.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private Receipt getReceiptFromForm() {
        Receipt receipt = new Receipt();
        receipt.setMerchant_name(mEditMerchantName.getText().toString());
        receipt.setAmount_cents(NumberFormatter.getLongFromString(mEditAmount.getText().toString()));
        receipt.setCurrency(mEditCurrency.getText().toString());
        receipt.setUsed_date(mUsedDateStamp);
        receipt.setReimbursable(mEditReimbursable.isChecked());
        receipt.setComment(mEditComment.getText().toString());
        receipt.setFirebase_ref(mReceipt.getFirebase_ref());
        receipt.setExpense_report_firebase_key(mReceipt.getExpense_report_firebase_key());
        return receipt;
    }

    @Override
    public void onDestroyView() {
        Receipt formReceipt = getReceiptFromForm();

        if (!ArgumentComparator.haveEqualArgs(formReceipt, mReceipt)) {
            Inbox.findReceipt(getActivity(), mFirebaseRef).setValue(formReceipt);
            new Task(getActivity(), "PATCH", formReceipt).performAsync();
        }

        super.onDestroyView();
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int month, int day) {
        mUsedDateStamp = DateFormatter.toDateString(year, month, day);
        mEditUsedDate.setText(DateFormatter.formatToLocale(getActivity(), mUsedDateStamp));
    }

    @Override
    public void onClick(View view) {
        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(this)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setDoneText(getString(R.string.affirm_action))
                .setCancelText(getString(R.string.cancel_action));
        cdp.show(((FirebaseActivity) getActivity()).getSupportFragmentManager(), "tag");
    }

    public void onDeleteAction() {
        if (mExpenseReport.isFinalized())
            Snackbar.make(mView.findViewById(R.id.receipt_coordinator), R.string.deleted_finalized_receipt_notice, Snackbar.LENGTH_SHORT).show();
        else
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.delete_receipt_confirmation_title)
                    .setMessage(R.string.delete_receipt_confirmation_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Inbox.findReceipt(getActivity(), mReceipt.getFirebase_ref()).removeValue();
                            new Task(getActivity(), "DELETE", mReceipt).performAsync();
                            Intent toExpenseReportActivity = new Intent(getActivity(), ExpenseReportActivity.class);
                            toExpenseReportActivity.putExtra("status", "deleted");
                            toExpenseReportActivity.putExtra("firebase_ref", mExpenseReportRef);
                            startActivity(toExpenseReportActivity);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
    }
}
