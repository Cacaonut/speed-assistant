package me.cacaonut.speedassistant.classes;

import android.location.Location;

import timber.log.Timber;

public class Way {

    private static final String TAG = "Way";
    
    private final int id;
    private final String name;
    private final Node[] points;
    private final String speedLimit;

    public Way(int id, String name, Node[] points, String speedLimit) {
        this.id = id;
        this.name = name;
        this.points = points;
        this.speedLimit = speedLimit;
    }

    public String getName() {
        return name;
    }

    public String getSpeedLimit() {
        return speedLimit;
    }

    public double squaredDistance(Location loc) {
        double x = loc.getLatitude();
        double y = loc.getLongitude();

        double squaredDistance = -1;
        for (int i = 0; i < points.length - 1; i++) {
            double x1 = points[i].getLat();
            double y1 = points[i].getLon();

            double x2 = points[i + 1].getLat();
            double y2 = points[i + 1].getLon();

            double top = (x2 - x1) * (y1 - y) - (x1 - x) * (y2 - y1);
            double bottom = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);

            double sDist = 1000000000 * top * top / bottom;
            if (squaredDistance == -1 || sDist < squaredDistance) squaredDistance = sDist;
        }

        return squaredDistance;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Way{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", speedLimit='" + speedLimit + '\'' +
                '}';
    }
}
