package pm.android.photoscreensaver;

import android.os.Bundle;

import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends LeanbackSettingsFragmentCompat {
    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(R.xml.prefs, null));
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        startPreferenceFragment(buildPreferenceFragment(R.xml.prefs, pref.getKey()));
        return true;
    }

    private PreferenceFragmentCompat buildPreferenceFragment(int preferenceResId, String root) {
        PreferenceFragmentCompat fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt("preferenceResource", preferenceResId);
        args.putString("root", root);
        fragment.setArguments(args);
        return fragment;
    }


}
