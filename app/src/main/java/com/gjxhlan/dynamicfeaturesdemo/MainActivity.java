package com.gjxhlan.dynamicfeaturesdemo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private SplitInstallManager manager;
    private final String TAG = "dynamic-test";
    private final String moduleName = "dynamicFeature";
    private final String packageName = "com.gjxhlan.dynamicfeatures";
    private final String dynamicFeatureActivity = "com.gjxhlan.dynamicfeatures.DynamicFeatureActivity";
    Button showImageButton, uninstall;
    TextView progressText;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = SplitInstallManagerFactory.create(this);
        initializeViews();
    }

    @Override
    protected void onResume() {
        manager.registerListener(listener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        manager.unregisterListener(listener);
        super.onPause();
    }

    private void initializeViews() {
        showImageButton = findViewById(R.id.show_images_collection_button);
        if (manager.getInstalledModules().contains(moduleName)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(packageName, dynamicFeatureActivity);
            if (getPackageManager().resolveActivity(intent, 0) == null) {
                showImageButton.setText("Wait for installing module");
            } else {
                showImageButton.setText("Show Images Collection");
            }
        }
        uninstall = findViewById(R.id.uninstall_module);
        progressText = findViewById(R.id.progress_text);
        progressBar = findViewById(R.id.progress_bar);
        setupClickListerner();
    }

    private void setupClickListerner() {
        showImageButton.setOnClickListener(view -> loadAndLaunchModule(moduleName));
        uninstall.setOnClickListener(view -> requestUninstall());
    }

    private void loadAndLaunchModule(String name) {
        updateProgressMessage("Loading module " + name);
        if (manager.getInstalledModules().contains(name)) {
            updateProgressMessage("Already installed");
            onSuccessfulLoad(name, true);
            return;
        }

        SplitInstallRequest request = SplitInstallRequest.newBuilder()
                .addModule(name)
                .build();

        manager.startInstall(request);

        updateProgressMessage("Starting install for " + name);
    }

    /** Request uninstall of all features. */
    private void requestUninstall() {

        toastAndLog("Requesting uninstall of all modules." +
                "This will happen at some point in the future.");

        List<String> installedModules = new ArrayList<>(manager.getInstalledModules());
        manager.deferredUninstall(installedModules).addOnSuccessListener(aVoid -> toastAndLog("Uninstalling " + installedModules.toString()));
    }

    Listener listener = new Listener();

    class Listener implements SplitInstallStateUpdatedListener {

        @Override
        public void onStateUpdate(SplitInstallSessionState state) {
            for (String module: state.moduleNames()) {
                switch (state.status()) {
                        case SplitInstallSessionStatus.DOWNLOADING: displayLoadingState(state, "Downloading " + module);
                                                                break;
                        case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                            try {
                                startIntentSender(state.resolutionIntent().getIntentSender(), null, 0,0,0);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case SplitInstallSessionStatus.INSTALLED:
                            Context context = getBaseContext();
                            try {
                                Context newContext = context.createPackageContext(getPackageName(), 0);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            onSuccessfulLoad(module, true);
                            break;
                        case SplitInstallSessionStatus.INSTALLING:
                            displayLoadingState(state, "Downloading " + module);
                            break;
                        case SplitInstallSessionStatus.FAILED:
                            Log.e(TAG, "Error " + state.errorCode() + " for module " + module);
                            break;
                }
            }
        }
    }

    private void displayLoadingState(SplitInstallSessionState state, String message) {
        displayProgress();

        progressBar.setMax((int)state.totalBytesToDownload());
        progressBar.setProgress((int) state.bytesDownloaded());

        Log.d(TAG, "Total: " + state.totalBytesToDownload());
        Log.d(TAG, state.bytesDownloaded() + "");

        updateProgressMessage(message);
    }

    private void updateProgressMessage(String message) {
        if (progressText.getVisibility() != View.VISIBLE) displayProgress();
        progressText.setText(message);
    }

    private void onSuccessfulLoad(String moduleName, boolean launch) {
        if (launch) {
            try {
                launchAcitivity(dynamicFeatureActivity);
            } catch (Exception e) {
                StringWriter erros = new StringWriter();
                e.printStackTrace(new PrintWriter(erros));
                Log.d(TAG, erros.toString());
                Log.d(TAG, getBaseContext().getPackageCodePath());
            }
        }
        hideProgress();
    }

    private void launchAcitivity(String className) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(getPackageName(), className);
        if (getPackageManager().queryIntentActivities(intent, 0).size() != 0) {
            Log.d(TAG, getPackageManager().queryIntentActivities(intent, 0).toString());
            startActivity(intent);
        }
    }

    private void toastAndLog(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        Log.d(TAG, text);
    }

    private void hideProgress() {
        progressText.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void displayProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
    }
}
