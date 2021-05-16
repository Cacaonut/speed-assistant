package me.cacaonut.speedassistant;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private TextView textViewSpeedLimit;
    private TextView textViewRoad;
    private TextView textViewSpeed;
    private ImageButton buttonService;
    private ImageButton buttonSettings;
    private ConstraintLayout layout;
    private ImageView imageViewSign;
    private SharedPreferences sharedPreferences;

    private boolean acceptData;
    private String displayMode = "txt";

    private boolean resultsFound = false;
    private String speedLimit = "unknown";
    private float speed = 0f;
    private String roadName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        createNotificationChannel();

        textViewSpeedLimit = findViewById(R.id.textView_speedLimit);
        textViewRoad = findViewById(R.id.textView_road);
        textViewSpeed = findViewById(R.id.textView_speed);
        buttonService = findViewById(R.id.button_service);
        buttonService.setOnClickListener(v -> {
            if (isServiceRunning(AssistantService.class)) stopService();
            else startService();
        });
        buttonSettings = findViewById(R.id.button_settings);
        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        layout = findViewById(R.id.layout);
        imageViewSign = findViewById(R.id.imageView_sign);

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        if (isServiceRunning(AssistantService.class)) {
            buttonService.setImageResource(R.drawable.ic_stop);
            acceptData = true;
        } else {
            buttonService.setImageResource(R.drawable.ic_start);
            acceptData = false;
        }
        updateUI();

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //startService();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver,
                new IntentFilter("data"));
    }

    @Override
    protected void onResume() {
        updateUI();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel statusChannel = new NotificationChannel(
                    "STATUS_CHANNEL",
                    "Status Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(statusChannel);
        }
    }

    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (acceptData) {
                resultsFound = intent.getBooleanExtra("resultsFound", false);
                if (resultsFound) {
                    speedLimit = intent.getStringExtra("speedLimit");
                    speed = intent.getFloatExtra("currentSpeed", 0f);
                    roadName = intent.getStringExtra("roadName");
                }
                updateUI();
            }
        }
    };

    private void startService() {
        if (!isServiceRunning(AssistantService.class)) {
            Intent intent = new Intent(this, AssistantService.class);
            startService(intent);
        }
        buttonService.setImageResource(R.drawable.ic_stop);
        acceptData = true;
        updateUI();
    }

    private void stopService() {
        if (isServiceRunning(AssistantService.class)) {
            Intent intent = new Intent(this, AssistantService.class);
            stopService(intent);
        }
        buttonService.setImageResource(R.drawable.ic_start);
        acceptData = false;
        updateUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                //startService();
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void updateUI() {
        displayMode = sharedPreferences.getString("style", "txt");

        if (isServiceRunning(AssistantService.class) && acceptData) {
            if (resultsFound) {
                float speedLimitNmb = -1;
                boolean special = true;
                if (TextUtils.isDigitsOnly(speedLimit)) {
                    speedLimitNmb = Integer.parseInt(speedLimit);
                    displayStandardLimit(speedLimitNmb);
                    special = false;
                } else if (TextUtils.isDigitsOnly(speedLimit.split(" ")[0])) {
                    if (speedLimit.split(" ")[1].equalsIgnoreCase("mph")) {
                        speedLimitNmb = Integer.parseInt(speedLimit.split(" ")[0]) * 1.609f;
                        displayStandardLimit(speedLimitNmb);
                        special = false;
                    }
                } else if (speedLimit.equalsIgnoreCase("walk")) {
                    displaySpecialLimit(speedLimit);
                    speedLimitNmb = 7;
                } else if (speedLimit.equalsIgnoreCase("none")) {
                    displaySpecialLimit(speedLimit);
                    speedLimitNmb = 999;
                }
                textViewRoad.setText(roadName);
                String unit = "km/h";
                float factor = 3.6f;
                if (displayMode.equalsIgnoreCase("us")) {
                    unit = "mph";
                    factor = 2.23694f;
                }
                textViewSpeed.setText((int) (speed * factor) + " " + unit);
                if (speedLimitNmb < 0) {
                    layout.setBackgroundColor(Color.parseColor("#ffcc00"));
                    textViewSpeed.setTextColor(Color.DKGRAY);
                    textViewRoad.setTextColor(Color.DKGRAY);
                    if (special || displayMode.equalsIgnoreCase("txt"))
                        textViewSpeedLimit.setTextColor(Color.DKGRAY);
                } else if (speed > speedLimitNmb) {
                    layout.setBackgroundColor(Color.RED);
                    textViewSpeed.setTextColor(Color.WHITE);
                    textViewRoad.setTextColor(Color.WHITE);
                    if (special || displayMode.equalsIgnoreCase("txt"))
                        textViewSpeedLimit.setTextColor(Color.WHITE);
                } else {
                    layout.setBackground(null);
                    textViewSpeed.setTextColor(Color.parseColor("#ffbbbbbb"));
                    textViewRoad.setTextColor(Color.parseColor("#ffbbbbbb"));
                    if (special || displayMode.equalsIgnoreCase("txt"))
                        textViewSpeedLimit.setTextColor(Color.parseColor("#ffbbbbbb"));
                }
            } else {
                textViewSpeedLimit.setText("");
                textViewRoad.setText("Searching for road...");
                textViewSpeed.setText("");
            }
        } else {
            textViewSpeedLimit.setText("");
            textViewRoad.setText("Not active");
            textViewSpeed.setText("");
        }
    }

    private void displayStandardLimit(float speedLimit) {
        switch (displayMode) {
            case "de":
                imageViewSign.setVisibility(View.VISIBLE);
                imageViewSign.setImageResource(R.drawable.speed_sign_de);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    textViewSpeedLimit.setTypeface(Typeface.create(Typeface.DEFAULT, 500, false));
                textViewSpeedLimit.setTextColor(Color.BLACK);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) textViewSpeedLimit.getLayoutParams();
                params.verticalBias = 0.5f;
                textViewSpeedLimit.setLayoutParams(params);
                textViewSpeedLimit.setText((int) speedLimit + "");
                break;
            case "us":
                imageViewSign.setVisibility(View.VISIBLE);
                imageViewSign.setImageResource(R.drawable.speed_sign_us);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    textViewSpeedLimit.setTypeface(Typeface.create(Typeface.DEFAULT, 500, false));
                params = (ConstraintLayout.LayoutParams) textViewSpeedLimit.getLayoutParams();
                params.verticalBias = 1.1f;
                textViewSpeedLimit.setLayoutParams(params);
                textViewSpeedLimit.setTextColor(Color.BLACK);
                textViewSpeedLimit.setText(Math.round(speedLimit * 0.621371f) + "");
                break;
            default:
                imageViewSign.setVisibility(View.INVISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    textViewSpeedLimit.setTypeface(Typeface.DEFAULT);
                params = (ConstraintLayout.LayoutParams) textViewSpeedLimit.getLayoutParams();
                params.verticalBias = 0.5f;
                textViewSpeedLimit.setLayoutParams(params);
                textViewSpeedLimit.setText((int) speedLimit + "");
                break;
        }
    }

    private void displaySpecialLimit(String speedLimit) {
        switch (displayMode) {
            case "de":
                imageViewSign.setVisibility(View.VISIBLE);
                if (speedLimit.equalsIgnoreCase("walk"))
                    imageViewSign.setImageResource(R.drawable.walk_sign_de);
                else if (speedLimit.equalsIgnoreCase("none"))
                    imageViewSign.setImageResource(R.drawable.none_speed_sign_de);
                textViewSpeedLimit.setText("");
                break;
            default:
                textViewSpeedLimit.setTypeface(Typeface.DEFAULT);
                textViewSpeedLimit.setText(speedLimit);
                break;
        }
    }
}