package frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JList;
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
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import autocorrect.AutoCorrectConstants;
import backend.Backend;
import backend.Util;

public class SearchAutoFillPane extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField searchField = null;
	private JPopupMenu popup = null;
	Backend b;

	private JTable searchTable = null;
	private TableRowSorter<DefaultTableModel> rowSorter = null;
	private DefaultTableModel searchTableModel = null;

	public SearchAutoFillPane(Backend b) {
		this.b = b;
		searchTableModel = new DefaultTableModel();

		rowSorter = new TableRowSorter<DefaultTableModel>(searchTableModel);
		searchTable = new JTable(searchTableModel);
		searchTable.setFillsViewportHeight(true);
		searchTable.getColumnModel().setColumnSelectionAllowed(false);
		searchTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		searchTable.getTableHeader().setReorderingAllowed(false);
		searchTable.setPreferredSize(new Dimension(775, 100));
		searchTable.setGridColor(Color.WHITE);
		searchTable.setEnabled(false);

		searchField = new JTextField(50);
		searchField.setCaret(new HighlightCaret());
		
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
					cycleTableSelectionUp();
					break;
				}

				case KeyEvent.VK_DOWN:
				{
					cycleTableSelectionDown();
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
				
				case KeyEvent.VK_ENTER:
				{
					String suggestion = (String) searchTable.getValueAt(searchTable.getSelectedRow(), 0);
					System.out.println(suggestion);
					searchField.setCaret(new HighlightCaret());
					searchField.setText(suggestion);
					int end = searchField.getSelectionEnd();
					searchField.setSelectionStart(end);
					searchField.setSelectionEnd(end);
					break;
				}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		
		KeyStroke keyStroke = KeyStroke.getKeyStroke("ESCAPE");
		searchField.getInputMap().put(keyStroke, "ESCAPE");
		searchField.getActionMap().put("ESCAPE", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				//Do what you wish here with the escape key.
			}
		});

		popup = new JPopupMenu();
		popup.add(searchTable);
		popup.setVisible(false);
		popup.setBorder(BorderFactory.createEmptyBorder());

		this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.add(searchField, BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(775, 100));
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

	private void showPopup(DocumentEvent e) {
		if(e.getDocument().getLength() > 0) {
			initTableModel();
			if(!popup.isVisible()) { 
				Rectangle r = searchField.getBounds();
				Util.out(r);
				popup.show(searchField, (r.x-130), (r.y+16));
				popup.setVisible(true);
			}

			newFilter();
			searchField.grabFocus();

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

class HighlightCaret extends DefaultCaret {

    private static final Highlighter.HighlightPainter unfocusedPainter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(230, 230, 210));
    private static final long serialVersionUID = 1L;
    private boolean isFocused;

    @Override
    protected Highlighter.HighlightPainter getSelectionPainter() {
        return isFocused ? super.getSelectionPainter() : unfocusedPainter;
    }

    @Override
    public void setSelectionVisible(boolean hasFocus) {
        if (hasFocus != isFocused) {
            isFocused = hasFocus;
            super.setSelectionVisible(false);
            super.setSelectionVisible(true);
        }
    }
}

