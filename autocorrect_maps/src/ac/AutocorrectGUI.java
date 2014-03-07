package ac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
//for action events
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class AutocorrectGUI extends JPanel implements ActionListener, DocumentListener, KeyListener {

	/** The GUI Interface for autocorrect. Much more complicated than the commandLine
	 *  interface because of swing, but also way cooler. It makes suggestions in 
	 *  REAL TIME! and you can select them and write stuff in it. I think its dope.
	 *  I spent a ton of time on optimizations to make it fast enough, so I hope you like it!
	 */
	private static final long serialVersionUID = 1L; //Not sure why it wanted me to put this here
	private static final String InputString = "Type here...";
	private static final String logoPath = "data/img/Autocorrect.png";
	private String path = "";
	private JLabel blueLabel;
	private JTextField textInputField;
	private JTextArea resultsText;
	private JButton clearButton, addTextButton;
	private JPanel resultsPanel, buttonPanel, contentPane;
	private JScrollPane scrollingResults;
	private JFileChooser fc;
	private Engine engine;
	private List<String> results;
	private int numEnter;
	
	static Color blue = new Color(28, 75, 140);

	AutocorrectGUI(Engine e) {
		this.engine = e;
		JFrame frame = new JFrame("AutoCorrect"); //Creates the frame

		frame.setContentPane(this.createDefaultContentPane());

		frame.setMinimumSize(new Dimension(600, 400));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	private JPanel createDefaultContentPane() {

		//Create a blue label with the logo in it to put in the content pane.
		ImageIcon originalSizeLogo = new ImageIcon(logoPath);
		Image img = originalSizeLogo.getImage();
		Image scaledImg = img.getScaledInstance(600, 300, java.awt.Image.SCALE_SMOOTH);
		ImageIcon resizedLogo = new ImageIcon(scaledImg);
		

		blueLabel = new JLabel(resizedLogo, SwingConstants.CENTER);
		blueLabel.setOpaque(true);
		blueLabel.setBackground(blue);
		blueLabel.setPreferredSize(new Dimension(600, 300));

		//Create a Text Field, which will be the input field for the user
		textInputField = new JTextField(20);
		textInputField.setText(InputString);
		textInputField.setOpaque(true);
		textInputField.setPreferredSize(new Dimension(100, 35));
		textInputField.setFont(new Font("Serif", Font.ITALIC, 14));
		textInputField.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		textInputField.addKeyListener(this);
		textInputField.getDocument().addDocumentListener(this); //For the real time text processing

		//Create a button to make a new directory
		addTextButton = new JButton("Add Source Text Files");
		addTextButton.addActionListener(this);
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY); //We don't want directories
		fc.setToolTipText("Choose a text file");


		//A button to clear the source tree.
		clearButton = new JButton("Clear Source Text");        
		clearButton.addActionListener(this);

		//This section is for displaying the suggestions.
		//Bottom container panel
		resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.LINE_AXIS));

		//scrollable Results panel. (In case we ever want more than 5 suggestions.)
		resultsText = new JTextArea();
		resultsText.setText("");
		resultsText.setEditable(false);
		resultsText.setOpaque(false);

		scrollingResults = new JScrollPane(resultsText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		scrollingResults.setPreferredSize(new Dimension(400, 100));
		scrollingResults.setBorder(BorderFactory.createLineBorder(blue, 30));
		resultsPanel.add(scrollingResults);
		resultsPanel.setBackground(blue);

		
		//Creates a panel of buttons to operate the program.
		buttonPanel = new JPanel();
		buttonPanel.setBackground(new Color(255, 255, 255));
		buttonPanel.add(addTextButton);
		buttonPanel.add(clearButton);

		//Put it all together
		//Set the menu bar and add the label to the content pane.        
		contentPane = new JPanel(new BorderLayout());
		contentPane.add(blueLabel, BorderLayout.CENTER);
		contentPane.add(textInputField, BorderLayout.SOUTH);
		contentPane.add(buttonPanel, BorderLayout.NORTH);
		
		
		
		//do we have any reference files? If not, we don't want to be able to use the program.
		if (!engine.hasFiles()) {
			clearButton.setEnabled(false);
			textInputField.setEnabled(false);
		}
		return contentPane;
	}

	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addTextButton) { //Opens the file chooser for adding new files.
			int returnVal = fc.showOpenDialog(AutocorrectGUI.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				path = file.getPath();
				if (path.length() > 0) {
					this.engine.addFile(path);
				clearButton.setEnabled(true);
				addTextButton.setText("Add Another Source File");
				textInputField.setEnabled(true); //Allows us to use the input field
				}
			}
		}
		else if (e.getSource() == clearButton) { //Clears RadixTree
			this.engine.clearRT();
			addTextButton.setText("Add A Source File"); 
			textInputField.setEnabled(false); //Prevents a user from typing
			clearButton.setEnabled(false); //No sense clearing twice
		}
	}

	@Override
	public void insertUpdate(DocumentEvent ev) { //Generates suggestions in real time
	    if (ev.getLength() != 1) { //In case 2 events get thrown somehow. (StackOverflow made me do it)
            return;
        }
		String input = textInputField.getText(); //Gets the users input
		if (input.compareTo(InputString) != 0) { //Makes sure they have started to generate suggestions
			numEnter = 0; //For selecting which result they want. Reset for each new set of suggestions
			resultsText.setText("Results are: \n");
			results = engine.suggest(input); //calls suggest in the engine
			for(String s : results) {
				resultsText.append(s + "\n");
			}
			contentPane.add(resultsPanel, BorderLayout.CENTER); //Add the results pane to the contentPane
			blueLabel.setVisible(false);
			textInputField.setBackground(new Color(255, 255, 255));
		}
	}
	
	@Override //When the user hits enter, cycles through the results. 
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				this.textInputField.setText(results.get(numEnter % results.size()));
				numEnter++;
		}
	}

	//Required (by eclipse?), but we don't want to use these for anything.
	@Override
	public void removeUpdate(DocumentEvent ev) {
	    if (ev.getLength() != 1) {
            return;
        }		
	}

	@Override
	public void changedUpdate(DocumentEvent ev) {
	    if (ev.getLength() != 1) {
            return;
        }		
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

}
