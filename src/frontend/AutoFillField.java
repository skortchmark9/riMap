package frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import backend.Util;
import client.Client;

@SuppressWarnings("serial")
public class AutoFillField extends JTextField {

	private JPopupMenu popup;
	private Client client;
	private JTable searchTable;
	private DefaultTableModel searchTableModel;
	private String initialText;
	private List<String> suggestions;
	private final int boxNo;
	//Suppress prevents the autocorrections from revealing after the document change listener
	//detects a change due to an accepted suggestion
	private boolean popped, suppress = false;

	
	/**
	 * This class is the autofillfield for the GUI - we have four
	 *  of them, one for each intersection we handle.
	 * @param client
	 * @param startField
	 * @param boxNo
	 */
	public AutoFillField(Client client, String startField, int boxNo) {
		super(10);
		this.client = client;
		initialText = startField;
		this.boxNo = boxNo;
		setForeground(Color.DARK_GRAY);
		setText(initialText);
		setOpaque(true);

		//The autofillfield is implemented as a single column table
		searchTableModel = new DefaultTableModel();
		searchTable = new JTable(searchTableModel);
		searchTable.setFillsViewportHeight(true);
		searchTable.getColumnModel().setColumnSelectionAllowed(false);
		searchTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		searchTable.getTableHeader().setReorderingAllowed(false);
		searchTable.setGridColor(Color.WHITE);
		searchTable.setEnabled(false);

		popup = new JPopupMenu();
		popup.add(searchTable);
		popup.setVisible(false);
		popup.setBorder(BorderFactory.createEmptyBorder());

		//We need this because of the focus juggling we do, we don't want to 
		//highlight the text field every time we accept a selection. It would
		//be awesome if we could deactivate the default focus behavior of the
		//text field, but I don't think we can.
		addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (popped) {
					int end = getDocument().getLength();
					setCaretPosition(end);
				} else {
					selectAll();
					setForeground(Color.BLACK);
					popped = true;
				}
			}
			@Override
			public void focusLost(FocusEvent e) {
				getHighlighter().removeAllHighlights();
			}
		});

		//We show the popup on user input.
		getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				if (e.getLength() == 1)
					requestAutocorrections();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				if (e.getLength() == 1)
					requestAutocorrections();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				if (e.getLength() == 1)
					requestAutocorrections();
			}
		});
		
		this.addMouseListener(new MouseListener() {
			@Override public void mousePressed(MouseEvent e) {
				selectAll();
			}
			
			//Dont need these methods...
			@Override public void mouseClicked(MouseEvent e) {
				selectAll();
			}
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
		});

		//Key Bindings For Field
		String cycleUp = "cycle up";
		String cycleDown = "cycle down";
		String esc = "escape";
		String acceptSpace = "accept with space";
		String acceptEnter = "accept with enter";

		InputMap inputMap = getInputMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), cycleUp);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), cycleDown);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), esc);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), esc);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), acceptSpace);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), acceptEnter);

		ActionMap actionMap = getActionMap();
		actionMap.put(cycleUp, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//If the popup isn't visible, the user is probably trying to
				//use the normal behavior of the text box
				if (popup.isVisible()) 
					cycleTableSelectionUp();
				else
					setCaretPosition(0);
			}
		});
		actionMap.put(cycleDown, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Same deal here.
				if (popup.isVisible())
					cycleTableSelectionDown();
				else
					setCaretPosition(getDocument().getLength());

			}
		});

		actionMap.put(esc, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hidePopup();
			}
		});

		actionMap.put(acceptSpace, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// We accept the selection, but add a space to the end so the
				// user can continue typing, as was probably her intention.
				if (popup.isVisible()) {
					int selectedRow = searchTable.getSelectedRow();
					if (selectedRow >= 0) {
						String suggestion = (String) searchTable.getValueAt(selectedRow, 0);
						setText(suggestion + " ");
						int end = getSelectionEnd();
						setSelectionStart(end);
						setSelectionEnd(end);
					}
					hidePopup();
				}
			}
		});

		actionMap.put(acceptEnter, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (popup.isVisible()) {
					int selectedRow = searchTable.getSelectedRow();
					if (selectedRow >= 0) {
						String suggestion = (String) searchTable.getValueAt(selectedRow, 0);
						suppress = true;
						setText(suggestion);
						int end = getSelectionEnd();
						setSelectionStart(end);
						setSelectionEnd(end);
					}
					hidePopup();
				}
			}
		});
	}

	/**
	 * Hides the autosuggestions.
	 */
	private void hidePopup() {
		if (popup.isVisible()) {
			popup.setVisible(false);
		}
	}

	/** A request will be sent from the client to the server, 
	 * and when it gets a response it will call this method.
	 * 
	 * @param suggestions
	 */
	public void setSuggestions(List<String> suggestions) {
		this.suggestions = suggestions;
		if (suppress) {
			suppress = false;
		} else {
		showPopup();
		}
	}

	private void showPopup() {
		initTableModel();
		if(!popup.isVisible()) { 
			popup.show(this, 4, (getHeight() - 4));
			popup.setVisible(true);
		}
		//We need to juggle focus a little to select the appropriate row.
		requestFocusInWindow();
	}
	
	public void populateField(String s, boolean suppressPopup) {
		suppress = suppressPopup;
		setForeground(Color.BLACK);
		setText(s);
	}

	/**
	 * Moves up to the previous search result.
	 */
	private void cycleTableSelectionUp() {
		ListSelectionModel selModel = searchTable.getSelectionModel();
		int index0 = selModel.getMinSelectionIndex();
		if(index0 > 0) {
			selModel.setSelectionInterval(index0-1, index0-1);
		}
	}

	/**
	 * Moves down to the next search result.
	 */
	private void cycleTableSelectionDown() {
		ListSelectionModel selModel = searchTable.getSelectionModel();
		int index0 = selModel.getMinSelectionIndex();
		if(index0 == -1) {
			selModel.setSelectionInterval(0, 0);
		}
		else if(index0 > -1) {
			selModel.setSelectionInterval(index0+1, index0+1);
		}
	}

	private void requestAutocorrections() {
			if (!this.getText().equals(initialText))
				client.requestAutocorrections(this.getText(), boxNo);
	}
	
	/**
	 * Resets the autofillfield to its initial value
	 */
	public void reset() {
		setForeground(Color.DARK_GRAY);
		populateField(initialText, true);
		setForeground(Color.BLACK);
		popped = false;
	}


	/**
	 * Initializes the table model - filling the table rows with suggestions
	 * from the backend.
	 */
	private void initTableModel() {
		//The table determines the size of the autocompletion suggestions,
		//so we wait until the text field has its size assigned by the gui
		//manager.
		searchTable.setPreferredSize(new Dimension(getWidth() - 8, 80));
		String input = getText();
		String[] columns = new String[] {input};
		if (suggestions == null)
			return;
		String[][] data = new String[suggestions.size()][];
		for(int i = 0; i < data.length; i++) {
			//We lowercase all text entering the prefix tree, so here we need
			//to capitalize it again. Thankfully, all street names are proper nouns.
			data[i] = new String[] {Util.capitalizeAll(suggestions.get(i))};
		}
		searchTableModel.setDataVector(data, columns);
	}
}
