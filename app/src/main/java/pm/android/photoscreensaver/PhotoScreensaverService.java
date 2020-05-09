package pm.android.photoscreensaver;

import android.service.dreams.DreamService;
import android.util.Log;

public class PhotoScreensaverService extends DreamService {

    private static final String TAG = PhotoScreensaverService.class.getName();

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "init screensaver");

        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);

        setContentView(R.layout.photo_screensaver);
    }

    @Override
    public void onDetachedFromWindow() {
        Log.d(TAG, "cleanup screensaver");
    }

    @Override
    public void onDreamingStarted() {
        Log.d(TAG, "start screensaver");
    }

    @Override
    public void onDreamingStopped() {
        Log.d(TAG, "stop screensaver");
    }
}
