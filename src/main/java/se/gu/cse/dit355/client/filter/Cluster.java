package se.gu.cse.dit355.client.filter;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private Coordinate centroid;
    private List<Coordinate> members;
    private double xSum;
    private double ySum;
    private double zSum;

    public Cluster() {
        members = new ArrayList<>();
        xSum = 0;
        ySum = 0;
        zSum = 0;
    }

    public void calculateCentroid() {
        centroid = Coordinate.calculateAverageCoordinate(members);
    }

    public void addCoordinate(Coordinate coord) {
        members.add(coord);
        double[] cartesian = coord.toCartesianUnitVector();
        xSum += cartesian[0];
        ySum += cartesian[1];
        zSum += cartesian[2];
    }

    public void removeCoordinate(int index) {
        if (index < 0 || index > members.size() - 1) {
            throw new IllegalArgumentException("Can not remove Coordinate from Cluster. Illegal index.");
        }

        Coordinate removeMe = members.remove(index);
        double[] cartesian = removeMe.toCartesianUnitVector();
        xSum -= cartesian[0];
        ySum -= cartesian[1];
        zSum -= cartesian[2];
    }
}
