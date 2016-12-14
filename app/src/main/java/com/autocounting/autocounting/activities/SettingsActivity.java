package com.autocounting.autocounting.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;

import com.autocounting.autocounting.R;
import com.autocounting.autocounting.managers.EnvironmentManager;
import com.autocounting.autocounting.models.User;

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
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference versionDisplay = findPreference("version_number");
            versionDisplay.setSummary(getVersionName());

            Preference emailDisplay = findPreference("user_email");
            emailDisplay.setSummary(User.getCurrentUser().getEmail());

            Preference environmentPref = findPreference("environment_pref");
            environmentPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    EnvironmentManager.reset();
                    return true;
                }
            });

            if(!User.isAdmin())
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
