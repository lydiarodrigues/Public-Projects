
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
public class Driver {
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