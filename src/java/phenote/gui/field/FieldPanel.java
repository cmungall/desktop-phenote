package phenote.gui.field;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import phenote.gui.VerticalScrollingOnlyPanel;
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
  private CharFieldManager charFieldManager = CharFieldManager.inst();
  private JPanel fieldPanel;
  private JTabbedPane jTabbedPane;
  private String group;
  private SelectionManager selectionManager;
  private EditManager groupEditMan;
  private EventSelectionModel<CharacterI> selectionModel;

  /** Configurable (with default 8) */
  private static int fieldsPerTab = 8; // 10;

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
    this(true, false, table.getGroup(), SelectionManager.inst(), table.getEditManager(), table.getSelectionModel());
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

    // search params now done in menu - this is always false - phase out?
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
    this.setPreferredSize(new Dimension(700,375));//690,490));
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
    if (Config.inst().getFieldsPerTab() > 0)
      fieldsPerTab = Config.inst().getFieldsPerTab();
    //    log().debug("fieldsPerTab = " + fieldsPerTab); // DEL

    fieldPanel = new VerticalScrollingOnlyPanel(new GridBagLayout());

    if (isTabbed()) {
      jTabbedPane  = new JTabbedPane();
      add(jTabbedPane);
    }
    else { // no tabs
      JScrollPane jsp = new JScrollPane(fieldPanel);
      add(jsp);
    }

    Hashtable tabbedPaneForTab = new Hashtable();
    int fieldNum = 0;
    int tab = 0; // will get incremented before use
    for (CharField charField : this.getCharFieldList()) {
      // skip hidden fields
      if (!Config.inst().isVisible(charField)) continue;

      // See if there's a named tab assigned to this field
      String tabForField = charField.getTabForField();
//      log().debug("Looking for tab " + tabForField + " for " + charField.getName() + ", isTabbed = " + isTabbed()); // DEL
      // If we're doing tabs by number
      if (isTabbed() && (fieldNum % fieldsPerTab == 0) && (tabForField == null)) {
        // Make a new tab (first increment tab number)
        ++tab;
        fieldPanel = new JPanel(new GridBagLayout());
//        log().debug("Created tab " + tab); // DEL
        jTabbedPane.addTab("Tab "+tab,fieldPanel);
      }
      // Assign field to the appropriate named tab
      else if (tabForField != null) {
        fieldPanel = (JPanel) tabbedPaneForTab.get(tabForField);
        if (fieldPanel == null) {
//          log().debug("Couldn't find pre-existing tab " + tabForField + " for " + charField.getName() + "--constructing."); // DEL
          fieldPanel = new JPanel(new GridBagLayout());
          tabbedPaneForTab.put(tabForField, fieldPanel);
          jTabbedPane.addTab(tabForField,fieldPanel);
        }
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
      if (thereIsAtLeastOneNamedTab())
        return true;
      if (this.group != null) {
        return this.charFieldManager.getCharFieldListForGroup(this.group).size() > FieldPanel.fieldsPerTab;
      } else {
        return charFieldManager.getNumberOfFields() > fieldsPerTab;
      }
    } else {
      return false;
    }
  }

  private boolean checkedWhetherThereIsANamedTab = false;

  private boolean thereIsAtLeastOneNamedTab() {
    for (CharField charField : this.getCharFieldList()) {
      // skip hidden fields
      if (!Config.inst().isVisible(charField)) { continue; }
      if (charField.getTabForField() != null) {
        checkedWhetherThereIsANamedTab = true;
        return true;
      }
    }
    checkedWhetherThereIsANamedTab = true;
    return false;
  }

  private int currentGridBagRow = 0;

  /** 0: label
      1: ont chooser or rest of label
      2: input field
      3: edit button(if dont fit), rest of input, list del, list?
      4: buttons, list?
      todo: move list to below input not to right of it - too scrunched
  */

  public void addCharFieldGuiToPanel(CharFieldGui fieldGui) {
    // first one does new row
    // label is x 0, width 1 with ont chooser, width 2 otherwise
    this.addLabel(fieldGui, getConstraintsNewRow()); // gridx 0
    // add ontology chooser x 1 width 1 if present
    int gridx = 1;
    this.addOntologyChooser(fieldGui, getConstraintsSameRow(gridx)); // 1
    //int width = 2; // in case need to slip in edit button if too many buttons
    this.addInputGui(fieldGui, getConstraintsSameRow(++gridx)); // 2
    gridx += 2;  // 4 (input is width 2, label/ont is width 2
    addListGui(fieldGui,getConstraintsSameRow(gridx-1));
    addButtons(fieldGui,getConstraintsSameRow(gridx)); //put all buttons in same col
  }
  
  /** make row of label & component, used by comparison gui */
  public void addRow(String labelStr, JComponent component) {
    addLabel(labelStr,getConstraintsNewRow());
    addInputGui(component,getConstraintsSameRow(1));
  }

  void addButtons(CharFieldGui fieldGui, GridBagConstraints constraints) {
    //constraints.gridx = 5; - set above and its 4
    this.addPostCompButton(fieldGui, constraints);
    if (!fieldGui.hasListGui()) this.addRetrieveButton(fieldGui, constraints);
    addEditButton(fieldGui, constraints);
    addLoadImageButton(fieldGui, constraints);
  }
  
  /** set up basic grid bag constraints with insets and increments currentGridBagRow/gridy
   */
  GridBagConstraints getConstraintsNewRow() {
    GridBagConstraints baseConstraints = getConstraintsSameRow(0);
    baseConstraints.gridy = ++this.currentGridBagRow;
    //baseConstraints.gridx = 0; // new row
    return baseConstraints;
  }
  
  
  /** set up basic grid bag constraints with insets and uses currentGridBagRow for gridy
      (doesnt increment it) */
  GridBagConstraints getConstraintsSameRow(int gridx) {
    GridBagConstraints baseConstraints = new GridBagConstraints();
    baseConstraints.insets = new Insets(2,3,2,3);
    baseConstraints.gridy = this.currentGridBagRow; // no incrementing
    baseConstraints.gridx = gridx;
    baseConstraints.gridwidth = 1; // default
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
    String tooltip = field.getCharField().getDesc();
    addLabel(field.getLabel(),constraints, tooltip);
  }

  private void addLabel(String label,GridBagConstraints constraints) {
    constraints.gridx = 0;
    //constraints.anchor = GridBagConstraints.EAST;
    fieldPanel.add(new JLabel(label), constraints);
  }

  private void addLabel(String labelText,GridBagConstraints constraints, String tooltip) {
    constraints.gridx = 0;
    //constraints.anchor = GridBagConstraints.EAST;
    JLabel label = new JLabel(labelText);
    if (tooltip != null && !tooltip.equals(""))
      label.setToolTipText(tooltip);
    fieldPanel.add(label, constraints);
  }
  
  private void addOntologyChooser(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    if (fieldGui.hasOntologyChooser()) {
      fieldPanel.add(fieldGui.getOntologyChooser(), constraints);
    }
  }
  
  private void addInputGui(CharFieldGui fieldGui, GridBagConstraints constraints) {
    constraints.gridwidth = inputGuiWidth(fieldGui);
    //if (!fieldGui.hasListGui()) constraints.gridwidth = 2;
    addInputGui(fieldGui.getUserInputGui(), constraints);
    if (shortenInputGui(fieldGui)) {
      hackForAnnoyingGridBagLayout(fieldGui,constraints);
    }
  }

  private void hackForAnnoyingGridBagLayout(CharFieldGui fieldGui,
                                            GridBagConstraints constraints) {
    ++constraints.gridx;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = new Insets(0,0,0,0);
    constraints.weightx = 0.01;
    JLabel l = new JLabel();
    l.setMinimumSize(new Dimension(0,0));
    l.setPreferredSize(new Dimension(0,0));
    fieldPanel.add(l,constraints);
  }

  private int inputGuiWidth(CharFieldGui fieldGui) {
    if (shortenInputGui(fieldGui)) return 1;
    return 2;
  }

  private boolean shortenInputGui(CharFieldGui fieldGui) {
    if (fieldGui.hasListGui()) return true;
    // this needs to be generalized!
    return fieldGui.hasEditButton() && fieldGui.hasRetrieveButton();
  }

  private void addInputGui(JComponent gui, GridBagConstraints constraints) {
    // constraints.gridx = 2; now pre-set in constraints
    constraints.weightx = 10.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    //constraints.anchor = GridBagConstraints.FIRST_LINE_END;
    fieldPanel.add(gui, constraints);
  }
  
  private void addListGui(CharFieldGui fieldGui, GridBagConstraints constraints) {
    if (!fieldGui.hasListGui()) return;
    // LIST - to the right of input gui
    constraints.gridx = 3;
    // width of 2 so it takes up part of input and buttons...
    constraints.gridwidth = 2; // 2?? // 1?
    constraints.gridheight = 2;
    fieldPanel.add(fieldGui.getListGui(),constraints);

    // LIST MESSAGE & DEL BUTTON - put under input gui near list
    constraints.gridy = ++this.currentGridBagRow; //? put in next row
    constraints.gridx = 2;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.anchor = GridBagConstraints.EAST;
    JPanel msgAndDelPanel = new JPanel();
    // list gui wipes out buttons, need to add them here
    if (fieldGui.hasRetrieveButton()) msgAndDelPanel.add(fieldGui.getRetrieveButton());
    msgAndDelPanel.add(fieldGui.getListMessage());
    msgAndDelPanel.add(fieldGui.getListDelButton());
    fieldPanel.add(msgAndDelPanel,constraints);
  }

  private void addPostCompButton(CharFieldGui fieldGui, GridBagConstraints constraints) {
    if (fieldGui.hasCompButton()) {
      fieldPanel.add(fieldGui.getCompButton(), constraints);
    }
  }
  
  private void addRetrieveButton(CharFieldGui fieldGui, GridBagConstraints constraints) {
    if (fieldGui.hasRetrieveButton()) {
      fieldPanel.add(fieldGui.getRetrieveButton(), constraints);
    }
  }

  private void addLoadImageButton(CharFieldGui fieldGui, GridBagConstraints constraints) {
    if (fieldGui.hasLoadImageButton()) {
      fieldPanel.add(fieldGui.getLoadImageButton(), constraints);
    }
  }

  /** edit button brings up big text field popup, may do other things in future? */
  private void addEditButton(CharFieldGui fieldGui, GridBagConstraints constraints) {
    if (!fieldGui.hasEditButton()) return;
    // if have retrieve button then shove edit button in previous column against
    // scrunched input field
    if (fieldGui.hasRetrieveButton())
      --constraints.gridx;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    fieldPanel.add(fieldGui.getEditButton(),constraints);
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

  public void addComponentRow(List<JComponent> comps) {
    JPanel compPanel = new JPanel();
    for (JComponent c : comps)
      compPanel.add(c);
    GridBagConstraints constraints = getConstraintsNewRow();
    constraints.gridwidth = 5;
    constraints.gridx = 1; // scoot it over a little?
    fieldPanel.add(compPanel,constraints);
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
      return this.charFieldManager.getCharFieldListForGroup(this.group);
    } else {
      return this.charFieldManager.getCharFieldList();
    }
  }
  
  public void setListSelectionModel(EventSelectionModel<CharacterI> model) {
    this.selectionModel = model;
    for (CharFieldGui gui : this.getCharFieldGuiList()) {
      gui.setListSelectionModel(this.selectionModel);
    }
  }
  
  public List<CharFieldGui> getCharFieldGuiList() {
    return Collections.unmodifiableList(this.charFieldGuiList);
  }
  
  @SuppressWarnings("unused")
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
