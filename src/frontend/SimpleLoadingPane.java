package frontend;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import backend.Constants;
 
public class SimpleLoadingPane extends JPanel {
 
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
    private JLabel taskOutput;
 
    public SimpleLoadingPane() {
        super(new BorderLayout());
        super.setOpaque(false);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true);
 
        taskOutput = new JLabel(String.format("Please Wait: %s", "Loading"), SwingConstants.CENTER);
        taskOutput.setForeground(Constants.GLOW_IN_THE_DARK);
 
        add(progressBar, BorderLayout.NORTH);
        add(taskOutput, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }
}