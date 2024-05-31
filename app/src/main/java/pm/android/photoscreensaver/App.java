package pm.android.photoscreensaver;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Handler getMainThreadHandler() {
        return this.mainThreadHandler;
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }
}
