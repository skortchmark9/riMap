package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import backend.Backend;
import backend.Util;

public class SearchAutoFillPane extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField searchField = null;
	private JPopupMenu popup = null;
	private Backend b;

	private JTable searchTable = null;
	private TableRowSorter<DefaultTableModel> rowSorter = null;
	private DefaultTableModel searchTableModel = null;

	public SearchAutoFillPane(Backend b, String paneName) {
		
		this.b = b;
		searchTableModel = new DefaultTableModel();
		searchField = new JTextField(10);
		searchField.getInputMap().put(KeyStroke.getKeyStroke("pressed UP"), "nothing");
		searchField.getInputMap().put(KeyStroke.getKeyStroke("pressed DOWN"), "nothing");

		rowSorter = new TableRowSorter<DefaultTableModel>(searchTableModel);
		searchTable = new JTable(searchTableModel);
		searchTable.setPreferredSize(new Dimension(searchField.getPreferredSize().width - 8, 80));
		searchTable.setFillsViewportHeight(true);
		searchTable.getColumnModel().setColumnSelectionAllowed(false);
		searchTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		searchTable.getTableHeader().setReorderingAllowed(false);
		searchTable.setGridColor(Color.WHITE);
		searchTable.setEnabled(false);


		searchField.addFocusListener(new FocusListener() {
		    @Override
		    public void focusGained(FocusEvent e) {
		    	int end = searchField.getDocument().getLength();
		    	searchField.setCaretPosition(end);
		    }

		    @Override
		    public void focusLost(FocusEvent e) {
		        searchField.getHighlighter().removeAllHighlights();
		    }
			
		});
		

		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				showPopup(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				showPopup(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				showPopup(e);
			}
		});

		searchField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				int code = e.getKeyCode();
				switch(code)
				{
				case KeyEvent.VK_UP:
				{
					if (popup.isVisible()) {
					cycleTableSelectionUp();
					} else {
						searchField.setCaretPosition(0);
					}
					break;
				}

				case KeyEvent.VK_DOWN:
				{
					if (popup.isVisible()) {
					cycleTableSelectionDown();
					} else {
						searchField.setCaretPosition(searchField.getDocument().getLength());
					}
					break;
				}

				case KeyEvent.VK_LEFT:
				{
					//Do whatever you want here
					break;
				}

				case KeyEvent.VK_RIGHT:
				{
					//Do whatever you want here
					break;
				}
				
				case KeyEvent.VK_TAB:
				{
					if (popup.isVisible()) {
						hidePopup();
					}
				}

				case KeyEvent.VK_ENTER:
				{
					if (popup.isVisible()) {
						int selectedRow = searchTable.getSelectedRow();
						if (selectedRow >= 0) {
						String suggestion = (String) searchTable.getValueAt(selectedRow, 0);
						System.out.println(suggestion);
						searchField.setText(suggestion);
						int end = searchField.getSelectionEnd();
						searchField.setSelectionStart(end);
						searchField.setSelectionEnd(end);
						hidePopup();
						} else {
							hidePopup();
						}
					}
					break;
				}
				case KeyEvent.VK_ESCAPE:
					hidePopup();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		popup = new JPopupMenu();
		popup.add(searchTable);
		popup.setVisible(false);
		popup.setBorder(BorderFactory.createEmptyBorder());

		this.add(searchField, BorderLayout.CENTER);

	}
	
	public String getText() {
		return searchField.getText();
	}

	private final void newFilter() {
		RowFilter<DefaultTableModel, Object> rf = null;

		try {
			rf = RowFilter.regexFilter(getFilterText(), 0);
		}
		catch(PatternSyntaxException e) {
			return;
		}
		rowSorter.setRowFilter(rf);
	}

	private final String getFilterText() {
		String orig = searchField.getText();
		return "("+orig.toLowerCase()+")|("+orig.toUpperCase()+")";
	}

	private void hidePopup() {
		if (popup.isVisible()) {
			popup.setVisible(false);
		}
	}

	private void showPopup(DocumentEvent e) {
		if(e.getDocument().getLength() > 0) {
			initTableModel();
			if(!popup.isVisible()) { 
				Rectangle r = searchField.getBounds();
				
				popup.show(searchField, 0 + 4, (r.y+16));
				popup.setVisible(true);
			}

			newFilter();
			searchField.requestFocusInWindow();
		}
		else {
			popup.setVisible(false);
		}
	}

	private void cycleTableSelectionUp() {
		ListSelectionModel selModel = searchTable.getSelectionModel();
		int index0 = selModel.getMinSelectionIndex();
		if(index0 > 0) {
			selModel.setSelectionInterval(index0-1, index0-1);
		}
	}

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

	private void initTableModel() {
		String input = searchField.getText();
		String[] columns = new String[] {input};
		List<String> suggestions = b.getAutoCorrections(input);
		List<String> cappedSuggestions = new LinkedList<>();
		int length = suggestions.size();
		for(String s : suggestions) {
			cappedSuggestions.add(Util.capitalizeAll(s));
		}

		String[][] data = new String[length][];
		for(int i = 0; i < length; i++) {
			data[i] = new String[] {cappedSuggestions.remove(0)};
		}
		searchTableModel.setDataVector(data, columns);
	}
}
