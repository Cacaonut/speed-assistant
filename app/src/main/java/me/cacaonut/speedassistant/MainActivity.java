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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import me.cacaonut.speedassistant.conditions.Condition;
import timber.log.Timber;

import static me.cacaonut.speedassistant.AssistantService.evaluateConditions;
import static me.cacaonut.speedassistant.classes.SignBuilder.buildDualSign;
import static me.cacaonut.speedassistant.classes.SignBuilder.buildSign;
import static me.cacaonut.speedassistant.classes.SignBuilder.buildTripleSign;

public class MainActivity extends AppCompatActivity {

    private TextView textViewRoad;
    private TextView textViewSpeed;
    private ImageButton buttonService;
    private ConstraintLayout layout;
    private ImageView imageViewSign;
    private ImageView imageViewSignSmall;
    private SharedPreferences sharedPreferences;

    private boolean acceptData;

    private boolean resultsFound = false;
    private float speedLimit = -1;
    private boolean isVariableLimit = false;
    private float[] speedLimitsConditional = new float[0];
    private String[][] conditions = new String[0][];
    private int useConditionalLimit = -1;
    private float speed = 0f;
    private String roadName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        createNotificationChannel();

        textViewRoad = findViewById(R.id.textView_road);
        textViewSpeed = findViewById(R.id.textView_speed);
        buttonService = findViewById(R.id.button_service);
        buttonService.setOnClickListener(v -> {
            if (isServiceRunning(AssistantService.class)) stopService();
            else startService();
        });
        ImageButton buttonSettings = findViewById(R.id.button_settings);
        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        layout = findViewById(R.id.layout);
        imageViewSign = findViewById(R.id.imageView_sign);
        imageViewSignSmall = findViewById(R.id.imageView_sign_small);

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
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(dataReceiver,
                new IntentFilter("data"));
    }

    @Override
    protected void onResume() {
        SpeedAssistant.activityActive = true;
        updateUI();
        super.onResume();
    }

    @Override
    protected void onPause() {
        SpeedAssistant.activityActive = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dataReceiver);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            NotificationChannel statusChannel = new NotificationChannel(
                    "STATUS_CHANNEL",
                    "Status Notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(statusChannel);
            NotificationChannel pushChannel = new NotificationChannel(
                    "PUSH_CHANNEL",
                    "Warning Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(pushChannel);
        }
    }

    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (acceptData) {
                resultsFound = intent.getBooleanExtra("resultsFound", false);
                if (resultsFound) {
                    speedLimit = intent.getFloatExtra("speedLimit", -1);
                    isVariableLimit = intent.getBooleanExtra("isVariableLimit", false);
                    speedLimitsConditional = intent.getFloatArrayExtra("speedLimitsConditional");
                    conditions = (String[][]) intent.getSerializableExtra("speedLimitsConditions");
                    useConditionalLimit = intent.getIntExtra("useConditionalLimit", -1);
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
        resultsFound = false;
        updateUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("resultsFound", resultsFound);
        outState.putFloat("speedLimit", speedLimit);
        outState.putBoolean("isVariableLimit", isVariableLimit);
        outState.putFloatArray("speedLimitsConditional", speedLimitsConditional);
        outState.putSerializable("speedLimitsConditions", conditions);
        outState.putInt("useConditionalLimit", useConditionalLimit);
        outState.putFloat("speed", speed);
        outState.putString("roadName", roadName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        resultsFound = savedInstanceState.getBoolean("resultsFound");
        speedLimit = savedInstanceState.getFloat("speedLimit");
        isVariableLimit = savedInstanceState.getBoolean("isVariableLimit");
        speedLimitsConditional = savedInstanceState.getFloatArray("speedLimitsConditional");
        conditions = (String[][]) savedInstanceState.getSerializable("speedLimitsConditions");
        useConditionalLimit = savedInstanceState.getInt("useConditionalLimit");
        speed = savedInstanceState.getFloat("speed");
        roadName = savedInstanceState.getString("roadName");
        updateUI();
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
        String displayMode = sharedPreferences.getString("style", "txt");

        if (isServiceRunning(AssistantService.class) && acceptData) {
            if (resultsFound) {
                textViewRoad.setText(roadName);

                String unit = sharedPreferences.getString("unit", "km/h");
                float factor = 1;
                switch (unit) {
                    case "km/h":
                        factor = 3.6f;
                        break;
                    case "mph":
                        factor = 2.23694f;
                        break;
                }
                float speedCalc = speed * factor;
                float speedLimitNmbCalc = speedLimit * factor;
                if (speedLimit < 0 || speedLimit >= 999) speedLimitNmbCalc = speedLimit;

                float[] speedLimitConditionalNmbCalc = new float[speedLimitsConditional.length];
                for (int i = 0; i < speedLimitsConditional.length; i++) {
                    speedLimitConditionalNmbCalc[i] = speedLimitsConditional[i] * factor;
                    if (speedLimitsConditional[i] < 0 || speedLimitsConditional[i] >= 999)
                        speedLimitConditionalNmbCalc[i] = speedLimitsConditional[i];
                }

                float applicantSpeedLimit = speedLimitNmbCalc;
                if (useConditionalLimit >= 0)
                    applicantSpeedLimit = speedLimitConditionalNmbCalc[useConditionalLimit];
                if (applicantSpeedLimit == -2)
                    applicantSpeedLimit = sharedPreferences.getInt("walk_speed", 7);

                int tolerance = sharedPreferences.getInt("speed_tolerance_low", 0);
                // Timber.d("Speed limit: %f", applicantSpeedLimit);
                // Timber.d("Tolerance: %d", tolerance);
                if (applicantSpeedLimit == -1) {
                    layout.setBackgroundColor(Color.parseColor("#ffcc00"));
                    textViewSpeed.setTextColor(Color.DKGRAY);
                    textViewRoad.setTextColor(Color.DKGRAY);
                } else if ((int) (speedCalc - tolerance) > (int) applicantSpeedLimit) {
                    layout.setBackgroundColor(Color.RED);
                    textViewSpeed.setTextColor(Color.WHITE);
                    textViewRoad.setTextColor(Color.WHITE);
                } else if (isVariableLimit) {
                    layout.setBackgroundColor(Color.parseColor("#ffcc00"));
                    textViewSpeed.setTextColor(Color.DKGRAY);
                    textViewRoad.setTextColor(Color.DKGRAY);
                } else {
                    layout.setBackground(null);
                    textViewSpeed.setTextColor(Color.parseColor("#ffbbbbbb"));
                    textViewRoad.setTextColor(Color.parseColor("#ffbbbbbb"));
                }

                imageViewSign.setVisibility(View.INVISIBLE);
                imageViewSignSmall.setVisibility(View.INVISIBLE);
                Bitmap sign;
                if (speedLimitsConditional.length > 0) {
                    boolean alwaysAllLimits = sharedPreferences.getBoolean("always_all_limits", false);
                    if (isVariableLimit || alwaysAllLimits) {
                        imageViewSign.setVisibility(View.VISIBLE);
                        if (speedLimitsConditional.length == 1) {
                            Condition[] conditionArray = evaluateConditions(conditions[0]);
                            String[] conditionsStr = new String[conditionArray.length];
                            for (int i = 0; i < conditionArray.length; i++) {
                                conditionsStr[i] = conditionArray[i].toPrettyString(this, displayMode);
                            }
                            sign = buildDualSign(this, (int) speedLimitNmbCalc, (int) speedLimitConditionalNmbCalc[0], conditionsStr, displayMode, textViewRoad.getCurrentTextColor());
                        } else {
                            Condition[] conditionArray0 = evaluateConditions(conditions[0]);
                            String[] conditionsStr0 = new String[conditionArray0.length];
                            for (int i = 0; i < conditionArray0.length; i++) {
                                conditionsStr0[i] = conditionArray0[i].toPrettyString(this, displayMode);
                            }
                            Condition[] conditionArray1 = evaluateConditions(conditions[1]);
                            String[] conditionsStr1 = new String[conditionArray1.length];
                            for (int i = 0; i < conditionArray1.length; i++) {
                                conditionsStr1[i] = conditionArray1[i].toPrettyString(this, displayMode);
                            }
                            sign = buildTripleSign(this, (int) speedLimitNmbCalc, (int) speedLimitConditionalNmbCalc[0], conditionsStr0, (int) speedLimitConditionalNmbCalc[1], conditionsStr1, displayMode, textViewRoad.getCurrentTextColor());
                        }

                        imageViewSign.setImageBitmap(sign);
                    } else {
                        imageViewSignSmall.setVisibility(View.VISIBLE);
                        if (useConditionalLimit >= 0)
                            sign = buildSign(this, (int) speedLimitConditionalNmbCalc[useConditionalLimit], displayMode, textViewRoad.getCurrentTextColor());
                        else
                            sign = buildSign(this, (int) speedLimitNmbCalc, displayMode, textViewRoad.getCurrentTextColor());
                        imageViewSignSmall.setImageBitmap(sign);
                    }
                } else {
                    imageViewSignSmall.setVisibility(View.VISIBLE);
                    sign = buildSign(this, (int) speedLimitNmbCalc, displayMode, textViewRoad.getCurrentTextColor());
                    imageViewSignSmall.setImageBitmap(sign);
                }

                textViewSpeed.setText((int) speedCalc + " " + unit);
            } else {
                layout.setBackground(null);
                textViewSpeed.setTextColor(Color.parseColor("#ffbbbbbb"));
                textViewRoad.setTextColor(Color.parseColor("#ffbbbbbb"));
                imageViewSign.setVisibility(View.INVISIBLE);
                imageViewSignSmall.setVisibility(View.INVISIBLE);
                textViewRoad.setText(R.string.searching_road);
                textViewSpeed.setText("");
            }
        } else {
            layout.setBackground(null);
            textViewSpeed.setTextColor(Color.parseColor("#ffbbbbbb"));
            textViewRoad.setTextColor(Color.parseColor("#ffbbbbbb"));
            imageViewSign.setVisibility(View.INVISIBLE);
            imageViewSignSmall.setVisibility(View.INVISIBLE);
            textViewRoad.setText(R.string.not_active);
            textViewSpeed.setText("");
        }
    }
}