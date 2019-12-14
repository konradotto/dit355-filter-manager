package se.gu.cse.dit355.client.filter;

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
}
