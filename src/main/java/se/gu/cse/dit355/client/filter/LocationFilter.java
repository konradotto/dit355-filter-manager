package se.gu.cse.dit355.client.filter;

public class LocationFilter implements Filter{

    private final Coordinate center;
    private final double maxDistance;     // maximum Distance from the center that is considered near
    private String passFilter;
    private String failFilter;

    public LocationFilter(Coordinate center, double maxDistance, String passFilter, String failFilter) {
        this.center = center;
        this.maxDistance = maxDistance;
        this.passFilter = passFilter;
        this.failFilter = failFilter;
    }

    public boolean passesFilter(TravelRequest request) {
        Coordinate orig = new Coordinate(request.getOrigin().getLatitude(), request.getOrigin().getLongitude());
        Coordinate dest = new Coordinate(request.getDestination().getLatitude(), request.getDestination().getLongitude());
        boolean origNear = center.calculateDistance(orig) <= maxDistance;
        boolean destNear = center.calculateDistance(dest) <= maxDistance;

        // both origin and destination must be in the area for the request to be in the area
        return origNear && destNear;
    }

    @Override
    public String filter(TravelRequest request) {
        return passesFilter(request) ? passFilter : failFilter;
    }
}
