package phenote.gui.field;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import phenote.datamodel.CharField;
import phenote.datamodel.OntologyManager;
import phenote.edit.EditManager;
import phenote.gui.selection.SelectionManager;

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
  private String group;
  private SelectionManager selectionManager;
  private EditManager editManager;

  /** eventually configurable (with default 12) - for now hardwire at 12 */
  private static int fieldsPerTab = 12;

//   public FieldPanel() {
//     this(true,false); // false - no search panel - in menu now
//   }
//   // false for post comp panel - boolean isPostComp?
//   public FieldPanel(boolean doAllFields,boolean addSearchPanel) {
//     this(doAllFields, addSearchPanel, null, SelectionManager.inst(), EditManager.inst());
//   }

  public FieldPanel(boolean doAllFields, boolean addSearchPanel, String grp) {
    setGroup(grp);
    init(doAllFields,addSearchPanel,group,SelectionManager.getSelMan(group),
         EditManager.getEditManager(group));
  }
  
  public FieldPanel(boolean doAllFields, boolean addSearchPanel,
                    String group, SelectionManager selectionManager,
                    EditManager editManager) {
    init(doAllFields,addSearchPanel,group,selectionManager,editManager);
  }

  private void init(boolean doAllFields, boolean addSearchPanel,String group,
                    SelectionManager selectionManager, EditManager editManager) {
    setGroup(group);
    this.selectionManager = selectionManager;
    this.editManager = editManager;
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

  private void setGroup(String g) {
    if (g == null) this.group = OntologyManager.DEFAULT_GROUP;
    else this.group = g;
  }

  private void initGui() {
    // should figure y from # of fields really!!! yes!!!
    // width of ontology labels effects x
    this.setPreferredSize(new Dimension(650,350));//690,490));
    //this.setMinimumSize(new Dimension(700,490));//690,490));
    //this.setMaximumSize(new Dimension(2000,750));
    //BoxLayout bl = new BoxLayout(this,BoxLayout.X_AXIS); // grid bag?
    //this.setLayout(bl);
    this.setLayout(new GridLayout());
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
    for (CharField charField : this.getCharFieldList()) {
      if (isTabbed() && fieldNum % fieldsPerTab == 0) {
        fieldPanel = new JPanel(new GridBagLayout());
        jTabbedPane.addTab("Tab "+tab++,fieldPanel);
      }
      ++fieldNum;
      //CharFieldGui gui = new CharFieldGui(charField,this); // adds to panel
      CharFieldGui gui = CharFieldGui.makeCharFieldGui(charField);
      gui.setSelectionManager(this.selectionManager);
      gui.setEditManager(this.editManager);
      addCharFieldGuiToPanel(gui);
      charFieldGuiList.add(gui);
    }
  }

  private boolean isTabbed() {
    if (this.group != null) {
      return this.ontologyManager.getCharFieldListForGroup(this.group).size() > FieldPanel.fieldsPerTab;
    } else {
      return ontologyManager.getNumberOfFields() > fieldsPerTab;
    }
  }

  private int currentGridBagRow = 0;
  void addCharFieldGuiToPanel(CharFieldGui fieldGui) {
    GridBagConstraints baseConstraints = new GridBagConstraints();
    baseConstraints.insets = new Insets(2,3,2,3);
    baseConstraints.gridy = this.currentGridBagRow++;
    this.addLabel(fieldGui, (GridBagConstraints)baseConstraints.clone());
    this.addOntologyChooser(fieldGui, (GridBagConstraints)baseConstraints.clone());
    this.addInputGui(fieldGui, (GridBagConstraints)baseConstraints.clone());
    this.addPostCompButton(fieldGui, (GridBagConstraints)baseConstraints.clone());
    this.addRetrieveButton(fieldGui, (GridBagConstraints)baseConstraints.clone());
  }
  
  private void addLabel(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridx = 0;
    if (!fieldGui.hasOntologyChooser()) {
      constraints.gridwidth = 2;
    }
    constraints.anchor = GridBagConstraints.EAST;
    fieldPanel.add(new JLabel(fieldGui.getLabel()), constraints);
  }
  
  private void addOntologyChooser(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    if (fieldGui.hasOntologyChooser()) {
      fieldPanel.add(fieldGui.getOntologyChooser(), constraints);
    }
  }
  
  private void addInputGui(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridx = 2;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    fieldPanel.add(fieldGui.getUserInputGui(), constraints);
  }
  
  private void addPostCompButton(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridx = 3;
    if (fieldGui.hasCompButton()) {
      fieldPanel.add(fieldGui.getCompButton(), constraints);
    }
  }
  
  private void addRetrieveButton(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridx = 4;
    if (fieldGui.hasRetrieveButton()) {
      fieldPanel.add(fieldGui.getRetrieveButton(), constraints);
    }
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
  
  private List<CharField> getCharFieldList() {
    if (this.group != null) {
      return this.ontologyManager.getCharFieldListForGroup(this.group);
    } else {
      return this.ontologyManager.getCharFieldList();
    }
  }

}

//   boolean hasLumpComboBox() {
//     //return lumpField.isCombo();
//     return getLumpComboBox() != null;
//   }
//   AbstractAutoCompList getLumpComboBox() {
//     return getComboBox(CharFieldEnum.LUMP);
//   }

