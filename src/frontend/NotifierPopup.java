package frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import backend.Util;

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
	
	public void displayInformation(String information, int time) {
		_timer.setInitialDelay(time * 1000);
		_timer.setRepeats(false);
		removeAll();
		JMenuItem item = add(information);
		item.setPreferredSize(new Dimension(_parent.getWidth(), 20));
		show(_parent, 0, (_parent.getHeight()));
		setVisible(true);
		_timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		_timer.stop();
		setVisible(false);
		Util.out("Timer fired");
		repaint();
		
	}

}
