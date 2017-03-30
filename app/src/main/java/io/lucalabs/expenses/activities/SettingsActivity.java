package io.lucalabs.expenses.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.Toast;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.abstracts.AppCompatPreferenceActivity;
import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.models.Device;
import io.lucalabs.expenses.models.User;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();

        fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS_FRAGMENT");
        fragmentTransaction.commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

                    if (debugCounter > DEBUG_COUNTER_LIMIT) {
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Device.register(getActivity());
                        }
                    }).start();
                    return true;
                }
            });

            if (!User.isAdmin())
                removeAdminSettings();
        }

        private void showDebugInfo() {
            Toast.makeText(this.getActivity(), "Hurray! You found the developer options", Toast.LENGTH_SHORT).show();
            preferenceScreen.addPreference(debugCategory);
            findPreference("uid").setSummary(User.getCurrentUser().getUid());
        }

        private void removeAdminSettings() {
            PreferenceCategory prefCategory = (PreferenceCategory) findPreference("advanced_cat");
            prefCategory.removePreference(findPreference("environment_pref"));
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
