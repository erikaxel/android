package io.lucalabs.expenses.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.managers.PermissionManager;
import io.lucalabs.expenses.models.ExpenseReport;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Task;
import io.lucalabs.expenses.network.webapi.TaskManagerService;
import io.lucalabs.expenses.utils.AnimationRunner;
import io.lucalabs.expenses.views.fragments.DetailsFragment;
import io.lucalabs.expenses.views.fragments.ReceiptIndexFragment;
import io.lucalabs.expenses.views.presenters.ExpenseReportPresenter;
import io.lucalabs.expenses.views.widgets.CameraFab;

public class ExpenseReportActivity extends FirebaseActivity implements ViewPager.OnPageChangeListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * Firebase reference for the Expense Report
     */
    private String mFirebaseRef;
    private CameraFab mCameraFab;
    private ExpenseReport mExpenseReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayDeleteIcon();
        setContentView(R.layout.activity_expense_report);

        // Toolbar must be set up before call to super
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displaySnackBarIfNecessary();
        mFirebaseRef = getIntent().getStringExtra("firebase_ref");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mCameraFab = (CameraFab) findViewById(R.id.camera_button);
        mCameraFab.setupForExpenseReport(this, mFirebaseRef);

        Inbox.findExpenseReport(this, mFirebaseRef).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mExpenseReport = dataSnapshot.getValue(ExpenseReport.class);
                        setTitle(ExpenseReportPresenter.getNameString(ExpenseReportActivity.this, mExpenseReport));
                        if (mExpenseReport.isFinalized())
                            mCameraFab.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ExpenseReportActivity.this, "Couldn't fetch expense report", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, TaskManagerService.class));
    }

    private void displaySnackBarIfNecessary() {
        if (getIntent().getStringExtra("status") != null)
            switch (getIntent().getStringExtra("status")) {
                case "created":
                    Snackbar.make(findViewById(R.id.expense_report_coordinator), R.string.expense_report_created_notice, Snackbar.LENGTH_SHORT).show();
                    break;
                case "deleted":
                    Snackbar.make(findViewById(R.id.expense_report_coordinator), R.string.receipt_deleted_notice, Snackbar.LENGTH_SHORT).show();
            }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ReceiptIndexFragment.newInstance(mFirebaseRef);
                default:
                    return DetailsFragment.newInstance(mFirebaseRef);
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.receipts);
                default:
                    return getString(R.string.details);
            }
        }
    }

    @Override
    protected void onDeleteAction() {
        if (mExpenseReport.isFinalized())
            Snackbar.make(findViewById(R.id.expense_report_coordinator), R.string.delete_finalized_report_notice, Snackbar.LENGTH_SHORT).show();
        else
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_report_confirmation_title)
                    .setMessage(R.string.delete_report_confirmation_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mExpenseReport.setFirebase_ref(mFirebaseRef);
                            Inbox.findExpenseReport(ExpenseReportActivity.this, mExpenseReport.getFirebase_ref()).removeValue();
                            new Task(ExpenseReportActivity.this, "DELETE", mExpenseReport).performAsync();
                            Intent toMainActivity = new Intent(ExpenseReportActivity.this, MainActivity.class);
                            toMainActivity.putExtra("status", "deleted");
                            startActivity(toMainActivity);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (!mExpenseReport.isFinalized())
            switch (position) {
                case 0:
                    new AnimationRunner(this, mCameraFab, R.anim.fab_scale_up, 200).run();
                    mCameraFab.setVisibility(View.VISIBLE);
                    mCameraFab.setEnabled(true);
                    break;
                case 1:
                    new AnimationRunner(this, mCameraFab, R.anim.fab_scale_down, 200).run();
                    mCameraFab.setVisibility(View.GONE);
                    mCameraFab.setEnabled(false);
                    break;
            }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (PermissionManager.allPermissionsWereGranted(grantResults)) {
            Intent toCameraIntent = new Intent(this, CameraActivity.class);
            toCameraIntent.putExtra("expense_report_ref", mFirebaseRef);
            startActivity(toCameraIntent);
        } else {
            Toast.makeText(this, R.string.needs_permissions_notice, Toast.LENGTH_LONG).show();
        }
    }
}
