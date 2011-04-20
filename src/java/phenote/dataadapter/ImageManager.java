/**
 * This class will manage any number of images to be used during annotation
 */
package phenote.dataadapter;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import phenote.dataadapter.Image;

import org.apache.log4j.Logger;

import phenote.util.FileUtil;


/**
 * @author Nicole
 * This should probably change to be a subclass of MediaTracker
 *
 */
public class ImageManager {

  private static ImageManager singleton;
  private JFileChooser fileChooser;
	private static final Logger LOG = Logger.getLogger(ImageManager.class);

	private Image image;
	
	public ImageManager() {
		// TODO Auto-generated constructor stub
	}
  
  public static ImageManager inst() {
    if (singleton==null) singleton = new ImageManager();
    	return singleton;  
    }

  
  public void loadImage() {
  	//TODO: File chooser or URL entry
  	fileChooser = new JFileChooser();
  	int returnValue = fileChooser.showOpenDialog(null);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
    	image = new Image(fileChooser.getSelectedFile().getPath());
    }
  }
  
  public Image getImage() {
  	return image;
  }

  public String getImageName() {
  	return image.getTitle();
  }
  
  private static Logger log() {
    return Logger.getLogger(ImageManager.class);
  }
  
}
