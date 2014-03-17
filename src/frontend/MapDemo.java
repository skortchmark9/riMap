package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setLayout(new BorderLayout());
                final MapPane pane = new MapPane();
                pane.setBorder(BorderFactory.createLineBorder(Color.blue));
                JPanel p2 = new JPanel();
                p2.setPreferredSize(new Dimension(100, 700));
                p2.setBackground(Color.WHITE);
                frame.add(p2, BorderLayout.EAST);
                JPanel p3 = new JPanel();
                p3.setPreferredSize(new Dimension(700, 100));
                p3.setBackground(Color.WHITE);
                frame.add(p3, BorderLayout.SOUTH);
                
                //frame.add(new JScrollPane(pane), BorderLayout.CENTER);
                frame.add(pane, BorderLayout.CENTER);
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
