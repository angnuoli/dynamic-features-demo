package com.gjxhlan.dynamicfeaturesdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.play.core.splitinstall.SplitInstallHelper;
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
    private final String dynamicFeatureActivity1 = "com.gjxhlan.dynamicfeatures.ondemand.DynamicFeatureActivity1";

    Button showImageButton, uninstall, immediatelyShow;
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
        immediatelyShow = findViewById(R.id.show_images_collection_button_no_restart);
        if (manager.getInstalledModules().contains(moduleName)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(packageName, dynamicFeatureActivity);
            if (getPackageManager().resolveActivity(intent, 0) == null) {
                showImageButton.setText("Wait for installing module");
            } else {
                showImageButton.setText("Show Images Collection");
                immediatelyShow.setVisibility(View.VISIBLE);
            }
        }
        uninstall = findViewById(R.id.uninstall_module);
        progressText = findViewById(R.id.progress_text);
        progressBar = findViewById(R.id.progress_bar);
        setupClickListerner();
    }

    private void setupClickListerner() {
        Drawable save = showImageButton.getBackground();
        Drawable save1 = uninstall.getBackground();
        Drawable save2 = immediatelyShow.getBackground();
        showImageButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    showImageButton.setBackgroundColor(getResources().getColor(R.color.clr_pressed, getTheme()));
                    break;
                case MotionEvent.ACTION_UP:
                    showImageButton.setBackground(save);
            }
            return false;
        });

        showImageButton.setOnClickListener(view -> loadAndLaunchModule(moduleName));
        uninstall.setOnClickListener(view -> requestUninstall());
        uninstall.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    uninstall.setBackgroundColor(getResources().getColor(R.color.clr_pressed, getTheme()));
                    break;
                case MotionEvent.ACTION_UP:
                    uninstall.setBackground(save1);
            }
            return false;
        });
        immediatelyShow.setOnClickListener(view -> launchAcitivity(dynamicFeatureActivity1));
        immediatelyShow.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    immediatelyShow.setBackgroundColor(getResources().getColor(R.color.clr_pressed, getTheme()));
                    break;
                case MotionEvent.ACTION_UP:
                    immediatelyShow.setBackground(save2);
            }
            return false;
        });
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
                            if (26 <= Build.VERSION.SDK_INT) {
                                Log.d("angnuo_test", String.valueOf(Build.VERSION.SDK_INT));
                                SplitInstallHelper.updateAppInfo(getApplicationContext());
                            }
                            showRestartDialog();
                            immediatelyShow.setVisibility(View.VISIBLE);
                            hideProgress();
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

    private void showRestartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message");
        builder.setMessage("For new feature onboard, please restart Launcher.");
        builder.setIcon(R.mipmap.ic_launcher_round);

        builder.setCancelable(true);
        // confirm
        builder.setPositiveButton("Restart", (dialog, which) -> {
            System.exit(0);
            dialog.dismiss();
        });
        // dismiss
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
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
        startActivity(intent);
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
