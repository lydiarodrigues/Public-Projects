/**
 * This class takes the latitude, longitude, and time of trips
 * from files to allow the user to find the distances, times, 
 * and other aspects of their entire file of trips. 
 * 
 * 
 * @author Lydia Rodrigues
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class TripPoint {
	private double lat;  //latitude
	private double lon; //longitude
	private int time;  //time
	private static ArrayList<TripPoint> trip; 
	
	/**
	 * Constructor setting parameters to fields
	 * @param time -int
	 * @param lat - double
	 * @param lon - double
	 */
	public TripPoint(int time, double lat, double lon) {
		this.time = time; 
		this.lon = lon; 
		this.lat = lat; 
	}
	
	/**
	 * Time getter
	 * @return - int time
	 */
	public int getTime() {
		return time; 
	}
	
	/**
	 * Lat getter
	 * @return - double lat
	 */
	public double getLat() {
		return lat; 
	}
	
	/**
	 * Lon getter
	 * @return - double lon
	 */
	public double getLon() {
		return lon; 
	}
	
	/**
	 * trip ArrayList getter
	 * @return - ArrayList<TripPoint> trip
	 */
	public static ArrayList<TripPoint> getTrip(){
		if(trip == null||trip.size() == 0) {
			return null; 
		}
		return new ArrayList<>(trip); //looked up best way to copy ArrayList
		//return trip; 
	}
	
	/**
	 * Gives the Haversine distance between two points in km. 
	 * Assumes that Earth is perfectly circular
	 * 
	 * @param a - TripPoint 1  (spot 1)
	 * @param b - TripPoint 2  (spot 2)
	 * @return - double haversine distance in km between the two points
	 */ 
	public static double haversineDistance(TripPoint a, TripPoint b) {
		double lat1 = Math.toRadians(a.getLat()); 
		double lat2 = Math.toRadians(b.getLat()); 
		double lon1 = Math.toRadians(a.getLon()); 
		double lon2 = Math.toRadians(b.getLon()); 
		double distanceLat = lat2 - lat1; 
		double distanceLon = lon2 - lon1; 
		final double EARTH_RADIUS_VALUE = 6371;  //earth radius in km from google
		
		double haversine = 2 * EARTH_RADIUS_VALUE * //wikipedia page formula
				Math.asin(Math.sqrt(Math.pow(Math.sin(distanceLat/2),2) 
						+ Math.cos(lat1) * Math.cos(lat2) 
						* Math.pow(Math.sin(distanceLon/2), 2)));
		return haversine; 
	}
	
	/**
	 * Finds the average speed of the 2 trips by finding the haversineDistance and
	 * dividing it by the difference in time. TripPoint parameters can be inputted
	 * in either order. 
	 * 
	 * @param a - TripPoint
	 * @param b - TripPoint
	 * @return - double of the average speed of the trips
	 */
	public static double avgSpeed(TripPoint a, TripPoint b) {
		double dist = haversineDistance(a,b); 
		
		// divide by 60 for min to hours and abs value so can subtract without checking which is bigger
		double diffInTime = Math.abs(a.getTime() - b.getTime())/60.0; 
		
		if(diffInTime != 0) { //make sure no math error for dividing by 0
			return dist/diffInTime; 
		}
		else {
			return 0; 
		}
	}
	
	/**
	 * Calculates the totalTime of the trip in hours. Needs
	 * to adjust from the TripPoint's time being saved in 
	 * minutes. 
	 * 
	 * @return - double : total time of the trip in hours
	 */
	public static double totalTime() {
		if(trip == null || trip.size() == 0 ) {
			return 0; 
		}
		int totalTime = trip.get(trip.size()-1).getTime() - trip.get(0).getTime(); 
//first thought to go through all of the trips, but asked for help from tutor and found out it can just be the last - first TripPoint's time
//		for(int i = 0; i<trip.size();i++) {  
//			totalTime += trip.get(i).getTime(); 
//		}
		return totalTime / 60.0;  // 60 to get to hours from min
		
	}
	
	/**
	 * Computes the total distance between every point of trip in km. 
	 * 
	 * @return - double of total distance in km
	 */
	public static double totalDistance() {
		if(trip == null || trip.size() == 0) {
			return 0; 
		}		
		double totalDist = 0; 
		for(int i = 0; i<=trip.size()-2; i++) {
			totalDist += haversineDistance(trip.get(i), trip.get(i+1));
		}
		return totalDist; 
	}
	
	/**
	 * Reads data from file (triplog.csv) into the trip ArrayList. 
	 * 
	 * @param filename - String name of the file
	 * @throws FileNotFoundException - in case file does not exist
	 * @throws IOException - for the BufferedReader unless not valid lines
	 */
	public static void readFile(String filename) throws FileNotFoundException, IOException { 
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String nextLine = br.readLine(); 
			if(trip == null) {
				trip = new ArrayList<TripPoint>(); 
			}
			nextLine = br.readLine(); //skip header line (used chatGPT to find problem)
			while(nextLine != null) {
				String[] sections = nextLine.split(","); 
				int newTime = Integer.parseInt(sections[0].trim()); 
				double newLat = Double.parseDouble(sections[1].trim());
				double newLon = Double.parseDouble(sections[2].trim());
				TripPoint newTrip = new TripPoint(newTime, newLat, newLon); 
				trip.add(newTrip); 
				nextLine = br.readLine(); 
			}
		}
	}
	
}
