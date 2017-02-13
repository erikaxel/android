package io.lucalabs.expenses.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.network.upload.UploadService;
import io.lucalabs.expenses.views.fragments.DetailsFragment;
import io.lucalabs.expenses.views.fragments.ReceiptsFragment;
import io.lucalabs.expenses.views.widgets.CameraFab;

public class ExpenseReportActivity extends FirebaseActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_report);
        setTitle(getIntent().getStringExtra("exp_name"));

        mFirebaseRef = getIntent().getStringExtra("firebase_ref");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ((CameraFab) findViewById(R.id.camera_button)).setupForExpenseReport(this, mFirebaseRef);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0,0);
        startService(new Intent(this, UploadService.class));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        overridePendingTransition(0, 0);
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
                    return ReceiptsFragment.newInstance(mFirebaseRef);
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
}
