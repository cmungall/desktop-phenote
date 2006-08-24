package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import phenote.datamodel.CharField;
import phenote.datamodel.CharField.CharFieldEnum;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.datamodel.SearchParamsI;
import phenote.edit.EditManager;
import phenote.edit.UpdateTransaction;
import phenote.gui.selection.CharSelectionListener;
import phenote.gui.selection.CharSelectionEvent;
import phenote.gui.selection.SelectionManager;

/** fields can either be text fields for free text or combo boxes if have 
    ontology to browse - CharFieldGui does either - with get/setText - hides the
    details of the gui - just a field that gives text */
class CharFieldGui {
  private AutoComboBox comboBox;
  private JTextField textField;
  private boolean isCombo = false;
  private CharField charField;
  private TermPanel termPanel;
  private JComboBox ontologyChooserCombo;


  CharFieldGui(CharField charField,Container parent, TermPanel tp) {
    this.charField = charField;
    termPanel = tp;
    if (!charField.hasOntologies())
      initTextField(charField.getName(),parent);
    //else if (charField.hasOneOntology())
    else
      initCombo(charField,parent);

    // do just for text field - or both???
    SelectionManager.inst().addCharSelectionListener(new FieldCharSelectListener());
  }

  AutoComboBox getAutoComboBox() {
    if (isCombo)
      return comboBox;
    return null;
  }

  // hasOntology?
  boolean isCombo() { return isCombo; }

  void setValueFromChar(CharacterI character) {
    if (charField == null) return;
    if (charField.getCharFieldEnum() == null) {
      System.out.println("ERROR: Cant set value for field. Gui for character field has"
                         +" not been associated with a datamodel field. check field"
                         +" names in config ");
      return;
    }
    String v = charField.getCharFieldEnum().getValue(character).getName();
    setText(v);
  }




  private void initCombo(CharField charField, Container parent) {
    isCombo = true;

    String name = charField.getFirstOntology().getName();
    JLabel label = termPanel.addLabel(name,parent);
    // if has more than one ontology(entity) than add ontology choose list
    if (!charField.hasOneOntology()) {
      label.setText(charField.getName());
      initOntologyChooser(charField,parent);
    }

    // assume 1 ontology for now...
    //initCombo(charField.getFirstOntology(),parent);
    // CHANGE THIS TO DO MULTIPLE ONTOLOGIES IF NEED BE
    comboBox = new AutoComboBox(charField.getFirstOntology(),termPanel.getSearchParams());
    termPanel.addFieldGui(comboBox,parent);
    comboBox.setCharField(charField);
  }

  private void initOntologyChooser(CharField charField,Container parent) {
    ontologyChooserCombo = new JComboBox();
    // add listener....
    for (Ontology o : charField.getOntologyList()) {
      ontologyChooserCombo.addItem(o.getName());
    }
    ontologyChooserCombo.addActionListener(new OntologyChooserListener());
    termPanel.addOntologyChooser(ontologyChooserCombo,parent);
  }

  private void initTextField(String label,Container parent) {
    isCombo = false;
    termPanel.addLabel(label,parent);
    textField = new JTextField(25);
    textField.setEditable(true);
    // addGenericDocumentListener...
    textField.getDocument().addDocumentListener(new TextFieldDocumentListener());
    termPanel.addFieldGui(textField,parent);
  }


  void setText(String text) {
    // set/getText interface to combo & text field?
    if (isCombo) comboBox.setText(text);
    else textField.setText(text);
  }
  String getText() {
    if (isCombo) return comboBox.getText();
    else return textField.getText();
  }

  CharFieldEnum getCharFieldEnum() { return charField.getCharFieldEnum(); }

  // separate char text field class?
  // this should be made generic to use beyond just genotype? yes yes yes
  // and put in CharFieldGui...
  private class TextFieldDocumentListener implements DocumentListener {
    private String previousVal = null;
    public void changedUpdate(DocumentEvent e) { updateInstanceTable(); }
    public void insertUpdate(DocumentEvent e) { updateInstanceTable(); }
    public void removeUpdate(DocumentEvent e) { updateInstanceTable(); }
    private void updateInstanceTable() {
      // on delete last pheno row clearing of text will trigger this
      //if (!characterTablePanel.hasRows()) return;
      //String genotype = lumpField.getText();
      //characterTablePanel.setSelectedGenotype(genotype);
      CharacterI c = SelectionManager.inst().getSelectedCharacter();
      String v = getText();
      UpdateTransaction ut = new UpdateTransaction(c,getCharFieldEnum(),v,previousVal);
      EditManager.inst().updateModel(this,ut);
      previousVal = v; // undo
    }
  }

  private class FieldCharSelectListener implements CharSelectionListener {
    public void characterSelected(CharSelectionEvent e) {
      setValueFromChar(e.getCharacter());
    }
  }

  private class OntologyChooserListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String s = ontologyChooserCombo.getSelectedItem().toString();
      Ontology o = OntologyManager.getOntologyForName(s);
      comboBox.setOntology(o);
    }
  }

}

//   private void addDocumentListener(DocumentListener dl) {
//     if (!isCombo)
//       textField.getDocument().addDocumentListener(dl);
//   }

//   private void initCombo(Ontology ontology, Container parent) {
//     // attach search params to ontology?
//     comboBox = new AutoComboBox(ontology,termPanel.getSearchParams());
//     // refactor... mvc - ACB talk directly to pheno model?
//     //comboBox.addActionListener(new ComboBoxActionListener(ontology.getName(),comboBox));
//     //parent.add(comboBox,makeFieldConstraint());
//     termPanel.addFieldGui(comboBox,parent);
//   }

//   /** No ontology -> free text field */
//   private CharFieldGui(String label,Container parent) {
//     initTextField(label,parent);
//   }
//   /** Fields with ontology -> combo box */
//   private CharFieldGui(Ontology ontology, Container parent) {
//     if (ontology == null) // shouldnt happen
//       initTextField(null,parent); 
//     else
//       initCombo(ontology,parent);
//   }

//   /** Listens for actions from combo boxes and puts terms into table 
//    * actions come from mouse select of term as well as return & tab 
//    change this - should only modify character - could be done in ACB except
//    gt text field which isnt done here anyways - then send out CharacterChangeEvent
//   for table to get and refresh itself 
//   this is being phased out for AutoComboBox's more generic ComboBoxActionListener*/
//   private class ComboBoxActionListener implements ActionListener {
//     private String ontology;
//     private AutoComboBox comboBox;

//     private ComboBoxActionListener(String ontology,AutoComboBox cb) {
//       this.ontology = ontology;
//       comboBox = cb;
//     }
//     public void actionPerformed(ActionEvent e) {
//       setTableFromField(ontology);
//     }

//     /** Sets table value from field. checks that text in text field from user
//         is actually an item in completion list, is an obo term. */
//     private void setTableFromField(String ontology) {
//       String input = comboBox.getText();//getInput(ontology);
      
//       // check if input is a real term - i think we can get away with checking
//       // if in present term completion list - not sure
//       boolean valid = comboBox.isInCompletionList(input);
//       if (!valid)
//         return;
//       // its valid - set the field
//       // no no no - edit model, send out model changed event
//       setTableColumn(ontology,input);
//     }
//   }
