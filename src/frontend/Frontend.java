package frontend;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import maps.Node;
import maps.Way;
import backend.Backend;
import backend.Constants;
import backend.Util;

public class Frontend implements ActionListener {
	SearchAutoFillPane box1, box2, box3, box4;
	JButton getDirections;
	MapPane map;
	Backend b;
	private AtomicInteger threadCount;
	
	Frontend(Backend b) {
		this.b = b;
		JFrame frame = new JFrame("MAPS");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setLayout(new FlowLayout());
		frame.setBackground(Color.BLACK);
		frame.setOpacity(1);
		
		
		JPanel searchButtonsPanel = new JPanel(new BorderLayout());
		JPanel topButtonPanel = new JPanel(new BorderLayout());
		JPanel bottomButtonPanel = new JPanel(new BorderLayout());
		box1 = new SearchAutoFillPane(b, "Street 1");
		box2 = new SearchAutoFillPane(b, "Steet 2");
		box3 = new SearchAutoFillPane(b, "Intersection 1");
		box4 = new SearchAutoFillPane(b, "Intersection 2");
		topButtonPanel.add(box1, BorderLayout.WEST);
		topButtonPanel.add(box2, BorderLayout.EAST);
		bottomButtonPanel.add(box3, BorderLayout.WEST);
		bottomButtonPanel.add(box4, BorderLayout.EAST);
		searchButtonsPanel.add(topButtonPanel, BorderLayout.NORTH);
		searchButtonsPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
		
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
			new PathFindingThread(map.getStart(), map.getEnd()).start();
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

