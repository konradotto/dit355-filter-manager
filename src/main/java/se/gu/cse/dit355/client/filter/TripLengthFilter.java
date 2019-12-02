package se.gu.cse.dit355.client.filter;

public class TripLengthFilter {

    public TripLengthFilter(){

    }
    public void checkDistance(TravelRequest request){
        if(request.distance() > 10) {
            request.setLongTrip(true);
        }
        else {
            request.setLongTrip(false);
        }
    }



}
