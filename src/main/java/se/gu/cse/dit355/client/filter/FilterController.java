package se.gu.cse.dit355.client.filter;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import org.json.JSONObject;
import com.google.gson.Gson;


public class FilterController implements MqttCallback {

	private final static ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();

	private final static String TOPIC_SOURCE = "travel_requests";

	private final static String LONG_TRIPS = "travel_requests/long_trips";

	private final static String SHORT_TRIPS = "travel_requests/short_trips";

	private final static String PRESET_BROKER = "tcp://localhost:1883";

	private final static String USER_ID = "Filter";

	private final IMqttClient middleware;

	private Gson gson;
	private DistanceFilter distanceFilter;


	public FilterController(String broker) throws MqttException {
		gson = new Gson();
		distanceFilter = new DistanceFilter();
		middleware = new MqttClient(broker, USER_ID);
		middleware.connect();
		middleware.setCallback(this);

	}

	public static void main(String[] args) throws MqttException, InterruptedException {
		Scanner input = new Scanner(System.in);
		System.out.println("Select choice:");
		System.out.println("1. Choose preset broker address");
		System.out.println("2. Enter address manually");
		String choice = input.nextLine();

		switch (choice){
			case "1":
				System.out.println("Address:");
				System.out.println("1. tcp://localhost:1883");
				choice = input.nextLine();
				switch (choice) {
					case "1":
						FilterController f = new FilterController(PRESET_BROKER);
						f.chooseTopic(input);
				}
			case "2":
				System.out.println("Enter broker address in the format 'tcp://192.168.00.00:port'");
				String broker = input.nextLine();
				FilterController f = new FilterController(broker);
				f.chooseTopic(input);
		}

	}
	private void chooseTopic(Scanner input){
		System.out.println("Select choice:");
		System.out.println("1. Choose preset topic");
		System.out.println("2. Enter topic manually");
		String choice = input.nextLine();
		switch (choice){
			case "1":
				System.out.println("topic:");
				System.out.println("1. travel_requests");
				choice = input.nextLine();
				switch (choice) {
					case "1":
						subscribeToMessages(TOPIC_SOURCE);
				}
			case "2":
				System.out.println("Enter broker address in the format 'tcp://192.168.00.00:port'");
				String topic = input.nextLine();
				subscribeToMessages(topic);

		}

	}

	private void subscribeToMessages(String topic) {
		THREAD_POOL.submit(() -> {
			try {
				middleware.subscribe(topic);
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
			middleware.disconnect();
			middleware.close();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		// Try to reestablish? Plan B?
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
		}
		else if (!request.isLongTrip()){
			middleware.publish(SHORT_TRIPS, outgoing);
		}
	}

}