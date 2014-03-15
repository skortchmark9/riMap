package frontend;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import maps.Node;

public class MapPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float scale = 1f;
	private final int WIDTH = 700;
	private final int HEIGHT = 700;
	private List<Node> startLocs;
	private List<Node> endLocs;


	MapPane(String fileName)   {
		setBackground(Color.black);
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		startLocs = new LinkedList<>();
		endLocs = new LinkedList<>();
		String line;
		try {
			FileReader fileReader = new FileReader(fileName); //Attempts to read the given file
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) !=null) { //Loops while bufferedReader can find a next line
				parseWay(line);
			} //This section is commented out because I believe it would break tests.
			bufferedReader.close(); 
		}
		catch(FileNotFoundException ex) {
			System.out.println("ERROR: Unable to open file '" + fileName + "'");
			System.exit(1);
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		g2d.drawLine(0, 0, WIDTH - 1, 0);
		g2d.drawLine(0, 0, 0, HEIGHT - 1);
		g2d.drawLine(WIDTH - 1, 0, WIDTH - 1, HEIGHT - 1);
		g2d.drawLine(0, HEIGHT - 1, WIDTH - 1, HEIGHT - 1);

		g2d.setColor(Color.WHITE);
		for(int i = 0; i < startLocs.size(); i++) {
			Node startLoc = startLocs.get(i);
			Node endLoc = endLocs.get(i);
			g.drawLine((int) startLoc.getLon(), (int) startLoc.getLat(), (int) endLoc.getLon(), (int) endLoc.getLat());
		}
	}


	void parseWay(String way) {
		String[] locs = way.split("\t");
//		startLocs.add(new PathNode(Double.parseDouble(locs[0]), Double.parseDouble(locs[1])));
//		endLocs.add(new PathNode(Double.parseDouble(locs[2]), Double.parseDouble(locs[3])));
	}
}

