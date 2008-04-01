package phenote.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.obo.datamodel.Instance;
import org.obo.datamodel.OBOSession;

import phenote.dataadapter.ncbi.OMIMAdapter;
import phenote.dataadapter.ncbi.PubMedAdapter;
import phenote.datamodel.CharFieldManager;
import phenote.gui.selection.IDSelectionEvent;
import phenote.gui.selection.IDSelectionListener;
import phenote.gui.selection.SelectionManager;

/**
  * This is a trial implementation of a simple window to display the contents
  * of a NCBI record retrieval.  All it is to do is display simple text.  This
  * might get fancy in the future.
  * <p>
  * @author Nicole Washington
  * 
  */
public class NcbiInfo extends AbstractGUIComponent {

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
  	super("NCBI:NCBI");
    this.selectionManager = selManager;
    this.selectionManager.addIDSelectionListener(new NCBIIDSelectionListener());
		init();
  }


	public void init() {

//		ncbiPanel = new JPanel(new BorderLayout(0, 0));
		this.setLayout(new BorderLayout(0,0));
		//		ncbiPanel.setMinimumSize(new Dimension(200, 200));
//		ncbiPanel.setPreferredSize(new Dimension(NCBI_INFO_DEFAULT_WIDTH,
//			 NCBI_INFO_DEFAULT_HEIGHT));

		//make the text area that the text will sit in
		ncbiTextArea = new JEditorPane();
		ncbiTextArea.setContentType("text/html");
		ncbiTextArea.setEditable(false);
		setNCBIInfoText("No references loaded", " ");

		// put it in a scrollpane
		ncbiInfoScroll = new JScrollPane(ncbiTextArea);
		ncbiInfoScroll.setBorder(null);

		// put the scrollpane into the whole bucket
		this.add(ncbiInfoScroll, BorderLayout.CENTER);
//		ncbiPanel.add(ncbiInfoScroll, BorderLayout.CENTER);
		
		
		// refresh
//		ncbiPanel.validate();
//		ncbiPanel.setVisible(true);
		
		this.setTitle("NCBI: Pubmed");
//		this.add(ncbiPanel);
		this.validate();
		this.repaint();
		
	}

//	public static JComponent getComponent() {
//		return ncbiPanel;
//	}
	
	public void setID(String id) {
		this.firePropertyChange("ID", null, id);
	}
	
	public void setNCBIInfoText(String text, String title) {
		ncbiTextArea.setText(text);
		if (title==null) {
			title="Pubmed: (error)";
		} else {
			title="Pubmed: "+title;
		}
//		ComponentManager.getManager().setLabel(this,title);

		this.validate();
		this.repaint();		
	}

	public void setNCBIInfofromInstance(Instance oboInstance) {
		String text=null;
		text="<html>";
		text+=oboInstance.getComment(); //comment is overloaded with the citation at the moment
		text+="<br><br><b>Abstract:</b> "+oboInstance.getDefinition()+" ("+oboInstance.getID()+")";
		text+="</html>";
		ncbiTextArea.setText(text);
		setComponentTitleFromOBOInstance(oboInstance);
		this.validate();
		this.repaint();		
	}

	public void setComponentTitleFromOBOInstance (Instance oboInstance) {
		String name="";
		if (oboInstance.getName().length()>35) {
			name = oboInstance.getName().substring(0, 35)+"...";
		} else {
				name = oboInstance.getName();
		}
		String title = "Article: "+ name;
		ComponentManager.getManager().setLabel(this,title);
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
  			setNCBIInfoText("Error:  "+id+" not found in NCBI database.", null);
			}
    }
  }
}
