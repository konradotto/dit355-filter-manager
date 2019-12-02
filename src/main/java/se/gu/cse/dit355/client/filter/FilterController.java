package se.gu.cse.dit355.client.filter;

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

	private final static String BROKER = "tcp://localhost:1883";

	private final static String USER_ID = "Filter";

	private final IMqttClient middleware;

	private Gson gson;
	private TripLengthFilter tripLengthFilter;


	public FilterController() throws MqttException {
		gson = new Gson();
		tripLengthFilter = new TripLengthFilter();
		middleware = new MqttClient(BROKER, USER_ID);
		middleware.connect();
		middleware.setCallback(this);

	}

	public static void main(String[] args) throws MqttException, InterruptedException {
		FilterController f = new FilterController();
		f.subscribeToMessages();
	}

	private void subscribeToMessages() {
		THREAD_POOL.submit(() -> {
			try {
				middleware.subscribe(TOPIC_SOURCE);
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

		tripLengthFilter.checkDistance(request);

		String output = gson.toJson(request); //make into JSon again (for printing to console)

		publishRequest(request); //publishes the request to the relevant topic
		System.out.println(output); //prints the current output to the console
		System.out.println(request.distance()); //prints the distance of the origin and destination points


		//TODO: publish the new data
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