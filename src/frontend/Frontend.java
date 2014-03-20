package frontend;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import maps.Node;
import maps.Way;
import backend.Backend;
import backend.Constants;
import backend.PathWayRequester;
import backend.Util;

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
		sidePanel.setBackground(Color.BLACK);
		sidePanel.setOpaque(true);
		
		threadCount = new AtomicInteger(0);
		frame.add(sidePanel, BorderLayout.WEST);
		map = new MapPane(b);
		frame.add(map, BorderLayout.CENTER);
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
			Node start = map.getStart();
			Node end = map.getEnd();
			List<Way> wayList;
			if (start != null && end != null) {
				try {
					wayList = pwRequester.getWays(start, end, 5);
					map.clearRoute();
					if (Constants.DEBUG_MODE) {
						Util.out("WAYS FOUND:", wayList);
					}
					map.setCalculatedRoute(wayList);
					map.repaint();
				} catch (TimeoutException e1) {
					Util.out("ERROR: the search timed out - try again with more time?");
				}
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

