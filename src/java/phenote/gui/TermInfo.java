package phenote.gui;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.awt.Color;
import java.awt.Dimension;
//import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JEditorPane;
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

import org.geneontology.oboedit.datamodel.Link;
import org.geneontology.oboedit.datamodel.LinkedObject;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;

import phenote.datamodel.OntologyManager;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;

class TermInfo {

  //private JEditorPane textArea;
  private JTextComponent textArea;
  //private static final boolean DO_HTML = false;
  private static final boolean DO_HTML = true;
  static final String PHENOTE_LINK_PREFIX = "Phenote?id=";
  private TermHyperlinkListener termHyperlinkListener;

  
  TermInfo(TermPanel termPanel) {
    // do this more generically? some sort of controller?
//     CompletionListListener l = new CompletionListListener();
//     termPanel.getEntityComboBox().addCompletionListListener(l);
//     termPanel.getPatoComboBox().addCompletionListListener(l);
//     if (termPanel.hasLumpComboBox())
//       termPanel.getLumpComboBox().addCompletionListListener(l);
    SelectionManager.inst().addTermSelectionListener(new InfoTermSelectionListener());
  }

  JComponent getComponent() {
    // JEditorPane allows for html formatting - bold... but doesnt do word wrap - bummer!
    // as far as i can tell
    // would have to explicitly put <br> in text. can also do hyperlinks!
    // for now just doing JTextArea
    if (DO_HTML) {
      JEditorPane editorPane = new JEditorPane(); 
      editorPane.setContentType("text/html"); // sets up HTMLEditorKit
      termHyperlinkListener = new TermHyperlinkListener();
      editorPane.addHyperlinkListener(termHyperlinkListener);
      textArea = editorPane;
    }
    else {
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
    scrollPane.setBorder(BorderFactory.createTitledBorder("Term Info"));
    return scrollPane;
  }

  // for TestPhenote
  String getText() { return textArea.getText(); }

  private void setTextFromOboClass(OBOClass oboClass) {
    StringBuffer sb = new StringBuffer();
    if (oboClass.isObsolete())
      sb.append("This term is OBSOLETE").append(newLine());
    sb.append(bold("Term: ")).append(oboClass.getName());
    Set syns = oboClass.getSynonyms();
    for (Iterator it = syns.iterator(); it.hasNext(); ) {
      sb.append(newLine()).append(bold("Synonym: ")).append(it.next());
    }
    
    sb.append(nl()).append(nl()).append(bold("PARENTS: "));
    sb.append(getParentalString(oboClass));
    sb.append(nl()).append(nl()).append(bold("CHILDREN: "));
    sb.append(getChildrenString(oboClass));

    String definition = oboClass.getDefinition();
    // definition = lineWrap(definition);
    if (definition != null && !definition.equals(""))
      sb.append(nl()).append(nl()).append(bold("Definition: ")).append(definition);

    textArea.setText(sb.toString());
    // scroll to top (by default does bottom)
    textArea.setCaretPosition(0);
  }

  /** Only works in html mode - do with string buffers? */
  private String bold(String text) {
    if (!DO_HTML) return text;
    return "<b>"+text+"</b>";
  }
  
  private String nl() { return newLine(); }

  private String newLine() {
    if (DO_HTML) return "\n<br>";
    return "\n";
  }
  
  private StringBuffer getParentalString(OBOClass oboClass) {
    Set parents = oboClass.getParents();
    return getLinksString(parents,false);
  }

  private StringBuffer getChildrenString(OBOClass oboClass) {
    Set children = oboClass.getChildren();
    return getLinksString(children,true);
  }

  private StringBuffer getLinksString(Set links, boolean isChild) {
    StringBuffer sb = new StringBuffer();
    // or should thi sjust be done more generically with a hash of string bufs
    // for each unique link type name?
    StringBuffer isaStringBuf = new StringBuffer();
    StringBuffer partofStringBuf = new StringBuffer();
    StringBuffer devFromStringBuf = new StringBuffer();
    StringBuffer otherStringBuf = new StringBuffer();
    for (Iterator it = links.iterator(); it.hasNext(); ) {
      Link link = (Link)it.next();
      OBOProperty type = link.getType();
      //sb.append(newLine());
      //if (type == OBOProperty.IS_A) - somehow theres 2 instances???
      if (type.getName().equals("is_a")) {
	isaStringBuf.append(newLine());
        isaStringBuf.append(bold( isChild ? "Subclass" : "Superclass"));
        isaStringBuf.append(bold("(ISA): "));
	appendLink(isaStringBuf,isChild,link);
      }
      else if (type.getName().equals("part of")) {
	partofStringBuf.append(newLine());
        partofStringBuf.append(bold( isChild ? "Subpart: " : "Part of: "));
	appendLink(partofStringBuf,isChild,link);
      }
      else if (type.getName().equals("develops from")) {
	devFromStringBuf.append(newLine());
	devFromStringBuf.append(bold( isChild ? "Develops into: ":"Develops from: "));
	appendLink(devFromStringBuf,isChild,link);
      }
      // catch all - any relationships missed just do its name capitalize? _->' '?
      else {
	otherStringBuf.append(newLine());
        otherStringBuf.append(bold(capitalize(type.getName()))).append(": ");
	appendLink(otherStringBuf,isChild,link);
      }
//       if (isChild)
//         termBuf.append(termLink(link.getChild()));
//       else
//         termBuf.append(termLink(link.getParent())); 
    }
    sb.append(isaStringBuf).append(partofStringBuf);
    sb.append(devFromStringBuf).append(otherStringBuf);
    return sb;
  }

  private void appendLink(StringBuffer sb, boolean isChild, Link link) {
    if (isChild)
      sb.append(termLink(link.getChild()));
    else
      sb.append(termLink(link.getParent())); 
  }

  private String termLink(LinkedObject term) {
    return "<a href='"+PHENOTE_LINK_PREFIX+term.getID()+"'>"+term.getName()+"</a>";
  }

  private String capitalize(String s) {
    if (s == null || s.equals("")) return "";
    String firstLetter = s.substring(0,1);
    return firstLetter.toUpperCase() + s.substring(1,s.length());
  }


  private class InfoTermSelectionListener implements TermSelectionListener {
    public boolean termSelected(TermSelectionEvent e) {
      setTextFromOboClass(e.getOboClass());
      return true;
    } 
  }

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

  
  /** for testing */
  void simulateHyperlinkEvent(HyperlinkEvent e) {
    termHyperlinkListener.hyperlinkUpdate(e);
  }


  private class TermHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED))
        return;

      URL url = e.getURL();
      //System.out.println("got url "+url+" desc "+e.getDescription());

      // internal link to term...
      if (isPhenoteLink(e)) {
        bringUpTermInPhenote(e);
        return;
      }

      if (url == null) { // relative urls are null
        System.out.println("invalid url "+url);
        return;
      }

      bringUpInBrowser(url);

      // ..... BrowserLauncher2? webstart BasicService? config browser...
      // somehow distinguish local links from non local?? 
      // look for localhost? - 
      // set up localhost servlet? or just pick out text from url?
      // hmmmmm......
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

    private boolean isPhenoteLink(HyperlinkEvent e) {
      return e.getURL() == null && e.getDescription().startsWith(PHENOTE_LINK_PREFIX);
    }

    private void bringUpTermInPhenote(HyperlinkEvent e) {
      String desc = e.getDescription();
      if (desc == null || desc.equals("")) return;
      String id = getIdFromDescription(desc);
      // or do through obo session?
      OBOClass term = OntologyManager.inst().getOboClass(id);
      setTextFromOboClass(term);
    }
    private String getIdFromDescription(String desc) {
      return desc.substring(PHENOTE_LINK_PREFIX.length());
    }
  }
}
