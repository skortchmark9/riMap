package frontend;

import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import maps.Way;
import backend.Constants;

public class DirectionsPopup extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JComponent _parent;
	private JTable searchTable;
	private DefaultTableModel searchTableModel;
	public final static int WALKING = 5;
	public final static int RUNNING = 10;
	public final static int BIKING = 20;
	public final static int DRIVING = 40;


	DirectionsPopup(JComponent parent) {
		super();
		_parent = parent;
		setOpaque(true);
		setBackground(Color.WHITE);
		setVisible(false);
		setEnabled(false);
		searchTableModel = new DefaultTableModel();
		searchTable = new JTable(searchTableModel);
		searchTable.getTableHeader().setReorderingAllowed(false);
		searchTable.getColumnModel().setColumnSelectionAllowed(false);
		searchTable.setGridColor(Constants.MIDNIGHT);
		searchTable.setEnabled(false);
		searchTable.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(searchTable);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(_parent.getWidth() - 20, _parent.getHeight()));
		add(new JPanel().add(scrollPane));
	}


	/**
	 * Hides the autosuggestions.
	 */
	public void hidePopup() {
		if (isVisible()) {
			setVisible(false);
		}
	}

	public void showPopup(List<Way> ways, int speed) {
		if (ways.isEmpty())
			return;
		else {
			initTableModel(provideDirectionData(ways, speed));
			if(!isVisible()) { 
				show(_parent, 4, (_parent.getHeight() - 4));
				setVisible(true);
			}
		}
	}

	private String[][] provideDirectionData(List<Way> ways, int speed) {
		ArrayList<String[]> data = new ArrayList<>();
		Iterator<Way> wayItr = ways.iterator();
		String streetName = ways.get(0).getName();
		double streetDistance = 0;
		double streetTime = 0;
		double totalDistance = 0;
		double totalTime = 0;
		while (wayItr.hasNext()) {
			Way currentWay = wayItr.next();
			String currentName = currentWay.getName();
			double currentDistance =  currentWay.getDistanceInKM();
			double currentTime = currentWay.getTime(speed) * 60;

			if (!streetName.equals(currentName) || !wayItr.hasNext()) {
				data.add(new String[]
						{streetName.length() == 0 ? "No Name" : streetName,
								toStringShort(streetDistance),
								toStringShort(streetTime)});
				totalDistance += streetDistance;
				totalTime += streetTime;

				streetName = currentName;
				streetDistance = currentDistance;
				streetTime = currentTime;

			} else {
				streetDistance += currentDistance;
				streetTime += currentTime;
			}
		}
		data.add(new String[] {"Total", toStringShort(totalDistance), toStringShort(totalTime)});
		return data.toArray(new String[data.size()][]);
	}

	private static String toStringShort(double d) {
		 final DecimalFormat df = new DecimalFormat(); 
		 df.setMaximumFractionDigits(2); 
		 return df.format(d);
	}

	/**
	 * Initializes the table model - filling the table rows with information
	 * about the path
	 */
	private void initTableModel(String[][] data) {
		//The table determines the size of the autocompletion suggestions,
		//so we wait until the text field has its size assigned by the gui
		//manager.
		String[] columns = new String[] {"Streets", "Distance (km)", "Walking Time (m)"};
		searchTableModel.setDataVector(data, columns);
	}
}
