package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.Border;

import org.obo.annotation.datamodel.AnnotationOntology;
import org.obo.datamodel.Instance;
import org.obo.datamodel.OBOSession;
import org.obo.util.TermUtil;

import phenote.dataadapter.ncbi.OMIMAdapter;
import phenote.dataadapter.ncbi.PubMedAdapter;
import phenote.datamodel.CharFieldManager;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.IDSelectionEvent;
import phenote.gui.selection.IDSelectionListener;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.BrowserLauncherRunner;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
  * This is a trial implementation of a simple window to display the contents
  * of a NCBI record retrieval.  All it is to do is display simple text.  This
  * might get fancy in the future.
  * <p>
  * @author Nicole Washington
  * 
  */
public class NcbiInfo extends JPanel {

	// constants
	private static final int NCBI_INFO_DEFAULT_WIDTH = 350;
	private static final int NCBI_INFO_DEFAULT_HEIGHT = 400;
	private static final int BUTTON_HEIGHT = 30;
	private static Border contentBorder = BorderFactory.createEmptyBorder(6, 8,
			6, 8);

	// content variables
	private SelectionManager selectionManager;
	private OMIMAdapter omimAdapter = new OMIMAdapter();
	private PubMedAdapter pubmedAdapter = new PubMedAdapter();


	// gui components
	private static JPanel ncbiPanel;
	private JScrollPane ncbiInfoScroll;
	private JEditorPane ncbiTextArea;
	private ActionListener actionListener;

	/**
	 * Create the panel
	 */

	public NcbiInfo() {
		this(SelectionManager.inst());
	}
	
  public NcbiInfo(SelectionManager selManager) {
    this.selectionManager = selManager;
    this.selectionManager.addIDSelectionListener(new NCBIIDSelectionListener());
		init();
  }


	private void init() {

		ncbiPanel = new JPanel(new BorderLayout(0, 0));
		ncbiPanel.setMinimumSize(new Dimension(200, 200));
		ncbiPanel.setPreferredSize(new Dimension(NCBI_INFO_DEFAULT_WIDTH,
			 NCBI_INFO_DEFAULT_HEIGHT));

		//make the text area that the text will sit in
		ncbiTextArea = new JEditorPane();
		ncbiTextArea.setContentType("text/html");
		ncbiTextArea.setEditable(false);
		setNCBIInfoText("No references loaded");

		// put it in a scrollpane
		ncbiInfoScroll = new JScrollPane(ncbiTextArea);
		ncbiInfoScroll.setBorder(null);

		// put the scrollpane into the whole bucket
		ncbiPanel.add(ncbiInfoScroll, BorderLayout.CENTER);
		
		// refresh
		ncbiPanel.validate();
		ncbiPanel.setVisible(true);

	}

	public static JComponent getComponent() {
		return ncbiPanel;
	}
	
	public void setID(String id) {
		this.firePropertyChange("ID", null, id);
	}
	
	public void setNCBIInfoText(String text) {
		ncbiTextArea.setText(text);
		ncbiPanel.validate();
		ncbiPanel.repaint();		
	}

	public void setNCBIInfofromInstance(Instance oboInstance) {
		String text=null;
		text="<html>";
		text+=oboInstance.getComment();
		text+="<br><br><b>Abstract:</b>"+oboInstance.getDefinition()+" ("+oboInstance.getID()+")";
		text+="</html>";
		ncbiTextArea.setText(text);
		ncbiPanel.validate();
		ncbiPanel.repaint();		
	}

	
  private class NCBIIDSelectionListener implements IDSelectionListener {
    public void IDSelected(IDSelectionEvent e) {
    	String id=e.getID();
    	String temp=null;
  		OBOSession session = CharFieldManager.inst().getOboSession();
  		Instance oboInstance=null;
  		Instance tempInstance=null;
  		if  ((oboInstance=(Instance)session.getObject(id))==null) { 
  			//current id not in the session, create it
  			if (e.getType().equalsIgnoreCase("pubmed")) {
//				temp = pubmedAdapter.query(id, "pubmed");
  				tempInstance = pubmedAdapter.query(id);
  			} else if (e.getType().equalsIgnoreCase("omim")) {
//  				temp = omimAdapter.query(id, "omim");
  				tempInstance = omimAdapter.query(id);
  			}
  			if (tempInstance!=null) {
  				//if the id isn't found at ncbi, don't add it to the session
  				oboInstance = tempInstance;
  				session.addObject(tempInstance);
  				System.out.println("added ID="+oboInstance.getID()+" to oboSession.");
  			}
  		}
			if (oboInstance!=null) {
				setNCBIInfofromInstance(oboInstance);
			} else {
				System.out.println("couldn't retrieve or find "+id);
  			setNCBIInfoText("Error:  "+id+" not found in NCBI database.");
			}
    }
  }
}
