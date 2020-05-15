package imageProcessing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.util.Scanner;

import javax.swing.JComponent;


public class GraphViewer extends JComponent {

	private static final long serialVersionUID = -7817205094537835673L;
//	private Grid grid;
//	private GradesHistogram hist;
	private int[] count;
	private int gradeRange;
	private int maxCount;
	int top;
	int bottom;

	public GraphViewer(Dimension preferredSize) {
		super();
		setPreferredSize(preferredSize);
//		grid = new Grid(0, 0, preferredSize.width, preferredSize.height);
		gradeRange = 60;
		count = retrieveCount();
		count = normalizeCount();
//		hist = new GradesHistogram(50, 50, count);
		
	}
	
	private int[] retrieveCount() {
//		Scanner sc;
//		
//		int[] count = new int[256];
//		try {
//			sc = new Scanner(new File("histogram.csv"));
//			String line = sc.nextLine();;
//			for(int i = 0; i < 2; i++) {
//				line = sc.nextLine();
//			}
//			String[] lumArray = line.split(",?\\s");
//			int j = 0;
//			for (String lum : lumArray) {
//				count[j] = Integer.parseInt(lum);
//				j++;
//			}
//			sc.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return count;
		Scanner sc;
		int[] count = new int[256];
		try {
			sc = new Scanner(new File("histogram.csv"));
			
			String line = sc.nextLine();
			for (int i = 0; i < 2; i++) {
				line = sc.nextLine();
			}
			for (int j = 0; j < line.split(",").length; j++) {
				count[j] = Integer.parseInt(line.split(",")[j]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return count;
	}
	
	public int[] normalizeCount() {
		int[] oldCount = retrieveCount();
		int[] normCount = new int[256];
		for (int i = 0; i < oldCount.length; i++) {
			normCount[i] = (int) (oldCount[i] * getScale(200));
			System.out.println(normCount[i]);
		}
		return normCount;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
//		grid.display(g2);
//		hist.draw(g2);
		
		int left = 170;
		int right = left + 510;
		int ordinateValueX = left - 20;
		top = 150;
		bottom = 350;
		int abscissaValueY = bottom + 25;
		int gap = 2;
		
//		g2.drawString("Student Grades", 250, top - 15);
		g2.drawString("Pixel Intensity", 400, bottom + 50);
		g2.drawString("Pixel Count", ordinateValueX - 100, bottom - 90);
		g2.drawString(Integer.toString(maxCount), ordinateValueX, top);
		g2.drawLine(left, bottom, right, bottom);
		g2.drawLine(left, bottom, left, top);
//      Arrays.sort(grades);
		g2.drawString("0", ordinateValueX + 10, abscissaValueY);
		for (int i = 1; i < count.length; i++) {
			g2.drawLine(left + gap * (i - 1), bottom - count[i - 1], left + gap * i, bottom - count[i]);
			if (i % 100 == 0 || i == 255) {
				g2.drawString(String.format("%d", i), left + gap * i, abscissaValueY);
			}
		}

	}

//	public void setRegradeMin() {
//		count = retrieveCount();
//		hist = new GradesHistogram(50, 50, count);
//		repaint();
//	}

//	public void toggleGrid() {
//		grid.toggleGrid();	
//		repaint();
//	}

//	public void resetRegradeMin() {
//		gradeRange = 55;
//		setRegradeMin();		
//	}

	private double getScale(double graphHeight) {
		  maxCount = count[0];
		  for (int n : count) {
		      if (n > maxCount)
		    	  maxCount = n;
		  }
		  double scale = graphHeight / maxCount;
		  return scale;
		}
}
