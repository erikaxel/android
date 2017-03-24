package io.lucalabs.expenses.views.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.ReceiptActivity;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.models.Task;
import io.lucalabs.expenses.views.adapters.ReceiptListAdapter;

/**
 * Fragment containing the expense report receipt list.
 */
public class ReceiptIndexFragment extends Fragment {
    private final static String FIREBASE_REF = "firebase_ref";
    private ReceiptListAdapter mListAdapter;

    public ReceiptIndexFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ReceiptIndexFragment newInstance(String firebaseRef) {
        ReceiptIndexFragment fragment = new ReceiptIndexFragment();
        Bundle args = new Bundle();
        args.putString(FIREBASE_REF, firebaseRef);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_receipt_index, container, false);

        Query receiptsForReport = Inbox.receiptsForExpenseReport(getContext(), getArguments().getString(FIREBASE_REF));

        final ListView receiptList = (ListView) rootView.findViewById(R.id.offline_list);

        mListAdapter = new ReceiptListAdapter(this.getActivity(), receiptsForReport);
        receiptList.setAdapter(mListAdapter);
        registerForContextMenu(receiptList);
        receiptList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Receipt receipt = (Receipt) receiptList.getItemAtPosition(position);
                Intent toReceiptActivity = new Intent(ReceiptIndexFragment.this.getContext(), ReceiptActivity.class);
                toReceiptActivity.putExtra("firebase_ref", receipt.getFirebase_ref());
                toReceiptActivity.putExtra("expense_report_ref", getActivity().getIntent().getStringExtra("firebase_ref"));
                startActivity(toReceiptActivity);
            }
        });

        receiptsForReport.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean atLeastOneReceiptIsPresent = dataSnapshot.getChildren().iterator().hasNext();
                if (atLeastOneReceiptIsPresent)
                    rootView.findViewById(R.id.no_receipts_card).setVisibility(View.GONE);
                else
                    rootView.findViewById(R.id.no_receipts_card).setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.receipt_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Receipt receipt = mListAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.open_receipt:
                Intent toReceiptActivity = new Intent(getContext(), ReceiptActivity.class);
                toReceiptActivity.putExtra("firebase_ref", receipt.getFirebase_ref());
                toReceiptActivity.putExtra("expense_report_ref", getActivity().getIntent().getStringExtra("firebase_ref"));
                startActivity(toReceiptActivity);
                return true;
            case R.id.delete_receipt:
                Query query = Inbox.findExpenseReport(getContext(), getArguments().getString(FIREBASE_REF));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ExpenseReport expenseReport = dataSnapshot.getValue(ExpenseReport.class);

                        if (expenseReport.isFinalized())
                            Snackbar.make(getActivity().findViewById(R.id.expense_report_coordinator), R.string.deleted_finalized_receipt_notice, Snackbar.LENGTH_SHORT).show();
                        else
                            new AlertDialog.Builder(getContext())
                                    .setTitle(R.string.delete_receipt_confirmation_title)
                                    .setMessage(R.string.delete_receipt_confirmation_message)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Inbox.findReceipt(getContext(), receipt.getFirebase_ref()).removeValue();
                                            new Task(getContext(), "DELETE", receipt).performAsync();
                                            Snackbar.make(getActivity().findViewById(R.id.expense_report_coordinator), R.string.receipt_deleted_notice, Snackbar.LENGTH_SHORT).show();
                                        }
                                    })

                                    .setNegativeButton(android.R.string.no, null).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
