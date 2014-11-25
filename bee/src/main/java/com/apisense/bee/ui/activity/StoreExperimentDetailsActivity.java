package com.apisense.bee.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.apisense.android.api.APS;
import com.apisense.android.api.APSLocalCrop;
import com.apisense.api.Callback;
import com.apisense.bee.R;
import com.apisense.bee.backend.experiment.SubscribeUnsubscribeExperimentTask;
import com.apisense.bee.ui.entity.ExperimentSerializable;

/**
 * Shows detailed informations about a given available Experiment from the store
 *
 */
public class StoreExperimentDetailsActivity extends Activity {
    private final String TAG = getClass().getSimpleName();

    private APSLocalCrop experiment;

    TextView mExperimentName;
    TextView mExperimentOrganization;
    TextView mExperimentVersion;

     MenuItem mSubscribeButton;

    // Async Tasks
    private SubscribeUnsubscribeExperimentTask experimentChangeSubscriptionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_experiment_details);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        initializeViews();
        displayExperimentInformation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_back_in,R.anim.slide_back_out);
    }
    public void initializeViews() {
        mExperimentName = (TextView) findViewById(R.id.store_detail_exp_name);
        mExperimentOrganization = (TextView) findViewById(R.id.store_detail_exp_organization);
        mExperimentVersion = (TextView) findViewById(R.id.store_detail_exp_version);
    }

    public void displayExperimentInformation() {
        Bundle b = getIntent().getExtras();
        // TODO : Switch to parcelable when available
        // Experiment expe =  b.getParcelable("experiment");
        // TODO Send directly experiment instead of experimentSerializable when possible
        ExperimentSerializable experimentS  = (ExperimentSerializable) b.getSerializable("experiment");
        try {
            experiment = APS.getCropDescription(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        experiment = APISENSE.apisServerService().getRemoteExperiment(experimentS.getName());

        mExperimentName.setText(experiment.getNiceName());
        mExperimentOrganization.setText(experiment.organization);
        mExperimentVersion.setText(" - v" + experiment.version);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.store_experiment_details, menu);

        mSubscribeButton = menu.findItem(R.id.store_detail_action_subscribe);
        updateSubscriptionMenu();
        return true;
    }

    private void updateSubscriptionMenu(){
        // TODO: Change to API method when available (isSubscribedExperiment)
        if (!SubscribeUnsubscribeExperimentTask.isInstalled(this, experiment.getName())) {
            mSubscribeButton.setTitle(getString(R.string.action_subscribe));
        } else {
            mSubscribeButton.setTitle(getString(R.string.action_unsubscribe));

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out);
    }

    public void doSubscribeUnsubscribe(MenuItem item) {
        if (experimentChangeSubscriptionStatus == null) {
            experimentChangeSubscriptionStatus = new SubscribeUnsubscribeExperimentTask(getApplicationContext(), new OnExperimentSubscriptionChanged());
            experimentChangeSubscriptionStatus.execute(experiment.getName());
        }
    }

    private class OnExperimentSubscriptionChanged implements Callback<Integer> {

        @Override
        public void onCall(Integer response) throws Exception {
            experimentChangeSubscriptionStatus = null;
            String experimentName = experiment.getNiceName();
            String toastMessage = "";
            switch (response){
                case SubscribeUnsubscribeExperimentTask.EXPERIMENT_SUBSCRIBED:
                    toastMessage = String.format(getString(R.string.experiment_subscribed), experimentName);
                    updateSubscriptionMenu();
                    break;
                case SubscribeUnsubscribeExperimentTask.EXPERIMENT_UNSUBSCRIBED:
                    toastMessage = String.format(getString(R.string.experiment_unsubscribed), experimentName);
                    updateSubscriptionMenu();
                    break;
            }
            // User feedback
            Toast.makeText(getBaseContext(), toastMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Throwable throwable) {
            experimentChangeSubscriptionStatus = null;
        }
    }
}
