package pm.android.photoscreensaver;

import android.net.Uri;

public interface PhotosProvider {
    void init(Runnable callback);
    Uri nextPhoto();
}
