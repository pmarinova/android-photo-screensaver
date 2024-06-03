package pm.android.photoscreensaver;

import android.net.Uri;

import java.util.function.Consumer;

public interface PhotosProvider {
    void init(Runnable onSuccess, Consumer<Exception> onError);
    Uri nextPhoto();
}
