package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import backend.Backend;

public class InputPane extends JPanel implements ActionListener, KeyListener,  DocumentListener {
	
	Backend b;
	JTextField textInputField;
	int resultNum;
	String inputString = "Type Here";
	List<String> results;
	String input;


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	InputPane(Backend b) {
		this.b = b;
		textInputField = new JTextField(20);
		textInputField.setText(inputString);
		textInputField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		textInputField.addKeyListener(this);
		textInputField.getDocument().addDocumentListener(this);
		this.add(textInputField);
	}

	@Override
	public void insertUpdate(DocumentEvent ev) { //Generates suggestions in real time
	    if (ev.getLength() != 1) { //In case 2 events get thrown somehow. (StackOverflow made me do it)
            return;
        }
		input = textInputField.getText(); //Gets the users input
		if (input.length() > 2) {
		resultNum = 0;
		autoComplete();
		}
	}
	
	private void autoComplete() {
	    Runnable doComplete = new Runnable() {
	        @Override
	        public void run() {
	        	if (input.compareTo(inputString) != 0) { //Makes sure they have started to generate suggestions
	    			results = b.getAutoCorrections(input); //calls suggest in the engine
	    			String completion = results.get(Math.abs(resultNum % results.size()));
	    			textInputField.setText(completion);
	    			textInputField.select(input.length(), completion.length());
	    		}
	        }
	    };       
	    SwingUtilities.invokeLater(doComplete);
	}
	
	@Override
	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (input == null) {
				return;
			}
				resultNum++;
				autoComplete();
				
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (input == null) {
				return;
			}
				resultNum--;
				autoComplete();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				textInputField.setText(results.get(Math.abs(resultNum % results.size())));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
