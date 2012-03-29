package phenote.dataadapter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import org.bbop.framework.ComponentManager;

import phenote.dataadapter.Image;
import phenote.gui.factories.ImageViewFactory;
import phenote.util.FileUtil;

import org.apache.log4j.Logger;

public class ImageManager {

    private static ImageManager singleton;
    private JFileChooser fileChooser;
    private static final Logger LOG = Logger.getLogger(ImageManager.class);

    private ImageViewFactory factory = null;
    private Image image;
	
    public ImageManager() {
        // TODO Auto-generated constructor stub
    }
  
  public static ImageManager inst() {
    if (singleton==null) singleton = new ImageManager();
    	return singleton;  
    }
  
  public String chooseImage() {
      if (fileChooser == null)
          fileChooser = new JFileChooser();
      else
          fileChooser.show();
      int returnValue = fileChooser.showOpenDialog(null);
      if (returnValue == JFileChooser.APPROVE_OPTION) {
          String selected = fileChooser.getSelectedFile().getPath();
          return selected;
      }
      return null;
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

    public void loadAndShowImage(String imageFileOrUrl) 
        throws IOException {
        // Determine whether we're dealing with a path or an URL
        URL url = null;
        try {
            url = new URL(imageFileOrUrl);
        } catch (MalformedURLException e) {
            // Not a valid URL--next we'll see if it's a file path
        }
        if (url != null) { // It's an URL
            if (!FileUtil.urlExists(url)) {
                throw new IOException("Can't open URL " + url);
            }
            image = new Image(url);
        }
        else {
            // Otherwise, it's not an URL--check if it's a valid file
            File f = new File(imageFileOrUrl);
            if (!f.canRead())
                throw new IOException("Can't open file " + imageFileOrUrl);
            image = new Image(imageFileOrUrl);
        }

        factory = new ImageViewFactory(image, getImageName());
        ComponentManager.getManager().showComponent(factory, null, getImageName(), false);
    }
}
