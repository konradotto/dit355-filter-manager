package se.gu.cse.dit355.client.filter;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.*;

public class ClusterBuilder {

    private static final int ORIGIN_CLUSTERING = 0;
    private static final int DESTINATION_CLUSTERING = 1;

    private static final int NOT_CLUSTERED = -1;

    private List<TravelRequest> requests;     // data structure to map requests to cluster centroids
    private List<TravelRequest> frozenRequests;
    private List<Cluster> clusters;
    private int numberOfClusters;
    private int frozenNumberOfClusters;
    private int mode;
    private int frozenMode;
    private double maxLatitude = Double.MIN_VALUE;
    private double minLatitude = Double.MAX_VALUE;
    private double maxLongitude = Double.MIN_VALUE;
    private double minLongitude = Double.MAX_VALUE;
    private double frozenMaxLatitude;
    private double frozenMinLatitude;
    private double frozenMaxLongitude;
    private double frozenMinLongitude;
    private boolean frozen = false;

    private FilterController controller;
    private String topic;


    public ClusterBuilder(int k) {
        topic = "";
        setNumberOfClusters(k);
        requests = new ArrayList<>();
        mode = ORIGIN_CLUSTERING;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setController(FilterController controller) {
        this.controller = controller;
    }

    public void addTravelRequest(TravelRequest request) {
        requests.add(request);

        // update extreme values
        Origin ori = request.getOrigin();
        Destination dest = request.getDestination();
        if (maxLatitude < ori.getLatitude() || maxLatitude < dest.getLatitude()) {
            maxLatitude = Math.max(ori.getLatitude(), dest.getLatitude());
        }
        if (minLatitude > ori.getLatitude() || minLatitude > dest.getLatitude()) {
            minLatitude = Math.min(ori.getLatitude(), dest.getLatitude());
        }
        if (maxLongitude < ori.getLongitude() || maxLongitude < dest.getLongitude()) {
            maxLongitude = Math.max(ori.getLongitude(), dest.getLongitude());
        }
        if (minLongitude > ori.getLongitude() || minLongitude > dest.getLongitude()) {
            minLongitude = Math.min(ori.getLongitude(), dest.getLongitude());
        }
    }

    public void calculateKMeans() {
        System.out.println("K Means will be calculated.");
        if (requests.size() < numberOfClusters) {
            throw new IllegalStateException("There should be more requests than clusters. Otherwise the solution is trivial.");
        }
        // only allow one thread calculating K-Means at any time
        if (frozen) {
            return;
        }
        frozen = true;

        frozenRequests = new ArrayList<>(requests);
        frozenNumberOfClusters = numberOfClusters;
        frozenMode = mode;
        frozenMaxLatitude = maxLatitude;
        frozenMinLatitude = minLatitude;
        frozenMaxLongitude = maxLongitude;
        frozenMinLongitude = minLongitude;

        // Move calculations to external Thread in order not to stop the processing of incoming requests
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                initializeCentroidsRandomly();

                boolean changesMade;
                do {
                    System.out.println("Iteration");
                    changesMade = assignRequestsToClusters();
                } while (changesMade);
                printClusters();
                publishClusters();
                frozen = false;
            }
        });
        t1.run();
    }

    public void initializeCentroidsRandomly() {
        if (frozenRequests.size() < 2) {
            throw new ExceptionInInitializerError("Centroid can not be initialized randomly " +
                    "while the request list contains less than 2 entries");
        }

        clusters = new ArrayList<>();

        // create numberOfClusters clusters with random centroids
        for (int i = 0; i < frozenNumberOfClusters; i++) {
            double latitude = getRandomLatitude();
            double longitude = getRandomLongitude();
            clusters.add(new Cluster(new Coordinate(latitude, longitude)));
        }
    }

    private double getRandomLatitude() {
        Random rand = new Random();
        return frozenMinLatitude + (frozenMaxLatitude - frozenMinLatitude) * rand.nextDouble();
    }

    private double getRandomLongitude() {
        Random rand = new Random();
        return frozenMinLongitude + (frozenMaxLongitude - frozenMinLongitude) * rand.nextDouble();
    }

    public boolean assignRequestsToClusters() {
        if (clusters.size() == 0) {
            throw new IllegalStateException("Cluster is empty. Can not assign requests for empty cluster.");
        }

        // clear the cluster lists
        // TODO: make this step unnecessary. Would be nice to not blindly remove and
        //  reassign all requests with every call. Current approach is brute force...
        for (Cluster cl : clusters) {
            cl.clearList();
        }

        for (TravelRequest request : frozenRequests) {
            addToClosestCluster(request);
        }

        boolean changesMade = false;
        for (Cluster cluster : clusters) {
            changesMade = cluster.calculateCentroid() || changesMade;
            if (cluster.getMagnitude() == 0) {
                cluster.setCentroid(getRandomLatitude(), getRandomLongitude());
            }
        }
        return changesMade;
    }

    public void addToClosestCluster(TravelRequest req) {
        double minDist = Double.MAX_VALUE;
        int minIndex = -1;

        Coordinate coord;
        switch (mode) {
            case DESTINATION_CLUSTERING:
                coord = new Coordinate(req.getDestination().getLatitude(), req.getDestination().getLongitude());
                break;
            case ORIGIN_CLUSTERING:
            default:
                coord = new Coordinate(req.getOrigin().getLatitude(), req.getOrigin().getLongitude());
                break;
        }

        // find closest cluster (using coordinate defined by mode)
        for (int i = 0; i < clusters.size(); i++) {
            double dist = clusters.get(i).getDistance(coord);
            if (dist < minDist) {
                minDist = dist;
                minIndex = i;
            }
        }

        clusters.get(minIndex).addCoordinate(coord);
    }

    public void setNumberOfClusters(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("Can not set number of clusters to " + k +
                    " There need to be more than 0 clusters.");
        }
        this.numberOfClusters = k;
    }

    public void printClusters() {
        for (Cluster cluster : clusters) {
            System.out.println(cluster);
        }
    }

    private void publishClusters() {
        long issuance = System.currentTimeMillis() / 1000L;
        String type = "cluster";
        String purpose = "cluster";
        String deviceId = "ClusterBuilder";

        int i = 1;
        for (Cluster cluster : clusters) {
            String requestId = String.valueOf(i);
            Coordinate centroid = cluster.getCentroid();
            Origin org = new Origin(centroid.getLatitude(), centroid.getLongitude());
            Destination dest = new Destination(centroid.getLatitude(), centroid.getLongitude());
            String magnitude = String.valueOf(cluster.getMagnitude());
            TravelRequest toRequset = new TravelRequest(issuance, type, deviceId, requestId, org, dest, magnitude, purpose);

            try {
                controller.publishRequest(toRequset, topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    public int getNumberOfClusters() {
        return numberOfClusters;
    }
}
