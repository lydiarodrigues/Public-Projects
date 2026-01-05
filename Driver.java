

import java.io.FileNotFoundException;
import java.io.IOException;

import cs2334sp23project1.Convert;

public class Driver {
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		// convert triplog.gpx into triplog.csv
		Convert.convertFile("triplog.gpx");
		
	}
	
	

}
