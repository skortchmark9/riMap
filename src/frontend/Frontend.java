package frontend;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

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
	
	Frontend(Backend b) {
		this.b = b;
		JFrame frame = new JFrame("MAPS");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setLayout(new FlowLayout());
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
			Node source = map.getStart();
			Node end = map.getEnd();
			map.clearRoute();
			List<Way> ways = b.getPath(source, end);
			if (Constants.DEBUG_MODE) {
				Util.out("WAYS FOUND:", ways);
			}
			if (!ways.isEmpty()) {
				map.setCalculatedRoute(ways);
				map.repaint();
			}
		}
	}

	public static void main(String[] args) {
		try {
			String arg1 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/ways.tsv";
			String arg2	= "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/nodes.tsv";
			String arg3 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/index.tsv";
			Backend b = new Backend(new String[] {arg1, arg2, arg3});
			new Frontend(b);
       } catch (IOException e) {
			e.printStackTrace();
		}
	}
}

