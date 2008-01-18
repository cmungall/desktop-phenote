package phenote.gui.field;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;

import phenote.config.Config;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.GroupAdapterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;
import phenote.gui.CharacterTableController;
import phenote.gui.CharacterTableSource;
import phenote.gui.selection.SelectionManager;
import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * FieldPanel holds all the fields for the terms - Genotype, Entity/Anatomy, QUALITY.
 * Can be populated by hand (Genotype), or  selection of instance in completion list. 
 also has SearchParamPanel(?).
 */

public class FieldPanel extends AbstractGUIComponent {
  
  private List<CharFieldGui> charFieldGuiList = new ArrayList<CharFieldGui>(8);
  private SearchParamPanel searchParamPanel; // searchParamManager?
  private CharFieldManager ontologyManager = CharFieldManager.inst();
  private JPanel fieldPanel;
  private JTabbedPane jTabbedPane;
  private String group;
  private SelectionManager selectionManager;
  private EditManager groupEditMan;
  private EventSelectionModel<CharacterI> selectionModel;

  /** eventually configurable (with default 12) - for now hardwire at 12 */
  private static int fieldsPerTab = 12;

//   public FieldPanel() {
//     this(true,false); // false - no search panel - in menu now
//   }
//   // false for post comp panel - boolean isPostComp?
//   public FieldPanel(boolean doAllFields,boolean addSearchPanel) {
//     this(doAllFields, addSearchPanel, null, SelectionManager.inst(), EditManager.inst());
//   }

  private FieldPanel() {
    this(false,false,Config.inst().getDefaultGroup().getName(),
         CharacterTableController.getDefaultController().getSelectionModel());
  }
  
  /** a panel with none of the features - stripped */
  public static FieldPanel makeBasicPanel() {
    return new FieldPanel();
  }
  
  public FieldPanel(CharacterTableSource table) {
    this(true, false, table.getGroup(), SelectionManager.inst(), EditManager.getEditManager(table.getGroup()), table.getSelectionModel());
  }

  // Group or String for group?
  public FieldPanel(boolean doAllFields, boolean addSearchPanel, String grp,
                    EventSelectionModel<CharacterI> model) {
    this(doAllFields, addSearchPanel, grp, SelectionManager.getSelMan(grp), EditManager.getEditManager(grp), model);
  }
  
  public FieldPanel(boolean doAllFields, boolean addSearchPanel,
                    String group, SelectionManager selectionManager,
                    EditManager groupEditMan, EventSelectionModel<CharacterI> model) {
		super("phenote-editor:phenote-editor");
    init(doAllFields,addSearchPanel,group,selectionManager,groupEditMan, model);
  }

  private void init(boolean doAllFields, boolean addSearchPanel,String group,
                    SelectionManager selectionManager, EditManager groupEditMan,
                    EventSelectionModel<CharacterI> model) {
    this.removeAll();
    setGroup(group);
    this.selectionModel = model;
    this.selectionManager = selectionManager;
    this.groupEditMan = groupEditMan;
    initGui();
    if (doAllFields) {
      initCharFieldGuis();
    }
    else { // used by post comp?
      fieldPanel = new JPanel(new GridBagLayout());
      //JScrollPane panelScrollPane = new JScrollPane(fieldPanel);
      add(fieldPanel);
    }
    if (addSearchPanel)
      initSearchPanel();

    // should there be a GroupConfig object? cant GroupAdap do this itself?
    // yea i think group adap can just do this - refactor!
    if (Config.inst().hasGroupAdapter(group)) {
      GroupAdapterI groupAdap = Config.inst().getGroupAdapter(group);
      if (groupAdap.hasCharChangeListener()) {
        // this is doing for all groups, should probably just for its own group
        groupEditMan.addCharChangeListener(groupAdap.getCharChangeListener());
      }
      if  (groupAdap.hasCharListChangeListener()) {
        CharacterListManager.getCharListMan(group).addCharListChangeListener(groupAdap.getCharListChangeListener());
      }
    }
    //I'm naming the field panel the name of the config
    this.setName(Config.inst().getConfigName());
    this.setTitle("Phenote editor: "+Config.inst().getConfigName());
  } 

  private void setGroup(String g) {
    if (g == null) this.group = CharFieldManager.DEFAULT_GROUP;
    else this.group = g;
  }

  private void initGui() {
    // should figure y from # of fields really!!! yes!!!
    // width of ontology labels effects x
    this.setPreferredSize(new Dimension(650,350));//690,490));
    //this.setMinimumSize(new Dimension(700,490));//690,490));
    //this.setMaximumSize(new Dimension(2000,750));
    BoxLayout bl = new BoxLayout(this,BoxLayout.X_AXIS); // grid bag?
    this.setLayout(bl);
    //this.setLayout(new GridLayout());
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
      JScrollPane jsp = new JScrollPane(fieldPanel);
      add(jsp);
    }

    int fieldNum = 0;
    int tab = 1;
    for (CharField charField : this.getCharFieldList()) {
      // skip hidden fields
      if (!Config.inst().isVisible(charField)) continue;
      if (isTabbed() && fieldNum % fieldsPerTab == 0) {
        fieldPanel = new JPanel(new GridBagLayout());
        // add scroll pane? or is that wierd with tabs?
        jTabbedPane.addTab("Tab "+tab++,fieldPanel);
      }
      int minCompChars = Config.inst().getMinCompChars(fieldNum);//fieldnum??
      CharFieldGui gui = CharFieldGui.makeCharFieldGui(charField,minCompChars);
      ++fieldNum;
      
      gui.setSelectionManager(this.selectionManager);
      gui.setListSelectionModel(this.selectionModel);
      gui.setEditManager(this.groupEditMan);
      addCharFieldGuiToPanel(gui);
      charFieldGuiList.add(gui);
    }
  }

  private boolean isTabbed() {
    if (Config.inst().shouldUseFieldPanelTabs()) {
      if (this.group != null) {
        return this.ontologyManager.getCharFieldListForGroup(this.group).size() > FieldPanel.fieldsPerTab;
      } else {
        return ontologyManager.getNumberOfFields() > fieldsPerTab;
      }
    } else {
      return false;
    }
  }

  private int fieldsPerPanel() {
    return fieldsPerTab;
  }

  private int currentGridBagRow = 0;
  private int currentGridBagCol = 0;
  private int buttonGridBagCol = 0;
  public void addCharFieldGuiToPanel(CharFieldGui fieldGui) {
    // first one does new row
    this.addLabel(fieldGui, getConstraintsNewRow());
    this.addOntologyChooser(fieldGui, getConstraintsSameRow());
    this.addInputGui(fieldGui, getConstraintsSameRow());
    addListGui(fieldGui,getConstraintsSameRow());
    addButtons(fieldGui,getConstraintsSameRow()); //put all buttons in same col
  }
  
  void addButtons(CharFieldGui fieldGui, GridBagConstraints constraints) {
    this.addPostCompButton(fieldGui, constraints);
    this.addRetrieveButton(fieldGui, constraints);  	
  }
  
  /** set up basic grid bag constraints with insets and increments currentGridBagRow/gridy
   */
  GridBagConstraints getConstraintsNewRow() {
    GridBagConstraints baseConstraints = getConstraintsSameRow();
    baseConstraints.gridy = ++this.currentGridBagRow;
    return baseConstraints;
  }
  
  
  /** set up basic grid bag constraints with insets and uses currentGridBagRow for gridy
      (doesnt increment it) */
  GridBagConstraints getConstraintsSameRow() {
    GridBagConstraints baseConstraints = new GridBagConstraints();
    baseConstraints.insets = new Insets(2,3,2,3);
    baseConstraints.gridy = this.currentGridBagRow; // no incrementing
    return baseConstraints;
  }
  
  
  /** used by comparison */
  public void addLabelForWholeRow(String label) {
    GridBagConstraints constraints = getConstraintsNewRow();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridwidth = 5; // whole row
    addLabel(label,constraints);
  }
  
  // anchors EAST/right
  private void addLabel(CharFieldGui field, GridBagConstraints constraints) {
    constraints.anchor = GridBagConstraints.EAST;
    if (!field.hasOntologyChooser())
      constraints.gridwidth = 2;
    addLabel(field.getLabel(),constraints);
  }

  private void addLabel(String label,GridBagConstraints constraints) {
    constraints.gridx = 0;
    //constraints.anchor = GridBagConstraints.EAST;
    fieldPanel.add(new JLabel(label), constraints);
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
    if (!fieldGui.hasListGui()) constraints.gridwidth = 2;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    fieldPanel.add(fieldGui.getUserInputGui(), constraints);
  }
  
  private void addListGui(CharFieldGui fieldGui, GridBagConstraints constraints) {
    if (!fieldGui.hasListGui()) return;
    // LIST - to the right of input gui
    constraints.gridx = 3;
    constraints.gridwidth = 2;
    constraints.gridheight = 2;
    fieldPanel.add(fieldGui.getListGui(),constraints);
    // DEL BUTTON - put under input gui near list
    constraints.gridy = ++this.currentGridBagRow; //? put in next row
    constraints.gridx = 2;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.anchor = GridBagConstraints.EAST;
    fieldPanel.add(fieldGui.getListDelButton(),constraints);
  }

  private void addPostCompButton(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridx = 4;
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

  /** for comparison - add a row of just buttons */
  public void addButtonRow(List<JButton> buttons) {
    JPanel buttonPanel = new JPanel();
    for (JButton b : buttons)
      buttonPanel.add(b);
    GridBagConstraints constraints = getConstraintsNewRow();
    constraints.gridwidth = 5;
    constraints.gridx = 1; // scoot it over a little?
    fieldPanel.add(buttonPanel,constraints);
  }


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
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}



//  SearchParamsI getSearchParams() {
////    if (searchParams == null)
////      searchParams = getSearchParamPanel().getSearchParams();
//    return searchParams;
//	  return SearchParams.inst();
//  }

  //void setSearchParams(SearchParamsI sp) { searchParams = sp; }
//   boolean hasLumpComboBox() {
//     //return lumpField.isCombo();
//     return getLumpComboBox() != null;
//   }
//   AbstractAutoCompList getLumpComboBox() {
//     return getComboBox(CharFieldEnum.LUMP);
//   }

