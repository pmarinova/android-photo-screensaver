package pm.android.photoscreensaver;

import android.graphics.drawable.Drawable;
import android.net.MacAddress;
import android.net.Uri;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.TimeUnit;

public class PhotoScreensaverService extends DreamService {

    private static final String TAG = PhotoScreensaverService.class.getName();

    /**
     * Time interval in seconds before switching to the next photo.
     */
    private static final int SWITCH_INTERVAL = 15;

    /**
     * Request timeout in milliseconds.
     */
    private static final int REQUEST_TIMEOUT = 10000;


    /**
     * Photos provider which provides the photos for the screensaver.
     */
    private PhotosProvider photosProvider;

    /**
     * The image view which displays the current photo on the screen.
     */
    private ImageView imageView;

    /**
     * Loading indicator which is displayed while the list of photos is loaded.
     */
    private ProgressBar loadingIndicator;

    /**
     * Error view which is displayed when the listing of photos fails.
     */
    private View errorView;

    /**
     * The error text message.
     */
    private TextView errorText;

    /**
     * URL of the photo currently loaded into the imageView.
     */
    private Uri photoUrl;

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

    @Override
    public void onAttachedToWindow() {
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);

        setContentView(R.layout.photo_screensaver);

        Prefs prefs = new Prefs(this);
        String host = prefs.getServerHost();
        int port = prefs.getServerPort();
        MacAddress mac = prefs.getServerMACAddress();
        boolean wakeOnLan = prefs.isWakeOnLanEnabled();
        PhotoServer photoServer = new PhotoServer(this, host, port, mac, wakeOnLan);
        Log.d(TAG, "photo server: " + photoServer);

        photosProvider = photoServer;
        imageView = findViewById(R.id.imageView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        errorView = findViewById(R.id.errorView);
        errorText = errorView.findViewById(R.id.errorMessage);
        mainThreadHandler = ((App)getApplication()).getMainThreadHandler();
    }

    @Override
    public void onDreamingStarted() {
        hideErrorView();
        showLoadingIndicator();

        photosProvider.init(() -> {
            hideLoadingIndicator();
            running = true;
            switchPhoto();
        }, (error) -> {
            hideLoadingIndicator();
            showErrorView(error);
        });
    }

    @Override
    public void onDreamingStopped() {
        running = false;
    }

    private void switchPhoto() {

        if (!running) {
            return; // screensaver was stopped
        }

        // keep the old photo until the new one is being loaded
        Uri oldPhotoUrl = photoUrl;

        photoUrl = photosProvider.nextPhoto();
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

    private void hideLoadingIndicator() {
        loadingIndicator.setVisibility(View.GONE);
    }

    private void showLoadingIndicator() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideErrorView() {
        errorView.setVisibility(View.GONE);
        errorText.setText("");
    }

    private void showErrorView(Throwable error) {
        String errorMessage = (error.getMessage() != null && !error.getMessage().isEmpty()) ?
                error.getMessage() : error.toString();
        errorView.setVisibility(View.VISIBLE);
        errorText.setText(errorMessage);
    }
}
