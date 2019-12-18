package se.gu.cse.dit355.client.filter;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private Coordinate centroid;
    private List<Coordinate> members;

    public Cluster(Coordinate centroid) {
        this.centroid = centroid;
        members = new ArrayList<>();
    }

    public boolean calculateCentroid() {
        Coordinate oldCentroid = centroid;
        centroid = Coordinate.calculateAverageCoordinate(members);

        return (centroid.equals(oldCentroid));
    }

    public void addCoordinate(Coordinate coord) {
        members.add(coord);
    }

    public void removeCoordinate(int index) {
        if (index < 0 || index > members.size() - 1) {
            throw new IllegalArgumentException("Can not remove Coordinate from Cluster. Illegal index.");
        }

        members.remove(index);
    }

    public void clearList() {
        members.clear();
    }

    public double getDistance(Coordinate coord) {
        return centroid.calculateDistance(coord);
    }

    @Override
    public String toString() {
        return centroid.toString();
    }
}
