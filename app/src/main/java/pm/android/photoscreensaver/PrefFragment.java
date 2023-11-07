package pm.android.photoscreensaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.net.InetAddress;

public class PrefFragment
        extends LeanbackPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, PhotoServiceDiscovery.Callback {

    private static final String TAG = PrefFragment.class.getName();

    private SwitchPreference autoDiscover;
    private PreferenceCategory availableServers;
    private PreferenceCategory serverSettings;
    private EditTextPreference serverHost;
    private EditTextPreference serverPort;
    private Preference startNow;

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

        autoDiscover = (SwitchPreference)findPreference(R.string.pref_key_discover_automatically);
        availableServers = (PreferenceCategory)findPreference(R.string.pref_key_available_servers);
        serverSettings = (PreferenceCategory)findPreference(R.string.pref_key_server_settings);
        serverHost = (EditTextPreference)findPreference(R.string.pref_key_server_host);
        serverPort = (EditTextPreference)findPreference(R.string.pref_key_server_port);
        startNow = findPreference(R.string.pref_key_start);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        serviceDiscovery = new PhotoServiceDiscovery(getPreferenceScreen().getContext(), this);

        startNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startScreensaver();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        updateEditTextPrefSummary(serverSettings);

        if (autoDiscover.isChecked()) {
            serviceDiscovery.start();
        }

        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        if (autoDiscover.isChecked()) {
            serviceDiscovery.stop();
            availableServers.removeAll();
        }

        super.onPause();
    }

    @Override
    public void onServiceFound(String serviceInstanceName, final InetAddress host, final int port) {
        Preference serverPref = new Preference(this.getPreferenceScreen().getContext());
        serverPref.setKey(serviceInstanceName);
        serverPref.setTitle(serviceInstanceName);
        serverPref.setSummary(host + ":" + port);

        serverPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                serverHost.setText(host.getHostAddress());
                serverPort.setText(Integer.toString(port));
                return true;
            }
        });

        availableServers.addPreference(serverPref);
    }

    @Override
    public void onServiceLost(String serviceInstanceName) {
        Preference serverPref = availableServers.findPreference(serviceInstanceName);
        availableServers.removePreference(serverPref);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference changedPreference = getPreferenceScreen().findPreference(key);
        if (changedPreference.equals(serverHost) || changedPreference.equals(serverPort)) {
            updateEditTextPrefSummary((EditTextPreference)changedPreference);
        } else if (changedPreference.equals(autoDiscover)) {
            if (autoDiscover.isChecked()) {
                serviceDiscovery.start();
            } else {
                serviceDiscovery.stop();
                availableServers.removeAll();
            }
        }
    }

    private Preference findPreference(int keyResId) {
        return findPreference(getString(keyResId));
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

    private void startScreensaver() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.systemui", "com.android.systemui.Somnambulator");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
