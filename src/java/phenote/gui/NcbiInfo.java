package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import org.obo.datamodel.Dbxref;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.ObsoletableObject;
import org.obo.datamodel.PropertyValue;
import org.obo.datamodel.Synonym;
import org.obo.util.TermUtil;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;
import phenote.gui.selection.UseTermListener;
import phenote.util.HtmlUtil;
import phenote.util.LinkCollection;
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

	// gui components
	private static JPanel ncbiPanel;
	private JScrollPane ncbiInfoScroll;
	private JEditorPane ncbiTextArea;
	private ActionListener actionListener;

	/**
	 * Create the panel
	 */

	public NcbiInfo() {
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
	

}
