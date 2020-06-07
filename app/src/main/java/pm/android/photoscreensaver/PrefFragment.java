package pm.android.photoscreensaver;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

public class PrefFragment
        extends LeanbackPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = PrefFragment.class.getName();

    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        String root = getArguments().getString("root", null);
        int prefResId = getArguments().getInt("preferenceResource");
        if (root == null) {
            addPreferencesFromResource(prefResId);
        } else {
            setPreferencesFromResource(prefResId, root);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);

        Preference hostPref = getPreferenceScreen().findPreference("pref_key_server_host");
        Preference portPref = getPreferenceScreen().findPreference("pref_key_server_port");

        updateSummary((EditTextPreference)hostPref);
        updateSummary((EditTextPreference)portPref);
    }

    @Override
    public void onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        prefs = null;
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference changedPreference = getPreferenceScreen().findPreference(key);
        if (changedPreference instanceof EditTextPreference) {
            updateSummary((EditTextPreference)changedPreference);
        }
    }

    private void updateSummary(EditTextPreference preference) {
        preference.setSummary(preference.getText());
    }
}
