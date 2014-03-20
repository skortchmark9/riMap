package frontend;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import backend.Backend;
import backend.Backend.BackendType;

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
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(800, 500);
				frame.setLocationRelativeTo(null);
				frame.setLayout(new FlowLayout());
				Backend b = null;
				try {
					String arg1 = "./data/mapsfiles/ways.tsv";
					String arg2	= "./data/mapsfiles/nodes.tsv";
					String arg3 = "./data/mapsfiles/index.tsv";
					b = new Backend(new String[] {arg1, arg2, arg3}, BackendType.AC);
	           } catch (IOException e) {
					e.printStackTrace();
				}
				frame.add(new SearchAutoFillPane(b, "!!!!"), BorderLayout.NORTH);
				frame.pack();
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