package frontend;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import backend.Backend;

public class AutoCorrectDemo {
	public static void main(String[] args) {
		new AutoCorrectDemo();
	}
	

    public AutoCorrectDemo() {

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

                JFrame frame = new JFrame();
                frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 400);
                frame.setLocationRelativeTo(null);
                frame.setLayout(new BorderLayout());
                Backend b;
				try {
					String arg1 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/ways.tsv";
					String arg2	= "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/nodes.tsv";
					String arg3 = "/Users/samkortchmar/Documents/Brown/Semester IV/CS032/Projects/CS032_Maps/data/mapsfiles/index.tsv";
					b = new Backend(new String[] {arg1, arg2, arg3});
	                frame.add(new InputPane(b));
				} catch (IOException e) {
					e.printStackTrace();
				}
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