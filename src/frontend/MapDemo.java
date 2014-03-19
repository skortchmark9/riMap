package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import backend.Backend;
import backend.Constants;
import backend.Util;

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
					String arg1 = "./data/mapsfiles/ways.tsv";
					String arg2	= "./data/mapsfiles/nodes.tsv";
					String arg3 = "./data/mapsfiles/index.tsv";
					if (Constants.DEBUG_MODE) {
						Util.out("Constructing backend");
						Util.memLog();
					}
					b = new Backend(new String[] {arg1, arg2, arg3});
					
					if (Constants.DEBUG_MODE) {
						Util.out("backend done.");
					}
						
				} catch (IOException e) {
				}

                JFrame frame = new JFrame();
                frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setLayout(new BorderLayout());
                long start = 0; //XXX: FOR DEBUGGING
                
                if (Constants.DEBUG_MODE) {
                	start = Util.resetClock();
                	Util.out("Constructing MapPane...");
                	Util.memLog();
                }

                final MapPane pane = new MapPane(b);
                
                if(Constants.DEBUG_MODE) {
                	Util.out("Finished MapPane (Elapsed:", Util.timeSince(start)+")");
        			Util.memLog();
                }
                
                pane.setBorder(BorderFactory.createLineBorder(Color.blue));
                JPanel p2 = new JPanel();
                p2.setPreferredSize(new Dimension(100, 700));
                p2.setBackground(Color.WHITE);
                frame.add(p2, BorderLayout.EAST);
                JPanel p3 = new JPanel();
                p3.setPreferredSize(new Dimension(700, 100));
                p3.setBackground(Color.WHITE);
                frame.add(p3, BorderLayout.SOUTH);
                
                frame.add(pane, BorderLayout.CENTER);
                center(frame);
                frame.setVisible(true);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                    }

                });

            }
        });
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
