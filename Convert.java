package cs2334sp23project1;
/**
 * Lydia Rodrigues
 * Project 1
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Convert {
	/**
	 * writing csv using PrintWriter
	 * 
	 * @param filename
	 */
	public static void convertFile(String filename) {
		try {
			BufferedReader reads = new BufferedReader(new FileReader(filename)); 
			PrintWriter printer = new PrintWriter(new FileWriter("triplog.csv")); //googled the best way to write a csv file

			printer.println("Time,Latitude,Longitude"); //header line

			int startTime = 0; 
			
			String ln = reads.readLine(); // line that is being analyzed and changed for the new file

			String lat = "lat=\""; //format of lat when reading file to use indexOf
			String lats; //value of lat
			int latSpot; //indexOf lat
			int latEnd; //end of lat value


			String lon = "lon=\""; //format of lon when reading file to use indexOf
			String lons; //value of lon
			int lonSpot;   //indexOf lon
			int lonEnd; //end of lon value

			if(ln.isEmpty()) {
				ln = reads.readLine();
			}
			while((ln = reads.readLine()) != null){
				if(ln.isEmpty()) {
					continue;  //googled this to be able to skip this round of the loop while it is empty
				}
				String text = ln; //trim off extra white space at end/beginning
				latSpot = text.indexOf(lat) + lat.length(); //add for the length of lat in the index to be where the lat actually starts
				latEnd = text.indexOf("\"", latSpot);
				lonSpot = text.indexOf(lon) + lon.length(); //add for the length of lon in the index to be where the lon actually starts
				lonEnd = text.indexOf("\"",lonSpot); 



				/**
				 * check if there are ? or extra spaces and replace them 
				 * by using the String api replace method
				 */
				if(latEnd > latSpot && latSpot>-1 && lonSpot> -1 && lonEnd > lonSpot) {
					lats = text.substring(latSpot,latEnd);
					lons = text.substring(lonSpot,lonEnd);
					lats = lats.replace("?", "");
					lons = lons.replace("?", ""); 
					lats = lats.replace(" ", "");
					lons = lons.replace(" " , ""); 
					lats = lats.replace("\t", "");
					lons = lons.replace("\t" , "");
					lats = lats.replace("\n", "");
					lons = lons.replace("\n" , "");
					if(lats.isEmpty()!= true && lons.isEmpty()!=true) {
						printer.println(startTime + "," + lats + "," + lons);
						startTime += 5;
					}
				}
			}
			reads.close(); 
			printer.close(); 
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
