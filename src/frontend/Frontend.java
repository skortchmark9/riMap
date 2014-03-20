package frontend;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import maps.MapFactory;
import maps.Node;
import maps.Way;
import backend.Backend;
import backend.Constants;
import backend.Util;
import backend.Backend.BackendType;

public class Frontend implements ActionListener {
	SearchAutoFillPane box1, box2, box3, box4;
	JButton getDirections, calcStreetNames;
	JLabel start, end;
	JFrame frame;
	MapPane map;
	Backend b;
	private AtomicInteger threadCount;
	final Cursor defaultCursor = Cursor.getDefaultCursor();
	final Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	
	Frontend(Backend b) {
		this.b = b;
		frame = new JFrame("MAPS");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setLayout(new FlowLayout());
		frame.getContentPane().setBackground(Color.BLACK);
		
		
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
		
		threadCount = new AtomicInteger(0);
		getDirections = new JButton("GET DIRECTIONS");
		getDirections.addActionListener(this);
		frame.add(searchButtonsPanel, BorderLayout.SOUTH);
		map = new MapPane(b);
		frame.add(map);
		frame.add(getDirections);
		frame.pack();
		frame.setVisible(true);
	}

	private void center(Window w) {
        int screenWidth  = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;

        int windowWidth = w.getWidth();
        int windowHeight = w.getHeight();

        if (windowHeight > screenHeight) {
            return;
        }

        if (windowWidth > screenWidth) {
            return;
        }

        int x = (screenWidth - windowWidth) / 2;
        int y = (screenHeight - windowHeight) / 2;

        w.setLocation(x, y);
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == getDirections) {
			if (map.hasPoints()) {
			new PathFindingThread(map.getStart(), map.getEnd()).start();
			} else {
				//TODO Message box for communicating with user?
			}
		} else if (e.getSource() == calcStreetNames) {
			String xs1a = box1.getText();
			String xs2a = box2.getText();
			String xs1b = box3.getText();
			String xs2b = box4.getText();
			Node sourceNode = MapFactory.createIntersection(xs1a, xs2a);
			Node endNode = MapFactory.createIntersection(xs1b, xs2b);

			if (sourceNode == null || endNode == null) {				
			if (sourceNode == null) {
				box1.setBackground(Color.RED);
				box2.setBackground(Color.RED);
			}
			if (endNode == null) {
				box3.setBackground(Color.RED);
				box4.setBackground(Color.RED);
			}
			} else {
				box1.setBackground(Color.WHITE);
				box2.setBackground(Color.WHITE);
				box3.setBackground(Color.WHITE);
				box4.setBackground(Color.WHITE);
				map.setPoints(sourceNode, sourceNode);
				frame.setCursor(busyCursor);
				new PathFindingThread(sourceNode, endNode).start();
			}
		}
	}
	
	private class PathFindingThread extends Thread {
		private int numberID;
		private Node start, end;
		private PathFindingThread(Node start, Node end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			if (Constants.DEBUG_MODE) {
				Util.out("Starting new PathFindingThread:");
				Util.out("Source:", start.toString());
				Util.out("Dest:", end.toString());
			}
			
			numberID = threadCount.incrementAndGet();
			List<Way> ways = b.getPath(start, end);
			if (threadCount.get() == numberID && !ways.isEmpty()) {
				map.clearRoute();
				
				if (Constants.DEBUG_MODE)
					Util.out("WAYS FOUND:", ways);
				map.setCalculatedRoute(ways);
				map.repaint();
				frame.setCursor(defaultCursor);
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

