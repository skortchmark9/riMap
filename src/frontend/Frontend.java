package frontend;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import maps.Node;
import maps.Way;
import backend.Constants;
import client.Client;

/**
 * A Class representing the Front-end of the maps program.
 * creates a JFrame and a few JPanels and interfaces between 
 * these buttons and the backend. Also makes some calls to 
 * update the map based on user input.
 * 
 * @author skortchm / emc3
 */
public class Frontend extends JFrame implements ActionListener, Runnable {
	private static final long serialVersionUID = 1L;
	//Boxes to input streetnames
	private AutoFillField _box1, _box2, _box3, _box4;
	//A button for getting directions and a button for clearing
	private JButton _getDirections, _clear;
	//The mapPane which displays the ways, paths, and points.
	private MapPane _map;
	//The client owner.
	private Client _client;
	//A floating controlPanel for user interactions
	private JInternalFrame _controlPanel;
	private NotifierPopup _notifier;
	private SimpleLoadingPane loadingScreen;
	private JPanel loadingPanel;
	private JLabel lblTimeouts;
	private JSpinner timeOutSpinner;
	boolean loading = true;

	/**
	 * Main constructor.<br>
	 * Creates a new JFrame and initializes the map inside of that.
	 * Also puts a few buttons on the screen.
	 */	
	public Frontend(Client client) {
		super("MAPS");
		_client = client;
		//Setting up the frame.
		setTitle("MAPS - By Samuel Kortchmar and Eli Martinez Cohen");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		getContentPane().setLayout(new FlowLayout());
		getContentPane().setBackground(Color.BLACK);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

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
		loadingPanel = new JPanel(new BorderLayout());
		loadingPanel.setOpaque(false);
		loadingPanel.add(loadingScreen);
		loadingPanel.add(boilerPlate, BorderLayout.NORTH);

		//Adding the loading screen to the frame.
		getContentPane().add(loadingPanel);
		pack();
		setVisible(true);
		loadingPanel.requestFocusInWindow();
	}

	@Override
	public void run() {
		//Waiting for the backend to be done/server connection to be made.
		while(!_client.serverReady()) {}

		//Finished loading, removing the loading screen.
		loading = false;
		remove(loadingPanel);
		setCursor(Cursor.getDefaultCursor());
		//Initializing the rest of the Frontend.
		initMainScreen();
	}

	void initMainScreen() {

		//A JInternal frame to hold all the controls.
		createControlPanel();

		//Initializes a JDesktopPane to hold the control panel and the map.
		JDesktopPane desktop = new JDesktopPane();
		desktop.setVisible(true);
		desktop.setOpaque(false);

		//Creates the map pane. Wrapping it in backgroundPanel allows us to use
		//a layoutManager to center it, even though JDesktopPane does not support one.
		_map = new MapPane(_client);
		_map.setBorder(BorderFactory.createEmptyBorder(100, 100, 100, 100));
		JPanel backgroundPanel = new JPanel(new GridBagLayout());
		backgroundPanel.setBackground(Color.BLACK);
		backgroundPanel.add(_map);
		backgroundPanel.setBounds(0, 0, this.getWidth(), this.getHeight());


		//Adds the controlPanel and Map/BackgroundPanel to the desktop.
		desktop.add(_controlPanel);
		desktop.add(backgroundPanel);
		setContentPane(desktop);
		setControlPanelFocus(true);
		
		//create a new resize listener
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension d = getPreferredSize();
				_map.updatePixelDimension(d);
			}
			
			/* Don't need to define these methods... */
			@Override public void componentMoved(ComponentEvent e) {}
			@Override public void componentShown(ComponentEvent e) {}
			@Override public void componentHidden(ComponentEvent e) {}
		});
		
		
		revalidate();		
		_box1.requestFocusInWindow();
	}

	/**
	 * Sets whether we want the control pane or the map to have the focus.
	 * Used by MapPane to enable arrow key traversal.
	 * @param yes -do we want it to have focus?
	 */
	private void setControlPanelFocus(boolean yes) {
		try {
			_controlPanel.setSelected(yes);
		} catch (PropertyVetoException e) {
			//yep
		}

	}

	/**
	 * Creates the control panel which handles all user input (buttons & typing)
	 */
	private void createControlPanel() {
		//The panel which holds the control box.
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		sidePanel.setBackground(Color.BLACK);
		sidePanel.setOpaque(true);


		//Holds the buttons and the autocomplete fields.
		JPanel searchButtonsPanel = new JPanel();
		searchButtonsPanel.setOpaque(false);
		sidePanel.add(searchButtonsPanel);
		_notifier = new NotifierPopup(sidePanel);

		//Initializing the autofill boxes.
		_box1 = new AutoFillField(_client, "Cross Street 1", 1);
		_box2 = new AutoFillField(_client, "Cross Street 2", 2);
		_box3 = new AutoFillField(_client, "Cross Street 1", 3);
		_box4 = new AutoFillField(_client, "Cross Street 2", 4);

		//These are buttons for finding and removing paths
		_getDirections = new JButton("Get Directions");
		_getDirections.addActionListener(this);

		_clear = new JButton("Clear");
		_clear.addActionListener(this);


		//This spinner is used to set the timeout duration for pwRequester.
		timeOutSpinner = new JSpinner(new SpinnerNumberModel(Constants.DEFAULT_REQUEST_TIMEOUT, 0, 10, 1));
		lblTimeouts = new JLabel("Timeout (s):");
		lblTimeouts.setForeground(Constants.GLOW_IN_THE_DARK);

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
										.addComponent(_getDirections, GroupLayout.PREFERRED_SIZE, 121, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(_clear, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
										.addGap(10))
										.addGroup(gl_searchButtonsPanel.createSequentialGroup()
												.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING, false)
														.addComponent(_box3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(_box1, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
														.addPreferredGap(ComponentPlacement.UNRELATED)
														.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING, false)
																.addComponent(_box4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(_box2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																.addGap(17)))
																.addContainerGap())
				);
		gl_searchButtonsPanel.setVerticalGroup(
				gl_searchButtonsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_searchButtonsPanel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(_box1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(_box2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
										.addComponent(_box3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(_box4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_searchButtonsPanel.createParallelGroup(Alignment.BASELINE)
												.addComponent(_getDirections, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblTimeouts)
												.addComponent(timeOutSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(_clear, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
				);
		gl_searchButtonsPanel.setAutoCreateContainerGaps(true);
		gl_searchButtonsPanel.setAutoCreateGaps(true);
		searchButtonsPanel.setLayout(gl_searchButtonsPanel);

		//Create a control panel.
		_controlPanel = new JInternalFrame("Controls", false, false, false, false);
		
		_controlPanel.add(sidePanel);
		_controlPanel.pack();
		_controlPanel.setVisible(true);
		
	}

	/**
	 * Updates the click point in the map pane and fills the 
	 * street-name search box to contain the name of the
	 * street.  
	 * @param neighbors - the list of neighbors returned by the request (should be size 1)
	 * @param isSource - whether or not to set the point as the source (green), 
	 * 					otherwise set the destination (red).
	 */
	public void updateNeighbor(Node neighbor, boolean isSource, String street1, String street2) {
		//set point on map
		_map.setPoint(neighbor, isSource);
		//update search boxes
		if (isSource) {
			_box1.populateField(street1, true);
			_box2.populateField(street2, true);
		} else {
			_box3.populateField(street1, true);
			_box4.populateField(street2, true);
		}
		_map.requestFocusInWindow();
		setControlPanelFocus(false);
	}

	/**
	 * Repaints the map if it is available.
	 */
	public void refreshMap() {
		if (_map != null)
			_map.repaint();
	}

	/**
	 * Passes info about paths to the mapPane.
	 * @param path - the list of ways to be drawn as the path
	 * @param source - the source node to be drawn as the start of the path
	 * @param end - the end node to be drawn as the end of the path
	 */
	public void giveDirections(List<Way> path, Node source, Node end) {
		_map.setCalculatedRoute(path);
		_map.setPoints(source, end);
		//TODO implement turn by turn directions
	}

	/**
	 * Sets the ways to be rendered on the map to be...
	 * @param ways - the ways to be rendered.
	 */
	public void setWays(List<Way> ways) {
		if (_map != null && ways != null)
		_map.renderWays(ways);
	}
	
	/**
	 * Adds the following ways to the maps existing renderedWaysList.
	 * @param ways - some ways which perhaps contain incomplete data.
	 */
	public void addWays(List<Way> ways) {
		if (_map != null)
			_map.addWays(ways);
	}
	
	/**
	 * Convenience method for returning the box of the given number.
	 * @param num - the number of box to be requested. Can be 1-4.
	 * @return
	 */
	public AutoFillField getBox(int num) {
		switch (num) {
		case 1: return _box1;
		case 2: return _box2;
		case 3: return _box3;
		case 4: return _box4;
		default: return null;
		}
	}

	/**
	 * Resets the autofill boxes and 
	 */
	private void clear() {
		//Resets boxes 1-4
		for(int i = 1; i <= 4; i++) {
			getBox(i).reset();
		}
		//Clears the points on the map.
		_map.clearClickPoints();
	}

	/**
	 * Log a message about the GUI or the state of the program
	 * to the user in the GUI. This could be, for example, whether
	 * or not Dijkstra's was able to find a path to connect the nodes.
	 * 
	 * @param str - the string (message) to display to the user.
	 * @param time - the time to display the message for (only used by notifier.
	 */
	public void guiMessage(String str, int time) {
		if (loading && loadingScreen != null)
			loadingScreen.updateProgress(str);
		if (_notifier != null)
			_notifier.displayInformation(str, time);
	}
	
	public void guiMessage(String str) {
		guiMessage(str, 3);
	}


	/**
	 * Handles the various buttons' actions from the GUI
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		//Get Directions from the Screen points (defined by clicking)
		if (e.getSource() == _getDirections) {
			Node start;
			Node end;
			String xs1S = _box1.getText();
			String xs2S = _box2.getText();
			String xs1E = _box3.getText();
			String xs2E = _box4.getText();
			start = _map.getStart();
			end = _map.getEnd();
			int timeOut = Constants.DEFAULT_REQUEST_TIMEOUT;
			try {
				timeOutSpinner.commitEdit();
				timeOut = (int) timeOutSpinner.getValue();
			} catch (ParseException e1) {
			}
			_client.requestPath(start, end, timeOut, xs1S, xs2S, xs1E, xs2E);

		} else if (e.getSource() == _clear) {
			clear();
		}
	}
}

