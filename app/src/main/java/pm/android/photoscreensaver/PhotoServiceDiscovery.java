package pm.android.photoscreensaver;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

public class PhotoServiceDiscovery implements NsdManager.DiscoveryListener {

    public interface Callback {
        void onServiceFound(NsdServiceInfo serviceInfo);
        void onServiceLost(NsdServiceInfo serviceInfo);
    }

    private static final String TAG = PhotoServiceDiscovery.class.getName();

    private static final String SERVICE_TYPE = "_photo-server._tcp";

    private final NsdManager nsdManager;
    private final Callback callback;

    public PhotoServiceDiscovery(Context context, Callback callback) {
        this.nsdManager = (NsdManager)context.getSystemService(Context.NSD_SERVICE);
        this.callback = callback;
    }

    public void start() {
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
    }

    public void stop() {
        nsdManager.stopServiceDiscovery(this);
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d(TAG, "service discovery started");
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(TAG, "service discovery stopped");
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.d(TAG, "service discovery start failed with error code " + errorCode);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.d(TAG, "service discovery stop failed with error code " + errorCode);
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                callback.onServiceFound(serviceInfo);
            }
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "service resolve failed with error code " + errorCode);
            }
        });
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        callback.onServiceLost(serviceInfo);
    }
}
