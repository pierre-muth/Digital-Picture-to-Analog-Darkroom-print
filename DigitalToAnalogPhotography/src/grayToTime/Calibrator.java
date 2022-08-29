package grayToTime;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

public class Calibrator extends JPanel implements ActionListener{
	public static final int TILES_NB = 31;
	public static final int TILES_SIZE = 80;
	public static final int MS_PER_TILES = 500;
	private int iterration = TILES_NB - 1;
	private Timer timer;
	private TilesRun tilesRun;
	private JButton jbStart;
	private JLabel[] tiles;
	
	public Calibrator() {
		initGUI();

		timer = new Timer();
	}
	
	private void initGUI() {
		setLayout(new BorderLayout());
		jbStart = new JButton("Start");
		jbStart.addActionListener(this);
		add(jbStart, BorderLayout.SOUTH);
		JPanel jpTiles = new JPanel(new GridLayout(1, TILES_NB));
		add(jpTiles, BorderLayout.CENTER);
		tiles = new JLabel[TILES_NB];
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = new JLabel(""+(i*MS_PER_TILES)/1000.0);
			tiles[i].setOpaque(true);
			tiles[i].setBackground(Color.BLACK);
			tiles[i].setForeground(Color.WHITE);
			tiles[i].setPreferredSize(new Dimension(TILES_SIZE, TILES_SIZE));
			tiles[i].setBorder(new LineBorder(Color.white, 1));
			tiles[i].setHorizontalAlignment(SwingConstants.CENTER);
			jpTiles.add(tiles[i]);
		}
		

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (tilesRun == null) {
			tilesRun = new TilesRun();
			timer.schedule(tilesRun, 1000, MS_PER_TILES);
		}
	}
	
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("Calibrator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new Calibrator(), BorderLayout.CENTER );
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	private class TilesRun extends TimerTask {
		@Override
		public void run() {
			if (iterration == 0) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							
							for (int i = 0; i < tiles.length; i++) {
								tiles[i].setBackground(Color.BLACK);
								tiles[i].setForeground(Color.WHITE);
							}
							timer.cancel();
							
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							
							tiles[iterration].setBackground(Color.white);
						
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			iterration--;
			
		}
		
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});

	}


}
