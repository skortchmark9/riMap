package frontend;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import backend.Constants;

import com.sun.java.swing.plaf.windows.WindowsBorders.ProgressBarBorder;

import java.beans.*;
import java.util.Random;
 
public class LoadingPane extends JPanel {
 
    private JProgressBar progressBar;
    private JButton startButton;
    private JLabel taskOutput;
 
 
    public void updateProgress(String s, int progress) {
    	try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
		}
    	taskOutput.setText(String.format("Please Wait: %s", s));
    	progressBar.setValue(progress);
    	if (progress >= 100) {
    		setCursor(null);
    	}
    }
 
    public LoadingPane() {
        super(new BorderLayout());
        super.setOpaque(false);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
 
        taskOutput = new JLabel(String.format("Please Wait: %s", "Loading"), JLabel.CENTER);
        taskOutput.setForeground(Constants.GLOW_IN_THE_DARK);
 
 
        add(progressBar, BorderLayout.NORTH);
        add(taskOutput, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }
}
