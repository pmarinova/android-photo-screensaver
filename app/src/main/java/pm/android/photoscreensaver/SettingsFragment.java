package pm.android.photoscreensaver;

import android.os.Bundle;

import androidx.leanback.preference.LeanbackSettingsFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends LeanbackSettingsFragment {
    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(R.xml.prefs, null));
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        startPreferenceFragment(buildPreferenceFragment(R.xml.prefs, pref.getKey()));
        return true;
    }

    private PreferenceFragment buildPreferenceFragment(int preferenceResId, String root) {
        PreferenceFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt("preferenceResource", preferenceResId);
        args.putString("root", root);
        fragment.setArguments(args);
        return fragment;
    }


}
