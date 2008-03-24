package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import org.obo.datamodel.OBOProperty;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterEx;
import phenote.datamodel.Comparison;
import phenote.datamodel.Ontology;
import phenote.edit.EditManager;
import phenote.config.Config;
import phenote.gui.field.CharFieldGui;
import phenote.gui.field.CharFieldGuiEx;
import phenote.gui.field.FieldPanel;
import phenote.gui.field.ReadOnlyFieldGui;
import phenote.gui.field.RelationCompList;

/** a gui for making comparisons between 2 statements/annotations
 there is comparison controller stuff in here - listens to gui elements and modifies
 model*/

class ComparisonGui {
  
  private JDialog dialog;
//   private CharacterI char1;
//   private CharacterI char2;
  //private ReadOnlyFieldGui subjectGui;
  private ComparisonCharacterGui subjectGui;
  private RelationCompList relFieldGui;
  private ComparisonCharacterGui objectGui;
  /** eventually may do a list of comparisons? */
  private Comparison currentComparison;
  private ComparisonListModel compListModel = new ComparisonListModel();
  private FieldPanel fieldPanel;

  ComparisonGui(Frame owner) { //, CharacterI c1, CharacterI c2) {
    try { init(owner); } 
    catch (CharFieldException x) {
      String m = "No relation ontology configured, cant do comparison";
      JOptionPane.showMessageDialog(null,m,"Error",JOptionPane.ERROR_MESSAGE);
    }
  }

  /** initialize gui with 2 sel characters (and frame owner)
   dont know if comp gui should deal with selected chars from table 
  as unclear what to do with them - new? find & update? */
  private void init(Frame owner) //, CharacterI sub, CharacterI obj) 
    throws CharFieldException {
    //currentComparison = new Comparison(); // ?
    //currentComparison.setSubject(sub);
    //currentComparison.setObject(obj);
    boolean modal = false;//true;
    dialog = new JDialog(owner,"Statement Comparison",modal);
    dialog.setAlwaysOnTop(true);

    fieldPanel = FieldPanel.makeBasicPanel();
    dialog.add(fieldPanel);
    
    addHelpText();

    addSubjectGui();

    // Relationship - dislpay rel if comp already made
    addRelGui(fieldPanel); // throws CharFieldException if no rel ontology

    addObjectGui();

    // Buttons OK & Cancel
    addButtons();
    
    dialog.add(createListGui(),BorderLayout.EAST);

    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
    dialog.pack(); // ?
  }

  private void addHelpText() {
    String m = "Drag rows from Annotation table and drop on Subject & Object fields:";
    fieldPanel.addLabelForWholeRow(m);
    fieldPanel.addLabelForWholeRow(" "); // cheap vert spacing
  }

  private void addSubjectGui() {
    subjectGui = addField(fieldPanel,"Subject");
    subjectGui.addObserver(new SubjectObserver());
  }
  private void addObjectGui() {
    objectGui = addField(fieldPanel,"Object");
    objectGui.addObserver(new ObjectObserver());
  }

  private ComparisonCharacterGui addField(FieldPanel fieldPanel,String name) {
    //ReadOnlyFieldGui fg=new ReadOnlyFieldGui(new CharField("Subject",null,null));
    ComparisonCharacterGui gui = new ComparisonCharacterGui(name);
    //fieldGui.setCharacter(currentComparison.getSubject()); // ???
    //fieldGui.enableCharDropListening(true);
    fieldPanel.addRow(name,gui.getComponent());
    return gui;
  }

  private void addRelGui(FieldPanel fp) throws CharFieldException {
    CharField relChar = new CharField(CharFieldEnum.RELATIONSHIP);
    // throws CharFieldEx if no rel ontol found
    Ontology o = CharFieldManager.inst().getComparisonRelationOntology();
    relChar.addOntology(o);
    relFieldGui = CharFieldGui.makeRelationList(relChar);//"Relationship"?
    // listen for new selected relations
    relFieldGui.addObserver(new RelationObserver());
    fp.addCharFieldGuiToPanel(relFieldGui);
  }

  private JComponent createListGui() {
    JList listGui = new JList();
    listGui.setModel(compListModel);
    listGui.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane listScroll = new JScrollPane(listGui);
    listScroll.setMinimumSize(new Dimension(90,40)); // w,h
    listScroll.setPreferredSize(new Dimension(130,60));
    return listScroll;
  }

  private void addButtons() {
    List<JComponent> buttons = new ArrayList<JComponent>(4);
    JButton ok = new JButton("OK");
    ok.addActionListener(new OkListener());
    buttons.add(ok);
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new CancelListener());
    buttons.add(cancel);
    JButton addComp = new JButton("+");
    buttons.add(new Box.Filler(new Dimension(10,0),new Dimension(200,0),
                               new Dimension(400,0)));
    buttons.add(addComp);
    fieldPanel.addComponentRow(buttons);
  }

  // OK
  private class OkListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      try {
        commitComparison();
      } catch (CharFieldGuiEx x) {
        String m = "Comparison failed "+x.getMessage();
        log().debug(m);
        JOptionPane.showMessageDialog(dialog,m,"error",JOptionPane.ERROR_MESSAGE);
        return; // leave window up - hit cancel to get rid of
      }
      dialog.dispose(); // no ex - ok
    }
  }

  private void commitComparison() throws CharFieldGuiEx {
    CharacterI sub = subjectGui.getCharacter();
    OBOProperty rel = relFieldGui.getCurrentRelation(); // ex if not filled in
    CharacterI obj = objectGui.getCharacter();
    if (sub==null || rel == null || obj == null)
      throw new CharFieldGuiEx("Comparison not fully filled out");
    
    currentComparison.setSubject(sub);
    currentComparison.setRelation(rel);
    currentComparison.setObject(obj);

    try {
      //char1.addComparison(rel,char2);
      EditManager.inst().addComparison(this,currentComparison);
    } 
    catch (CharacterEx e) {
      String m = e.getMessage();
      JOptionPane.showMessageDialog(dialog,m,"error",JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  // CANCEL
  private class CancelListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      dialog.dispose();
    }
  }


  /** This class is called whenever the relFieldGui is changed. The new rel
      is then queried from rel gui and put in to current comparison. So its a pull
      rather than push, and CompGui/RelObs is the controller
      could also do this with event/listener of course - very similar */
  private class RelationObserver implements Observer {
    public void 	update(Observable o, Object arg) {
      try { currentComparison.setRelation(relFieldGui.getCurrentRelation()); }
      catch (CharFieldGuiEx e) { currentComparison.setRelation(null); } // ?
      compListModel.fireContentsChanged();
    }
  }

  private class SubjectObserver implements Observer {
    public void 	update(Observable o, Object arg) {
      currentComparison.setSubject(subjectGui.getCharacter());
      compListModel.fireContentsChanged();
    }
  }
  private class ObjectObserver implements Observer {
    public void 	update(Observable o, Object arg) {
      currentComparison.setObject(objectGui.getCharacter());
      compListModel.fireContentsChanged();
    }
  }

  /** INNER CLASS MODEL FOR COMPARISON JList, a list of all comparisons */ 
  private class ComparisonListModel extends AbstractListModel {
    private List<Comparison> compList = new ArrayList<Comparison>();

    private ComparisonListModel() {
      initComparisonList();
    }

    private void initComparisonList() {
      compList = getComparisonsFromCharList();
      // if non empty select 1st item?
      // if empty then add new item to edit
      if (compList.isEmpty()) {
        currentComparison = new Comparison();
        compList.add(currentComparison);
      }
      // else select 1st item and set to currentComparison
    }
    
    private List<Comparison> getComparisonsFromCharList() {
      // for now just return empty list
      return new ArrayList<Comparison>();
    }

    public Object getElementAt(int index) {
      if (compList==null) return null;
      return compList.get(index);
    }
    public int getSize() {
      if (compList==null) return 0;
      return compList.size();
    }

    // contents have changed - do a repaint
    private void fireContentsChanged() {
      fireContentsChanged(this,0,getSize()-1);
    }
    
  }

  private Logger log() {
    return Logger.getLogger(getClass());
  }


}

  // util fn?
//   private String charString(CharacterI c) {
//     return ReadOnlyFieldGui.charString(c);
//   }
//     if (c == null) return "";
//     StringBuffer sb = new StringBuffer();
//     for (CharField cf : c.getAllCharFields()) {
//       if (!Config.inst().isVisible(cf)) continue;
//       String val = c.getValueString(cf);
//       if (val == null || val.trim().equals("")) continue;
//       sb.append(val).append(" ");
//     }
//     return sb.toString().trim();
