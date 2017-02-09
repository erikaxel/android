package io.lucalabs.expenses.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.ReceiptActivity;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.views.adapters.ReceiptListAdapter;
import io.lucalabs.expenses.views.widgets.CameraFab;

/**
 * Fragment containing the expense report receipt list.
 */
public class ReceiptsFragment extends Fragment {
    private final static String FIREBASE_REF = "firebase_ref";

    public ReceiptsFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ReceiptsFragment newInstance(String firebaseRef){
        ReceiptsFragment fragment = new ReceiptsFragment();
        Bundle args = new Bundle();
        args.putString(FIREBASE_REF, firebaseRef);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_expense_report, container, false);

        final ListView receiptList = (ListView) rootView.findViewById(R.id.offline_list);
        receiptList.setAdapter(new ReceiptListAdapter(this.getActivity(), Inbox.receiptsForExpenseReport(this.getContext(), getArguments().getString(FIREBASE_REF))));
        receiptList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Receipt receipt = (Receipt) receiptList.getItemAtPosition(position);
                Intent toReceiptActivity = new Intent(ReceiptsFragment.this.getContext(), ReceiptActivity.class);
                toReceiptActivity.putExtra("firebase_ref", receipt.getFirebase_ref());
                startActivity(toReceiptActivity);
            }
        });

        return rootView;
    }
}