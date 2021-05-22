package me.cacaonut.speedassistant;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.cacaonut.speedassistant.classes.ImplicitSpeedLimits;
import me.cacaonut.speedassistant.classes.Node;
import me.cacaonut.speedassistant.conditions.Condition;
import me.cacaonut.speedassistant.conditions.SpecialCondition;
import me.cacaonut.speedassistant.conditions.TimeCondition;
import me.cacaonut.speedassistant.classes.Way;
import me.cacaonut.speedassistant.conditions.UnknownCondition;
import timber.log.Timber;

import static me.cacaonut.speedassistant.classes.SignBuilder.buildSign;

public class AssistantService extends Service {

    private static final String TAG = "AssistantService";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SharedPreferences sharedPreferences;

    private boolean warnedLow = false;
    private boolean warnedHigh = false;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        startLocationUpdates();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "STATUS_CHANNEL")
                .setContentTitle("Speed Assistant")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_notification_normal)
                .setContentIntent(pendingIntent)
                .build();

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NotNull LocationResult locationResult) {
                Location loc = locationResult.getLastLocation();
                // Timber.d("Location: (%f, %f)", loc.getLatitude(), loc.getLongitude());
                int radius = 20;
                String url = "https://overpass.kumi.systems/api/interpreter?data=[out:json];(way(around:" + radius + "," + loc.getLatitude() + "," + loc.getLongitude() + ")[highway~\"motorway|trunk|primary|secondary|tertiary|unclassified|residential|living_street\"];way(around:" + radius + "," + loc.getLatitude() + "," + loc.getLongitude() + ")[highway~\"service|track|road\"][maxspeed];);(._;%3E;);out;";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, response -> {
                            try {
                                JSONArray results = response.getJSONArray("elements");
                                Way nearestWay = selectRoad(results, loc);
                                if (nearestWay != null) {
                                    Timber.d("Road: %s", nearestWay.toString());

                                    String unit = sharedPreferences.getString("unit", "km/h");
                                    float factor = 1;
                                    switch (unit) {
                                        case "km/h":
                                            factor = 0.277777777778f;
                                            break;
                                        case "mph":
                                            factor = 0.447040357632f;
                                            break;
                                    }

                                    // Normal speed limit
                                    float speedLimitNmb = parseSpeedLimit(nearestWay.getSpeedLimit());

                                    // Conditional speed limit
                                    String[] speedLimitsConditional = nearestWay.getConditionLimits();
                                    float[] speedLimitsConditionalNmb = new float[speedLimitsConditional.length];
                                    for (int i = 0; i < speedLimitsConditional.length; i++) {
                                        speedLimitsConditionalNmb[i] = parseSpeedLimit(speedLimitsConditional[i]);
                                    }

                                    String[][] conditions = nearestWay.getConditions();
                                    int useConditionLimit = -1;
                                    boolean isVariableLimit = nearestWay.isVariable();
                                    float currentMaxSpeed = 999;
                                    for (int i = 0; i < conditions.length; i++) {
                                        Condition[] conditionsOfSign = evaluateConditions(conditions[i]);
                                        for (Condition condition : conditionsOfSign) {
                                            if (!condition.isApplicable())
                                                isVariableLimit = true;
                                            if (condition.applies() && speedLimitsConditionalNmb[i] < currentMaxSpeed) {
                                                useConditionLimit = i;
                                                currentMaxSpeed = speedLimitsConditionalNmb[i];
                                            }
                                        }
                                    }

                                    float speedLimitCalc = speedLimitNmb;
                                    if (useConditionLimit >= 0)
                                        speedLimitCalc = speedLimitsConditionalNmb[useConditionLimit];
                                    if (speedLimitCalc == -2)
                                        speedLimitCalc = sharedPreferences.getInt("walk_speed", 7);

                                    float speed = loc.getSpeed();
                                    // speed = sharedPreferences.getInt("speed", 20);

                                    // High tolerance warning
                                    int toleranceHigh = sharedPreferences.getInt("speed_tolerance_high", 0);
                                    float toleranceHighCalc = toleranceHigh * factor;
                                    if (speedLimitCalc > 0 && speed - toleranceHighCalc > speedLimitCalc) {
                                        if (!warnedHigh) {
                                            if (sharedPreferences.getBoolean("vibration_high", true)) {
                                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    v.vibrate(VibrationEffect.createOneShot(1500, 255));
                                                } else {
                                                    v.vibrate(1000);
                                                }
                                            }

                                            String soundHigh = sharedPreferences.getString("sound_high", "buzz_2");
                                            if (!soundHigh.equalsIgnoreCase("none")) {
                                                int resId = getResources().getIdentifier(soundHigh, "raw", getPackageName());
                                                final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), resId);
                                                mp.start();
                                            }

                                            if (!SpeedAssistant.activityActive) {
                                                String displayMode = sharedPreferences.getString("style", "txt");

                                                float factor2 = 1;
                                                switch (unit) {
                                                    case "km/h":
                                                        factor2 = 3.6f;
                                                        break;
                                                    case "mph":
                                                        factor2 = 2.23694f;
                                                        break;
                                                }
                                                float speedLimitReal;
                                                if (useConditionLimit >= 0) {
                                                    speedLimitReal = speedLimitsConditionalNmb[useConditionLimit] * factor2;
                                                    if (speedLimitsConditionalNmb[useConditionLimit] < 0 || speedLimitsConditionalNmb[useConditionLimit] >= 999)
                                                        speedLimitReal = speedLimitsConditionalNmb[useConditionLimit];
                                                } else {
                                                    speedLimitReal = speedLimitNmb * factor2;
                                                    if (speedLimitNmb < 0 || speedLimitNmb >= 999)
                                                        speedLimitReal = speedLimitNmb;
                                                }

                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "PUSH_CHANNEL")
                                                        .setSmallIcon(R.drawable.ic_notification_normal)
                                                        .setColor(Color.RED)
                                                        .setContentTitle("Mind your speed!")
                                                        .setContentText("You're currently driving above the speed limit.")
                                                        .setContentIntent(pendingIntent);

                                                builder.setLargeIcon(buildSign(getApplicationContext(), (int) speedLimitReal, displayMode, Color.DKGRAY));


                                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                                notificationManager.notify(2, builder.build());
                                            }
                                            warnedHigh = true;
                                        }
                                    } else {
                                        warnedHigh = false;

                                        // Low tolerance warning
                                        int toleranceLow = sharedPreferences.getInt("speed_tolerance_low", 0);
                                        float toleranceLowCalc = toleranceLow * factor;
                                        if (speedLimitCalc > 0 && (speed - toleranceLowCalc) > speedLimitCalc) {
                                            if (!warnedLow) {
                                                if (sharedPreferences.getBoolean("vibration_low", true)) {
                                                    Timber.d("Vibrating");
                                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
                                                    } else {
                                                        v.vibrate(1500);
                                                    }
                                                }

                                                String soundLow = sharedPreferences.getString("sound_low", "none");
                                                if (!soundLow.equalsIgnoreCase("none")) {
                                                    int resId = getResources().getIdentifier(soundLow, "raw", getPackageName());
                                                    final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), resId);
                                                    mp.start();
                                                }

                                                warnedLow = true;
                                            }
                                        } else {
                                            warnedLow = false;
                                        }
                                    }

                                    Intent intent = new Intent("data");
                                    intent.putExtra("resultsFound", true);
                                    intent.putExtra("speedLimit", speedLimitNmb);
                                    intent.putExtra("isVariableLimit", isVariableLimit);
                                    intent.putExtra("useConditionalLimit", useConditionLimit);
                                    intent.putExtra("speedLimitsConditional", speedLimitsConditionalNmb);
                                    intent.putExtra("speedLimitsConditions", nearestWay.getConditions());
                                    intent.putExtra("roadName", nearestWay.getName());
                                    intent.putExtra("currentSpeed", speed);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                } else {
                                    Timber.d("No road found.");
                                    Intent intent = new Intent("data");
                                    intent.putExtra("resultsFound", false);
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                                }
                            } catch (JSONException e) {
                                Timber.e(e);
                            }
                        }, error -> Timber.d("Error: %s", error.getMessage()));
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(jsonObjectRequest);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private float parseSpeedLimit(String speedLimit) {
        float speedLimitNmb = -1;
        if (ImplicitSpeedLimits.values.containsKey(speedLimit)) {
            speedLimit = ImplicitSpeedLimits.values.get(speedLimit);
        }
        if (TextUtils.isDigitsOnly(speedLimit)) {
            speedLimitNmb = Integer.parseInt(speedLimit) / 3.6f;
        } else if (TextUtils.isDigitsOnly(speedLimit.split(" ")[0])) {
            String unitStr = speedLimit.split(" ")[1];
            if (unitStr.equalsIgnoreCase("mph")) {
                speedLimitNmb = Integer.parseInt(speedLimit.split(" ")[0]) * 0.44704f;
            } else if (unitStr.equalsIgnoreCase("km/h") || unitStr.equalsIgnoreCase("kmh") || unitStr.equalsIgnoreCase("kph")) {
                speedLimitNmb = Integer.parseInt(speedLimit.split(" ")[0]) / 3.6f;
            } else if (unitStr.equalsIgnoreCase("knots")) {
                speedLimitNmb = Integer.parseInt(speedLimit.split(" ")[0]) * 0.514444f;
            }
        } else if (speedLimit.equalsIgnoreCase("walk")) {
            speedLimitNmb = -2;
        } else if (speedLimit.equalsIgnoreCase("none")) {
            speedLimitNmb = 999;
        }

        return speedLimitNmb;
    }

    public static Condition[] evaluateConditions(String[] conditions) {
        Condition[] conditionArray = new Condition[conditions.length];
        for (int i = 0; i < conditions.length; i++) {
            String condition = conditions[i];
            condition = condition.trim();
            if (condition.matches("^((Mo|Tu|We|Th|Fr|Sa|So)|(Mo|Tu|We|Th|Fr|Sa|So)-(Mo|Tu|We|Th|Fr|Sa|So))? ?(\\d\\d:\\d\\d-\\d\\d:\\d\\d)?$")) {
                // Weekdays and Time
                TimeCondition timeCondition = TimeCondition.from(condition);
                conditionArray[i] = timeCondition;
            } else if (condition.equalsIgnoreCase("ph off")) {
                conditionArray[i] = new SpecialCondition(R.string.ph_off, false);
            } else if (condition.equalsIgnoreCase("ph on")) {
                conditionArray[i] = new SpecialCondition(R.string.ph_on, false);
            } else if (condition.equalsIgnoreCase("sh off")) {
                conditionArray[i] = new SpecialCondition(R.string.sh_off, false);
            } else if (condition.equalsIgnoreCase("sh on")) {
                conditionArray[i] = new SpecialCondition(R.string.sh_on, false);
            } else if (condition.equalsIgnoreCase("wet")) {
                conditionArray[i] = new SpecialCondition(R.string.wet, false);
            } else if (condition.equalsIgnoreCase("snow")) {
                conditionArray[i] = new SpecialCondition(R.string.snow, false);
            } else {
                conditionArray[i] = new UnknownCondition();
            }
        }
        return conditionArray;
    }

    private Way selectRoad(JSONArray results, Location loc) throws JSONException {
        List<Node> nodes = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            if (result.getString("type").equalsIgnoreCase("node")) {
                nodes.add(new Node(result.getLong("id"), result.getDouble("lat"), result.getDouble("lon")));
            } else if (result.getString("type").equalsIgnoreCase("way")) {
                // Id
                int id = result.getInt("id");

                // Name
                String name = result.getJSONObject("tags").optString("name");
                if (name.equals("")) name = result.getJSONObject("tags").optString("ref");
                if (name.equals("")) name = "-";

                // Points
                JSONArray wayNodes = result.getJSONArray("nodes");
                Node[] points = new Node[wayNodes.length()];
                int index = 0;
                for (int j = 0; j < wayNodes.length(); j++) {
                    long nodeId = wayNodes.getLong(j);
                    for (Node node : nodes) {
                        if (node.getId() == nodeId) {
                            points[index] = node;
                            index++;
                        }
                    }
                }

                // Speed Limit
                String speedLimit = result.getJSONObject("tags").optString("maxspeed");
                if (speedLimit.equals("")) speedLimit = "unknown";

                // Variable limit
                String speedLimitVariable = result.getJSONObject("tags").optString("maxspeed:variable");
                boolean variable = (!speedLimitVariable.equals("") && !speedLimitVariable.equals("no")) || speedLimit.equals("signals");

                // Conditional limit
                String conditionalRes = result.getJSONObject("tags").optString("maxspeed:conditional");
                Matcher matcher = Pattern.compile("(\\d{1,3}|walk|none|\\d{1,3} (km/h|kmh|kph|mph|knots)) *@ *.*?(?=(; *(\\d{1,3}|walk|none|\\d{1,3} (km/h|kmh|kph|mph|knots)) *@)|;?$)").matcher(conditionalRes);
                List<String> allMatches = new ArrayList<>();
                while (matcher.find()) {
                    allMatches.add(matcher.group());
                }

                String[] conditionalLimit = new String[allMatches.size()];
                String[][] conditions = new String[allMatches.size()][];
                for (int j = 0; j < conditionalLimit.length; j++) {
                    String conditionalStr = allMatches.get(j);
                    if (conditionalStr.equals("")) {
                        conditionalLimit[j] = "";
                        conditions[j] = new String[0];
                    } else {
                        conditionalLimit[j] = conditionalStr.split("@")[0].trim();
                        String condition = conditionalStr.split("@")[1].trim();
                        condition = condition.replace("(", "").replace(")", "");
                        conditions[j] = condition.split(";");
                        for (int k = 0; k < conditions[j].length; k++) {
                            conditions[j][k] = conditions[j][k].trim();
                        }
                    }
                }

                ways.add(new Way(id, name, points, speedLimit, variable, conditionalLimit, conditions));
            }
        }

        Way nearestWay = null;
        double minDistance = Integer.MAX_VALUE;
        for (Way way : ways) {
            double distance = way.squaredDistance(loc);
            // Timber.d("Road: %s", way.toString());
            // Timber.d("Distance: %f", distance);
            // Timber.d("-----------------------");
            if (distance < minDistance) {
                minDistance = distance;
                nearestWay = way;
            }
        }

        return nearestWay;
    }
}
