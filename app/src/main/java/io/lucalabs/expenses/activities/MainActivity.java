package io.lucalabs.expenses.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.models.ApiRequestObject;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.network.NetworkStatus;
import io.lucalabs.expenses.network.Routes;
import io.lucalabs.expenses.network.upload.UploadService;
import io.lucalabs.expenses.network.webapi.ApiRequestTask;
import io.lucalabs.expenses.views.adapters.ExpenseReportListAdapter;

public class MainActivity extends FirebaseActivity {
    private static final String TAG = "MainActivity";
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Query allExpenseReports = Inbox.allExpenseReports(this).orderByChild("finalized");
        final ListView expenseReportList = (ListView) findViewById(R.id.offline_list);
        final ExpenseReportListAdapter expListAdapter = new ExpenseReportListAdapter(this, allExpenseReports);
        expenseReportList.setAdapter(expListAdapter);
        expenseReportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ExpenseReport expenseReport = expListAdapter.getItem(position);
                Intent toExpenseReportIntent = new Intent(MainActivity.this, ExpenseReportActivity.class);
                toExpenseReportIntent.putExtra("firebase_ref", expListAdapter.getRef(position).getKey());
                toExpenseReportIntent.putExtra("exp_name", expenseReport.getNameString(MainActivity.this));
                MainActivity.this.startActivity(toExpenseReportIntent);
            }
        });

        final FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.create_expense_report);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExpenseReport expenseReport = Inbox.createExpenseReport(MainActivity.this);
                new ApiRequestTask(MainActivity.this, "POST", new ApiRequestObject(expenseReport), Routes.expenseReportsUrl(MainActivity.this, null)).execute();
                Snackbar.make(((CoordinatorLayout) MainActivity.this.findViewById(R.id.offline_coordinator)), R.string.expense_report_created_notice, Snackbar.LENGTH_SHORT).show();
            }
        });

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.offline_coordinator);

        displayErrorIfNecessary(getIntent().getIntExtra("networkStatus", NetworkStatus.OK));

        allExpenseReports.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren().iterator().hasNext())
                    ((CardView) findViewById(R.id.no_expenses_card)).setVisibility(View.INVISIBLE);
                else
                    ((CardView) findViewById(R.id.no_expenses_card)).setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        startService(new Intent(this, UploadService.class));
    }

    private void displayErrorIfNecessary(int networkStatus) {
        switch (networkStatus) {
            case NetworkStatus.INTERNET_UNAVAILABLE:
                Snackbar.make(coordinatorLayout, "Network unavailable", Snackbar.LENGTH_LONG).show();
                break;
            case NetworkStatus.SERVER_ERROR:
                Snackbar.make(coordinatorLayout, "An error occurred on server.", Snackbar.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(setIntent);
    }
}
