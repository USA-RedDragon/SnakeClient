package com.mcswainsoftware.snakeapp;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

public class SettingsActivity extends AppCompatActivity {

    public final RootPreferenceFragment rootPreferenceFragment = new RootPreferenceFragment();
    public final GeneralPreferenceFragment generalPreferenceFragment = new GeneralPreferenceFragment();
    public final NotificationPreferenceFragment notificationPreferenceFragment = new NotificationPreferenceFragment();
    private PreferenceFragment currentFragment = rootPreferenceFragment;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary(R.string.pref_ringtone_silent);
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));
                    if (ringtone == null) {
                        preference.setSummary(null);
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    public boolean onSupportNavigateUp(){
        if(currentFragment.equals(generalPreferenceFragment) || currentFragment.equals(notificationPreferenceFragment)) {
            currentFragment = rootPreferenceFragment;
            getFragmentManager().beginTransaction().replace(android.R.id.content, rootPreferenceFragment).commit();
            return true;
        } else
            finish();
        return false;
    }

    public void setCurrentFragment(PreferenceFragment fragment) {
        this.currentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        if(currentFragment.equals(generalPreferenceFragment) || currentFragment.equals(notificationPreferenceFragment)) {
            currentFragment = rootPreferenceFragment;
            getFragmentManager().beginTransaction().replace(android.R.id.content, rootPreferenceFragment).commit();
        } else
            super.onBackPressed();
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        currentFragment = rootPreferenceFragment;
        getFragmentManager().beginTransaction().replace(android.R.id.content, rootPreferenceFragment).commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class RootPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_root);
            findPreference("general").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) getActivity()).setCurrentFragment(((SettingsActivity) getActivity()).generalPreferenceFragment);
                    getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, ((SettingsActivity) getActivity()).generalPreferenceFragment).commit();
                    return true;
                }
            });
            findPreference("notifications").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) getActivity()).setCurrentFragment(((SettingsActivity) getActivity()).notificationPreferenceFragment);
                    getActivity().getFragmentManager().beginTransaction().replace(android.R.id.content, ((SettingsActivity) getActivity()).notificationPreferenceFragment).commit();
                    return true;
                }
            });
        }
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            bindPreferenceSummaryToValue(findPreference("url_text"));
        }
    }

    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

}
