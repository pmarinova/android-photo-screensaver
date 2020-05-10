package pm.android.photoscreensaver;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Response;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PhotoScreensaverService extends DreamService {

    private static final String TAG = PhotoScreensaverService.class.getName();

    /**
     * URL of the server from where the screensaver photos are loaded.
     * The server returns a JSON array with a list of URLs of all photos.
     */
    private static final String PHOTOS_URL = "http://192.168.1.5:9090/photos/list";

    /**
     * Time interval in seconds before switching to the next photo.
     */
    private static final int SWITCH_INTERVAL = 15;

    /**
     * Volley is the library that handles the HTTP request to load the photos list.
     * VolleyHelper is a tiny wrapper over Volley that simplifies the request.
     */
    private VolleyHelper volley;

    /**
     * The list of photos provided by the server.
     * Each entry is a URL of a single photo.
     */
    private List<String> photos;

    /**
     * The image view which displays the current photo on the screen.
     */
    private ImageView imageView;

    /**
     * A handler associated with the main thread that we use to schedule the switching of the photos.
     */
    private Handler mainThreadHandler;

    /**
     * A flag that we use to stop switching photos when the screensaver is stopped.
     */
    private boolean running = false;

    @Override
    public void onAttachedToWindow() {
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);

        setContentView(R.layout.photo_screensaver);

        volley = new VolleyHelper(this);
        imageView = findViewById(R.id.imageView);
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDreamingStarted() {
        loadPhotosList(new Runnable() {
            public void run() {
                running = true;
                switchPhoto();
            }
        });
    }

    @Override
    public void onDreamingStopped() {
        running = false;
    }

    private void loadPhotosList(final Runnable callback) {
        volley.getJSONArray(PHOTOS_URL, new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                photos = jsonArrayToList(response);
                Log.d(TAG, "loaded photos: " + photos);
                callback.run();
            }
        });
    }

    private void switchPhoto() {

        if (!running) {
            return; // screensaver was stopped
        }

        // keep the current photo as a placeholder until the new one is being loaded
        Drawable currentPhoto = imageView.getDrawable();

        String nextPhotoUrl = getRandomPhotoUrl();
        Log.d(TAG, "loading photo " + nextPhotoUrl);

        Glide.with(this)
                .load(nextPhotoUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(currentPhoto)
                .dontAnimate()
                .into(imageView);

        mainThreadHandler.postDelayed(new Runnable() {
            public void run() { switchPhoto(); }
        }, TimeUnit.SECONDS.toMillis(SWITCH_INTERVAL));
    }

    private String getRandomPhotoUrl() {
        int randomIndex = new Random().nextInt(photos.size());
        return photos.get(randomIndex);
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
