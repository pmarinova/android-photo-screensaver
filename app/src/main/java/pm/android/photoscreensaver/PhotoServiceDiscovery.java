package pm.android.photoscreensaver;

import android.content.Context;
import android.net.MacAddress;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class PhotoServiceDiscovery implements NsdManager.DiscoveryListener {

    public interface Callback {
        void onServiceFound(String serviceInstanceName, InetAddress host, int port, MacAddress mac);
        void onServiceLost(String serviceInstanceName);
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
        Log.d(TAG, "service found: " + serviceInfo.getServiceName());
        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "service resolved: " + serviceInfo);

                byte[] macValue = serviceInfo.getAttributes().get("MAC");
                String macString = macValue != null ? new String(macValue, StandardCharsets.UTF_8) : null;
                MacAddress macAddress = macString != null ? MacAddress.fromString(macString) : null;

                callback.onServiceFound(serviceInfo.getServiceName(),
                        serviceInfo.getHost(), serviceInfo.getPort(), macAddress);
            }
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "service resolve failed with error code " + errorCode);
            }
        });
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "service lost: " + serviceInfo.getServiceName());
        callback.onServiceLost(serviceInfo.getServiceName());
    }
}
