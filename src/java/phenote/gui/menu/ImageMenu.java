package phenote.gui.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.swing.DynamicMenu;

import phenote.config.Config;

import phenote.dataadapter.ImageManager;
import phenote.gui.factories.ImageInfoFactory;
import phenote.gui.factories.ImageViewFactory;



/**
 * This menu will have image-specific actions.  Including:
 * open/close
 * things having to do with ROIs
 * 
 * This menu has been adapted to work with the new bbop framework.<p>
 *
 * @author Nicole Washington
 *
 */
public class ImageMenu extends DynamicMenu {

  protected static final GUIComponentFactory ImageViewerFactory = null;
	private static final Logger LOG = Logger.getLogger(ImageMenu.class);
	private ImageManager imageManager = ImageManager.inst();

	public ImageMenu() {
    super("Image");
    init();
  }

  @SuppressWarnings("serial")
  private void init() {

    // OPEN
    addOpenImageItem();
    
    // CLOSE
    addCloseImageItem();

    // SAVE
    //addSaveItem();

    add(new JSeparator());
    
    addGetInfoItem();
    
    
  }

  /** Open Menu item, for getting new image */
  private void addOpenImageItem() {
    final Action openImageAction = new AbstractAction("Open...") {
      public void actionPerformed(ActionEvent e) {
	  try {
	      String image = ImageManager.inst().chooseImage();
	      ImageManager.inst().loadAndShowImage(image);
	  } catch (IOException ioe) {
	      log().debug("Couldn't load image: " + ioe.getMessage());
	      return;
	  }
//    	  ImagePanelFactory factory = new ImagePanelFactory(ImageManager.inst().getImage(),ImageManager.inst().getImageName());
    	  ImageViewFactory factory = new ImageViewFactory(ImageManager.inst().getImage(),ImageManager.inst().getImageName());
    	  ComponentManager.getManager().showComponent(factory, null, null, false);
      }
      @Override
      public boolean isEnabled() {
        return Config.inst().hasDataAdapters();
      }  
    };
    
    openImageAction.putValue(Action.ACCELERATOR_KEY,getKeyStroke(KeyEvent.VK_O));
    add(new JMenuItem(openImageAction));

  }

  private void addCloseImageItem() {
    final Action closeImageAction = new AbstractAction("Close") {
      public void actionPerformed(ActionEvent e) {
      }
      @Override
      public boolean isEnabled() {
      	//TODO: should only be if there is an image open
      	return true;
      }  
    };
    
    //closeAction.putValue(Action.ACCELERATOR_KEY,getKeyStroke(KeyEvent.VK_O));
    add(new JMenuItem(closeImageAction));

  }
  
  private void addGetInfoItem() {
    final Action getImageInfoAction = new AbstractAction("Info") {
      public void actionPerformed(ActionEvent e) {
    	  ImageInfoFactory factory = new ImageInfoFactory();
    	  //GUIComponent c = factory.createComponent("ABC");
    	  ComponentManager.getManager().showComponent(factory, null, null, false);
      }
      @Override
      public boolean isEnabled() {
      	//TODO: should only be if there is an image open
      	return true;
      }  
    };
    add(new JMenuItem(getImageInfoAction));
    

  }
  
  private KeyStroke getKeyStroke(int keyCode) {
    int modifiers =  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    return KeyStroke.getKeyStroke(keyCode,modifiers);
  }

  // for testing
  public void clickLoad() {
    //loadMenuItem.doClick();
  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
}
