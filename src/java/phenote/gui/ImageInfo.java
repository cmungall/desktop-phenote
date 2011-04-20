package phenote.gui;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;

import phenote.dataadapter.Image;
import phenote.gui.selection.SelectionManager;

//This gui component will display the associated information for an image
//To start it will simply display the filesystem info:
//* filename (path)
//* date + time
//* size
//will eventually display other things that comes in the annotation file
//like experimental details (cy3/cy5), plate/well info, etc.


public class ImageInfo extends AbstractGUIComponent {

	private static final Logger LOG = Logger.getLogger(ImagePanel.class);

	private static ImageInfo singleton;
	private Image image;
	private SelectionManager selectionManager;
	private JPanel ip;
		
	public ImageInfo(Image i) {
		super(i.getId());
		image = i;
		init();
		addImageInfo();
	}
	
	public ImageInfo(Image i, String id) {
		super(id);
		image = i;
		init();
		addImageInfo();
	}
	
  @Override
  public void init() {
    super.init();
    this.setLayout(new BorderLayout());
    this.add(this.createPanel(), BorderLayout.CENTER);
    this.setVisible(true);
    this.repaint();
  }
  
  private JComponent createPanel() {
    final JPanel ip = new JPanel();
    final JScrollPane scrollPane = new JScrollPane(ip);
    return scrollPane;
  }
  
  private void addImageInfo() {
  	JLabel name = new JLabel("Name:" + image.getTitle());
  	//JLabel location = new JLabel("Location:" + "TBD");
  	//JLabel timestamp = new JLabel("Timestamp:" + image.getFileTimestamp());

  	this.add(name);
  	//this.add(location);
  	//this.add(timestamp);

  	this.repaint();
  }
	
}
