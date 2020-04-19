package grayToTime;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

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
import javax.swing.SwingUtilities;
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
			if (timings[i] < 100) timings[i] = 100;
			System.out.println("for gray " +i+", time "+ timings[i]);
		}
		
		for (int i = 0; i < 5; i++) {
			timings[i] = timings[5];
		}
		
		timings[255] = 2000;
		
	}

	private void startPlayingGrays() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					for (int j = 0; j < stepsImages.length; j++) {
						final ImageIcon step = stepsImages[j];

						SwingUtilities.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								jlImage.setIcon(step);
							}
						});
						System.out.println("Step "+j+ ", "+ timings[j] + "ms");
						Thread.sleep(timings[j]);
					}
					
					System.out.println("Stop");
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							jlImage.setIcon(stopImage);
						}
					});
					
				} catch (InvocationTargetException | InterruptedException e) {
					e.printStackTrace();
				}


			}
		}).start();


	}

	private void computeAnimationImages(File imageFile) throws IOException {
		// create source image
		BufferedImage sourceImage = ImageIO.read(imageFile);
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		// convert to grayscale
		BufferedImage imageGrayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = imageGrayscale.createGraphics();
		g.drawImage(sourceImage, 0, 0, null);
		g.dispose();

		// to output images on file
//		IIOImage outputImage;
//		FileImageOutputStream outputStream;
//		ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("bmp").next();
		//
		
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
			
			// to output images on file
//			outputImage = new IIOImage(stepImage, null, null);
//			outputStream = new FileImageOutputStream( new File("C:\\Users\\muthi\\Pictures\\DAP\\anim\\"+String.format("%03d", i)+".bmp") );
//			imageWriter.setOutput(outputStream);
//			imageWriter.write(outputImage);
//			imageWriter.dispose();
//			outputStream.close();
			//
		}
		
		stopImage = stepsImages[0];

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				jbStart.setEnabled(true);
				jlImage.setPreferredSize(new Dimension(width, height));
				jlImage.setIcon(stopImage);
			}
		});
		


		
		
	}

	private void initGUI() {
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

		jlImage = new JLabel();
		jlImage.setOpaque(true);
		jlImage.setBackground(Color.gray);
		add(jlImage, BorderLayout.CENTER);

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
