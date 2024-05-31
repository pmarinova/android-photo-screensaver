package pm.android.photoscreensaver;

import android.content.Context;
import android.net.MacAddress;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PhotoServer implements PhotosProvider {

    private static final String TAG = PhotoServer.class.getName();

    /**
     * Base URL path from where the screensaver photos are loaded.
     */
    private static final String PHOTOS_BASE_URL_PATH = "/photos";

    /**
     * URL path from where the list of photos is loaded.
     * The server returns a JSON array with a list of all photos.
     */
    private static final String PHOTOS_LIST_URL_PATH = PHOTOS_BASE_URL_PATH + "/list";


    private final String host;
    private final int port;
    private final MacAddress mac;
    private final boolean wakeOnLan;

    private final Handler mainThreadHandler;
    private final Executor executorService;
    private final RequestQueue requestQueue;

    private List<Uri> photos = Collections.emptyList();

    private final Random random = new Random();

    public PhotoServer(Context context, String host, int port, MacAddress mac, boolean wakeOnLan) {
        this.host = host;
        this.port = port;
        this.mac = mac;
        this.wakeOnLan = wakeOnLan;
        this.mainThreadHandler = ((App)context.getApplicationContext()).getMainThreadHandler();
        this.executorService = ((App)context.getApplicationContext()).getExecutorService();
        this.requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public void init(Runnable callback) {
        loadPhotosList((photos) -> {
            this.photos = photos.stream()
                    .map(this::getPhotoUrl)
                    .map(Uri::parse)
                    .collect(Collectors.toList());
            callback.run();
        }, 3);
    }

    @Override
    public Uri nextPhoto() {
        return photos.get(random.nextInt(photos.size()));
    }

    private void loadPhotosList(Consumer<List<String>> callback, int retryCount) {
        requestQueue.add(new JsonArrayRequest(getPhotosListUrl(),
                (response) -> {
                    List<String> photos = jsonArrayToList(response);
                    Log.d(TAG, "loaded photos: " + photos);
                    callback.accept(photos);
                },
                (error) -> {
                    if (wakeOnLan && mac != null && retryCount > 0) {
                        Log.d(TAG, "server not available, send wake-on-lan packet and retry...");
                        sendWakeOnLan(mac, () -> loadPhotosList(callback, retryCount-1));
                    } else {
                        Log.d(TAG, "request failed: " + error);
                    }
                }
        ));
    }

    private void sendWakeOnLan(MacAddress macAddress, Runnable callback) {
        executorService.execute(() -> {
            WakeOnLan.sendWolPacket(macAddress);
            mainThreadHandler.postDelayed(callback, 500);
        });
    }

    private String getServerUrl() {
        return "http://" + host + ":" + port;
    }

    private String getPhotosListUrl() {
        String serverUrl = getServerUrl();
        return serverUrl + PHOTOS_LIST_URL_PATH;
    }

    private String getPhotoUrl(String photo) {
        String serverUrl = getServerUrl();
        return serverUrl + PHOTOS_BASE_URL_PATH + "/" + photo;
    }

    @NonNull
    @Override
    public String toString() {
        return getServerUrl();
    }

    private static List<String> jsonArrayToList(JSONArray jsonArray) {
        try {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
            return list;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
