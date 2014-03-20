package frontend;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

public class Demo {
	ImageIcon loadingIcon;

	Demo() {
		final JFrame frame = new JFrame("Example");
		final JButton button = new JButton("Please, press me!");
		frame.getContentPane().add(button, BorderLayout.NORTH);

		final JTextPane pane = new JTextPane();
		frame.getContentPane().add(pane);

		final Cursor defaultCursor = Cursor.getDefaultCursor();
		final Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		try {
			loadingIcon = new ImageIcon(ImageIO.read(new FileImageInputStream(new File("./data/img/loading.gif"))));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button.setIcon(loadingIcon);
				frame.setCursor(busyCursor);

				SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {
					@Override
					public String doInBackground() throws InterruptedException {
						// do some work...
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < 25; i++) {
							sb.append((char)(i + 65));
							Thread.sleep(100);
						}
						return sb.toString();
					}

					@Override
					protected void done() {
						try {
							//set result of doInBackground work
							pane.setText(get());
							frame.setCursor(defaultCursor);
							button.setIcon(null);

						} catch (Exception ignore) {
						}
					}
				};
				worker.execute();
			}
		});

		frame.setSize(new Dimension(400, 300));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
	
	public static void main(String[] args) {
		new Demo();
	}
}