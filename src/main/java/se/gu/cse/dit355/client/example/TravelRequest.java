package se.gu.cse.dit355.client.example;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Time;
import com.google.gson.Gson;

public class TravelRequest {

	private String deviceId;
	private String requestId;
	private Origin origin;
	private Destination destination;
	private String timeOfDeparture;
	private String purpose;
	private boolean longTrip;

	public TravelRequest(String deviceID, String requestID, Origin origin, Destination destination, String TimeOfDeparture, String purpose){
		this.deviceId = deviceID;
		this.requestId = requestID;
		this.origin = origin;
		this.destination = destination;
		this.timeOfDeparture = TimeOfDeparture;
		this.purpose = purpose;
	}
	public TravelRequest(){

	}

	public boolean isLongTrip() {
		return longTrip;
	}

	public void setLongTrip(boolean longTrip) {
		this.longTrip = longTrip;
	}

	public double distance() {
		if ((origin.getLatitude() == destination.getLatitude()) && (origin.getLongitude() == destination.getLongitude())) {
			return 0;
		}
		else {
			double theta = origin.getLongitude() - destination.getLongitude();
			double dist = Math.sin(Math.toRadians(origin.getLatitude())) * Math.sin(Math.toRadians(destination.getLatitude())) + Math.cos(Math.toRadians(origin.getLatitude())) * Math.cos(Math.toRadians(destination.getLatitude())) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			dist = dist * 1.609344;

			return (dist);
		}
	}

	public String getDeviceID() {
		return deviceId;
	}

	public void setDeviceID(String deviceID) {
		this.deviceId = deviceID;
	}

	public se.gu.cse.dit355.client.example.Origin getOrigin() {
		return origin;
	}

	public void setOrigin(se.gu.cse.dit355.client.example.Origin origin) {
		origin = origin;
	}

	public se.gu.cse.dit355.client.example.Destination getDestination() {
		return destination;
	}

	public void setDestination(se.gu.cse.dit355.client.example.Destination destination) {
		this.destination = destination;
	}

	public String getTimeOfDeparture() {
		return timeOfDeparture;
	}

	public void setTimeOfDeparture(String timeOfDeparture) {
		this.timeOfDeparture = timeOfDeparture;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

}
