package phenote.gui.factories;

import java.awt.image.BufferedImage;

import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUIComponentFactory.FactoryCategory;

import phenote.dataadapter.Image;
import phenote.dataadapter.ImageManager;
import phenote.gui.ImagePanel;
import phenote.gui.ImageView;

public class ImageViewFactory extends AbstractComponentFactory<ImageView> {
	
	private String imageID;
	private Image image;

	
	public ImageViewFactory() {}
	
	public ImageViewFactory(Image i) {
    super();
    this.imageID = i.getId();
    this.image = i;
  }

	public ImageViewFactory(Image i, String filename) {
    super();
    this.imageID = filename;
    this.image = i;
  }

	
  private BufferedImage getImage() {
    return ImageManager.inst().getImage().getImage();
  }
  
	public boolean isSingleton() {
		return true;
	}
	
	public void cleanup() {
		//TODO: need to uninstall the Image menu when the component is closed
		return;
	};

	@Override
	public ImageView doCreateComponent(String id) {
		return new ImageView(image,imageID);
	}

	public String getName() {
		return this.imageID;
	}

	public FactoryCategory getCategory() {
		return null;
	}

	public String getID() {
		return this.imageID;
	}
}
