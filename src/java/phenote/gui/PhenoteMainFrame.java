package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.bbop.framework.DockPanelFactory;
import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIManager;
import org.bbop.framework.MainFrame;
import org.bbop.swing.EnhancedMenuBar;

public class PhenoteMainFrame extends MainFrame {
	private EnhancedMenuBar menubar;

	protected JPanel mainPanel;
	
	protected boolean lockDoc;
	
	protected JToolBar mainToolBar;

  /** config size? 3 arrays of numbers to try for frame size - compare with
      screen size, first # in array is width, 2nd is height, the 3rd one doesnt
      seem to be used??? */
	private int[][] dimensionInfo = { { 620, 460, 160 }, { 760, 560, 300 },
			{ 960, 700, 400 }, { 1220, 800, 400 } };

	public PhenoteMainFrame(String title) {
		super(title);

		try {

			menubar = new EnhancedMenuBar();
			mainToolBar = new StandardToolbar().getComponent();
			lockDoc=false;  //by default, the doc will not be locked
			setJMenuBar(menubar);
			setToolBar(mainToolBar);
			setLockDoc(false);
			addMenus();
			createListeners();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setVisible(boolean arg0) {
		if (true)
			initGUI();
		super.setVisible(arg0);
	}

	protected void setupFrame() {
		GUIComponent c = DockPanelFactory.getInstance().createComponent(
				DockPanelFactory.getInstance().getDefaultID());
		c.init();
		mainPanel.add((Component) c);
	}

	public void addMenu(JMenu menu) {
		menubar.add(menu);
	}

	public void setHelpMenu(JMenu menu) {
		menubar.add(menu);
	}

	protected void addMenus() {

	}
	
	//right now this is one toolbar.  but could conceiveably be a panel of toolbars
	public void setToolBar(JToolBar toolbar) {
		mainToolBar = toolbar;
	}
	
	public void setLockDoc(boolean lock) {
		lockDoc = lock;
	}
	
	public boolean getLockDoc() {
		return lockDoc;
	}

	protected void createListeners() {
	}

  /** Tries out dimensionInfo array trying to find biggest dimensions for 
      frame/window that will fit on screen - couldnt this also just do a 
      percentage of screen size - also should have configurable override */
	protected void initGUI() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		//		mainPanel.setLayout(new GridLayout(1, 1));
		setContentPane(mainPanel);
		getContentPane().add(mainToolBar, BorderLayout.NORTH);
		mainToolBar.setBackground(GUIManager.getManager().getFrame().getBackground());
		//FIXME: toggle this
		setupFrame();
		int i;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		for (i = 1; i < dimensionInfo.length; i++) {
			if (screenSize.width < dimensionInfo[i][0]
					|| screenSize.height < dimensionInfo[i][1])
				break;

		}
		setSize(dimensionInfo[i - 1][0], dimensionInfo[i - 1][1]);
	}
}
