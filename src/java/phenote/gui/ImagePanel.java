package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Vector;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import phenote.dataadapter.Image;

// Each panel will hold a single image, and perhaps it will be the container
// to hold other things like ROIs, etc.  not sure yet.
// This will allow there to be multiple images open in the single image viewer
// component

public class ImagePanel extends JPanel implements MouseListener {

  private static float borderWidth = 1.0f;
  private static float selectedBorderWidth = 3.0f;
	private static final Logger LOG = Logger.getLogger(ImagePanel.class);
	final static BasicStroke selectedStroke = new BasicStroke(selectedBorderWidth);
	final static BasicStroke stroke = new BasicStroke(borderWidth);

	private Image image;
	
  private int currentMap;
  private Color fill;
  private float alpha;
  
	
	public ImagePanel(Image i) {
		image = i;
		createPanel();
		//add this to the ImageViewer frame
	}
	
  public void createPanel() {
//    super.init();
    currentMap = -1;
    addMouseListener(this);
    alpha = 0.1f;
    fill = Color.BLUE;
    this.setBackground(Color.black);
    this.setLayout(new BorderLayout());
    this.setVisible(true);
    this.repaint();
  }
  
  
  public ImagePanel getImagePanel() {
  	return this;
  }
  
  public Image getImage() {
  	return image;
  }
  
	@Override
  public void paintComponent(Graphics g) {
      //g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
//		Vector<Polygon> polys = image.getPolys();
		Vector<Shape> regions = image.getROIs();
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(Color.white);
		g2.drawImage(this.image.getImage(), 0, 0, this);
//		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, this.alpha));
		g2.setStroke(stroke);
		//First, draw all the ROIs
		for (int i=0; i<regions.size(); i++) {
			g2.setColor(Color.yellow);
			g2.draw(regions.get(i));      	
		}
			
		//Highlight the ROI that is selected
		if (this.currentMap > -1) {
//			g2.setColor(Color.RED);
			g2.setStroke(selectedStroke);
			g2.draw(regions.get(this.currentMap));
		}
	}
	
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
	
	public void setFill(Color color){
		this.fill = color;
	}
	
	public void setFill(Color color, float alpha){
		this.fill = color;
		this.alpha = alpha;
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	// unused mouse
	public void mouseClicked(MouseEvent e) {
		Vector<Shape> regions = image.getROIs();
		for(int i=0; i<regions.size();i++){
			if(regions.get(i).contains(e.getX(), e.getY())){
				this.firePropertyChange("CurrentMap", currentMap, i);
				currentMap = i;
				repaint();
				break; // no need to keep checking
			}
		}	
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {	
	}

	public void mousePressed(MouseEvent e) {}
  
}
