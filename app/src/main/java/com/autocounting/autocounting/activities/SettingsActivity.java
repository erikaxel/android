package com.autocounting.autocounting.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

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
        private PreferenceScreen preferenceScreen;
        private PreferenceCategory debugCategory;

        private int debugCounter = 0;
        private final int DEBUG_COUNTER_LIMIT = 7;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference versionDisplay = findPreference("version_number");
            versionDisplay.setSummary(getVersionName());

            preferenceScreen = ((PreferenceScreen) findPreference("pref_screen"));
            debugCategory = (PreferenceCategory) findPreference("debug_cat");
            preferenceScreen.removePreference(debugCategory);

            final Preference emailDisplay = findPreference("user_email");
            emailDisplay.setSummary(User.getCurrentUser().getEmail());
            emailDisplay.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    debugCounter++;

                    if(debugCounter > DEBUG_COUNTER_LIMIT) {
                        showDebugInfo();
                        emailDisplay.setOnPreferenceClickListener(null);
                    }

                    return true;
                }
            });

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

        private void showDebugInfo() {
            Toast.makeText(this.getActivity(), "Displaying debug info", Toast.LENGTH_SHORT).show();
            preferenceScreen.addPreference(debugCategory);

            ((Preference) findPreference("uid")).setSummary(User.getCurrentUser().getUid());
        }

        private void removeAdminSettings() {
            PreferenceCategory prefCategory = (PreferenceCategory) findPreference("advanced_cat");
            prefCategory.addPreference(findPreference("environment_pref"));
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
