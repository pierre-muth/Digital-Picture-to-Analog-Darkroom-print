package grayToTime;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GrayScalePlayer extends JPanel implements ActionListener{

	private JButton jbStart;
	private JButton jbOpen;
	private JLabel jlImage;
	private JFileChooser fileChooser;
	private static String A_OPEN = "OPEN";
	private static String A_START = "START";

	private int[] timings = new int[256];

	private ImageIcon[] stepsImages = new ImageIcon[256];
	private ImageIcon stopImage;


	public GrayScalePlayer(){
		initGUI();
		computeTimings();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getActionCommand().equals(A_OPEN)){
			int returnVal = fileChooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("Openning " + fileChooser.getSelectedFile().getName());
			}
			try {
				computeAnimationImages(fileChooser.getSelectedFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (actionEvent.getActionCommand().equals(A_START)){
			startPlayingGrays();
		}

	}

	private void computeTimings(){

		int cumulatedTimeMs = 0;
		for (int i = 5; i < 256; i++) {
			cumulatedTimeMs = (int) (  (-17.06*Math.log((double)i)+96.417)*1000  );
			System.out.println("for gray " +i+", exposure "+ cumulatedTimeMs);
			timings[i] = cumulatedTimeMs;
		}

		for (int i = 5; i < timings.length-1; i++) {
			timings[i] = timings[i] - timings[i+1];
			if (timings[i] < 80) timings[i] = 80;
			System.out.println("for gray " +i+", time "+ timings[i]);
		}

		for (int i = 0; i < 5; i++) {
			timings[i] = timings[5]+100;
		}

		timings[255] = 800;

	}

	private void startPlayingGrays() {
		
		jbStart.setEnabled(false);
		jbStart.setText("Playing");

		new Thread(new Runnable() {

			@Override
			public void run() {
				long lastMili = new Date().getTime();
				long currentMili = 0;

				try {
					for (int j = 0; j < stepsImages.length; j++) {
						final ImageIcon step = stepsImages[j];

						SwingUtilities.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								jlImage.setIcon(step);
							}
						});

						final int stepPlaying = j;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								jbStart.setText("step "+stepPlaying+" /255");
							}
						});
						
						Thread.sleep(timings[j]);

						currentMili = new Date().getTime();
						System.out.println("Step "+j+ ", "+ timings[j] + "ms, measured: "+ (currentMili - lastMili) );
						lastMili = currentMili;
					}

					System.out.println("Stop");
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							jlImage.setIcon(stopImage);
							jbStart.setEnabled(true);
							jbStart.setText("START");
						}
					});

				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}


			}
		}).start();


	}

	private void computeAnimationImages(File imageFile) throws IOException {
		new Thread(new Runnable() {

			@Override
			public void run() {

				// create source image
				BufferedImage sourceImage;
				try {
					sourceImage = ImageIO.read(imageFile);
					int width = sourceImage.getWidth();
					int height = sourceImage.getHeight();
					// convert to grayscale
					BufferedImage imageGrayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
					Graphics2D g = imageGrayscale.createGraphics();
					g.drawImage(sourceImage, 0, 0, null);
					g.dispose();

					/* to output images on file
					IIOImage outputImage;
					FileImageOutputStream outputStream;
					ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("bmp").next();
					 */

					for (int i = 0; i < stepsImages.length; i++) {
						System.out.println("Computing step "+i);

						BufferedImage stepImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
						int[] pixels = new int[width*height];
						imageGrayscale.getData().getPixels(0, 0, width, height, pixels);

						for (int j = 0; j < pixels.length; j++) {
							if (pixels[j] < i) pixels[j] = 255;
							else pixels[j] = 0;
						}

						WritableRaster wr = stepImage.getData().createCompatibleWritableRaster();
						wr.setPixels(0, 0, width, height, pixels);
						stepImage.setData(wr);

						stepsImages[i] = new ImageIcon(stepImage);

						/* to output images on file
						outputImage = new IIOImage(stepImage, null, null);
						outputStream = new FileImageOutputStream( new File("C:\\Users\\muthi\\Pictures\\DAP\\anim\\"+String.format("%03d", i)+".bmp") );
						imageWriter.setOutput(outputStream);
						imageWriter.write(outputImage);
						imageWriter.dispose();
						outputStream.close();
						 */

						final int step = i;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								jbStart.setText("Computing step "+step+" /255");
							}
						});
					}

					stopImage = stepsImages[0];

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							jbStart.setEnabled(true);
							jbStart.setText("START");
							jlImage.setPreferredSize(new Dimension(width, height));
							jlImage.setIcon(stopImage);
						}
					});

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();



	}

	private void initGUI() {
		JPanel dummy01 = new JPanel();
		JPanel dummy02 = new JPanel();
		JPanel dummy03 = new JPanel();
		dummy01.setBackground(Color.WHITE);
		dummy02.setBackground(Color.WHITE);
		dummy03.setBackground(Color.WHITE);
		dummy01.setMinimumSize(new Dimension(0, 0));
		dummy02.setMinimumSize(new Dimension(0, 0));
		dummy03.setMinimumSize(new Dimension(0, 0));
		dummy01.setPreferredSize(new Dimension(1, 1));
		dummy02.setPreferredSize(new Dimension(1, 1));
		dummy03.setPreferredSize(new Dimension(1, 1));

		setLayout(new BorderLayout());
		JPanel jpButtons = new JPanel(new BorderLayout());
		add(jpButtons, BorderLayout.SOUTH);
		jbStart = new JButton(A_START);
		jbStart.setActionCommand(A_START);
		jbStart.addActionListener(this);
		jbStart.setEnabled(false);
		jbOpen = new JButton(A_OPEN);
		jbOpen.setActionCommand(A_OPEN);
		jbOpen.addActionListener(this);
		jpButtons.add(jbOpen, BorderLayout.WEST);
		jpButtons.add(jbStart, BorderLayout.CENTER);

		GridBagLayout lImage = new GridBagLayout();
		JPanel jpImage = new JPanel(lImage);
		jpImage.setBackground(Color.WHITE);
		jlImage = new JLabel();
		jlImage.setOpaque(true);
		jlImage.setBackground(Color.gray);
		jlImage.setHorizontalAlignment(SwingConstants.CENTER);
		jlImage.setVerticalAlignment(SwingConstants.CENTER);
		jlImage.setBorder(new LineBorder(Color.black, 5));
		jpImage.add(jlImage, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jpImage.add(dummy01, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jpImage.add(dummy02, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jpImage.add(dummy03, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		add(jpImage, BorderLayout.CENTER);

		fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Image file", "jpg", "jpeg", "png");
		fileChooser.setFileFilter(filter);

		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(300, 200));
	}

	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("GrayScale Player");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new GrayScalePlayer(), BorderLayout.CENTER );
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}


}
