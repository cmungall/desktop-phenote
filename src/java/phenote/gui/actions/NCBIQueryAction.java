package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import org.obo.datamodel.Instance;
import org.obo.datamodel.OBOSession;

import phenote.dataadapter.ncbi.OMIMAdapter;
import phenote.datamodel.CharFieldManager;

//This is the first pass at creating action items for NCBI retrieveal of
//information using their RESTful interface
//this should probably use a separate search box/interface


/**
 * as you get into bigger examples with lots and lots of lookups to 
 * external databases, you should probably create a wrapper for OBOSession 
 * where the getObject(id) method automatically creates these instances. 
 * That way you can encapsulate the whole fetching/caching thing inside 
 * the OBOSession.
 * 
 * @author Nicole Washington
 *
 */
public class NCBIQueryAction extends AbstractAction {
	private OMIMAdapter omimAdapter = new OMIMAdapter();
	public NCBIQueryAction() {
		super("Fetch from NCBI");
		putValue(SHORT_DESCRIPTION, "Retrieve matching record from NCBI"); // tooltip text
		putValue(NAME, "Fetch from NCBI");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));		
	}
	
	@Override
	public boolean isEnabled() {
		return true;
		//this should really check to make sure there is a URL definition for NCBI,
		//and/or if there is an internet connection
	}

	public void actionPerformed(ActionEvent e) {
		//grab the text from the field that is selected or whatever
		String id="601653";
		String fullID="OMIM:"+id;
		OBOSession session = CharFieldManager.inst().getOboSession();
		Instance oboInstance;
		if  ((oboInstance=(Instance)session.getObject(fullID))==null) { 		//check to see if it is already in the obosession
			//fetch the info from ncbi
			String temp = omimAdapter.getOMIMbyID(id);
			//create an obo instance.
                        // doesnt compile - need to check in latest obo jar i think
                        // also would be great to set your eclipse to use 2 space tabbing - this is going off the end of my page........
                        //			oboInstance = (Instance)session.getObjectFactory().createObject(fullID, AnnotationOntology.PUBLICATION(), false);
			oboInstance.setComment(temp);
			//add the oboinstance to the current session
			session.addObject(oboInstance);
			System.out.println("added ID="+oboInstance.getID()+" to oboSession.");
		} else {
			System.out.println("retrieved ID="+oboInstance.getID()+" from oboSession.");
		}
		//fire ncbiwindow update
		//display the results in the window
		//**********JOHN, this is where i want to fire an update event!
	}
	// return action;
}
