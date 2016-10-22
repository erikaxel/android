package com.autocounting.autocounting;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.basecamp.turbolinks.TurbolinksSession;

public class SettingsActivity extends PreferenceActivity {

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

            Preference preference = findPreference("version_number");
            preference.setSummary(getVersionName());
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
