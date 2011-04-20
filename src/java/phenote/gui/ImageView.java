package phenote.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentFactory.FactoryCategory;

import phenote.dataadapter.Image;
import phenote.util.FileUtil;

/*
 * This class is the component that will hold the image panel and toolbar
 */
public class ImageView extends AbstractGUIComponent {

	private static final Logger LOG = Logger.getLogger(ImageView.class);

	private JToolBar imageToolbar;
	private JPanel imagePanel; //will be class ImagePanel
	private Image image;
	
	public ImageView(Image i, String id) {
		super(id);
		image = i;
		init();
		//add this to the ImageViewer frame
	}

	@Override
  public void init() {
    super.init();
    imageToolbar = createToolbar();
    imagePanel = new ImagePanel(image);
    this.setLayout(new BorderLayout());
    this.add(imagePanel, BorderLayout.CENTER);
    this.add(imageToolbar, BorderLayout.NORTH);
    this.setVisible(true);
    this.repaint();
  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

  private JToolBar createToolbar() {
  	JToolBar t = new JToolBar("Default Toolbar");
  	JButton b = new JButton();
    try {
      b = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
          	// TODO: add action
          }
        });
      b.setToolTipText("Add");
      t.add(b);
      
    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    
    t.setFloatable(false);
  	
  	return t;
  }
  
}
