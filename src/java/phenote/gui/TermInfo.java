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
import phenote.edit.EditManager;
import phenote.edit.UpdateTransaction;
import phenote.util.HtmlUtil;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;

public class TermInfo {

  //private JEditorPane textArea;
  private JTextComponent textArea;
  private static final boolean DO_HTML = HtmlUtil.DO_HTML;
  private TermHyperlinkListener termHyperlinkListener;
  // current obo class being navigated
  private OBOClass currentOboClass;
  private OBOClass previousOboClass=null; // for undo - not implemented yet

  
  public TermInfo(TermPanel termPanel) {
    SelectionManager.inst().addTermSelectionListener(new InfoTermSelectionListener());
  }

  public JComponent getComponent() {
    JPanel termInfoPanel = new JPanel(new BorderLayout(0,0)); // hgap,vgap
    termInfoPanel.setPreferredSize(new Dimension(600,700));
    termInfoPanel.setMinimumSize(new Dimension(350,500));
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

  /** Commits currently browsed term when useTermButton is pressed */
  private class UseTermActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      commitTerm();
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
      setTextFromOboClass(e.getOboClass());
      return true;
    } 
  }

  /** put present term into current character - not used yet...*/
  private void commitTerm() {
    CharacterI ch = SelectionManager.inst().getSelectedCharacter();
    if (ch == null) { // can this happen?
      System.out.println("ERROR: no character selected to update");
      return;
    }
    if (currentOboClass == null) { // can this happen?
      System.out.println("ERROR: no term in term info to add to character");
      return;
    }

    CharFieldEnum cfe = OntologyManager.inst().getCharFieldEnumForOboClass(currentOboClass);
    
    UpdateTransaction ut = new UpdateTransaction(ch,cfe,currentOboClass,previousOboClass);
    EditManager.inst().updateModel(this,ut);
    previousOboClass = currentOboClass;
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
      OBOClass term = OntologyManager.inst().getOboClass(id);
      setTextFromOboClass(term);
    }
  }
}

     // JEditorPane allows for html formatting - bold... but doesnt do word wrap - bummer!
    // as far as i can tell
    // would have to explicitly put <br> in text. can also do hyperlinks!
    // for now just doing JTextArea
   // do this more generically? some sort of controller?
//     CompletionListListener l = new CompletionListListener();
//     termPanel.getEntityComboBox().addCompletionListListener(l);
//     termPanel.getPatoComboBox().addCompletionListListener(l);
//     if (termPanel.hasLumpComboBox())
//       termPanel.getLumpComboBox().addCompletionListListener(l);

      // ..... BrowserLauncher2? webstart BasicService? config browser...
      // somehow distinguish local links from non local?? 
      // look for localhost? - 
      // set up localhost servlet? or just pick out text from url?
      // hmmmmm......
//     private boolean isPhenoteLink(HyperlinkEvent e) {
//       return e.getURL() == null && e.getDescription().startsWith(PHENOTE_LINK_PREFIX);
//     }

//       String desc = e.getDescription();
//       if (desc == null || desc.equals("")) return;
//       String id = getIdFromDescription(desc);

//     private String getIdFromDescription(String desc) {
//       return desc.substring(PHENOTE_LINK_PREFIX.length());
//     }

//   /** Listens for completion list selection (via moouse over) and populates
//       term info with term selected -- this is pase - replaced by
//       TermSelectionEvent/Listener */
//   private class CompletionListListener implements ListSelectionListener {
//     public void valueChanged(ListSelectionEvent e) {
//       //int index = e.getFirstIndex();
//       Object source = e.getSource();
//       // hate to cast but it is handy here... and it is in fact a JList
//       //System.out.println("TI got sel - src: "+source.getClass()+" list? "+(source instanceof javax.swing.JList));
//       if (!(source instanceof JList)) {
//         System.out.println("source of combo box mouse over is not JList "+
//                            source.getClass());
//         return;
//       }
//       JList jList = (JList)source;
//       Object selectedValue = jList.getSelectedValue();
//       if (selectedValue == null)
//         return;
//       //System.out.println("sel val "+selectedValue.getClass()+" name "+selectedValue);
//       // the selected item should be an OBOClass
//       if (!(selectedValue instanceof OBOClass)) {
//         System.out.println("selected completion term is not obo class "
//                            +selectedValue.getClass());
//         return;
//       }
//       OBOClass oboClass = (OBOClass)selectedValue;
//       setTextFromOboClass(oboClass);
//     }
//   } // end of CompletionListListener inner class
