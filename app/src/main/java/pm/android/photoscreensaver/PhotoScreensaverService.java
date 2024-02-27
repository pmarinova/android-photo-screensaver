package pm.android.photoscreensaver;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PhotoScreensaverService extends DreamService {

    private static final String TAG = PhotoScreensaverService.class.getName();

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
     * Time interval in seconds before switching to the next photo.
     */
    private static final int SWITCH_INTERVAL = 15;

    /**
     * Request timeout in milliseconds.
     */
    private static final int REQUEST_TIMEOUT = 10000;

    /**
     * Volley is the library that handles the HTTP request to load the photos list.
     * VolleyHelper is a tiny wrapper over Volley that simplifies the request.
     */
    private VolleyHelper volley;

    /**
     * The list of photos provided by the server.
     * Each entry contains the relative path of the photo.
     */
    private List<String> photos;

    /**
     * The image view which displays the current photo on the screen.
     */
    private ImageView imageView;

    /**
     * URL of the photo currently loaded into the imageView.
     */
    private String photoUrl;

    /**
     * A handler associated with the main thread that we use to schedule the switching of the photos.
     */
    private Handler mainThreadHandler;

    /**
     * A flag that we use to stop switching photos when the screensaver is stopped.
     */
    private boolean running = false;

    /**
     * Glide request error listener.
     */
    private final RequestListener<Drawable> errorListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(
                GlideException e,
                Object model,
                @NonNull Target<Drawable> target,
                boolean isFirstResource) {
            Log.e(TAG, "failed to load " + model, e);
            return false;
        }
        @Override
        public boolean onResourceReady(
                @NonNull Drawable resource,
                @NonNull Object model,
                Target<Drawable> target,
                @NonNull DataSource dataSource,
                boolean isFirstResource) {
            return false;
        }
    };

    private String getServerUrl() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String host = prefs.getString(getString(R.string.pref_key_server_host), null);
        String port = prefs.getString(getString(R.string.pref_key_server_port), null);
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

    @Override
    public void onAttachedToWindow() {
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);

        setContentView(R.layout.photo_screensaver);

        volley = new VolleyHelper(this);
        imageView = findViewById(R.id.imageView);
        mainThreadHandler = new Handler(Looper.getMainLooper());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String host = prefs.getString(getString(R.string.pref_key_server_host), null);
        String port = prefs.getString(getString(R.string.pref_key_server_port), null);

        Log.d(TAG, "host: " + host);
        Log.d(TAG, "port: " + port);
    }

    @Override
    public void onDreamingStarted() {
        loadPhotosList(() -> {
            running = true;
            switchPhoto();
        });
    }

    @Override
    public void onDreamingStopped() {
        running = false;
    }

    private void loadPhotosList(final Runnable callback) {
        volley.getJSONArray(getPhotosListUrl(), (response) -> {
            photos = jsonArrayToList(response);
            Log.d(TAG, "loaded photos: " + photos);
            callback.run();
        });
    }

    private void switchPhoto() {

        if (!running) {
            return; // screensaver was stopped
        }

        // keep the old photo until the new one is being loaded
        String oldPhotoUrl = photoUrl;

        photoUrl = getRandomPhotoUrl();
        Log.d(TAG, "loading photo " + photoUrl);

        RequestOptions options = new RequestOptions()
                .timeout(REQUEST_TIMEOUT)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fitCenter();



        Glide.with(this)
                .load(photoUrl)
                .apply(options)
                .thumbnail(Glide.with(this)
                    .load(oldPhotoUrl)
                    .apply(options)
                )
                .listener(errorListener)
                .into(imageView);

        mainThreadHandler.postDelayed(
                this::switchPhoto,
                TimeUnit.SECONDS.toMillis(SWITCH_INTERVAL));
    }

    private String getRandomPhotoUrl() {
        int randomIndex = new Random().nextInt(photos.size());
        String photo = photos.get(randomIndex);
        return getPhotoUrl(photo);
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
