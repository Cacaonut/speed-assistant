package me.cacaonut.speedassistant;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.cacaonut.speedassistant.classes.Node;
import me.cacaonut.speedassistant.classes.Way;
import timber.log.Timber;

public class AssistantService extends Service {

    private static final String TAG = "AssistantService";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        startLocationUpdates();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, "STATUS_CHANNEL")
                .setContentTitle("Speed Assistant")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_notification_normal)
                .setContentIntent(pendingIntent)
                .build();

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
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location loc = locationResult.getLastLocation();
                Timber.d("Location: (%f, %f)", loc.getLatitude(), loc.getLongitude());
                int radius = 50;
                String url = "https://overpass.kumi.systems/api/interpreter?data=[out:json];way(around:" + radius + "," + loc.getLatitude() + "," + loc.getLongitude() + ")[%22maxspeed%22];(._;%3E;);out;";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, response -> {
                            try {
                                JSONArray results = response.getJSONArray("elements");
                                Way nearestWay = selectRoad(results, loc);
                                if (nearestWay != null) {
                                    Timber.d("Road: %s", nearestWay.getName());
                                    Timber.d("Speed Limit: %s", nearestWay.getSpeedLimit());

                                    Intent intent = new Intent("data");
                                    intent.putExtra("resultsFound", true);
                                    intent.putExtra("speedLimit", nearestWay.getSpeedLimit());
                                    intent.putExtra("roadName", nearestWay.getName());
                                    // intent.putExtra("currentSpeed", loc.getSpeed());
                                    intent.putExtra("currentSpeed", 10f);
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
                        }, error -> {
                            Timber.d("Error: %s", error.getMessage());
                        });
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(jsonObjectRequest);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private Way selectRoad(JSONArray results, Location loc) throws JSONException {
        List<Node> nodes = new ArrayList<>();
        List<Way> ways = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject result = results.getJSONObject(i);
            if (result.getString("type").equalsIgnoreCase("node")) {
                nodes.add(new Node(result.getInt("id"), result.getDouble("lat"), result.getDouble("lon")));
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
                    int nodeId = wayNodes.getInt(j);
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

                ways.add(new Way(id, name, points, speedLimit));
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
