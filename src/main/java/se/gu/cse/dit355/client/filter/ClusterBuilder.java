package se.gu.cse.dit355.client.filter;

import java.util.List;
import java.util.Map;

public class ClusterBuilder {

    private Map<Integer, TravelRequest> requestMap;     // data structure to map requests to cluster centroids
    private List<Cluster> clusters;
    private int numberOfClusters;

    public ClusterBuilder(int k) {
        this.numberOfClusters = k;

    }

}
