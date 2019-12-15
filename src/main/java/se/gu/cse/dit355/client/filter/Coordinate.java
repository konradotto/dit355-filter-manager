package se.gu.cse.dit355.client.filter;

import java.util.Objects;

public class Coordinate {

    private double latitude;
    private double longitude;
    private final static double MEAN_EARTH_RADIUS = 6371009;    // in meters according to IUGG

    public Coordinate(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Illegal latitude passed to Coordinate.");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Illegal longitude passed to Coordinate.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Coordinate  newCoordinateFromCartesian(double x, double y, double z) {
        // Check whether the arguments form a unit vector
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length < 1.0 - 1e-8 || length > 1.0 + 1e-8) {
            throw new IllegalArgumentException("Doubles passed to Coordinate constructor need to form a unit vector");
        }

        double phi = Math.toDegrees(Math.atan2(y, x));
        double longitude = phi > 180.0 ? 180.0 - phi : phi;
        double theta = Math.acos(z);
        double latitude = 90.0 - Math.toDegrees(theta);

        return new Coordinate(latitude, longitude);
    }

    public static Coordinate newCoordinateFromCartesian(double[] unitVector) {
        if (unitVector.length != 3) {
            throw new IllegalArgumentException("Double Array passed to Coordinate constructor needs to hold exactly 3 values");
        }
        return newCoordinateFromCartesian(unitVector[0], unitVector[1], unitVector[2]);
    }

    // Earth coordinate distance calculation using the Haversine formula from geeksforgeeks article by Twinkl Bajaj
    // and code by Prasad Kshirsagar
    // https://www.geeksforgeeks.org/program-distance-two-points-earth/, accessed Dec. 14th 2019
    // The result is given in meters
    public double calculateDistance(Coordinate other) {
        double lon1 = Math.toRadians(this.longitude);
        double lon2 = Math.toRadians(other.longitude);
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(other.latitude);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2)
                 + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);   // intermediate term of formula
        double dist = 2 * MEAN_EARTH_RADIUS * Math.asin(Math.sqrt(a));

        return dist;
    }

    public double[] toCartesianUnitVector() {
        double theta = Math.toRadians(90.0 - latitude);     // transform latitude into angel from North Pole
        double phi = longitude >= 0 ? Math.toRadians(longitude) : Math.toRadians(180.0 - longitude);     // transform from -180-180 into 0-360

        double x = Math.sin(theta) * Math.cos(phi);
        double y = Math.sin(theta) * Math.sin(phi);
        double z = Math.cos(theta);

        // transform into unit vector
        double length = Math.sqrt(x * x + y * y + z * z);

        double[] cartesian = {x/length, y/length, z/length};
        return cartesian;
    }

    public boolean almostEquals(Object o, double epsilon) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;

        return Math.abs(that.latitude - latitude) < epsilon ? Math.abs(that.longitude - longitude) < epsilon : false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
