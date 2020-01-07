package se.gu.cse.dit355.client.filter;

public class DistanceFilter implements Filter {

    private double separatingDistance;
    private final static String passFilter = "long_trips";
    private final static String failFilter = "short_trips";

    public DistanceFilter(double separatingDistance) {
        this.separatingDistance = separatingDistance;
    }

    public void checkDistance(TravelRequest request) {
        request.setLongTrip(isLongDistance(request));
    }

    public boolean isLongDistance(TravelRequest request) {
        return request.distance() > separatingDistance;
    }

    public String filter(TravelRequest request) {
        return isLongDistance(request) ? passFilter : failFilter;
    }
}
