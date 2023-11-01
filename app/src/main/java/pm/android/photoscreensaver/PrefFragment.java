package pm.android.photoscreensaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;

import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

public class PrefFragment
        extends LeanbackPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, PhotoServiceDiscovery.Callback {

    private static final String TAG = PrefFragment.class.getName();

    private SharedPreferences prefs;
    private PhotoServiceDiscovery serviceDiscovery;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        String root = getArguments().getString("root", null);
        int prefResId = getArguments().getInt("preferenceResource");
        if (root == null) {
            addPreferencesFromResource(prefResId);
        } else {
            setPreferencesFromResource(prefResId, root);
        }

        serviceDiscovery = new PhotoServiceDiscovery(getPreferenceScreen().getContext(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
        updateEditTextPrefSummary(getPreferenceScreen());
        serviceDiscovery.start();
    }

    @Override
    public void onPause() {
        serviceDiscovery.stop();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        prefs = null;
        super.onPause();
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Preference pref = new Preference(this.getPreferenceScreen().getContext());
        pref.setTitle(serviceInfo.getServiceName());
        pref.setSummary(serviceInfo.getHost().toString() + ":" + serviceInfo.getPort());

        PreferenceCategory availableServers = findPreferenceCategory(R.string.pref_key_available_servers);
        availableServers.addPreference(pref);
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        //TODO
    }

    private PreferenceCategory findPreferenceCategory(int keyResId) {
        return (PreferenceCategory)findPreference(getString(keyResId));
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
