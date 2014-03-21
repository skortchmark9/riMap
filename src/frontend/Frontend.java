package frontend;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import maps.MapFactory;
import maps.Node;
import maps.Way;
import backend.Backend;
import backend.Constants;
import backend.PathWayRequester;
import backend.Util;

public class Frontend implements ActionListener {
	SearchAutoFillPane box1, box2, box3, box4;
	JButton getDirections, calcStreetNames, clearPoints;
	JLabel start, end;
	JTextArea msgBox;
	JFrame frame;
	MapPane map;
	Backend b;
	private AtomicInteger threadCount;
	final Cursor defaultCursor = Cursor.getDefaultCursor();
	final Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	PathWayRequester pwRequester;

	Frontend(Backend b) {
		this.b = b;
		frame = new JFrame("MAPS");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setLayout(new FlowLayout());
		frame.getContentPane().setBackground(Color.BLACK);
		pwRequester = new PathWayRequester(b);		

		calcStreetNames = new JButton("Calculate from cross-streets");
		calcStreetNames.addActionListener(this);

		JPanel searchButtonsPanel = new JPanel();
		searchButtonsPanel.setLayout(new BoxLayout(searchButtonsPanel, BoxLayout.PAGE_AXIS));
		JPanel topButtonPanel = new JPanel();
		JPanel bottomButtonPanel = new JPanel();
		box1 = new SearchAutoFillPane(b, "Cross Street 1");
		box2 = new SearchAutoFillPane(b, "Cross Street 2");
		box3 = new SearchAutoFillPane(b, "Cross Street 1");
		box4 = new SearchAutoFillPane(b, "Cross Street 2");
		topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.LINE_AXIS));
		start = new JLabel("Start: ");
		start.setFont(new Font("Sans-Serif", Font.ITALIC, 16));

		topButtonPanel.add(start);
		topButtonPanel.add(box1);
		topButtonPanel.add(box2);
		bottomButtonPanel.setLayout(new BoxLayout(bottomButtonPanel, BoxLayout.LINE_AXIS));		
		end = new JLabel("  End: ");
		end.setFont(new Font("Sans-Serif", Font.ITALIC, 16));

		bottomButtonPanel.add(end);
		bottomButtonPanel.add(box3);
		bottomButtonPanel.add(box4);
		searchButtonsPanel.add(topButtonPanel);
		searchButtonsPanel.add(bottomButtonPanel);
		searchButtonsPanel.add(calcStreetNames);

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
		sidePanel.add(searchButtonsPanel);

		getDirections = new JButton("GET DIRECTIONS");
		getDirections.addActionListener(this);
		sidePanel.add(getDirections);

		clearPoints = new JButton("CLEAR POINTS ON MAP");
		clearPoints.addActionListener(this);
		sidePanel.add(clearPoints);

		msgBox = new JTextArea();
		msgBox.setEditable(false);
		msgBox.setPreferredSize(new Dimension(100, 50));
		Util.setGUIMessageBox(msgBox);
		msgBox.setOpaque(false);
		sidePanel.add(new JScrollPane(msgBox, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		Util.guiMessage("Hi!");
		Util.guiMessage("Hada!");
		Util.guiMessage("Hi!");


		Util.guiMessage("asad!");
		Util.guiMessage("kjshdfks");
		
		
		sidePanel.setBackground(Color.BLACK);
		sidePanel.setOpaque(true);

		threadCount = new AtomicInteger(0);
		frame.add(sidePanel, BorderLayout.WEST);
		map = new MapPane(b);
		frame.add(map, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
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
		} else if (e.getSource() == clearPoints) {
			map.clearClickPoints();
		} else if (e.getSource() == calcStreetNames) {
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
				Util.guiMessage("Could not find intersection of: " + xs1S + " and " +  xs2S);
				return;
			}
			List<Way> wayList;
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

	public static void main(String[] args) {
		try {
			String arg1 = "./data/mapsfiles/ways.tsv";
			String arg2	= "./data/mapsfiles/nodes.tsv";
			String arg3 = "./data/mapsfiles/index.tsv";
			Backend b = new Backend(new String[] {arg1, arg2, arg3});
			new Frontend(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

