package pm.android.photoscreensaver;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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


    /**
     * Volley is the library that handles the HTTP request to load the photos list.
     * VolleyHelper is a tiny wrapper over Volley that simplifies the request.
     */
    private final VolleyHelper volley;

    private final String host;
    private final int port;

    private List<Uri> photos = Collections.emptyList();

    private final Random random = new Random();

    public PhotoServer(Context context, String host, int port) {
        this.volley = new VolleyHelper(context);
        this.host = host;
        this.port = port;
    }

    @Override
    public void init(Runnable callback) {
        loadPhotosList((photos) -> {
            this.photos = photos.stream()
                    .map(this::getPhotoUrl)
                    .map(Uri::parse)
                    .collect(Collectors.toList());
            callback.run();
        });
    }

    @Override
    public Uri nextPhoto() {
        return photos.get(random.nextInt(photos.size()));
    }

    private void loadPhotosList(Consumer<List<String>> callback) {
        volley.getJSONArray(getPhotosListUrl(), (response) -> {
            List<String> photos = jsonArrayToList(response);
            Log.d(TAG, "loaded photos: " + photos);
            callback.accept(photos);
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
