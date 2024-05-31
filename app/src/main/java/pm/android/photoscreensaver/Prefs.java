package pm.android.photoscreensaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.MacAddress;

import androidx.preference.PreferenceManager;

public class Prefs {

    private final Context context;
    private final SharedPreferences prefs;

    public Prefs(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getServerHost() {
        return getString(R.string.pref_key_server_host);
    }

    public int getServerPort() {
        String port = getString(R.string.pref_key_server_port, "0");
        return Integer.parseInt(port);
    }

    public MacAddress getServerMACAddress() {
        String macAddress = getString(R.string.pref_key_server_mac_address);
        return macAddress != null ? MacAddress.fromString(macAddress) : null;
    }

    public boolean isWakeOnLanEnabled() {
        return getBoolean(R.string.pref_key_send_wake_on_lan);
    }

    private String getString(int keyResId) {
        return getString(keyResId, null);
    }

    private String getString(int keyResId, String defaultValue) {
        return prefs.getString(getStringRes(keyResId), defaultValue);
    }

    private boolean getBoolean(int keyResId) {
        return this.prefs.getBoolean(getStringRes(keyResId), false);
    }

    private String getStringRes(int resId) {
        return context.getString(resId);
    }
}
