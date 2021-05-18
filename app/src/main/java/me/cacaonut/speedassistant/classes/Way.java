package me.cacaonut.speedassistant.classes;

import android.location.Location;

import java.util.Arrays;

import timber.log.Timber;

public class Way {

    private static final String TAG = "Way";

    private final int id;
    private final String name;
    private final Node[] points;
    private final String speedLimit;
    private final boolean variable;
    private final String[] conditionLimits;
    private final String[][] conditions;

    public Way(int id, String name, Node[] points, String speedLimit, boolean variable, String[] conditionLimits, String[][] conditions) {
        this.id = id;
        this.name = name;
        this.points = points;
        this.speedLimit = speedLimit;
        this.variable = variable;
        this.conditionLimits = conditionLimits;
        this.conditions = conditions;
    }

    public String getName() {
        return name;
    }

    public String getSpeedLimit() {
        return speedLimit;
    }

    public boolean isVariable() {
        return variable;
    }

    public String[] getConditionLimits() {
        return conditionLimits;
    }

    public String[][] getConditions() {
        return conditions;
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

            double sDist = 1000000000 * squaredPointDistance(x, y, x1, y1, x2, y2);
            if (squaredDistance == -1 || sDist < squaredDistance) {
                squaredDistance = sDist;
            }
        }

        return squaredDistance;
    }

    private double squaredPointDistance(double x, double y, double x1, double y1, double x2, double y2) {
        double A = x - x1;
        double B = y - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = -1;
        if (len_sq != 0)
            param = dot / len_sq;

        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = x - xx;
        double dy = y - yy;
        return dx * dx + dy * dy;
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
                ", variable=" + variable +
                ", conditionLimits=" + Arrays.toString(conditionLimits) +
                '}';
    }
}
