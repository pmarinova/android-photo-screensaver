package pm.android.photoscreensaver;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

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

        updateEditTextPrefSummary(getPreferenceScreen());
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
            updateEditTextPrefSummary((EditTextPreference)changedPreference);
        }
    }

    private void updateEditTextPrefSummary(PreferenceGroup preferenceGroup) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                updateEditTextPrefSummary((PreferenceGroup)preference);
            } else if (preference instanceof EditTextPreference) {
                updateEditTextPrefSummary((EditTextPreference)preference);
            }
        }
    }

    private void updateEditTextPrefSummary(EditTextPreference preference) {
        preference.setSummary(preference.getText());
    }
}
