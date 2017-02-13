package io.lucalabs.expenses.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.network.webapi.PatchReceiptTask;
import io.lucalabs.expenses.utils.DateFormatter;
import io.lucalabs.expenses.utils.NumberFormatter;
import io.lucalabs.expenses.views.fragments.DetailsFragment;

public class ReceiptActivity extends AppCompatActivity implements CalendarDatePickerDialogFragment.OnDateSetListener, View.OnClickListener {

    private TextInputEditText mEditMerchantName;
    private TextInputEditText mEditAmount;
    private TextInputEditText mEditCurrency;
    private TextInputEditText mEditUsedDate;
    private CheckBox mEditReimbursable;
    private TextInputEditText mEditComment;

    private String mUsedDateStamp;

    private String mFirebaseRef;
    private Receipt mReceipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra("exp_name"));

        setContentView(R.layout.activity_receipt);

        mEditMerchantName = (TextInputEditText) findViewById(R.id.edit_receipt_merchant_name);
        mEditAmount = (TextInputEditText) findViewById(R.id.edit_receipt_amount);
        mEditCurrency = (TextInputEditText) findViewById(R.id.edit_receipt_currency);
        mEditUsedDate = (TextInputEditText) findViewById(R.id.edit_receipt_used_date);
        mEditReimbursable = (CheckBox) findViewById(R.id.edit_receipt_reimbursable);
        mEditComment = (TextInputEditText) findViewById(R.id.edit_receipt_comment);
        mFirebaseRef = getIntent().getStringExtra("firebase_ref");

        mEditUsedDate.setOnClickListener(this);

        setReceipt();
    }

    private void setReceipt() {
        Inbox.findReceipt(this, mFirebaseRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mReceipt = (Receipt) dataSnapshot.getValue(Receipt.class);

                mEditMerchantName.setText(mReceipt.getMerchant_name());
                mEditAmount.setText(mReceipt.getAmountString());
                mEditCurrency.setText(mReceipt.getCurrency());
                mEditUsedDate.setText(mReceipt.getUsedDateString(ReceiptActivity.this));
                mUsedDateStamp = mReceipt.getUsed_date();
                mEditReimbursable.setChecked(mReceipt.isReimbursable());
                mEditComment.setText(mReceipt.getComment());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ReceiptActivity.this, "Couldn't fetch receipt", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        Receipt formReceipt = getReceiptFromForm();

        if (mReceipt != null && !mReceipt.equals(formReceipt)) {
            new PatchReceiptTask(this, formReceipt).execute();
            Inbox.findReceipt(this, mFirebaseRef).setValue(formReceipt);
        }

        super.onDestroy();
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int month, int day) {
            mUsedDateStamp = DateFormatter.toDateString(year, month, day);
            mEditUsedDate.setText(DateFormatter.formatToLocale(this, mUsedDateStamp));
    }

    @Override
    public void onClick(View view) {
        CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(this)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setDoneText(getString(R.string.affirm_action))
                .setCancelText(getString(R.string.cancel_action));
        cdp.show(getSupportFragmentManager(), "tag");
    }
}
