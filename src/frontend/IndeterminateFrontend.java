package frontend;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultCaret;

import maps.MapFactory;
import maps.Node;
import backend.Constants;
import backend.PathWayRunnable;
import backend.Util;
import client.Client;

/**
 * A Class representing the Front-end of the maps program.
 * creates a JFrame and a few JPanels and interfaces between 
 * these buttons and the backend. Also makes some calls to 
 * update the map based on user input.
 * 
 * @author skortchm / emc3
 */
public class IndeterminateFrontend implements ActionListener {
	AutoFillField box1, box2, box3, box4;
	JButton getDirections, clearPoints;
	JTextArea msgBox;
	JFrame frame;
	MapPane map;
	Client client;
	final Cursor defaultCursor = Cursor.getDefaultCursor();
	final Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	
	PathWayRunnable pwRequester;
	SimpleLoadingPane loadingScreen;
	private JLabel lblTimeouts;
	private JSpinner timeOutSpinner;

	/**
	 * Main constructor.<br>
	 * Creates a new JFrame and initializes the map inside of that.
	 * Also puts a few buttons on the screen.
	 */	
	public IndeterminateFrontend(Client client) {
		
		this.client = client;

		//Setting up the frame.
		frame = new JFrame("MAPS");
		frame.setTitle("MAPS - By Samuel Kortchmar and Eli Martinez Cohen");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setCursor(busyCursor);
		
		//Setting up the loading screen's info.
		JPanel boilerPlate = new JPanel();
		boilerPlate.setLayout(new BoxLayout(boilerPlate, BoxLayout.Y_AXIS));
		boilerPlate.setBorder(BorderFactory.createEmptyBorder(300, 100, 100, 100));
		boilerPlate.setOpaque(false);

		
		//Making our names look dope.
		String[] titles = {"       MAPS", "BY ELIAS MARTINEZ COHEN", "AND SAMUEL V. KORTCHMAR"};
		for(int i = 0; i < titles.length; i++ ) {
			JLabel label = new JLabel(titles[i], SwingConstants.CENTER);
			label.setForeground(Constants.GLOW_IN_THE_DARK);
			if (i == 0)
				label.setFont(new Font(Font.MONOSPACED, Font.BOLD|Font.ITALIC, 16));
			else
				label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			boilerPlate.add(label);
		}
		
		//Creating the loading screen.
		loadingScreen = new SimpleLoadingPane();
		JPanel loadingPanel = new JPanel(new BorderLayout());
		loadingPanel.setOpaque(false);
		loadingPanel.add(loadingScreen);
		loadingPanel.add(boilerPlate, BorderLayout.NORTH);
		
		//Adding the loading screen to the frame.
		frame.getContentPane().add(loadingPanel);
		frame.pack();
		frame.setVisible(true);
		loadingPanel.requestFocusInWindow();
		
		while(!client.isReady()) {
		}
		//Preparing the backend to send messages to the loading screen.
		//Initializing the backend.

		//Finished loading, removing the loading screen.
		frame.remove(loadingPanel);
		frame.setCursor(defaultCursor);
		//Initializing the rest of the Frontend.
		initMainScreen();
	}

	void initMainScreen() {

		//A JInternal frame to hold all the controls.
		JInternalFrame controlPanel = createControlPanel();

		//Initializes a JDesktopPane to hold the control panel and the map.
		JDesktopPane desktop = new JDesktopPane();
		desktop.setVisible(true);
		desktop.setOpaque(false);

		//Creates the map pane. Wrapping it in backgroundPanel allows us to use
		//a layoutManager to center it, even though JDesktopPane does not support one.
		map = new MapPane(this, client);
		map.setBorder(BorderFactory.createEmptyBorder(100, 100, 100, 100));
		JPanel backgroundPanel = new JPanel(new GridBagLayout());
		backgroundPanel.setBackground(Color.BLACK);
		backgroundPanel.add(map);
		backgroundPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight());

		//pwRequester handles requests to the backend for shortest path searches.
		//TODO: part of client?
		pwRequester = new PathWayRunnable(client, map);		

		
		//Adds the controlPanel and Map/BackgroundPanel to the desktop.
		desktop.add(controlPanel);
		desktop.add(backgroundPanel);
		frame.setContentPane(desktop);
		try {
			controlPanel.setSelected(true);
		} catch (PropertyVetoException e) {
		}
		frame.revalidate();
		
		//XXX not really sure the diff
		box1.requestFocusInWindow();
		box1.requestFocus();
		
	}
	
	private JInternalFrame createControlPanel() {
		//The panel which holds the control box.
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		sidePanel.setBackground(Color.BLACK);
		sidePanel.setOpaque(true);

		
		//Holds the buttons and the autocomplete fields.
		JPanel searchButtonsPanel = new JPanel();
		searchButtonsPanel.setOpaque(false);
		sidePanel.add(searchButtonsPanel);

		//Initializing the autofill boxes.
		box1 = new AutoFillField(client, "Cross Street 1");
		box2 = new AutoFillField(client, "Cross Street 2");
		box3 = new AutoFillField(client, "Cross Street 1");
		box4 = new AutoFillField(client, "Cross Street 2");

		//These are buttons for finding and removing paths
		getDirections = new JButton("Get Directions");
		getDirections.addActionListener(this);

		clearPoints = new JButton("Clear");
		clearPoints.addActionListener(this);
		
		
		//This spinner is used to set the timeout duration for pwRequester.
		timeOutSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_REQUEST_TIMEOUT, 0, 10, 1));
		lblTimeouts = new JLabel("Timeout (s):");
		lblTimeouts.setForeground(Constants.GLOW_IN_THE_DARK);
		
		//console box to print messages to the user
		msgBox = new JTextArea(5, 20);
		msgBox.setMargin(new Insets(5,5,5,5));		
		msgBox.setEditable(false);
		//Note these lines are necessary because we don't handle appending text
		//from the event dispatching thread.
		DefaultCaret caret = (DefaultCaret)msgBox.getCaret();
		Util.setGUIMessageBox(msgBox);
		JScrollPane scrollPane = new JScrollPane(msgBox);
		sidePanel.add(scrollPane);
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);		
		Util.guiMessage("Console: look here for messages");

		
		//Lays out the components for the control panel. It's a mess because it
		//was made with WindowBuilder.
		GroupLayout gl_searchButtonsPanel = new GroupLayout(searchButtonsPanel);
		gl_searchButtonsPanel.setHorizontalGroup(
			gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_searchButtonsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_searchButtonsPanel.createSequentialGroup()
							.addComponent(lblTimeouts)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(timeOutSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(getDirections, GroupLayout.PREFERRED_SIZE, 121, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(clearPoints, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
							.addGap(10))
						.addGroup(gl_searchButtonsPanel.createSequentialGroup()
							.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(box3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(box1, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(box4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(box2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addGap(17)))
					.addContainerGap())
		);
		gl_searchButtonsPanel.setVerticalGroup(
			gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_searchButtonsPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(box1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(box2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(box3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(box4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(getDirections, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblTimeouts)
						.addComponent(timeOutSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(clearPoints, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
		);
		gl_searchButtonsPanel.setAutoCreateContainerGaps(true);
		gl_searchButtonsPanel.setAutoCreateGaps(true);
		searchButtonsPanel.setLayout(gl_searchButtonsPanel);
		
		//Create a control panel.
		JInternalFrame controlPanel = new JInternalFrame("Controls", true, false, false, true);
		controlPanel.add(sidePanel);
		controlPanel.pack();
		controlPanel.setVisible(true);
		return controlPanel;
	}
	
	/**
	 * Sets the text of the AutoCorrect boxes to contain the intersection
	 * selected by clicking the map.
	 * @param wayIDs - the way IDs stored at the node selected on the map
	 * @param flag - the "clickSwitch": if true, update the first set of cross
	 * streets but if false, update the second set of cross streets.
	 */
	public void updateInputFields(List<String> wayIDs, boolean flag) {
		String street1 = MapFactory.createWay(wayIDs.get(0)).getName();
		String street2 = MapFactory.createWay(wayIDs.get(1)).getName();
		
		if (flag) {
			box1.setText(street1);
			box2.setText(street2);
		} else {
			box3.setText(street1);
			box4.setText(street2);
		}
		
	}

	/**
	 * Handles the various buttons' actions from the GUI
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//Get Directions from the Screen points (defined by clicking)
		if (e.getSource() == getDirections) {
			Node start;
			Node end;
			if (map.hasPoints()) {
				start = map.getStart();
				end = map.getEnd();
			} else {
				String xs1S = box1.getText();
				String xs2S = box2.getText();
				String xs1E = box3.getText();
				String xs2E = box4.getText();
				start = MapFactory.createIntersection(xs1S, xs2S);
				end = MapFactory.createIntersection(xs1E, xs2E);
				if (start == null || end == null) {
					if (start == null)
						Util.guiMessage("Could not find intersection of: " + xs1S + " and " +  xs2S);
					if (end == null)
						Util.guiMessage("Could not find intersection of: " + xs1E + " and " +  xs2E);
					return;
				}
				map.setPoints(start, end); //draw points on map
			}
			int timeOut = Constants.DEFAULT_REQUEST_TIMEOUT;
			try {
				timeOutSpinner.commitEdit();
				timeOut = (int) timeOutSpinner.getValue();
			} catch (ParseException e1) {
			}
			pwRequester.findPath(timeOut);

		} else if (e.getSource() == clearPoints) {
			map.clearClickPoints();
		}
	}
}

