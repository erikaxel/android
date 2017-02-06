package io.lucalabs.expenses.views.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.views.adapters.ReceiptListAdapter;

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

        ((ListView) rootView.findViewById(R.id.offline_list))
                .setAdapter(new ReceiptListAdapter(this.getActivity(), Inbox.receiptsForExpenseReport(this.getContext(), getArguments().getString(FIREBASE_REF))));

        return rootView;
    }
}