package frontend;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import backend.Backend;
import backend.Backend.BackendType;

public class Frontend {
	SearchAutoFillPane box1, box2, box3, box4;
	MapPane map;
	
	Frontend(Backend b) {
		JFrame frame = new JFrame("MAPS");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setLayout(new FlowLayout());
		JPanel searchButtonsPanel = new JPanel(new BorderLayout());
		JPanel topButtonPanel = new JPanel(new BorderLayout());
		JPanel bottomButtonPanel = new JPanel(new BorderLayout());
		box1 = new SearchAutoFillPane(b, "b1");
		box2 = new SearchAutoFillPane(b, "b2");
		box3 = new SearchAutoFillPane(b, "b3");
		box4 = new SearchAutoFillPane(b, "b4");
		topButtonPanel.add(box1, BorderLayout.WEST);
		topButtonPanel.add(box2, BorderLayout.EAST);
		bottomButtonPanel.add(box3, BorderLayout.WEST);
		bottomButtonPanel.add(box4, BorderLayout.EAST);
		searchButtonsPanel.add(topButtonPanel, BorderLayout.NORTH);
		searchButtonsPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
		frame.add(searchButtonsPanel, BorderLayout.SOUTH);
		frame.add(new MapPane(b));
		frame.pack();
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		try {
			String arg1 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/ways.tsv";
			String arg2	= "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/nodes.tsv";
			String arg3 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/index.tsv";
			Backend b = new Backend(new String[] {arg1, arg2, arg3}, BackendType.AC);
			new Frontend(b);
       } catch (IOException e) {
			e.printStackTrace();
		}
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
}

