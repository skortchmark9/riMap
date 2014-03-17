package frontend;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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

                JFrame frame = new JFrame();
                frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 400);
                frame.setLocationRelativeTo(null);
                frame.setLayout(new BorderLayout());
                final MapPane pane = new MapPane("./data/balls.txt");
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
