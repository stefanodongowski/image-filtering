package imageProcessing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.TreeSet;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import imageProcessing.GraphViewer;

/**
 * This class allows you to view and edit pictures.
 */
public class Picture extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private String source;
	private JLabel label;
	private BufferedImage image;
	private boolean showLabel;
	public int[] histogram;
	private int maxCount;

	/**
	 * Constructs a blank picture.
	 */
	public Picture() {
		super("Dongowski - Image Processor");  // to do #6 - change out last name
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setJMenuBar(createMenuBar());
		label = new JLabel("(No Image)");
		label.setBackground(new Color(155, 155, 155));
		label.setOpaque(true);
		add(label);
		pack();
		setVisible(true);
	}

	/**
	 * Constructs and labels the picture found in the given path
	 * 
	 * @param path to the image, may be absolute, relative, or a web address
	 */
	public Picture(String path) {
		this();
		showLabel = true;
		load(path);
		setHistogram();
	}

	/**
	 * Constructs a bordered picture with or without a label
	 *     invokes this(), initializes the field showLabel, sets label text to the empty string,
	 *     invokes load with path,
	 *     invokes scale with newWidth and newHeight each set to 40 less than 
	 *         image width and height, leaving 10 pixels of background inside the border 
	 *         if the border is 10 pixels
	 *     invokes border with 10 and new Color(55, 55, 55)
	 * @param path      to the image, may be a web address
	 * @param showLabel
	 */
	public Picture(String path, boolean showLabel) {
		this(path);
		this.showLabel = showLabel;
		frameImage();
	}
	
	private void frameImage() {
		int newWidth = getImageWidth() - 40;
	    int newHeight = getImageHeight() - 40;
	    scale(newWidth, newHeight);
	    moveImage(20, 20);
//	    border(10);
	    border(10, new Color(55, 55, 55));
	}

	/**
	 * Creates a JMenuBar. See sampleOut for completed menus.
	 * @return a complete JMenuBar with all items registered with listeners. 
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		JMenuItem menuItem = new JMenuItem("Open", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);

		menuItem = new JMenuItem("Save", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(this);
		menu.add(menuItem);

		String[] formats = getFormats();
		// to do #1 - add a Save As... JMenu to menu after populating it with 
		// formats returned from getFormats by iterating over formats, 
		// instantiating a menuItem for each format in formats and registering this as the listener.
		JMenu menuSA = new JMenu("Save As...");
		menuSA.setMnemonic('A');
		for(String fmt : formats) {
			menuItem = new JMenuItem(fmt);
			menuItem.addActionListener(this);
			menuSA.add(menuItem);
		}
		menu.add(menuSA);
		
		menuBar.add(menu);
		
		menu = new JMenu("Options");
		menu.setMnemonic(KeyEvent.VK_O);
		String[] options = {"Original", "Frame", "Grayscale", "Grayscale - Luminance", "Save Grayscale-Luminance Histogram","Show Histogram", "Equalize Histogram", 
				"Sobel Edge Detect", "Show Image Name (on/off)"};
		// to do #2 - iterate over options, instantiate a menuItem and register this as the listener for each opt in options.
		for(String opt : options) {
			menuItem = new JMenuItem(opt);
			menuItem.addActionListener(this);
			menu.add(menuItem);
		}
		menuBar.add(menu);
		return menuBar;
	}

	/**
	 * Gets the width of this picture.
	 * 
	 * @return the width
	 */
	public int getImageWidth() {
		return image.getWidth();
	}

	/**
	 * Gets the height of this picture.
	 * 
	 * @return the height
	 */
	public int getImageHeight() {
		return image.getHeight();
	}

	/**
	 * Loads a picture from a given source.
	 * 
	 * @param source the image source. If the source starts with http://, it is a
	 *               URL, otherwise, a filename.
	 */
	public void load(String source) {
		try {
			this.source = source;
			BufferedImage img;
			if (source.startsWith("http://"))
				img = ImageIO.read(new URL(source).openStream());
			else
				img = ImageIO.read(new File(source));

			setImage(img);
		} catch (Exception ex) {
			this.source = null;
			ex.printStackTrace();
		}
	}

	/**
	 * Reloads this picture, undoing any manipulations.
	 */
	public void reload() {
		load(source);
	}

	/**
	 * Displays a file chooser for picking a picture.
	 */
	public void pick() {
		JFileChooser chooser = new JFileChooser("./images");
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			load(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * Moves this picture by the given amount in x- and y-direction.
	 * 
	 * @param dx the offset in the x-direction
	 * @param dy the offset in the y-direction
	 */
	public void moveImage(int dx, int dy) {
		BufferedImageOp op = new AffineTransformOp(AffineTransform.getTranslateInstance(dx, dy),
				AffineTransformOp.TYPE_BILINEAR);
		BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		op.filter(image, filteredImage);
		setImage(filteredImage);
	}

	/**
	 * Scales this picture to a new size. If the new size is smaller than the old
	 * size, the remainder is filled with transparent pixels. If it is larger, it is
	 * clipped.
	 * 
	 * @param newWidth  the new width of the picture
	 * @param newHeight the new height of the picture
	 */
	public void scale(int newWidth, int newHeight) {
		double dx = newWidth * 1.0 / image.getWidth();
		double dy = newHeight * 1.0 / image.getHeight();
		BufferedImageOp op = new AffineTransformOp(AffineTransform.getScaleInstance(dx, dy),
				AffineTransformOp.TYPE_BILINEAR);
		BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		op.filter(image, filteredImage);
		setImage(filteredImage);
	}

	/**
	 * Adds a black border to the image.
	 * 
	 * @param width the border width
	 */
	public void border(int width) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				setColorAt(x, y, Color.BLACK);
				setColorAt(image.getWidth() - 1 - x, y, Color.BLACK);
			}
		}
		for (int y = 0; y < width; y++) {
			for (int x = width; x < image.getWidth() - width; x++) {
				setColorAt(x, y, Color.BLACK);
				setColorAt(x, image.getHeight() - 1 - y, Color.BLACK);
			}
		}
	}
	/**
	 * Adds a color border to the image
	 * @param width
	 * @param c
	 */
	public void border(int width, Color c) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				setColorAt(x, y, c);
				setColorAt(image.getWidth() - 1 - x, y, c);
			}
		}
		for (int y = 0; y < width; y++) {
			for (int x = width; x < image.getWidth() - width; x++) {
				setColorAt(x, y, c);
				setColorAt(x, image.getHeight() - 1 - y, c);
			}
		}
	}

	/**
	 * Gets the color of a pixel.
	 * 
	 * @param x the column index (between 0 and getWidth() - 1)
	 * @param y the row index (between 0 and getHeight() - 1)
	 * @return the color of the pixel at position (x, y)
	 */
	public Color getColorAt(int x, int y) {
		Raster raster = image.getRaster();
		ColorModel model = image.getColorModel();
		int argb = model.getRGB(raster.getDataElements(x, y, null));
		return new Color(argb, true);
	}


	/**
	 * Sets the color of a pixel.
	 * 
	 * @param x the column index (between 0 and getWidth() - 1)
	 * @param y the row index (between 0 and getHeight() - 1)
	 * @param c the color for the pixel at position (x, y)
	 */
	public void setColorAt(int x, int y, Color c) {
		WritableRaster raster = image.getRaster();
		ColorModel model = image.getColorModel();
		Object colorData = model.getDataElements(c.getRGB(), null);
		raster.setDataElements(x, y, colorData);
		label.repaint();
	}

	private void setImage(BufferedImage image) {
		this.image = image;
		label.setIcon(new ImageIcon(image));
		if (showLabel) {
			// Set the position of the text, relative to the icon:
			label.setVerticalTextPosition(JLabel.BOTTOM);
			label.setHorizontalTextPosition(JLabel.CENTER);
			label.setText(getImageName());
		} else  label.setText("");
		label.setSize(image.getWidth(), image.getHeight());
		pack();
	}
	/**
	 * Parses the field source to retrieve just the image name without the extension.
	 * @return the name of the image. For example source is "/images/queen-mary.png" would
	 *     return the String queen-mary.
	 */
	private String getImageName() {
		// to do #3
		return source.substring(
				(source.lastIndexOf('\\') == -1 ? source.lastIndexOf('/') : source.lastIndexOf('\\')) + 1, 
				source.lastIndexOf('.'));
	}
	
	private void save() {
		save(source.substring(source.lastIndexOf('.') + 1));
	}
	
	private void save(String format) {
		/*
		 * Use the format name to initialize the file suffix. Format names typically
		 * correspond to suffixes
		 */
		File saveFile = new File(getImageName() + "." + format);
		JFileChooser chooser = new JFileChooser("./images");
		chooser.setSelectedFile(saveFile);
		int rval = chooser.showSaveDialog(this);
		if (rval == JFileChooser.APPROVE_OPTION) {
			saveFile = chooser.getSelectedFile();
			
			try {
				ImageIO.write(image, format, saveFile);
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	/**
	 * Returns an array of Strings listing all of the
	 * informal format names understood by the current set of registered writers.
	 * @return the formats sorted alphabetically and in lower case
	 */
	private String[] getFormats() {
		String[] formats = ImageIO.getWriterFormatNames();
		TreeSet<String> formatSet = new TreeSet<String>();
		for (String s : formats) {
			formatSet.add(s.toLowerCase());
		}
		return formatSet.toArray(new String[formatSet.size()]);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		switch (cmd) {
		case "Open":
			pick();
			setHistogram();
			break;
		case "Save":
			save();
			break;
		case "bmp": case "gif": case "jpeg": case "jpg": case "png":
		case "tif": case "tiff": case "wbmp":
			System.out.println("Save As ." + cmd);
			save(cmd);
			break;
		case "Original":
			reload();
			setHistogram();
			saveHistogram();
			break;
		case "Frame":
			frameImage();
			break;
		case "Grayscale":
			grayscale();
			setHistogram();
			saveHistogram();
			break;
		case "Grayscale - Luminance":
			grayscaleLuminance();
			setHistogram();
			saveHistogram();
			break;
		case "Equalize Histogram":
			histogramEqualize();
			break;
		case "Sobel Edge Detect":
			sobelEdgeDetect();
			break;
		case "Save Grayscale-Luminance Histogram":
			setHistogram();
			saveHistogram();
			break;
		case "Show Histogram":
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension screenSize = toolkit.getScreenSize();
			Dimension preferredSize = new Dimension(800, 600);
			setMinimumSize(preferredSize);
			System.out.println(preferredSize);
			label.setVisible(false);

			GraphViewer component = new GraphViewer(preferredSize);
			add(component);
			break;
		case "Show Image Name (on/off)":
			showLabel = !showLabel;
			setImage(image);
			break;
		}
	}
	
	private void grayscale() {
		BufferedImage greyFiltered = new BufferedImage(getImageWidth(), getImageHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = greyFiltered.getGraphics();
		g.drawImage(image, 0, 0, null); // better performance than the pixel by pixel conversion
		g.dispose();
		setImage(greyFiltered);
	}


	/**
	 * Initializes histogram to 256 cells. Iterates over the field image, parsing each rgb into
	 * red, green, and blue bands, calculates luminance, and counts that luminance value in the 
	 * histogram.
	 */
	private void setHistogram() {
		int w = getImageWidth();
		int h = getImageHeight();
		histogram = new int[256];
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				int rgb = image.getRGB(x, y);
				int r = rgb >> 16 & 0xFF;
				int g = rgb >> 8 & 0xFF;
				int b = rgb & 0xFF;
				int luma = (int) (r * .2989 + g * .587 + b * .114);
				histogram[luma]++;
			}
	}

	/**
	 * Converts the current image to byte gray, storing its histogram in the process.
	 * Creates local variables as needed, including a new BufferredImage TYPE_BYTE_GRAY. (produces a darker image)
	 * Contrast this with the efficient conversion used in grayscale(). 
	 * Compare the objects in the debugger by looking at the ColorModel fields and raster data. 
	 * Currently set to use TYPE_INT_RGB.
	 * Uses the same width and height as the field image,
	 * instantiates the field histogram, size 256,
	 * iterates over image getting each rgb value,
	 *     breaks down the r, g, and b, to create a luminance value based on ITU-R Recommendation BT.601
	 *     luma = (int) (r * .2989 + g * .587 + b * .114);
	 *     sets the corresponding pixel in the grayscale image to the luma value
	 *         be sure to cast luma to a byte when calling setRGB on a byte type image.
	 * invokes setImage with the completed grayscale image to display the modified image.
	 * 
	 */
	private void grayscaleLuminance() {
		// to do #4
		int w = getImageWidth();
		int h = getImageHeight();
		BufferedImage grayscaleLum = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);  // TYPE_INT_RGB);
//		BufferedImage grayscaleLum = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		histogram = new int[256];
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++) {
				int rgb = image.getRGB(x, y);
				int r = rgb >> 16 & 0xFF;
				int g = rgb >> 8 & 0xFF;
				int b = rgb & 0xFF;
				int luma = (int) (r * .2989 + g * .587 + b * .114);
				histogram[luma]++;
				int argb = 0xFF << 24 | luma << 16 | luma << 8 | luma;
				grayscaleLum.setRGB(x, y, (byte)luma);
//				grayscaleLum.setRGB(x, y, argb);  // (byte)luma);
			}
		setImage(grayscaleLum);
	}
	
	/**
	 * Writes the grayscale luminance histogram to a csv file. Iterates over the
	 * field histogram writing three lines of comma separated entries. The first
	 * line: the image name. The second line: 0, 1, 2, ..., 255. The third line
	 * contains the values stored at the corresponding indices of the field
	 * histogram.
	 * 
	 */
	private void saveHistogram() {
		// to do #5
		try {
			Writer fw = new FileWriter("histogram.csv");
			fw.write(getImageName() + "\n");
			for (int i = 0; i < 256; i++) {
				fw.write(Integer.toString(i) + ",");
			}
			fw.write("\n");
			for (int j : histogram) {
				fw.write(Integer.toString(j) + ",");
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * Draw the Histogram
	 * Add functionality to graph the histogram. Make the image 800 x 600. 
	 * Include labels and values. The maxValue should also be labeled. 
	 * Ex. http://people.uncw.edu/tompkinsj/331/labs/queen-maryHistogram.png  -sample graph with labels    
	 * There are graphing examples linked on our schedule. Use paper and pencil, make your own plan. 
	 * You will need to scale the values to fit your graph -see getScale above. 
	 */
	
	/**
	 * Histogram Equalize
	   create a list, lut, size L = 256
		determine the coefficient,
		coef = (L-1)/n, where n is the number of pixels in the image
		
		for each pixel with lumenance k in h, calculate a corresponding pixel level, sk.
		  Each sk can easily be determined by multiplying coeff times the cumulative sum of the histogram values. 
		  This algorithm implements the following equation
			lut[k] = sk = coeff Σ h[k] for k=0 to L-1
			
		Iterates over image, getting each pixel, calculates luma, sets that pixel based on the lut.
		Invokes label's repaint method.
	 */
	public void histogramEqualize() {
	    //call CalculateHist method to get the histogram
	    //calculate total number of pixel
	    int n = image.getWidth() * image.getHeight();
	    int L = 256;
	    long sum = 0;
	    int[] lut = new int[256];
	    float coef = (float) 255.0 / n;
	    for (int i = 0; i < L; i++) {
	        sum += histogram[i];
	        int sK = (int) (coef * sum);
	        lut[i] = sK;
	    }
	    for (int y = 0; y < image.getHeight(); y++) {
	        for (int x = 0; x < image.getWidth(); x++) {
	        	int rgb = image.getRGB(x, y);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = rgb & 0xFF;
				int luma = (int) (r * .2989 + g * .587 + b * .114);
				
	        Color equalizedColor = new Color(lut[luma], lut[luma], lut[luma]);
	        rgb = equalizedColor.getRGB();
	        image.setRGB(x, y, rgb);
	    }
	  }
	    repaint();
	}



	
	/**
	 * sobelEdgeDetect
		# calculate the length of the gradients, ix, iy by applying the
		Sobel Operators gx and gy to each pixel not on the border
		length = math.sqrt((ix * ix) + (iy * iy))

		# normalize the length of gradient to the range 0 to 255
		((255*4)^2*2)^.5 = 1443
		length = length / 1443 * 255
		((3*255*4)^2*2)^.5 = 4328  // if using r, g, and b bands
		length = length / 4328 * 255
		
		# convert the length to an integer
		length = int(length)

		# set the length in the edge image at x, y
		
		# invokes setImage with the edgeDetect image for display 
	 */

	public void sobelEdgeDetect() {
		histogramEqualize();
		
		int width = getImageWidth();
		int height = getImageHeight();
		BufferedImage edgeDetect = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[][] gx = {{-1,0,1},{-2,0,2},{-1,0,1}};
		int[][] gy = {{-1,-2,-1},{0,0,0},{1,2,1}};
		
		for(int x = 1; x < (width - 1); x++) {
			for(int y = 1; y < (height - 1); y++) {
				int ix = 0;
				int iy = 0;
				for(int row = 0; row < 3; row++) {
					for(int column = 0; column < 3; column++) {
						
						int blue = image.getRGB(x + column - 1, y + row-1) & 0xFF;
							ix += gx[row][column] * blue;
							iy += gy[row][column] * blue;
					}
				}
				double length = Math.sqrt((ix*ix) + (iy*iy));
				length = length / 1443 * 255;
				edgeDetect.setRGB(x, y, (int) length);
			}
		}
		setImage(edgeDetect);
	}
	
	public static void main(String[] args) {
//		new Picture();
//		new Picture("images/queen-mary.png");  // to do - comment out once constructor is overloaded with two arguments      
		new Picture("images/queen-mary.png", true);  // to do - test with true and then with false
	}

}
