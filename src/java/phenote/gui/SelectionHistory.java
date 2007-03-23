package phenote.gui;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.Iterator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.BrowserLauncherRunner;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.OntologyManager;
import phenote.datamodel.TermNotFoundException;
import phenote.edit.EditManager;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.UpdateTransaction;
import phenote.edit.TransactionI;
import phenote.util.HtmlUtil;
import phenote.gui.TermInfo;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;

public class SelectionHistory {

  //private JEditorPane textArea;
  private JTextComponent textArea;
  private static final boolean DO_HTML = HtmlUtil.DO_HTML;
  private TermHyperlinkListener termHyperlinkListener;
  // current obo class being navigated
  private OBOClass currentOboClass;
  private TermInfo termInfo;

  public SelectionHistory() { //TermPanel termPanel) {
	    EditManager.inst().addCharChangeListener(new HistorySelectionListener());
	  }
  
//  public SelectionHistory() { //TermPanel termPanel) {
//    SelectionManager.inst().addTermSelectionListener(new HistorySelectionListener());
//  }
  

  public JComponent getComponent() {
    JPanel termHistoryPanel = new JPanel(new BorderLayout(0,0)); // hgap,vgap
    termHistoryPanel.setPreferredSize(new Dimension(200,100));
    //termHistoryPanel.setMinimumSize(new Dimension(100,100));
    if (DO_HTML) {
      JEditorPane editorPane = new JEditorPane(); 
      editorPane.setContentType("text/html"); // sets up HTMLEditorKit
      termHyperlinkListener = new TermHyperlinkListener();
      editorPane.addHyperlinkListener(termHyperlinkListener);
      textArea = editorPane;
    }
    else { // pase - delete?
      JTextArea jTextArea = new JTextArea(17,50);
      jTextArea.setLineWrap(true);
      jTextArea.setWrapStyleWord(true);
      textArea = jTextArea;
    }
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(400,300));
    scrollPane.setMaximumSize(new Dimension(400,300));
    // border - make JPanel for it (there is a disclaimer about non JPanel)
    //scrollPane.setBorder(BorderFactory.createTitledBorder("Term Info"));
    termHistoryPanel.setBorder(BorderFactory.createTitledBorder("Term History"));
    termHistoryPanel.add(scrollPane,BorderLayout.CENTER);

    return termHistoryPanel;
  }

 
  // for TestPhenote
  String getText() { return textArea.getText(); }
  
  void setOboClass(OBOClass term) {
	    // actually i think null is valid for non-required fields - undo & blanking field
	    // right even if required field should still be able to undo back to init/null
//	     if (term == null) {
//	       log().error("Attempt to set term to null");
//	       return; // debug stack trace?
//	     }
	    currentOboClass = term;
	    String val = term == null ? "" : term.getName();
//	    setText(val,false); // no completion
	  }

  
  /* creating a list of links from the transaction history */
  private void setHistoryFromList(List<TransactionI> transList) {
	  String html = "";
	  //will move this into HtmlUtil shortly...
	  for (int i=transList.size(); i>0; i--)
	  {
		  OBOClass term = transList.get(i-1).getNewTerm();
		  if (term!=null)
			  html = html + "<br>"+HtmlUtil.termLink(term);
	  }
	  textArea.setText(html);
	  textArea.setCaretPosition(0);
  }

  /** Listen for selection from phenote (mouse over completion list) */
  //need to make sure this is also recording the "use term" events
  
  private SelectionManager getSelectionManager() {
	    return SelectionManager.inst();
	  }
  
  private EditManager getEditManager() {
	return EditManager.inst();
  }

private class HistorySelectionListener implements CharChangeListener {
  public void charChanged(CharChangeEvent e) {
	  if (e.isUpdate()) {
		  EditManager em = getEditManager();
		  List<TransactionI> transList = em.getTransactionList();
		  String html="";
		  setHistoryFromList(transList);
	  }
  }	
}
  
//  private class HistorySelectionListener implements TermSelectionListener {
//	  public boolean termSelected(TermSelectionEvent e) {
////		  System.out.println("link = "+e.isHyperlinkEvent());
//	      if (e.isMouseOverEvent() ) return false;
//	      setHistoryFromList(e.getOboClass());
//           return true;       
//	  }
//  }
  private UseTermListener useTermListener;
  private UseTermListener getUseTermListener() {
    if (useTermListener == null) useTermListener = new HistoryUseTermListener();
    return useTermListener;
  }
  /** Listens for UseTermEvents from term info,if editModel is true then edits model*/
  private class HistoryUseTermListener implements UseTermListener {
    public void useTerm(UseTermEvent e) {
      currentOboClass = e.getTerm();	
//      System.out.println("term = "+currentOboClass.getName());   
//      if (editModelEnabled()) editModel();
    }
  }


   
  //listens for clicks on the links, and then brings up the term in termInfo
  private class TermHyperlinkListener implements HyperlinkListener {

	    public void hyperlinkUpdate(HyperlinkEvent e) {
	      if (!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED))
	        return;

	      // internal link to term...
	      if (HtmlUtil.isPhenoteLink(e)) {
	        bringUpTermInTermInfo(e);
	        return;
	      }
	    }
	    private void bringUpTermInTermInfo(HyperlinkEvent e) {
	        // or do through obo session?
	        String id = HtmlUtil.getIdFromHyperlink(e);
//	    	System.out.println("link to *"+id+"* selected");
	        if (id == null) return;
	        try {
	          OBOClass term = OntologyManager.inst().getOboClass(id); // ex
	          currentOboClass = term;
	          // send out term selection (non mouse over) for DAG view
	          Object src = SelectionHistory.this;
//	          getSelectionManager().selectHistoryTerm(src, term, getUseTermListener());
	          getSelectionManager().selectMouseOverTerm(src,term,getUseTermListener());
	        }
	        catch (TermNotFoundException ex) { return; }
	      }
  }
}
