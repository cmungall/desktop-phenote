package phenote.gui.factories;

import java.awt.image.BufferedImage;

import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUIComponentFactory.FactoryCategory;

import phenote.dataadapter.Image;
import phenote.dataadapter.ImageManager;
import phenote.gui.ImageInfo;
import phenote.gui.ImagePanel;

public class ImageInfoFactory extends AbstractComponentFactory<ImageInfo> {
	
	private Image image;
	
	public ImageInfoFactory() {}
	
	public ImageInfoFactory(Image i) {
    super();
    this.image = i;
  }
	  
	public boolean isSingleton() {
		return true;
	}
	
	public void cleanup() {
		return;
	};

	@Override
	public ImageInfo doCreateComponent(String id) {
		return new ImageInfo(image,id);
	}

	public String getName() {
		return this.image.getTitle();
	}

	public FactoryCategory getCategory() {
		return null;
	}

	public String getID() {
		// TODO different id?
		return "imageinfo_" + image.getId();
	}
}
