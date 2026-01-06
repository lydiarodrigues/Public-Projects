
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 * Project 5 
 * Lydia Rodrigues
 */
public class Driver2 {
	private static ArrayList<TripPoint> tripPoints; 
	private static ArrayList<TripPoint> movePoints;  
	private static JButton playButton; 
	private static JCheckBox stopCheckBox; 
	private static JComboBox<String> animationTime;  
	private static JMapViewer mapV; 

	public static void main(String[] args) throws FileNotFoundException,IOException {
		try {
			TripPoint.readFile("triplog.csv");
			tripPoints = TripPoint.getTrip(); 
			TripPoint.h2StopDetection(); 
			movePoints = TripPoint.getMovingTrip(); 
		} catch(FileNotFoundException e) {
			System.out.println("triplog.csv not found");
			return; 
		}
		SwingUtilities.invokeLater(() -> { //looked up
			try {
				createAndShowGUI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the frame and panels. 
	 * Calls the other methods to move the listeners. 
	 */
	private static void createAndShowGUI() {
		JFrame frame = new JFrame("Project 5 - Lydia Rodrigues");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(1000, 900);
		frame.setLocationRelativeTo(null);

		//top control panel
		JPanel topPanel = createTopPanel();
		frame.add(topPanel, BorderLayout.NORTH);

		//map panel
		JPanel mapPanel = createMapPanel();
		frame.add(mapPanel, BorderLayout.CENTER);

		addListeners();

		frame.setVisible(true);
	}

	/**
	 * Creates the top panel for the frame
	 * @return
	 */
	private static JPanel createTopPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		panel.setBackground(new Color(0, 120, 215));
		panel.setPreferredSize(new Dimension(Component.WIDTH, 80));

		animationTime = new JComboBox<>(new String[]{"15","30","60","90"}); //the time options
		animationTime.setSelectedIndex(0);
		panel.add(animationTime);

		stopCheckBox = new JCheckBox("Include Stops");
		stopCheckBox.setSelected(false);
		panel.add(stopCheckBox);

		playButton = new JButton("Play");
		panel.add(playButton);
		playButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(playButton.getText().equals("Play")) {
					playButton.setText("Reset");
				}
				else {
					playButton.setText("Play");
					mapV.removeAllMapMarkers(); 
					mapV.removeAllMapPolygons(); 

				}
			}
		});

		return panel;
	}


	/**
	 * Creates the map panel for the frame
	 * @return
	 */
	private static JPanel createMapPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		mapV = new JMapViewer();
		mapV.setTileSource(new OsmTileSource.TransportMap());
		mapV.setTileLoader(new OsmTileLoader(mapV)); //looked up because my map was just hour glasses before
		mapV.setScrollWrapEnabled(true);
		
		//find the center of the map
		panel.add(mapV,BorderLayout.CENTER); 
		double minLat = Double.MAX_VALUE;
		double maxLat = -Double.MAX_VALUE; 
		double minLon = Double.MAX_VALUE;
		double maxLon = -Double.MAX_VALUE;
		for(TripPoint point: tripPoints) {
			minLat = Math.min(minLat, point.getLat()); 
			maxLat = Math.max(maxLat, point.getLat()); 
			minLon = Math.min(minLon, point.getLon()); 
			maxLon = Math.max(maxLon, point.getLon()); 
		}

		double cLat = (minLat+maxLat)/2; 
		double cLon = (minLon+maxLon)/2; 
		mapV.setDisplayPosition(new Coordinate(cLat,cLon), 10);
		mapV.setZoom(6);
		return panel;
	}

	/**
	 * makes and adds the listeners
	 */
	private static void addListeners() {
		playButton.addActionListener(e -> {
			mapV.removeAllMapMarkers(); 
			mapV.removeAllMapPolygons(); 
			ArrayList<TripPoint> animationPoints; 
			if(stopCheckBox.isSelected()) {
				animationPoints = tripPoints; 
			}
			else {
				animationPoints = movePoints; 
			} 
			int seconds = Integer.parseInt((String) animationTime.getSelectedItem());
			animateTrip(animationPoints, seconds);
		});
	}
	
	/**
	 * Moves the raccoon image and the red line along the map to the points. 
	 * @param points - arraylist of points on the trip
	 * @param durationSeconds - time picked in comboBox
	 */
	private static void animateTrip(List<?> points, int durationSeconds) {
		BufferedImage raccoon;
		try {
			raccoon = ImageIO.read(new File("raccoon.png"));
		} catch (IOException e) {
			System.out.println("No raccoon image");
			return;
		}

		mapV.removeAllMapMarkers();
		mapV.removeAllMapPolygons();

		int delay;
		if (tripPoints.size() > 0) {
			delay = (durationSeconds * 1000) / tripPoints.size();
		} else {
			delay = 100;
		}

		final int[] currentIndex = {0}; //gave me an error when using an int, so changed to an int[]
		ArrayList<Coordinate> pathCoordinates = new ArrayList<>(); //store path coordinates
		Timer animationTimer = new Timer(delay, e -> { //looked up how to do that
			if (currentIndex[0] >= tripPoints.size()) {
				// Stop timer at the end of the trip
				((Timer) e.getSource()).stop();
				return;
			}
			TripPoint currentPoint = tripPoints.get(currentIndex[0]);
			Coordinate coord = new Coordinate(currentPoint.getLat(), currentPoint.getLon());
			
			pathCoordinates.add(coord); //add new coordinate to the path
			
			mapV.removeAllMapMarkers();
			
			if (pathCoordinates.size() >= 2) { //drawing as new line (not successful)
				mapV.removeAllMapPolygons(); 
				MapPolygonImpl pathLine = new MapPolygonImpl(pathCoordinates);
				pathLine.setColor(Color.RED);
				pathLine.setStroke(new BasicStroke(3));
				pathLine.setBackColor(null); 
				mapV.addMapPolygon(pathLine);
			}
			mapV.addMapMarker(new IconMarker(coord, raccoon));
			currentIndex[0]++;
			mapV.repaint();
		});
		animationTimer.start();
	}
	

	/**
	 * Loads the trip data into TripPoint to get the data
	 * @param filename
	 * @return 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static java.util.List<?> loadTripData(String filename) throws FileNotFoundException, IOException {
		TripPoint.readFile(filename);
		return TripPoint.getTrip();
	}
}


//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.util.ArrayList;
//
//import javax.imageio.ImageIO;
//import javax.swing.*;
//import org.openstreetmap.gui.jmapviewer.*;
//import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
//
//
//public class Driver {
//
//	// Declare class data
//	private static ArrayList<TripPoint> tripPoints; 
//	private static ArrayList<TripPoint> movePoints; 
//	private static ArrayList<TripPoint> stopPoints; 
//	private static JButton playButton; 
//	private static JCheckBox stopCheckBox; 
//	private static JComboBox<Integer> animationTime; 
//	private static int time; 
//	private static JMapViewer mapV;  
//	private static Timer animationTimer; 
//	private static BufferedImage raccoon; 
//
//	public static void main(String[] args) throws FileNotFoundException, IOException {
//
//		// Read file and call stop detection
//		try {
//			TripPoint.readFile("triplog.csv");
//			tripPoints = TripPoint.getTrip(); 
//			TripPoint.h2StopDetection(); 
//			movePoints = TripPoint.getMovingTrip(); 
//		} catch(FileNotFoundException e) {
//			System.out.println("triplog.csv not found");
//			return; 
//		}
//
//		try {
//			raccoon = ImageIO.read(new File("raccoon.png")); //looked up
//		} catch(IOException e) {
//			System.out.println("raccoon.png not found");
//			return; 
//		}
//
//		// Set up frame, include your name in the title
//		JFrame frame = new JFrame("Lydia Rodrigues Frame"); 
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setLayout(new BorderLayout());
//		frame.setSize(800,700); 
//		frame.setLocationRelativeTo(null);
//
//		// Set up Panel for input selections
//		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10)); 
//
//		panel.setBackground(Color.GRAY);
//		panel.setPreferredSize(new Dimension(800,80)); //looked up how to do this
//		frame.add(panel, BorderLayout.NORTH);
//
//		JPanel mapPanel = new JPanel(new BorderLayout()); 
//		frame.add(mapPanel,BorderLayout.CENTER);
//		// Play Button
//		playButton = new JButton("Play"); 
//
//		// CheckBox to enable/disable stops
//		stopCheckBox = new JCheckBox("Include Stops"); 
//		stopCheckBox.setSelected(false);
//		// ComboBox to pick animation time
//		Integer[] times = {15,30,60,90};  //since should be String[] in JComboBox, used Integer[]
//		animationTime = new JComboBox<>(times); 
//		animationTime.setSelectedIndex(0); 
//
//		// Add all to top panel
//		panel.add(playButton);
//		panel.add(stopCheckBox);
//		panel.add(animationTime);
//
//		// Set up mapViewer
//		mapV = new JMapViewer(); 
//		mapV.setPreferredSize(new Dimension(900,700));
//		
//		mapV.setTileSource(new OsmTileSource.TransportMap());
//		mapV.setTileLoader(new OsmTileLoader(mapV));
//		mapV.setScrollWrapEnabled(true); //looked up
//		mapV.setZoom(6);
//		mapPanel.add(mapV,BorderLayout.CENTER); 
//		// Add listeners for GUI components
//		playButton.addActionListener(new ActionListener(){
//			public void actionPerformed(ActionEvent e) {
//				if(animationTimer != null && animationTimer.isRunning()) {
//					animationTimer.stop(); //stopping any ongoing timer
//				}
//				mapV.removeAllMapMarkers();
//				mapV.removeAllMapPolygons();
//
//				ArrayList<TripPoint> animationPoints; 
//				if(stopCheckBox.isSelected()) {
//					animationPoints = tripPoints; 
//				}
//				else {
//					animationPoints = movePoints; 
//				}
//				if(animationPoints.isEmpty()) {
//					System.out.println("No points");
//					return; 
//				}
//				int totalPoints = animationPoints.size(); 
//				time = (Integer)animationTime.getSelectedItem();
//				int delay = (time*1000)/totalPoints; 
//				int[] curr = {0};
//				animationTimer = new Timer(delay, new ActionListener(){
//					public void actionPerformed(ActionEvent e) {
//						if(curr[0]>=totalPoints) {
//							((Timer) e.getSource()).stop(); 
//							return; 
//						}
//						mapV.removeAllMapMarkers(); 
//						for(int i = 1; i<=curr[0]; i++) {
//							TripPoint currPt = animationPoints.get(i); 
//							Coordinate currCoord = new Coordinate(currPt.getLat(),currPt.getLon()); 
//							TripPoint prev = animationPoints.get(i-1);
//							Coordinate prevCoord = new Coordinate(prev.getLat(), prev.getLon()); 
//							MapPolygonImpl line = new MapPolygonImpl(prevCoord, currCoord, currCoord); 
//							line.setColor(Color.RED); 
//							mapV.addMapPolygon(line); //add line to the map
//
//						}
//						TripPoint currPt = animationPoints.get(curr[0]); 
//						Coordinate currCoord = new Coordinate(currPt.getLat(), currPt.getLon());
//						IconMarker marker = new IconMarker(currCoord,raccoon); 
//						mapV.addMapMarker(marker);
//						curr[0]++; 
//						mapV.repaint(); //looked up 
//					}
//				}); 
//
//				animationTimer.start(); 
//			}
//		});
//
//		// Set the map center and zoom level
//		double minLat = Double.MAX_VALUE;
//		double maxLat = -Double.MAX_VALUE; 
//		double minLon = Double.MAX_VALUE;
//		double maxLon = -Double.MAX_VALUE;
//		frame.add(mapV,BorderLayout.CENTER); 
//		for(TripPoint point: tripPoints) {
//			minLat = Math.min(minLat, point.getLat()); 
//			maxLat = Math.max(maxLat, point.getLat()); 
//			minLon = Math.min(minLon, point.getLon()); 
//			maxLon = Math.max(maxLon, point.getLon()); 
//		}
//
//		double cLat = (minLat+maxLat)/2; 
//		double cLon = (minLon+maxLon)/2; 
//		mapV.setDisplayPosition(new Coordinate(cLat,cLon), 10);
//
//		frame.setVisible(true);
//
//	}
//
//	// Animate the trip based on selections from the GUI components
//
//
//}
//
//
////import javax.swing.*;
////import java.awt.*;
////import java.awt.image.BufferedImage;
////import java.io.File;
////import java.io.FileNotFoundException;
////import java.io.IOException;
////import java.util.ArrayList;
////import java.util.List;
////import javax.imageio.ImageIO;
////import org.openstreetmap.gui.jmapviewer.JMapViewer;
////import org.openstreetmap.gui.jmapviewer.Coordinate;
////import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
////
/////**
////* Project 5 Template – completed implementation.
////*/
////public class Driver {
////	// Fields to hold data and UI controls
////	private static List<TripPoint> allPoints;
////	private static List<TripPoint> movingPoints;
////	private static List<TripPoint> stopPoints;
////	private static JComboBox<String> animationCombo;
////	private static JCheckBox includeStopsCheckbox;
////	private static JButton playButton;
////	private static JMapViewer mapViewer;
////	private static BufferedImage raccoonImage;
////
////	public static void main(String[] args) {
////		// 1) Read the triplog file and detect stops BEFORE showing UI
////		try {
////			allPoints = loadTripData("triplog.csv");
////			TripPoint.h2StopDetection();
////			movingPoints = TripPoint.getMovingTrip();
////			// Compute stopPoints for completeness
////			stopPoints = new ArrayList<>(allPoints);
////			stopPoints.removeAll(movingPoints);
////			// Load raccoon image
////			raccoonImage = ImageIO.read(new File("raccoon.png"));
////		} catch (FileNotFoundException e) {
////			System.err.println("Error: triplog.csv not found");
////			return;
////		} catch (IOException e) {
////			System.err.println("Error reading triplog.csv or raccoon.png: " + e.getMessage());
////			return;
////		}
////
////		SwingUtilities.invokeLater(() -> {
////			try {
////				createAndShowGUI();
////			} catch (Exception e) {
////				e.printStackTrace();
////			}
////		});
////	}
////
////	private static void createAndShowGUI() {
////		// 2) Create the main frame, include your name in the title
////		JFrame frame = new JFrame("Project 5 – Lydia Rodrigues");
////		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
////		frame.setLayout(new BorderLayout());
////		frame.setSize(1200, 1000);
////		frame.setLocationRelativeTo(null);
////
////		// 3) Build and add the top control panel
////		JPanel topPanel = createTopPanel();
////		frame.add(topPanel, BorderLayout.NORTH);
////
////		// 4) Build and add the map panel
////		JPanel mapPanel = createMapPanel();
////		frame.add(mapPanel, BorderLayout.CENTER);
////
////		// 5) Attach listeners
////		addListeners();
////
////		// 6) Show the window
////		frame.setVisible(true);
////	}
////
////	private static JPanel createTopPanel() {
////		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
////		panel.setBackground(new Color(0, 120, 215));
////		panel.setPreferredSize(new Dimension(Component.WIDTH, 80));
////
////		// a) Animation speed selector (JComboBox with 15, 30, 60, 90)
////		animationCombo = new JComboBox<>(new String[]{"15", "30", "60", "90"});
////		animationCombo.setSelectedItem("15");
////		panel.add(animationCombo);
////
////		// b) Include stops checkbox (default unchecked)
////		includeStopsCheckbox = new JCheckBox("Include Stops");
////		includeStopsCheckbox.setSelected(false);
////		panel.add(includeStopsCheckbox);
////
////		// c) Play/Reset button
////		playButton = new JButton("Play");
////		panel.add(playButton);
////
////		return panel;
////	}
////
////	private static JPanel createMapPanel() {
////		JPanel panel = new JPanel(new BorderLayout());
////
////		// 1) Instantiate JMapViewer
////		JMapViewer map = new JMapViewer();
////		mapViewer = map;
////
////		// 2) Optional: set a custom tile source (not used here)
////		// map.setTileSource(...);
////
////		// 3) Enable wrap-around
////		map.setScrollWrapEnabled(true);
////
////		// 4) Center on the trip’s area dynamically
////		centerMap(allPoints);
////
////		// 5) Optionally, add initial marker (not used to keep map clear initially)
////		// map.addMapMarker(new MapMarkerDot(35.2233, -97.4419));
////
////		panel.add(map, BorderLayout.CENTER);
////		return panel;
////	}
////
////	// Listener wiring
////	private static void addListeners() {
////		playButton.addActionListener(e -> {
////			// 1) Clear existing markers/lines from mapViewer
////			mapViewer.removeAllMapMarkers();
////			mapViewer.removeAllMapPolygons();
////
////			// 2) Choose data based on includeStopsCheckbox
////			List<TripPoint> data = includeStopsCheckbox.isSelected() ? allPoints : movingPoints;
////
////			// 3) Get animation duration
////			int seconds = Integer.parseInt((String) animationCombo.getSelectedItem());
////
////			// 4) Start animation
////			animateTrip(data, seconds);
////		});
////	}
////
////	// Animation implementation
////	private static void animateTrip(List<?> points, int durationSeconds) {
////		@SuppressWarnings("unchecked")
////		List<TripPoint> tripPoints = (List<TripPoint>) points;
////
////		// Clear any previous overlays/markers
////		mapViewer.removeAllMapMarkers();
////		mapViewer.removeAllMapPolygons();
////
////		// Compute delay
////		int totalPoints = tripPoints.size();
////		int delayMs = totalPoints > 0 ? (durationSeconds * 1000) / totalPoints : 100;
////
////		// Use a Swing Timer for animation
////		final int[] currentIndex = {0};
////		Timer animationTimer = new Timer(delayMs, e -> {
////			if (currentIndex[0] >= totalPoints) {
////				// Stop timer at the end of the trip
////				((Timer) e.getSource()).stop();
////				return;
////			}
////
////			TripPoint currentPoint = tripPoints.get(currentIndex[0]);
////			Coordinate coord = new Coordinate(currentPoint.getLat(), currentPoint.getLon());
////
////			if (currentIndex[0] == 0) {
////				// First iteration: add starting point marker
////				mapViewer.addMapMarker(new IconMarker(coord, raccoonImage));
////			} else {
////				// Subsequent iterations
////				// Remove existing markers
////				mapViewer.removeAllMapMarkers();
////
////				// Add red line segment from previous to current point
////				TripPoint prevPoint = tripPoints.get(currentIndex[0] - 1);
////				Coordinate prevCoord = new Coordinate(prevPoint.getLat(), prevPoint.getLon());
////				ArrayList<Coordinate> segment = new ArrayList<>();
////				segment.add(prevCoord);
////				segment.add(coord);
////				MapPolygonImpl line = new MapPolygonImpl(segment);
////				line.setColor(Color.RED);
////				mapViewer.addMapPolygon(line);
////
////				// Add marker for current point
////				mapViewer.addMapMarker(new IconMarker(coord, raccoonImage));
////			}
////
////			// Increment index
////			currentIndex[0]++;
////
////			// Repaint map
////			mapViewer.repaint();
////		});
////
////		// Start the timer
////		animationTimer.start();
////	}
////
////	// Data-loading implementation
////	private static List<TripPoint> loadTripData(String filename) throws FileNotFoundException, IOException {
////		// Use TripPoint.readFile to parse the CSV
////		TripPoint.readFile(filename);
////		// Return the full trip
////		return TripPoint.getTrip();
////	}
////
////	// Helper method to center the map dynamically
////	private static void centerMap(List<TripPoint> points) {
////		double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
////		double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
////
////		for (TripPoint point : points) {
////			minLat = Math.min(minLat, point.getLat());
////			maxLat = Math.max(maxLat, point.getLat());
////			minLon = Math.min(minLon, point.getLon());
////			maxLon = Math.max(maxLon, point.getLon());
////		}
////
////		// Calculate center
////		double centerLat = (minLat + maxLat) / 2;
////		double centerLon = (minLon + maxLon) / 2;
////		mapViewer.setDisplayPosition(new Coordinate(centerLat, centerLon), 10);
////	}
////
////	private void setupFrameAnimation(Coordinate[] dataPoints, int animationSeconds) {
////		// Record the start time in nanoseconds
////		final long[] startTimeNs = { System.nanoTime() };
////		// Total animation duration converted to milliseconds
////		final long animationDurationMs = animationSeconds * 1_000L;
////		// Number of frames to display
////		final int frameCount = dataPoints.length;
////
////		// Create a Swing timer to update the animation every 15ms
////		Timer frameTimer = new Timer(15, null);
////		frameTimer.setCoalesce(false);
////		frameTimer.setInitialDelay(0);
////
////		// Use an array so it can be mutated inside the listener
////		final int[] currentFrameIndex = { 0 };
////
////		frameTimer.addActionListener(evt -> {
////			long now = System.nanoTime();
////			long elapsedMs = (now - startTimeNs[0]) / 1_000_000L;
////
////			// Determine which frame should be shown based on elapsed time
////			int targetFrame = (int) ((elapsedMs / (double) animationDurationMs) * (frameCount - 1));
////			targetFrame = Math.min(targetFrame, frameCount - 1);
////
////			// Advance the frame index up to the target
////			while (currentFrameIndex[0] <= targetFrame) {
////				Coordinate next = dataPoints[currentFrameIndex[0]];
////				marker.setLat(next.getLat());
////				marker.setLon(next.getLon());
////				if (currentFrameIndex[0] > 0) {
////					pathSegments[currentFrameIndex[0] - 1].setVisible(true);
////				}
////				currentFrameIndex[0]++;
////			}
////
////			mapViewer.repaint();
////String label; 
////			// Stop the timer and finalize when all frames shown
////			if (currentFrameIndex[0] >= frameCount) {
////				frameTimer.stop();
////				double elapsedSec = (now - startTimeNs[0]) / 1e9;
////				label.setText(String.format("Duration: %.2f Seconds", elapsedSec));
////				restartAnimation(false);
////			}
////		});
////
////		frameTimer.start();
////	}
////}
