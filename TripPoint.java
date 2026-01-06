import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashSet; 
/**
 * Takes data from a trip into points of a trip to return as a file. 
 * Can remove close trips as stops. 
 * 
 * @author Lydia Rodrigues
 */
public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes


	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip; 

	/**
	 *  default constructor
	 */
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}

	/**
	 * constructor given time, latitude, and longitude
	 * @param time
	 * @param lat
	 * @param lon
	 */
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * @return time
	 */
	public int getTime() {
		return time;
	}

	/**
	 *  
	 * @return latitude
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @return longitude
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @return copy of trip ArrayList
	 */
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}

	/**
	 * @return copy of movingTrip ArrayList
	 */
	public static ArrayList<TripPoint> getMovingTrip() {
		return new ArrayList<>(movingTrip);
	}

	/**
	 * uses the haversine formula for great sphere distance between two points
	 * @param first - trip 1
	 * @param second - trip 2
	 * @return - the distance between points
	 */
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		// convert to radians
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// apply formulae
		double a = Math.pow(Math.sin(dLat / 2), 2) +
				Math.pow(Math.sin(dLon / 2), 2) *
				Math.cos(lat1) *
				Math.cos(lat2);
		double rad = 6371;
		double c = 2 * Math.asin(Math.sqrt(a));
		return rad * c;
	}

	/**
	 * finds the average speed between two TripPoints in km/hr
	 * @param a - trip 1
	 * @param b - trip 2
	 * @return average speed between the points
	 */
	public static double avgSpeed(TripPoint a, TripPoint b) {

		int timeInMin = Math.abs(a.getTime() - b.getTime());

		double dis = haversineDistance(a, b);

		double kmpmin = dis / timeInMin;

		return kmpmin*60;
	}


	/**
	 * @return total time of trip in hours
	 */
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}

	/**
	 * finds the total distance traveled over the trip
	 * @return
	 * @throws FileNotFoundException - thrown if file does not exist
	 * @throws IOException
	 */
	public static double totalDistance() throws FileNotFoundException, IOException {

		double distance = 0.0;

		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}

		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}

		return distance;
	}

	/**
	 * Reads file from user
	 * @param filename - input from user
	 * @throws FileNotFoundException - thrown if file is not found
	 * @throws IOException
	 */
	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);

		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}

	/**
	 * Uses the first heuristic of stop zones to remove possible stops from movingTrip. The displacement 
	 * threshold is 0.6 km. This is when two stops are within 0.6 km from each other, it is removed as a
	 * stop. 
	 * @return the number of stops removed
	 */
	public static int h1StopDetection() {
		movingTrip = new ArrayList<TripPoint>(); 
		int total = 0; 
		movingTrip.add(trip.get(0)); 
		for(int i = 0; i<trip.size()-1;i++) {
			if(Math.abs(haversineDistance(trip.get(i),trip.get(i+1)))>0.6) {
				movingTrip.add(trip.get(i+1)); 
			}
			else {
				total++;
			}
		}
		return total; 
	}


	/**
	 * Uses the second heuristic of stop zones to remove possible stops from movingTrip. The displacement
	 * threshold is 0.5km. This is when three or more points are within 0.5 km of each other, they are
	 * removed as stops. 
	 * 
	 * many attempts at this method kept in comments to refer back to
	 * 
	 * @return the number of stops removed
	 */
	//	public static int h2StopDetection() {
	//		//				int total = 0; 
	//		//				movingTrip = new ArrayList<>(); 
	//		//				movingTrip.add(trip.get(0)); 
	//		//				for(int i = 0; i<trip.size()-1; i++) { //used chatGPT to find off by 1 error
	//		//		
	//		//					TripPoint a = trip.get(i); 
	//		//					TripPoint b = trip.get(i+1); 
	//		//					if(Math.abs(haversineDistance(a,b))>.5) {
	//		//						movingTrip.add(b); 
	//		//					}
	//		//					else {
	//		//						if(i+2 < trip.size()) {
	//		//							TripPoint c = trip.get(i+2); 
	//		//							if(Math.abs(haversineDistance(a,c))>.5) {
	//		//								movingTrip.add(c);
	//		//							}
	//		//							else {
	//		//								total++; 
	//		//								i++;
	//		//							}
	//		//						}
	//		//						else {
	//		//							total++; 
	//		//							break; 
	//		//						}
	//		//					}
	//		//				}
	//		//				return total; 
	//		////		
	//		//		int total = 0;
	//		//		movingTrip = new ArrayList<>(trip);
	//		//		ArrayList<TripPoint> stops = new ArrayList<>();
	//		//		ArrayList<ArrayList<TripPoint>> stoppedZones = new ArrayList<>();
	//		//
	//		//		for(int i = 0; i < trip.size(); i++) {
	//		//		    TripPoint a = trip.get(i);
	//		//		    int pointsNear = 0;
	//		//		    
	//		//		    for(int j = 0; j < trip.size(); j++) {
	//		//		        if(i != j) {  
	//		//		            TripPoint b = trip.get(j);
	//		//		            if(haversineDistance(a, b) <= 0.5) {
	//		//		                pointsNear++;
	//		//		            }
	//		//		        }
	//		//		    }
	//		//		    
	//		//		    // Changed from >= 2 to >= 3
	//		//		    if(pointsNear >= 3) {
	//		//		        if(!stops.contains(a)) {
	//		//		            stops.add(a);
	//		//		        }
	//		//		    }
	//		//		}
	//		//
	//		//		if(stops.size() >= 3) {
	//		//		    stoppedZones.add(new ArrayList<>(stops));
	//		//		}
	//		//
	//		//		for(ArrayList<TripPoint> zone : stoppedZones) {
	//		//		    for(TripPoint point : zone) {
	//		//		        if(movingTrip.contains(point)) {
	//		//		            movingTrip.remove(point);
	//		//		            total++;
	//		//		        }
	//		//		    }
	//		//		}
	//		//
	//		//		return total;
	//
	//		//		int total = 0;
	//		//		movingTrip = new ArrayList<>(trip);
	//		//		ArrayList<TripPoint> stops = new ArrayList<>();
	//		//		ArrayList<ArrayList<TripPoint>> stoppedZones = new ArrayList<>();
	//		//
	//		//		for(int i = 0; i < trip.size(); i++) {
	//		//		    TripPoint a = trip.get(i);
	//		//		    int pointsNear = 0;
	//		//		    
	//		//		    for(int j = 0; j < trip.size(); j++) {
	//		//		        if(i != j) {  
	//		//		            TripPoint b = trip.get(j);
	//		//		            if(haversineDistance(a, b) <= 0.5) {
	//		//		                pointsNear++;
	//		//		            }
	//		//		        }
	//		//		    }
	//		//		    // Add point to stops if it has at least 2 nearby points
	//		//		    if(pointsNear >= 2) {
	//		//		        if(!stops.contains(a)) {  // Prevent duplicates
	//		//		            stops.add(a);
	//		//		        }
	//		//		    }
	//		//		}
	//		//
	//		//		if(stops.size() >= 3) {
	//		//		    stoppedZones.add(new ArrayList<>(stops));
	//		//		}
	//		//
	//		//		// Remove stop points and count them
	//		//		for(ArrayList<TripPoint> zone : stoppedZones) {
	//		//		    for(TripPoint point : zone) {
	//		//		        if(movingTrip.contains(point)) {  // Only remove if still present
	//		//		            movingTrip.remove(point);
	//		//		            total++;
	//		//		        }
	//		//		    }
	//		//		}
	//		//
	//		//		return total;
	//
	//
	//		int total = 0;
	//		movingTrip = new ArrayList<>(trip);
	//		HashSet<TripPoint> stops = new HashSet<>();  // Changed to HashSet
	//
	//		// First pass: identify stop points
	//		for (int i = 0; i < trip.size(); i++) {
	//			TripPoint a = trip.get(i);
	//			int pointsNear = 0;
	//
	//			for (int j = 0; j < trip.size(); j++) {
	//				if (i != j) {  
	//					TripPoint b = trip.get(j);
	//					if (haversineDistance(a, b) <= 0.5) {
	//						pointsNear++;
	//					}
	//				}
	//			}
	//			// Add point to stops if it has at least 2 nearby points
	//			if (pointsNear >= 2) {
	//				stops.add(a);  // HashSet automatically handles duplicates
	//			}
	//		}
	//
	//		// Remove stop points and count them
	//		if (stops.size() >= 3) {  // Still checking for minimum of 3 stops
	//			for (TripPoint point : stops) {
	//				if (movingTrip.contains(point)) {
	//					movingTrip.remove(point);
	//					total++;
	//				}
	//			}
	//		}
	//
	//		return total;
	//
	//
	//		//		int total = 0; 
	//		//		boolean stopped = false; 
	//		//		movingTrip = new ArrayList<>(trip); 
	//		//		ArrayList<TripPoint> stops = new ArrayList<>(); 
	//		//		ArrayList<ArrayList<TripPoint>> stoppedZones = new ArrayList<>(); 
	//		//		
	//		//		for(int i = 0; i<trip.size();i++) {
	//		//			stopped = false; 
	//		//			int pointsNear = 0; 
	//		//			TripPoint a = trip.get(i); 
	//		//			
	//		//			for(int j = 0; j<stops.size();j++) {
	//		//				TripPoint b = stops.get(j);
	//		//				if(haversineDistance(a,b)<=0.5) {
	//		//					pointsNear++; 
	//		//				}
	//		//			}
	//		//			if(pointsNear>=2) {//if its part of stop
	//		//				stopped = true; 
	//		//				stops.add(a); 
	//		//			}
	//		//			else {
	//		//				if(stops.size()>=3) {
	//		//					stoppedZones.add(new ArrayList<>(stops)); 
	//		//				}
	//		//
	//		//				stops = new ArrayList<>(); 
	//		//				stops.add(a); 
	//		//			}
	//		//		}
	//		//		if(stops.size() >= 3) {
	//		//			stoppedZones.add(new ArrayList<>(stops)); 
	//		//		}
	//		//		for(int i = 0; i<stoppedZones.size(); i++) {
	//		//			ArrayList<TripPoint> currZone = stoppedZones.get(i); 
	//		//			for(int j = 0; j<currZone.size();j++) {
	//		//				TripPoint point = currZone.get(j);
	//		//				movingTrip.remove(point); 
	//		//				total++; 
	//		//			}
	//		//
	//		//		}
	//		//		return total; 
	//		//
	//		//
	//		//				HashSet<Integer> set = new HashSet<>(); 
	//		//				movingTrip = new ArrayList<>();
	//		//				int total = 1; 
	//		//				for(int i = 0; i<trip.size(); i++) {
	//		//					TripPoint a = trip.get(i);
	//		//					int pointsNear = 0;
	//		//					for(int j = 0;j<trip.size();j++) {
	//		//						TripPoint b = trip.get(j); 
	//		//						if(j!=i) {
	//		//							if(haversineDistance(a,b)<=0.5) {
	//		//								pointsNear++; 
	//		//							}
	//		//							if(pointsNear>=2) {
	//		//								set.add(i);
	//		//								break;
	//		//							}
	//		//						}
	//		//					}
	//		//				}
	//		//				for(int i = 0; i<trip.size();i++) {
	//		//					if(set.contains(i)) {
	//		//						total++; 
	//		//					}
	//		//					else {
	//		//						movingTrip.add(trip.get(i)); 
	//		//					}
	//		//				}
	//		//				return total; 
	//
	//		//		    movingTrip = new ArrayList<>();
	//		//		    HashSet<Integer> stopIndices = new HashSet<>();
	//		//		    
	//		//		    // First pass: identify all stop points
	//		//		    for (int i = 0; i < trip.size(); i++) {
	//		//		        TripPoint a = trip.get(i);
	//		//		        int pointsNear = 0;
	//		//		        
	//		//		        // Count nearby points
	//		//		        for (int j = 0; j < trip.size(); j++) {
	//		//		            if (i != j) {
	//		//		                TripPoint b = trip.get(j);
	//		//		                if (haversineDistance(a, b) <= 0.5) {
	//		//		                    pointsNear++;
	//		//		                }
	//		//		            }
	//		//		            // If we have at least 2 nearby points, this is a stop
	//		//		            if (pointsNear >= 2) {
	//		//		                stopIndices.add(i);
	//		//		                break;
	//		//		            }
	//		//		        }
	//		//		    }
	//		//		    
	//		//		    // Second pass: build movingTrip and count stops
	//		//		    int totalStops = 0;
	//		//		    for (int i = 0; i < trip.size(); i++) {
	//		//		        if (stopIndices.contains(i)) {
	//		//		            totalStops++;
	//		//		        } else {
	//		//		            movingTrip.add(trip.get(i));
	//		//		        }
	//		//		    }
	//		//		    
	//		//		    return totalStops;
	//		//		
	//	}

	/**
	 * 	 * Uses the second heuristic of stop zones to remove possible stops from movingTrip. The displacement
	 * threshold is 0.5km. This is when three or more points are within 0.5 km of each other, they are
	 * removed as stops. 
	 * 
	 * many attempts at this method kept in comments to refer back to
	 * 
	 * @return the number of stops removed
	 * @throws FileNotFoundException - if triplog.csv is not found
	 * @throws IOException
	 */
	public static int h2StopDetection() throws FileNotFoundException, IOException {
		int total = 0;
		if (trip == null) {
			readFile("triplog.csv"); //if no trip, read the file
		}
		movingTrip = getTrip();
		ArrayList<ArrayList<TripPoint>> stops = new ArrayList<>();
		ArrayList<TripPoint> stoppedZones = new ArrayList<>();
		for (int i = 0; i < trip.size(); i++) {
			TripPoint curr = trip.get(i);
			boolean stop = false;
			for (int j = 0; j< stoppedZones.size();j++) {
				TripPoint point = stoppedZones.get(j); 
				if (haversineDistance(curr, point) <= .5) {
					stop = true;
					break;
				}
			}
			if (stop == true) {
				stoppedZones.add(curr);
			} else {
				if (stoppedZones.size() >= 3) {
					stops.add(stoppedZones);
				}
				stoppedZones = new ArrayList<>();
				stoppedZones.add(curr);
			}
		}
		if (stoppedZones.size() >= 3) {
			stops.add(stoppedZones);
		}
		for (int i = 0; i < stops.size(); ++i) {
			movingTrip.removeAll(stops.get(i)); //looked up the removeAll method
			total += stops.get(i).size();
		}
		return total;
	}

	//	public static int h2StopDetection() throws FileNotFoundException,IOException{
	//		if(trip == null) {
	//			readFile("triplog.csv");
	//		}
	//		int total = 0; 
	//		movingTrip = new ArrayList<>(trip);
	//		ArrayList<TripPoint> stops = new ArrayList<TripPoint>();
	//		ArrayList<ArrayList<TripPoint>> stoppedZones = new  ArrayList<ArrayList<TripPoint>>(); 
	//		for(int i = 0; i<trip.size();i++) {
	//			TripPoint curr = trip.get(i); 
	//			stops.add(curr); 
	//			for(int )
	//		}
	//		
	//	}


	/**
	 * Finds the average speed over the trip only using moving points in km/hr.
	 * @return the average speed
	 */
	public static double avgMovingSpeed() {
		if(movingTrip == null || movingTrip.size()<2) {
			return 0.0; 
		}
		double time = movingTime(); 
		double dist = 0.0; 
		for(int i = 1; i<movingTrip.size();i++) {
			dist+=haversineDistance(movingTrip.get(i),movingTrip.get(i-1));
		}
		if(time == 0) { // checking for /0 error
			return 0;
		}
		return dist/time; 
		
	}

	/**
	 * Finds the amount of time spend moving in hours. 
	 * @return time spent moving
	 */
	public static double movingTime() {
		int time = 0; 
		if(movingTrip == null) {
			return 0; 
		}
		for(int i = 1; i<movingTrip.size();i++) {
			time +=5; 
		}
		return time/60.0; //returned in hours and as double
	}

	/**
	 * Finds time spent stopped. 
	 * @return time stopped
	 */
	public static double stoppedTime() {
		return totalTime() - movingTime(); 
	}



}
