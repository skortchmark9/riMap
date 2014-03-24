package frontend;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import maps.MapFactory;
import maps.Node;
import maps.Way;
import backend.Backend;
import backend.Constants;
import backend.PathWayRequester;
import backend.Util;

/**
 * A Class representing the Front-end of the maps program.
 * creates a JFrame and a few JPanels and interfaces between 
 * these buttons and the backend. Also makes some calls to 
 * update the map based on user input.
 * 
 * @author skortchm / emc3
 */
public class Frontend implements ActionListener {
	SearchAutoFillPane box1, box2, box3, box4;
	JButton getDirections, calcStreetNames, clearPoints;
	JLabel start, end;
	JTextArea msgBox;
	JFrame frame;
	MapPane map;
	Backend b;
	final Cursor defaultCursor = Cursor.getDefaultCursor();
	final Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	PathWayRequester pwRequester;

	/**
	 * Main constructor.<br>
	 * Creates a new JFrame and initializes the map inside of that.
	 * Also puts a few buttons on the screen.
	 * @param b - the backend which drives the Maps application
	 */
	
	public Frontend() {
		
	}
	
	
	public Frontend(Backend b) {
		this.b = b; //set backend reference
		
		//JFrame init
		frame = new JFrame("MAPS");
		frame.setTitle("MAPS - By Samuel Kortchmar and Eli Martinez Cohen");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setLayout(new FlowLayout());
		frame.getContentPane().setBackground(Color.BLACK);
		
		//pwRequester handles requests to the backend for shortest path searches.
		pwRequester = new PathWayRequester(b);		

		//Some panels to hold our buttons and search boxes
		JPanel searchButtonsPanel = new JPanel();
		JPanel topButtonPanel = new JPanel();
		JPanel bottomButtonPanel = new JPanel();
		
		//layouts
		searchButtonsPanel.setLayout(new BoxLayout(searchButtonsPanel, BoxLayout.PAGE_AXIS));
		topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.LINE_AXIS));
		bottomButtonPanel.setLayout(new BoxLayout(bottomButtonPanel, BoxLayout.LINE_AXIS));	
		
		
		//Button for text input boxes (find directions by street names)
		calcStreetNames = new JButton("Calculate from cross-streets");
		calcStreetNames.addActionListener(this);
		
		//search boxes
		box1 = new SearchAutoFillPane(b, "Cross Street 1");
		box2 = new SearchAutoFillPane(b, "Cross Street 2");
		box3 = new SearchAutoFillPane(b, "Cross Street 1");
		box4 = new SearchAutoFillPane(b, "Cross Street 2");
		
		//Labels for search panel
		start = new JLabel("Start: ");
		start.setFont(new Font("Sans-Serif", Font.ITALIC, 16));
		end = new JLabel("  End: ");
		end.setFont(new Font("Sans-Serif", Font.ITALIC, 16));

		//Add shit to GUI
		bottomButtonPanel.add(end);
		bottomButtonPanel.add(box3);
		bottomButtonPanel.add(box4);
		searchButtonsPanel.add(topButtonPanel);
		searchButtonsPanel.add(bottomButtonPanel);
		searchButtonsPanel.add(calcStreetNames);
		topButtonPanel.add(start);
		topButtonPanel.add(box1);
		topButtonPanel.add(box2);

		//one last panel
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		
		getDirections = new JButton("GET DIRECTIONS");
		getDirections.addActionListener(this);

		clearPoints = new JButton("CLEAR POINTS ON MAP");
		clearPoints.addActionListener(this);

		sidePanel.add(searchButtonsPanel);
		sidePanel.add(getDirections);
		sidePanel.add(clearPoints);
		
		//message box to print messages to the user
		msgBox = new JTextArea(5, 20);
		msgBox.setMargin(new Insets(5,5,5,5));		
		msgBox.setEditable(false);
		//Note these lines are necessary because we don't handle appending text
		//from the event dispatching thread.
		DefaultCaret caret = (DefaultCaret)msgBox.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);		
		Util.setGUIMessageBox(msgBox);
		sidePanel.add(new JScrollPane(msgBox));
		Util.guiMessage("Console: look here for messages");
		
		sidePanel.setBackground(Color.BLACK);
		sidePanel.setOpaque(true);
		
		//Create a new map!
		map = new MapPane(b);
		
		//add panels to Frame
		frame.add(sidePanel, BorderLayout.WEST);
		frame.add(map, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * Handles the various buttons' actions from the GUI
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//Get Directions from the Screen points (defined by clicking)
		if (e.getSource() == getDirections) {
			if (map.hasPoints()) {
				Node start = map.getStart();
				Node end = map.getEnd();
				List<Way> wayList;
				try {
					wayList = pwRequester.getWays(start, end, 5);
					map.clearRoute();
					if (Constants.DEBUG_MODE) {
						Util.out("WAYS FOUND:", wayList);
					}
					map.setCalculatedRoute(wayList);
					map.repaint();
				} catch (TimeoutException e1) {
					Util.guiMessage("ERROR: the search timed out - try again with more time?");
				}
			}
		} 
		
		//clear onscreen points
		else if (e.getSource() == clearPoints) {
			map.clearClickPoints();
		} 
		
		//Calculate path from inputed street names
		else if (e.getSource() == calcStreetNames) {
			String xs1S = box1.getText();
			String xs2S = box2.getText();
			String xs1E = box3.getText();
			String xs2E = box4.getText();
			Node source = MapFactory.createIntersection(xs1S, xs2S);
			Node dest = MapFactory.createIntersection(xs1E, xs2E);
			if (source == null) {
				Util.guiMessage("Could not find intersection of: " + xs1S + " and " +  xs2S);
				return;
			}
			if (dest == null) {
				Util.guiMessage("Could not find intersection of: " + xs1E + " and " +  xs2E);
				return;
			}
			List<Way> wayList;
			if (Constants.DEBUG_MODE) {
				Util.out("Get Directions Between:");
				Util.out(source.toString(), "\n", dest.toString());
			}
			map.setPoints(source, dest); //draw points to map
			try {
				wayList = pwRequester.getWays(source, dest, 5);
				map.clearRoute();
				if (wayList.size() == 0) {
					Util.guiMessage("Could not find route between intersections");
				}  else {
					if (Constants.DEBUG_MODE) {
						Util.out("WAYS FOUND:", wayList);
					}
					map.setCalculatedRoute(wayList);
					map.repaint();
				}
			} catch (TimeoutException e1) {
				Util.guiMessage("ERROR: the search timed out - try again with more time?");
			}
		}
	}
}

