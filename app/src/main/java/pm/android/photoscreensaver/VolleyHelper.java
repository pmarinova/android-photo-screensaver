package pm.android.photoscreensaver;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

public class VolleyHelper {

    private static final String TAG = VolleyHelper.class.getName();

    private final RequestQueue requestQueue;

    private final Response.ErrorListener defaultErrorHandler = new Response.ErrorListener() {
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "request failed: " + error);
        }
    };

    public VolleyHelper(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public RequestQueue getRequestQueue() {
        return this.requestQueue;
    }

    public Request<JSONArray> getJSONArray(String url, Response.Listener<JSONArray> responseHandler) {
        return this.requestQueue.add(new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                responseHandler,
                defaultErrorHandler
        ));
    }
}
