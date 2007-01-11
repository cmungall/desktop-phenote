package phenote.gui;

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
import phenote.edit.UpdateTransaction;
import phenote.util.HtmlUtil;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;

public class TermInfo {

  //private JEditorPane textArea;
  private JTextComponent textArea;
  private static final boolean DO_HTML = HtmlUtil.DO_HTML;
  private TermHyperlinkListener termHyperlinkListener;
  // current obo class being navigated
  private OBOClass currentOboClass;
  private UseTermListener useTermListener;

  
  public TermInfo() { //TermPanel termPanel) {
    SelectionManager.inst().addTermSelectionListener(new InfoTermSelectionListener());
  }

  public JComponent getComponent() {
    JPanel termInfoPanel = new JPanel(new BorderLayout(0,0)); // hgap,vgap
    termInfoPanel.setPreferredSize(new Dimension(700,100));
    termInfoPanel.setMinimumSize(new Dimension(380,100));
    //termInfoPanel.setMaximumSize(new Dimension(380,400));
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
    termInfoPanel.setBorder(BorderFactory.createTitledBorder("Term Info"));
    termInfoPanel.add(scrollPane,BorderLayout.CENTER);

    JButton useTermButton = new JButton("Use Term");
    useTermButton.addActionListener(new UseTermActionListener());
    termInfoPanel.add(useTermButton,BorderLayout.SOUTH);

    return termInfoPanel;
  }

  /** Fires use term event to use term listener with currently browsed term when
      useTermButton is pressed */
  private class UseTermActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //commitTerm();
      // relation comp list sets to null
      if (useTermListener == null) return;
      if (currentOboClass == null) return; // shouldnt happen
      useTermListener.useTerm(new UseTermEvent(TermInfo.this,currentOboClass));
    }
  }

  // for TestPhenote
  String getText() { return textArea.getText(); }

  private void setTextFromOboClass(OBOClass oboClass) {
    currentOboClass = oboClass;

    String html = HtmlUtil.termInfo(oboClass);

    textArea.setText(html);
    // scroll to top (by default does bottom)
    textArea.setCaretPosition(0);
  }
  
  
  /** Listen for selection from phenote (mouse over completion list) */
  private class InfoTermSelectionListener implements TermSelectionListener {
    public boolean termSelected(TermSelectionEvent e) {
      if (!e.isMouseOverEvent()) return false;
      setTextFromOboClass(e.getOboClass());
      // This sets who now listens to use term button clicks (only 1 listener)
      setUseTermListener(e.getUseTermListener());
      return true;
    } 
  }

  private void setUseTermListener(UseTermListener utl) {
    useTermListener = utl;
  }

  
  /** for testing */
  void simulateHyperlinkEvent(HyperlinkEvent e) {
    termHyperlinkListener.hyperlinkUpdate(e);
  }


  /** inner class TermHyperlink Listener, listens for clicks on term & external
      hyper links and brings up the term or brings up the external web page */
  private class TermHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED))
        return;

      URL url = e.getURL();
      //System.out.println("got url "+url+" desc "+e.getDescription());

      // internal link to term...
      if (HtmlUtil.isPhenoteLink(e)) {
        bringUpTermInTermInfo(e);
        return;
      }

      if (url == null) { // relative urls are null
        System.out.println("invalid url "+url);
        return;
      }

      bringUpInBrowser(url);

    }

    private void bringUpInBrowser(URL url) {
      if (url == null) return;
      try {
        BrowserLauncher bl = new BrowserLauncher(null); // no logger
        BrowserLauncherRunner br = new BrowserLauncherRunner(bl,url.toString(),null);
        new Thread(br).start();
      }
      catch (BrowserLaunchingInitializingException be) {
        System.out.println("cant launch browser "+be);
      }
      catch (UnsupportedOperatingSystemException ue) {
        System.out.println("cant launch browser "+ue);
      }
    }


    private void bringUpTermInTermInfo(HyperlinkEvent e) {
      // or do through obo session?
      String id = HtmlUtil.getIdFromHyperlink(e);
      if (id == null) return;
      try {
        OBOClass term = OntologyManager.inst().getOboClass(id); // ex
        setTextFromOboClass(term);
        // send out term selection (non mouse over) for DAG view
        SelectionManager.inst().selectTerm(TermInfo.this, term);
      }
      catch (TermNotFoundException ex) { return; }
    }
  }
}

//   /** put present term into current character - not used yet...
//    this is now policy of the UseTermListener(AutoComboBox) TermInfo just sends out
//    UseTermEvent and UTL/ACB decides what to do with it */
//   private void commitTerm() {
//     CharacterI ch = SelectionManager.inst().getSelectedCharacter();
//     if (ch == null) { // can this happen?
//       System.out.println("ERROR: no character selected to update");
//       return;
//     }
//     if (currentOboClass == null) { // can this happen?
//       System.out.println("ERROR: no term in term info to add to character");
//       return;
//     }

//     CharFieldEnum cfe = OntologyManager.inst().getCharFieldEnumForOboClass(currentOboClass);
    
//     UpdateTransaction ut = new UpdateTransaction(ch,cfe,currentOboClass);
//     EditManager.inst().updateModel(this,ut);
//     //previousOboClass = currentOboClass;
//   }
     // JEditorPane allows for html formatting - bold... but doesnt do word wrap - bummer!
