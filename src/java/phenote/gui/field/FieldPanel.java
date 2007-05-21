package phenote.gui.field;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OntologyManager;
import phenote.gui.GridBagUtil;

import phenote.gui.SearchParamsI;
import phenote.gui.SearchParams;

/**
 * FieldPanel holds all the fields for the terms - Genotype, Entity/Anatomy, QUALITY.
 * Can be populated by hand (Genotype), or  selection of instance in completion list. 
 also has SearchParamPanel(?).
 */

public class FieldPanel extends JPanel {
  
  private List<CharFieldGui> charFieldGuiList = new ArrayList<CharFieldGui>(8);
  private SearchParamPanel searchParamPanel; // searchParamManager?
  private OntologyManager ontologyManager = OntologyManager.inst();
  private JPanel fieldPanel;
  private JTabbedPane jTabbedPane;

  /** eventually configurable (with default 12) - for now hardwire at 12 */
  private static int fieldsPerTab = 12;

  public FieldPanel() {
    this(true,false);
  }
  // false for post comp panel - boolean isPostComp?
  public FieldPanel(boolean doAllFields,boolean addSearchPanel) {
    initGui();
    if (doAllFields) {
      initCharFieldGuis();
    }
    else {
      fieldPanel = new JPanel(new GridBagLayout());
      add(fieldPanel);
    }
    if (addSearchPanel)
      initSearchPanel();
  }

  private void initGui() {
    // should figure y from # of fields really!!! yes!!!
    // width of ontology labels effects x
    this.setPreferredSize(new Dimension(650,350));//690,490));
    //this.setMinimumSize(new Dimension(700,490));//690,490));
    //this.setMaximumSize(new Dimension(2000,750));
    BoxLayout bl = new BoxLayout(this,BoxLayout.X_AXIS); // grid bag?
    this.setLayout(bl);
  }

  private void initSearchPanel() {
    // search param panel - maybe search panel should be added to main frame?
    add(Box.createRigidArea(new Dimension(2,0)));
    add(getSearchParamPanel().getPanel());
  }


  private void initCharFieldGuis() {

    fieldPanel = new JPanel(new GridBagLayout());
    if (isTabbed()) {
      jTabbedPane  = new JTabbedPane();
      add(jTabbedPane);
    }
    else { // no tabs
      add(fieldPanel);
    }

    int fieldNum = 0;
    int tab = 1;
    for (CharField charField : ontologyManager.getCharFieldList()) {
      if (isTabbed() && fieldNum % fieldsPerTab == 0) {
        fieldPanel = new JPanel(new GridBagLayout());
        jTabbedPane.addTab("Tab "+tab++,fieldPanel);
      }
      ++fieldNum;
      //CharFieldGui gui = new CharFieldGui(charField,this); // adds to panel
      CharFieldGui gui = CharFieldGui.makeCharFieldGui(charField);
      addCharFieldGuiToPanel(gui);
      charFieldGuiList.add(gui);
    }
  }

  private boolean isTabbed() {
    return ontologyManager.getNumberOfFields() > fieldsPerTab;
  }

  void addCharFieldGuiToPanel(CharFieldGui fieldGui) {
    addLabel(fieldGui.getLabel(),fieldGui.hasOntologyChooser());
    if (fieldGui.hasOntologyChooser())
      addOntologyChooser(fieldGui.getOntologyChooser());
    addFieldGui(fieldGui.getUserInputGui());
    if (fieldGui.hasCompButton())
      addPostCompButton(fieldGui.getCompButton());
    if (fieldGui.hasRetrieveButton())
      addRetrieveButton(fieldGui.getRetrieveButton());
  }

//  SearchParamsI getSearchParams() {
////    if (searchParams == null)
////      searchParams = getSearchParamPanel().getSearchParams();
//    return searchParams;
//	  return SearchParams.inst();
//  }

  //void setSearchParams(SearchParamsI sp) { searchParams = sp; }

  public SearchParamPanel getSearchParamPanel() {
    if (searchParamPanel == null)
      searchParamPanel = new SearchParamPanel();
    return searchParamPanel;
  }

  JLabel addLabel(String labelString) {//,Container parent) {
    return addLabel(labelString,false); // false - no ont chooser
  }

  JLabel addLabel(String labelString,boolean hasOntChooser) {
    JLabel label = new JLabel(labelString);
    GridBagConstraints gbc = makeLabelConstraint(hasOntChooser);
    fieldPanel.add(label,gbc);
    return label;
  }

  /** if a field has more than one ontology than theres a combo to choose the ontology*/
  void addOntologyChooser(JComboBox ontologyChooser) {
    fieldPanel.add(ontologyChooser,makeOntologyChooserConstraint());
  }
  
  void addFieldGui(Component comp) {
    fieldPanel.add(comp,makeFieldConstraint());
  }

  void addPostCompButton(JButton pc) {
    fieldPanel.add(pc,makePostCompConstraint());
  }
  
  void addRetrieveButton(JButton rb) {
    // for now assume fields dont do both post comp and retrieve - fix this!
    fieldPanel.add(rb,makePostCompConstraint());
  }

  private int gridbagRow = 0;
  boolean ontologyChooserPresent = false;
  private GridBagConstraints makeLabelConstraint(boolean hasOntChooser) {
    ontologyChooserPresent = hasOntChooser; // ??
    // x,y,horizPad,vertPad
    // make width 2 unless theres a chooser, then 1
    int width = hasOntChooser ? 1 : 2;
    return GridBagUtil.makeWidthConstraint(0,gridbagRow,1,3,width);
  }

  private GridBagConstraints makeOntologyChooserConstraint() {
    ontologyChooserPresent = true; // cheesy - dont need?
    return GridBagUtil.makeConstraint(1,gridbagRow,1,3); // width 1
  }

  private GridBagConstraints makeFieldConstraint() {
    int x = 2;//ontologyChooserPresent ? 2 : 1;
    int width = 1;//ontologyChooserPresent ? 1 : 2; // ???
    return GridBagUtil.makeWidthConstraint(x,gridbagRow++,1,2,width);
  }

  // put button at end of regular row? or beginning of pc row?
  private GridBagConstraints makePostCompConstraint() {
    return GridBagUtil.makeWidthConstraint(3,gridbagRow-1,1,3,2); // width 1
  }
  

  // for test to listen - move to test code?
  public AbstractAutoCompList getEntityComboBox() {
    //return getComboBox(CharFieldEnum.ENTITY);
    return getComboBox("Entity");
  }
  
  private AbstractAutoCompList getComboBox(String name) {
    for (CharFieldGui cfg : charFieldGuiList)
      if (cfg.getLabel().equals(name)) return cfg.getCompList();
    return null;
  }


  public AbstractAutoCompList getQualityComboBox() {
    return getComboBox("Quality");
  }

}

//   boolean hasLumpComboBox() {
//     //return lumpField.isCombo();
//     return getLumpComboBox() != null;
//   }
//   AbstractAutoCompList getLumpComboBox() {
//     return getComboBox(CharFieldEnum.LUMP);
//   }

