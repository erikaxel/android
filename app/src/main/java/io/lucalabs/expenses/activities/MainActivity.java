package io.lucalabs.expenses.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Task;
import io.lucalabs.expenses.network.NetworkStatus;
import io.lucalabs.expenses.network.upload.UploadService;
import io.lucalabs.expenses.views.adapters.ExpenseReportListAdapter;

public class MainActivity extends FirebaseActivity {
    private static final String TAG = "MainActivity";
    private CoordinatorLayout coordinatorLayout;
    private ExpenseReportListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displaySnackBarIfNecessary();

        Query allExpenseReports = Inbox.allExpenseReports(this);
        final ListView expenseReportList = (ListView) findViewById(R.id.offline_list);
        mListAdapter = new ExpenseReportListAdapter(this, allExpenseReports);
        expenseReportList.setAdapter(mListAdapter);

        expenseReportList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent toExpenseReportActivity = new Intent(MainActivity.this, ExpenseReportActivity.class);
                toExpenseReportActivity.putExtra("firebase_ref", mListAdapter.getRef(position).getKey());
                startActivity(toExpenseReportActivity);
            }
        });

        registerForContextMenu(expenseReportList);

        final FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.create_expense_report);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ExpenseReport expenseReport = Inbox.createExpenseReport(MainActivity.this);
                new Task(MainActivity.this, "POST", expenseReport).performAsync();
                Intent toExpenseReportIntent = new Intent(MainActivity.this, ExpenseReportActivity.class);
                toExpenseReportIntent.putExtra("firebase_ref", expenseReport.getFirebase_ref());
                toExpenseReportIntent.putExtra("status", "created");
                startActivity(toExpenseReportIntent);
            }
        });

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator);

        displayErrorIfNecessary(getIntent().getIntExtra("networkStatus", NetworkStatus.OK));
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        startService(new Intent(this, UploadService.class));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final ExpenseReport expenseReport = mListAdapter.getItem(info.position);
        expenseReport.setFirebase_ref(mListAdapter.getRef(info.position).getKey());
        DatabaseReference key = mListAdapter.getRef(info.position);
        switch (item.getItemId()) {
            case R.id.open_expense_report:
                Intent toExpenseReportIntent = new Intent(MainActivity.this, ExpenseReportActivity.class);
                toExpenseReportIntent.putExtra("firebase_ref", expenseReport.getFirebase_ref());
                startActivity(toExpenseReportIntent);
                return true;
            case R.id.delete_expense_report:
                if (expenseReport.isFinalized())
                    Snackbar.make(findViewById(R.id.main_coordinator), R.string.delete_finalized_report_notice, Snackbar.LENGTH_SHORT).show();
                else
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.delete_report_confirmation_title)
                            .setMessage(R.string.delete_report_confirmation_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Inbox.findExpenseReport(MainActivity.this, expenseReport.getFirebase_ref()).removeValue();
                                    new Task(MainActivity.this, "DELETE", expenseReport).performAsync();
                                    Snackbar.make(findViewById(R.id.main_coordinator), R.string.expense_report_deleted_notice, Snackbar.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

    private void displaySnackBarIfNecessary() {
        String status = getIntent().getStringExtra("status");
        if (status != null)
            switch (status) {
                case "deleted":
                    Snackbar.make(findViewById(R.id.main_coordinator), R.string.expense_report_deleted_notice, Snackbar.LENGTH_SHORT).show();
            }
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(setIntent);
    }
}
