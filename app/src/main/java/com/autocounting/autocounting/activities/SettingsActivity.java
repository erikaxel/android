package com.autocounting.autocounting.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.autocounting.autocounting.R;
import com.autocounting.autocounting.models.User;
import com.basecamp.turbolinks.TurbolinksSession;

public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();

        fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS_FRAGMENT");
        fragmentTransaction.commit();
    }


    @Override
    protected void onStop() {
        TurbolinksSession.resetDefault();
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference pref = findPreference("version_number");
            pref.setSummary(getVersionName());

            if(!User.getCurrentUser(this.getActivity().getApplicationContext()).isAdmin())
                removeAdminSettings();
        }

        private void removeAdminSettings() {
            PreferenceCategory prefCategory = (PreferenceCategory) findPreference("advanced_cat");
            Preference pref = findPreference("environment_pref");
            prefCategory.removePreference(pref);
        }

        private String getVersionName() {
            try {
                return getActivity().getPackageManager()
                        .getPackageInfo(getActivity().getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                return "Unknown";
            }
        }
    }
}
