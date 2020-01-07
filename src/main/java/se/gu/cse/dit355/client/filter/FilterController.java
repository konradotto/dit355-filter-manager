package se.gu.cse.dit355.client.filter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

public class FilterController implements MqttCallback {

    private final static String LS = System.lineSeparator();

    private final static String CHOOSE_PRESET_BROKER = "1";
    private final static String ENTER_BROKER_MANUALLY = "2";

    private final static int DEFAULT_NUMBER_OF_CLUSTERS = 20;

    // Pattern for IP4 validation
    private static final String PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static Pattern ip4Pattern = Pattern.compile(PATTERN);

    private final static ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

    private final static String TOPIC_SOURCE = "travel_requests";
    private static final String TOPIC_EXTERNAL = "external";

    private final static String LONG_TRIPS = "travel_requests/long_trips";

    private final static String SHORT_TRIPS = "travel_requests/short_trips";

    private final static String PRESET_BROKER = "tcp://localhost:1883";

    private final static String USER_ID = MqttClient.generateClientId();

    public static final boolean CLEAN_SESSION_DEFAULT = false;

    private static final double SEPARATING_DISTANCE = 10.0;

    private static final double GBG_CENTER_LAT = 57.707233;
    private static final double GBG_CENTER_LONG = 11.967017;
    private static final Coordinate GOTHENBURG_CENTER = new Coordinate(GBG_CENTER_LAT, GBG_CENTER_LONG);
    private static final double GOTHENBURG_MAX_RADIUS = 22000;     // meters approximated using circles on a map

    private IMqttClient middleware;
    private String currentTopic;
    private Gson gson;
    private DistanceFilter tripLengthFilter;
    private LocationFilter filterInGothenburg;
    private ClusterBuilder longTripClusterBuilderOrigin;
    private ClusterBuilder longTripClusterBuilderDestination;
    private ClusterBuilder shortTripClusterBuilderOrigin;
    private ClusterBuilder shortTripClusterBuilderDestination;

    private ClusterBuilder activeClusterBuilderOrigin;
    private ClusterBuilder activeClusterBuilderDestination;

    private static PrintStream out = System.out;

    // flags for activating/deactivating filters
    private boolean filterTripLength = true;
    private boolean filterGothenburg = true;

    // flag for switching clustering
    private boolean clusterLongDist = true;
    private int nrClusters = DEFAULT_NUMBER_OF_CLUSTERS;

    private List<Filter> filters;

    public FilterController(String broker) throws MqttException, NullPointerException {
        gson = new Gson();
        tripLengthFilter = new DistanceFilter(SEPARATING_DISTANCE);
        filterInGothenburg = new LocationFilter(GOTHENBURG_CENTER, GOTHENBURG_MAX_RADIUS, "gothenburg", "not_gothenburg");
        longTripClusterBuilderOrigin = new ClusterBuilder(nrClusters, ClusterBuilder.ORIGIN_CLUSTERING);
        longTripClusterBuilderDestination = new ClusterBuilder(nrClusters, ClusterBuilder.DESTINATION_CLUSTERING);
        shortTripClusterBuilderOrigin = new ClusterBuilder(nrClusters, ClusterBuilder.ORIGIN_CLUSTERING);
        shortTripClusterBuilderDestination = new ClusterBuilder(nrClusters, ClusterBuilder.DESTINATION_CLUSTERING);
        System.out.println("Broker: " + broker + LS + USER_ID);
        middleware = new MqttClient(broker, USER_ID, new MemoryPersistence());
        middleware.connect();
        middleware.setCallback(this);
        longTripClusterBuilderOrigin.setController(this);
        longTripClusterBuilderOrigin.setTopic("travel_requests/long_trips/gothenburg");
        longTripClusterBuilderDestination.setController(this);
        longTripClusterBuilderDestination.setTopic("travel_requests/long_trips/gothenburg");

        shortTripClusterBuilderOrigin.setController(this);
        shortTripClusterBuilderOrigin.setTopic("travel_requests/short_trips/gothenburg");
        shortTripClusterBuilderDestination.setController(this);
        shortTripClusterBuilderDestination.setTopic("travel_requests/short_trips/gothenburg");
        initFilters();
        activateClusterBuilders();
    }

    private void initFilters() {
        filters = new ArrayList<>();
        if (filterTripLength) {
            filters.add(tripLengthFilter);
        }
        if (filterGothenburg) {
            filters.add(filterInGothenburg);
        }
    }

    private void activateClusterBuilders() {
        if (clusterLongDist) {
            activeClusterBuilderOrigin = longTripClusterBuilderOrigin;
            activeClusterBuilderDestination = longTripClusterBuilderDestination;
        } else {
            activeClusterBuilderOrigin = shortTripClusterBuilderOrigin;
            activeClusterBuilderDestination = shortTripClusterBuilderDestination;
        }
    }


    public static void main(String[] args) throws MqttException, InterruptedException {
        Scanner input = new Scanner(System.in);
        FilterController controller = new FilterController(chooseBrokerAddress(input));
        controller.chooseTopic(input);
        controller.inputLoop(input);
        input.close();
        System.exit(0);
    }

    private void inputLoop(Scanner input) {
        boolean keepLooping = true;
        while (keepLooping) {
            printLoopMenu();
            switch (input.nextLine().toLowerCase()) {
                case "c":
                    activeClusterBuilderOrigin.sendClear();
                    activeClusterBuilderOrigin.calculateKMeans();
                    activeClusterBuilderDestination.calculateKMeans();
                    break;
                case "o":
                    activeClusterBuilderOrigin.sendClear();
                    activeClusterBuilderOrigin.calculateKMeans();
                    break;
                case "d":
                    activeClusterBuilderDestination.sendClear();
                    activeClusterBuilderDestination.calculateKMeans();
                    break;
                case "s":
                    clusterLongDist = !clusterLongDist;
                    activateClusterBuilders();
                    break;
                case "u":
                    setClusterNumber(input);
                    break;
                case "t":
                    if (filterTripLength) {
                        filters.remove(tripLengthFilter);
                        filterTripLength = false;
                    } else {
                        filters.add(tripLengthFilter);
                        filterTripLength = true;
                    }
                    break;
                case "g":
                    if (filterGothenburg) {
                        filters.remove(filterInGothenburg);
                        filterGothenburg = false;
                    } else {
                        filters.add(filterInGothenburg);
                        filterGothenburg = true;
                    }
                    break;
                case "q":
                    keepLooping = false;
                    out.println("Exiting program");
                    break;
                default:
                    out.println("Invalid option selected.");
            }
        }
    }

    private boolean printLoopMenu() {
        out.flush();
        out.println(LS + "Interactive mode. Enter one of the following options to trigger an event:");
        out.println("---------------------------------------------------------------------------");
        out.println("Currently clustering " + (clusterLongDist ? "long trips" : "short trips") + ":");
        out.println("* [c]luster origins and destinations");
        out.println("* [o]rigin clustering only");
        out.println("* [d]estination clustering only");
        out.println("* [s]witch to clustering " + (clusterLongDist ? "short trips" : "long trips"));
        out.println("* [u]pdate amount of clusters (currently " + nrClusters + ")");


        out.println("Toggle on/off filters:");
        out.println("* [t]rip length filter turn " + (filterTripLength ? "off" : "on"));
        out.println("* [g]othenburg filter turn " + (filterGothenburg ? "off" : "on"));

        out.println(LS + "Program options:");
        out.println("* [q]uit the program");

        out.flush();
        return true;
    }

    private void setClusterNumber(Scanner in) {
        out.println("How many clusters would you like to use? Please enter an integer > 0:");
        String nr = in.nextLine();
        try {
            int k = Integer.valueOf(nr);
            shortTripClusterBuilderOrigin.setNumberOfClusters(k);
            shortTripClusterBuilderDestination.setNumberOfClusters(k);
            longTripClusterBuilderOrigin.setNumberOfClusters(k);
            longTripClusterBuilderDestination.setNumberOfClusters(k);
            nrClusters = k;
            out.println("Number of clusters successfully set to " + k);
        } catch (Exception e) {
            out.println("Failed to change the number of clusters with the following error:");
            out.println(e.getMessage());
        }
    }

    private static String chooseBrokerAddress(Scanner input) {
        System.err.flush();
        out.println(LS + "Select option:");
        out.println(CHOOSE_PRESET_BROKER + ". Select preset broker address");
        out.println(ENTER_BROKER_MANUALLY + ". Enter address manually");
        String choice = input.nextLine();
        System.out.println("____________________________________");

        switch (choice) {
            case CHOOSE_PRESET_BROKER:
                out.println("Select the broker:");
                out.println("1. " + PRESET_BROKER);
                choice = input.nextLine();
                switch (choice) {

                    case "1":
                        return PRESET_BROKER;

                    default:
                        System.err.println("Invalid choice. Repeating broker selection.");
                        return chooseBrokerAddress(input);
                }


            case ENTER_BROKER_MANUALLY:
                // Prompt user to enter the ip address of the broker
                out.println("Enter broker ip-address in the format '192.168.00.00'");
                String ipAddress = input.nextLine();

                if (!ip4Pattern.matcher(ipAddress).matches()) {
                    System.err.println("Invalid ip4 address entered. Repeating broker selection.");
                    return chooseBrokerAddress(input);
                }

                // Prompt user to enter the port the broker is running on
                // Port numbers range from 0 to 65535 where the ports from 0 to 1023 are reserved for privileged services

                out.println("Enter broker port. This should be a number between '1024' and '65535':");
                String portString = input.nextLine();

                int port = -1;

                try {
                    port = Integer.parseInt(portString.trim());
                } catch (NumberFormatException nfe) {
                    System.err.println("Failed to transform entered port into number. Repeating broker selection.");
                    return chooseBrokerAddress(input);
                }

                if (port < 1024 || port > 65535) {
                    System.err.println("Invalid port number entered. Repeating broker selection.");
                    return chooseBrokerAddress(input);
                }

                return "tcp://" + ipAddress + ":" + port;


            default:
                System.err.println("Invalid selection mode. Repeating broker selection.");
                return chooseBrokerAddress(input);
        }
    }

    private void chooseTopic(Scanner input) {
        System.out.println("Select options:");
        System.out.println("1. Choose preset topic");
        System.out.println("2. Enter topic manually");
        String choice = input.nextLine();

        System.out.println("____________________________________");


        boolean connected = false;

        while (!connected) {
            switch (choice) {
                case "1":
                    System.out.println("Select topic:");
                    System.out.println("1. " + TOPIC_SOURCE);
                    out.println("2. " + TOPIC_EXTERNAL);

                    choice = input.nextLine();

                    switch (choice) {
                        case "1":
                            connect(TOPIC_SOURCE);
                            connected = !confirm("Do you want to connect to further topics?", input);
                            break;
                        case "2":
                            connect(TOPIC_EXTERNAL);
                            connected = !confirm("Do you want to connect to further topics?", input);
                            break;
                    }
                    break;
                case "2":
                    out.println("Enter the name of the topic you'd like to connect to");
                    String topic = input.nextLine();
                    connect(topic);
                    connected = !confirm("Do you want to connect to further topics?", input);
                    break;
                default:
                    out.println("Invalid selection.");
            }
        }
    }

    private boolean confirm(String question, Scanner in) {
        out.println(question + " [y]es or [n]o (default is no)");
        switch (in.nextLine()) {
            case "y":
                return true;
            case "n":
                return false;
            default:
                out.println("Invalid input. Interpreting it as a 'no'.");
                return false;
        }
    }

    private void connect(String topic) {
        THREAD_POOL.submit(() -> {
            try {
                middleware.subscribe(topic);
                this.currentTopic = topic;
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void connectionLost(Throwable throwable) {

        System.out.println("Connection lost!");

        try {
            middleware.disconnectForcibly();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        reconnect(CLEAN_SESSION_DEFAULT);
        System.out.println(this.currentTopic);
    }


    public void reconnect(boolean cleanSessionDefault) {
        int i = 0;
        boolean connection = false;
        while (i < 10 && !connection) {
            try {
                i++;
                System.out.println("Trying to reconnect...(" + i + ")");
                TimeUnit.SECONDS.sleep(1);
                middleware.connect();
                middleware.setCallback(this);
                connection = true;
            } catch (Exception e) {
                System.out.println("Failed to connect(" + i + ")");
            }

        }
        System.out.println("Connected!");
        connect(currentTopic);
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    @Override
    public void messageArrived(String topic, MqttMessage incoming) throws Exception {
        JSONObject jsonMsg;
        try {
            jsonMsg = new JSONObject(new String(incoming.getPayload())); // topic does not matter, we can make7
        } catch (Exception e) {
            // System.out.println("Message of illegal format detected.");
            return;
        }
        TravelRequest request;
        try {
            request = gson.fromJson(jsonMsg.toString(), TravelRequest.class);
        } catch (Exception e) {
            // System.out.println("Message can't be converted to TravelRequest");
            return;
        }
        // validate that the message fulfills the minimum requirement for a TravelRequest
        if (!request.isValid()) {
            // System.out.println("not a valid travel request.");
            return;
        }
        publishRequest(request, ""); //publishes the request to the relevant topic
    }


    public void publishRequest(TravelRequest request, String top) throws MqttPersistenceException, MqttException {

        MqttMessage outgoing = new MqttMessage();
        String output = gson.toJson(request);
        outgoing.setPayload(output.getBytes());

        String topic = TOPIC_SOURCE;

        for (Filter filter : filters) {
            if (!filter.filter(request).equals("")) {
                topic += "/" + filter.filter(request);
            }
        }

        if (filterInGothenburg.passesFilter(request)) {
            if (tripLengthFilter.isLongDistance(request)) {
                longTripClusterBuilderOrigin.addTravelRequest(request);
                longTripClusterBuilderDestination.addTravelRequest(request);
            } else {
                shortTripClusterBuilderOrigin.addTravelRequest(request);
                shortTripClusterBuilderDestination.addTravelRequest(request);
            }
        }

        if (!top.equals("")) {
            topic = top;
        }

        if (!topic.equals(TOPIC_SOURCE)) {
            middleware.publish(topic, outgoing);
        }
    }
}