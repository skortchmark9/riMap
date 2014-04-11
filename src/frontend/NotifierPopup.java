package frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

/**
 * This class is used for displaying basic status messages from the server
 * It pops up briefly and then recedes into the background on click or after
 * a certain time period. For example, we use them tog ive information about
 * the traffic bot's condition.
 * @author samkortchmar
 *
 */
public class NotifierPopup extends JPopupMenu implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	JComponent _parent;
	Timer _timer;
	
	NotifierPopup(JComponent parent) {
		super();
		_parent = parent;
		setOpaque(true);
		setBackground(Color.WHITE);
		setVisible(false);
		setEnabled(false);
		_timer = new Timer(0, this);
	}
	
	public void displayInformation(String information) {
		displayInformation(information, 2);
	}
	
	public void setColor(Color c) {
		setBackground(c);
		setForeground(c);
	}
	
	public void displayInformationForever(String information) {
		displayInformation(information, -1);
	}
	
	public void displayInformation(String information, int time) {
		removeAll();
		JMenuItem item = add(information);
		item.setPreferredSize(new Dimension(_parent.getWidth(), 20));
		show(_parent, 0, _parent.getHeight());
		setVisible(true);
		if (time > 0) {
			_timer.setInitialDelay(time * 1000);
			_timer.setRepeats(false);
			_timer.start();
		}
	}
	
	public void hidePopup() {
		_timer.stop();
		setVisible(false);
		repaint();		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		hidePopup();
	}

}
