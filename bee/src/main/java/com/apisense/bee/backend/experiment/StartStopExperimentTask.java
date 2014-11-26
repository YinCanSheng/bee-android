package com.apisense.bee.backend.experiment;

import android.content.Context;
import android.util.Log;
import com.apisense.android.api.APS;
import com.apisense.android.api.APSLocalCrop;
import com.apisense.api.Callback;
import com.apisense.api.LocalCrop;

/**
 * Start and Stop AsyncTask wrapper to simplify usage in activities
 *
 */
public class StartStopExperimentTask {
    private String TAG = getClass().getSimpleName();

    public static final int EXPERIMENT_STARTED = 1;
    public static final int EXPERIMENT_STOPPED = 2;

    private final Callback<Integer> listener;
    private Context context;

    public StartStopExperimentTask(Context context, Callback<Integer> listener){
        this.context = context;
        this.listener = listener;
    }

    public void execute(String cropId) {
        try {
            if (! getCropFromId(cropId).isRunning()) {
                Log.i(TAG, "Starting experiment: " + cropId);
                APS.startCrop(context, cropId);
                listener.onCall(EXPERIMENT_STARTED);
            } else {
                Log.i(TAG, "Stopping experiment: " + cropId);
                APS.stopCrop(context, cropId);
                listener.onCall(EXPERIMENT_STOPPED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(e);
        }
   }

    private LocalCrop getCropFromId(String id){
        APSLocalCrop crop = null;
        try {
            crop = APS.getCropDescription(context, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  crop;
    }
}
