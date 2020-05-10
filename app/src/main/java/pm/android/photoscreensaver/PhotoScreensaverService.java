package pm.android.photoscreensaver;

import android.service.dreams.DreamService;
import android.util.Log;

import com.android.volley.Response;

import org.json.JSONArray;

public class PhotoScreensaverService extends DreamService {

    private static final String TAG = PhotoScreensaverService.class.getName();

    /**
     * URL of the server from where the screensaver photos are loaded.
     * The server returns a JSON array with a list of URLs of all photos.
     */
    private static final String PHOTOS_URL = "http://192.168.1.5:9090/photos/list";

    /**
     * Volley is the library that handles the HTTP request to load the photos list.
     * VolleyHelper is a tiny wrapper over Volley that simplifies the request.
     */
    private VolleyHelper volley;

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "init screensaver");

        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);

        setContentView(R.layout.photo_screensaver);

        volley = new VolleyHelper(this);
    }

    @Override
    public void onDetachedFromWindow() {
        Log.d(TAG, "cleanup screensaver");
    }

    @Override
    public void onDreamingStarted() {
        Log.d(TAG, "start screensaver");
        loadPhotosList();
    }

    @Override
    public void onDreamingStopped() {
        Log.d(TAG, "stop screensaver");
    }

    private void loadPhotosList() {
        volley.getJSONArray(PHOTOS_URL, new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.d(TAG, "loaded photos: " + response);
            }
        });
    }
}
