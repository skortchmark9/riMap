package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import backend.Backend;

public class MapDemo {
	public static void main(String[] args) {
		new MapDemo();
	}
	
    public MapDemo() {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException classNotFoundException) {
                } catch (InstantiationException instantiationException) {
                } catch (IllegalAccessException illegalAccessException) {
                } catch (UnsupportedLookAndFeelException unsupportedLookAndFeelException) {
                }
               
                
                Backend b = null;
				try {
					String arg1 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/ways.tsv";
					String arg2	= "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/nodes.tsv";
					String arg3 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/index.tsv";
					b = new Backend(new String[] {arg1, arg2, arg3});
				} catch (IOException e) {
				}

                JFrame frame = new JFrame();
                frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setLayout(new BorderLayout());
                final MapPane pane = new MapPane(b);
                pane.setBorder(BorderFactory.createLineBorder(Color.blue));
                JPanel p2 = new JPanel();
                p2.setBackground(Color.WHITE);
                frame.add(p2);
                frame.add(new JScrollPane(pane));
                frame.setVisible(true);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                    }

                });

            }
        });
    }

}
