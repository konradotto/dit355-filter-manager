package se.gu.cse.dit355.client.filter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import org.json.JSONObject;

public class FilterController implements MqttCallback {

    private final static String CHOOSE_PRESET_BROKER = "1";
    private final static String ENTER_BROKER_MANUALLY = "2";

    // Pattern for IP4 validation
    private static final String PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static Pattern ip4Pattern = Pattern.compile(PATTERN);

    private final static ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

    private final static String TOPIC_SOURCE = "travel_requests";

    private final static String LONG_TRIPS = "travel_requests/long_trips";

    private final static String SHORT_TRIPS = "travel_requests/short_trips";

    private final static String PRESET_BROKER = "tcp://localhost:1883";

    private final static String USER_ID = MqttClient.generateClientId();

    public static final boolean CLEAN_SESSION_DEFAULT = false;

    private IMqttClient middleware;
    private String currentTopic;
    private Gson gson;
    private DistanceFilter distanceFilter;
    private static PrintStream out = System.out;


    public FilterController(String broker) throws MqttException, NullPointerException {
        gson = new Gson();
        distanceFilter = new DistanceFilter();
        System.out.println("Broker: " + broker+ "\n"+ USER_ID);
        middleware = new MqttClient(broker,USER_ID);
        middleware.connect();
        middleware.setCallback(this);
    }


    public static void main(String[] args) throws MqttException, InterruptedException {
        Scanner input = new Scanner(System.in);
        FilterController controller = new FilterController(chooseBrokerAddress(input));
        controller.chooseTopic(input);
        input.close();
    }

    private static String chooseBrokerAddress(Scanner input) {
        System.err.flush();
        out.println("\nSelect option:");
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

      switch (choice) {
        case "1":
          System.out.println("Select topic:");
          System.out.println("1. travel_requests");

          choice = input.nextLine();

          switch (choice) {
            case "1":
              String topic = TOPIC_SOURCE;
              connect(topic);
          }
          break;

          case "2":
            System.out.println("Enter broker address in the format 'tcp://192.168.00.00:port'");
            String topic = input.nextLine();
            connect(topic);
            break;
      }
    }

   
    private void connect(String topic) {
      THREAD_POOL.submit(() -> {
        try {
          middleware.subscribe(topic);
          this.currentTopic = topic;
          System.out.println("DEBUGG: entered try-catch for method called connect");
        }

        catch (MqttSecurityException e) {
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
      System.out.println("DEBUGG:  in try-catch for connectionlost method");
      middleware.disconnectForcibly();
    }

    catch (MqttException e) {
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
        JSONObject jsonMsg = new JSONObject(new String(incoming.getPayload())); // topic does not matter, we can make7

        TravelRequest request = gson.fromJson(jsonMsg.toString(), TravelRequest.class); // gold from anything.

        distanceFilter.checkDistance(request);

        String output = gson.toJson(request); //make into JSon again (for printing to console)

        publishRequest(request); //publishes the request to the relevant topic
        System.out.println(output); //prints the current output to the console
        System.out.println(request.distance()); //prints the distance of the origin and destination points
    }


    private void publishRequest(TravelRequest request) throws MqttPersistenceException, MqttException {

        MqttMessage outgoing = new MqttMessage();
        String output = gson.toJson(request);
        outgoing.setPayload(output.getBytes());

        if (request.isLongTrip()) {
            middleware.publish(LONG_TRIPS, outgoing); // place gold on the public// square; take and forward
            // at free will!
        } else if (!request.isLongTrip()) {
            middleware.publish(SHORT_TRIPS, outgoing);
        }
    }

}