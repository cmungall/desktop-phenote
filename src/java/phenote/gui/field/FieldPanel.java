package phenote.gui.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.text.Document;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.config.FieldConfig;
import phenote.gui.GridBagUtil;

/**
 * TermPanel holds all the fields for the terms - Genotype, Entity/Anatomy, QUALITY.
 * Can be populated by hand (Genotype), or  selection of instance in completion list. 
 rename FieldPanel or CharFieldPanel?
 also has SearchParamPanel.
 */

public class FieldPanel extends JPanel {
  
  private List<CharFieldGui> charFieldGuiList = new ArrayList<CharFieldGui>(8);
  private SearchParamPanel searchParamPanel; // searchParamManager?
  private OntologyManager ontologyManager = OntologyManager.inst();
  private JPanel fieldPanel;
  private SearchParamsI searchParams;

  public FieldPanel() {
    this(true);
    //initAllOntologies();
  }
  // false for post comp panel
  public FieldPanel(boolean doAllOntologies) {
    fieldPanel = new JPanel(new GridBagLayout());
    add(fieldPanel); // default flow layout?
    if (doAllOntologies)
      initAllOntologies();
  }

  /** from selection in plump table - hmmm mvc - get from model change event - 
   should ACB listen for event and change themselves? - oh wait this is for selection
   not editing - no editing in table silly - should do selection event/listener */
  void setFieldsFromCharacter(CharacterI character) {
    for (CharFieldGui fieldGui : charFieldGuiList) 
      fieldGui.setValueFromChar(character);
  }

  private void initAllOntologies() {

    for (CharField charField : ontologyManager.getCharFieldList()) {
      SearchParamsI s = getSearchParamPanel().getSearchParams();
      CharFieldGui gui = new CharFieldGui(charField,this); // adds to panel
      charFieldGuiList.add(gui);
    }

    // search param panel - maybe search panel should be added to main frame?
    add(getSearchParamPanel().getPanel());
  }

  SearchParamsI getSearchParams() {
    if (searchParams == null)
      searchParams = getSearchParamPanel().getSearchParams();
    return searchParams;
  }

  void setSearchParams(SearchParamsI sp) { searchParams = sp; }

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
  
  void addFieldGui(JComponent comp) {
    fieldPanel.add(comp,makeFieldConstraint());
  }

  void addPostCompButton(JButton pc) {
    fieldPanel.add(pc,makePostCompConstraint());
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
    return GridBagUtil.makeWidthConstraint(x,gridbagRow++,1,3,width);
  }

  // put button at end of regular row? or beginning of pc row?
  private GridBagConstraints makePostCompConstraint() {
    return GridBagUtil.makeWidthConstraint(3,gridbagRow-1,1,3,2); // width 1
  }
  

  // for test to listen - move to test code?
  public AbstractAutoCompList getEntityComboBox() {
    return getComboBox(CharFieldEnum.ENTITY);
  }
  
  // for testing - move to test?
  private AbstractAutoCompList getComboBox(CharFieldEnum cfe) {
    for (CharFieldGui cfg : charFieldGuiList)
      if (cfg.getCharFieldEnum() == cfe)
        return cfg.getCompList();
    return null;
  }

  public AbstractAutoCompList getQualityComboBox() {
    return getComboBox(CharFieldEnum.QUALITY);
  }
  boolean hasLumpComboBox() {
    //return lumpField.isCombo();
    return getLumpComboBox() != null;
  }
  AbstractAutoCompList getLumpComboBox() {
    return getComboBox(CharFieldEnum.LUMP);
  }

}


// GARBAGE - DELETE

    // load up terms for term completion
    /// Ontology.init(); // hmmmm.... now initializes with get - thread!
    //return entityField.getAutoComboBox();
// dont think we need this at least for sandbox, if we do jut iter fields
//   void clear() { 
//     lumpField.setText("");
//     entityField.setText("");
//     qualityField.setText("");
//   }

  // phase out direct connection for event/listener - obo edit stuff?
  //private CharacterTablePanel characterTablePanel;

//     characterTablePanel.setSelectedEntityTerm(entityTerm);
//   private boolean isEntity(String ontology) {
//     return ontology.equals(ANATOMY); // || GO...
//   }
//   private boolean isQuality(String ontology) {
//     return ontology.equals(QUALITY);
//   }

//   // this should be made generic to use beyond just genotype? yes yes yes
//   // and put in CharFieldGui...
//   private class GenotypeDocumentListener implements DocumentListener {
//     public void changedUpdate(DocumentEvent e) { updateInstanceTable(); }
//     public void insertUpdate(DocumentEvent e) { updateInstanceTable(); }
//     public void removeUpdate(DocumentEvent e) { updateInstanceTable(); }
//     private void updateInstanceTable() {
//       // on delete last pheno row clearing of text will trigger this
//       if (!characterTablePanel.hasRows())
//         return;
//       String genotype = lumpField.getText();
//       characterTablePanel.setSelectedGenotype(genotype);
//     }
//   }
//   // used by ComboBoxActionListener to set table panel - rename this
//   // do this with event? yes event/listener... yes yes yes
//   void setEntityTableColumn(String entityTerm) {
//       //entityField.setText(entityTerm);
//     characterTablePanel.setSelectedEntityTerm(entityTerm);
//   }

//   // change -> mvc
//   void setQualityTableColumn(String qualityTerm) {
//     // characterTablePanel.setSelectedColumn(ontEnum,term)???
//     characterTablePanel.setSelectedQualityTerm(qualityTerm);
//   }

//   /** rename - this sets term in table not field - hardwires from ontology
//       to methods in table - have mapping enum to table column in table? 
//       this aint right - use MVC - edit character model & send out modelChanged event 
//       need model changed manager, listener event...  REFACTOR!!!*/
//   private void setTableColumn(String ontology, String term) {
//     if (isEntity(ontology))
//       setEntityTableColumn(term);
//     else if (isQuality(ontology))
//       setQualityTableColumn(term);
//     else if (ontology.equals(TAXONOMY))
//       characterTablePanel.setSelectedGenotype(term);
//     //...
//   // used by ComboBoxActionListener to set table panel - rename this
//   // do this with event? yes event/listener... yes yes yes
//   void setEntityTableColumn(String entityTerm) {
//       //entityField.setText(entityTerm);
//     characterTablePanel.setSelectedEntityTerm(entityTerm);
//   }

//   // change -> mvc
//   void setQualityTableColumn(String qualityTerm) {
//     // characterTablePanel.setSelectedColumn(ontEnum,term)???
//     characterTablePanel.setSelectedQualityTerm(qualityTerm);
//   }

//   /** rename - this sets term in table not field - hardwires from ontology
//       to methods in table - have mapping enum to table column in table? 
//       this aint right - use MVC - edit character model & send out modelChanged event 
//       need model changed manager, listener event...  REFACTOR!!!*/
//   private void setTableColumn(String ontology, String term) {
//     if (isEntity(ontology))
//       setEntityTableColumn(term);
//     else if (isQuality(ontology))
//       setQualityTableColumn(term);
//     else if (ontology.equals(TAXONOMY))
//       characterTablePanel.setSelectedGenotype(term);
//     //...
//   }
//   }
//   private void addGeneticContextField(JPanel parent) {
//     if (!ontologyManager.hasGeneticContext())
//       return; // for now assume genetic context has to have ontology/combo
//     //Ontology gco = ontologyManager.getGeneticContextOntology();
//     //geneticContextField = new CharFieldGui(gco,parent);
//     // eventually will just loop through CharFields
//     CharField cf = ontologyManager.getGeneticContextCharField();
//     geneticContextField = new CharFieldGui(cf,parent);
//   }

//   private void addField(FieldConfig fieldConfig, JComponent parent) {
//     //List<Ontology> ol = ontologyManager.getOntologyList(charFieldEnum);
//   }
  //private void addField(FieldOntology fo, JComponent parent) {
  // List<Ontology> ol = fo.getOntologyList();
  // String label = fo.getLabel();
  // new CharFieldGui(fo,parent);??  }
//   private void addLumpField(JPanel parent) {
//     if (ontologyManager.haveLumpOntology()) {
//       //Ontology lumpOntology = getLumpOntology();
//       Ontology lumpOntology = ontologyManager.getLumpOntology();
//       lumpField = new CharFieldGui(lumpOntology,parent);
//     }
//     else {
//       //lumpField = addField("Genotype",parent,true); 
//       lumpField = new CharFieldGui("Genotype",parent);
//       GenotypeDocumentListener gdl = new GenotypeDocumentListener();
//       lumpField.addDocumentListener(gdl);
//     }
//   }

//   private boolean haveLumpOntology() {
//     return ontologyManager.haveLumpOntology();
//   }

//   private Ontology getLumpOntology() {
//     //return new Ontology(TAXONOMY,"BTO.obo");
//     return ontologyManager.getLumpOntology();
//   }

//   /** hardwired for now - eventually come generically from configuration... */
//   private Ontology getAnatomyOntology() {
//     // this causes parsing of obo file - background thread?
//     return ontologyManager.getEntityOntology();
//   }

//   private Ontology getQualityOntology() {
//     //return new Ontology("Quality","attribute_and_value.obo");
//     return ontologyManager.getQualityOntology();
//   }

//     lumpField.setText(instance.getGenotype());
//     entityField.setText(instance.getEntity());
//     qualityField.setText(instance.getQuality()); ????????
//     addLumpField(fieldPanel);
//     addGeneticContextField(fieldPanel); // if configured...
//     entityField = new CharFieldGui(getAnatomyOntology(),fieldPanel);
//     //qualityField = new CharFieldGui(getQualityOntology(),fieldPanel);
//     qualityField = new CharFieldGui(,fieldPanel);
    // for (FieldConfig fieldConfig : config.getFieldConfigList() )
    // addField(fieldConfig,fieldPanel); but field configs dont have ontologies...
    // would then have to do something like ontMan.getOntology(fc.getFieldEnum)
    // for (FieldOntology fieldOntology : ontologyManager.getFieldOntologyList())
    // addField(fieldOntology,fieldPanel);
//   final static String ANATOMY = "Anatomy";
//   final static String QUALITY = "Quality";
//   final static String TAXONOMY = "Taxonomy";


//   private CharFieldGui lumpField;
//   private CharFieldGui entityField;
//   private CharFieldGui qualityField; 
//   private CharFieldGui geneticContextField;
//   private String getInput(String ontology) {
//     if (isEntity(ontology))
//       return entityField.getText();
//     if (isQuality(ontology))
//       return qualityField.getText();
//     return ""; // error?
//   }
  // take out editable - always editable
//   private JTextField addField(String labelString,JPanel parent,boolean editable) {
    
//     addLabel(labelString,parent);
//     JTextField textField = new JTextField(25);
//     // parameterize - eventually allow edit for new ont terms
//     textField.setEditable(editable); 
//     GridBagConstraints gbc = makeFieldConstraint();
//     parent.add(textField,gbc);
    
//     return textField;
//   }

//   private AutoComboBox addComboBox(Ontology ontology, Container parent) {
//     addLabel(ontology.getName(),parent);
//     AutoComboBox comboBox = new AutoComboBox(ontology);
//     comboBox.setSearchParams(getSearchParamPanel().getSearchParams());
//     // refactor... mvc - ACB talk directly to pheno model?
//     comboBox.addActionListener(new ComboBoxActionListener(ontology.getName(),comboBox));
//     parent.add(comboBox,makeFieldConstraint());
//     return comboBox;
//   }

//   private class AutoActionListener implements ActionListener {
//     private String ontology;
//     private JComboBox comboBox;
//     private AutoActionListener(String o,JComboBox jcb) {
//       ontology = o;
//       comboBox = jcb;
//     }
//     public void actionPerformed(ActionEvent e) {
//       doCompletion();
//     }
//     private void doCompletion() {
//       String input = (String)comboBox.getEditor().getItem();
//       CompletionList cl = CompletionList.getCompletionList();
//       Vector v = cl.getCompletionTerms(ontology,input);
//       System.out.println(input + v);
//       comboBox.setModel(new DefaultComboBoxModel(v));
//     }
//   }
  // delete...
//   private class AutoDocumentListener implements DocumentListener {
//     private String ontology;
//       private AutoDocumentListener(String o) { this.ontology = o; }
//       public void changedUpdate(DocumentEvent e) { doCompletion(e); }
//     public void insertUpdate(DocumentEvent e) { doCompletion(e); }
//     public void removeUpdate(DocumentEvent e) { doCompletion(e); }
//       private void doCompletion(DocumentEvent e) {
// 	  Document d = e.getDocument();
// 	  try {
// 	      searchPanel.redoCompletionList(ontology,e.getDocument().getText(0,d.getLength()));
// 	  }
// 	  catch (javax.swing.text.BadLocationException ex) {
// 	      System.out.println("bad location");
// 	  }
//       }
//   }
  // HasText interface? to cover JTextField as well?
  //  private String getUserInput(String ontology) {
    //if (ontology.equals(ANATOMY))
    //return (String)entityField.getEditor().getItem(); //}
//   void setSearchPanel(SearchPanel sp) { 
//     searchPanel = sp;
//     // overlay?? home made jcombo - jcombo mac bug - list on top of jtext
//     // mac bug worked around thank goodness
//     //searchPanel.setLocation(qualityTextField.getLocation());
//   }
    //comboBox.setEditable(true);
    //ActionListener a = new AutoActionListener(ontologyString,comboBox); 
    //comboBox.getEditor().addActionListener(a);
    //comboBox.addActionListener(a);
    //entityField.getDocument().addDocumentListener(new AutoDocumentListener(ANATOMY));
    //qualityTextField = addField("Quality Term",this,true);
    //qualityTextField.getDocument().addDocumentListener(new AutoDocumentListener(QUALITY));
