package phenote.gui.factories;

import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;
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
    private ImageView imageView;

    public ImageViewFactory() {}
	
    public ImageViewFactory(Image i) {
        super();
        this.imageID = i.getId();
        this.image = i;
        //    log().debug("Creating ImageViewFactory with no filename; imageID = " + imageID);
    }

    public ImageViewFactory(Image i, String filename) {
        super();
        this.imageID = filename;
        this.image = i;
        //    log().debug("Created ImageViewFactory with filename; imageID = " + imageID);
    }

	
    private BufferedImage getImage() {
        return ImageManager.inst().getImage().getImage();
    }

    public void setImage(Image i, String imageName) {
        this.image = i;
        this.imageID = imageName;
        this.imageView.setImage(i);
    }
  
    public boolean isSingleton() {
        //		return true;
        return false;
    }
	
    public void cleanup() {
        System.out.println("ImageViewFactory.cleanup");
    };

    @Override
    // Note: id not used
    public ImageView doCreateComponent(String id) {
        //            System.out.println("ImageViewFactory.doCreateComponent: id = " + id + ", imageID = " + imageID); // DEL
        imageView = new ImageView(image, "IMAGE_VIEW");
        return imageView;
    }

    public String getName() {
        return this.imageID;
    }

    public FactoryCategory getCategory() {
        return null;
    }

    public String getID() {
        //            return "IMAGE_VIEW";
        return imageID;
    }

    private static Logger log() {
        return Logger.getLogger(ImageViewFactory.class);
    }

}
