package phenote.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.datamodel.SearchParamsI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
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
  private String label;
  private boolean editModel = true;
  

  CharFieldGui(CharField charField, TermPanel tp) {/*Container parent,*/
    init(charField,tp);
  }

  /** @param editModel - whether charFieldGui edits model directly - for post comp it
      doesnt */
  CharFieldGui(CharField cf,TermPanel tp,String label,boolean editModel) {
    this.label = label;
    this.editModel = editModel;
    init(cf,tp);
  }

  private void init(CharField cf, TermPanel tp) {
    charField = cf;
    termPanel = tp;
    if (!charField.hasOntologies())
      initTextField(charField.getName());//,parent);
    //else if (charField.hasOneOntology())
    else
      initCombo(charField);//,parent);

    // do just for text field - or both??? listens for selection (eg from table)
    SelectionManager.inst().addCharSelectionListener(new FieldCharSelectListener());
    // listen for model changes (eg TermInfo commit)
    
    EditManager.inst().addCharChangeListener(new FieldCharChangeListener());
  }

  private class FieldCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      // check charField is this char field
      if (e.getSource() != CharFieldGui.this && e.isForCharField(charField))
        // i think all we need to do is setText to synch with model
        setText(e.getValueString());
    }
  }

  AutoComboBox getAutoComboBox() {
    if (isCombo)
      return comboBox;
    return null;
  }

  // hasOntology?
  boolean isCombo() { return isCombo; }

  /** Set the gui from the model (selection) */
  void setValueFromChar(CharacterI character) {
    if (character == null) {
      System.out.println("ERROR: setting to null character");
      return;
    }
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


  private String getLabel() {
    if (label == null) {
      if (charField.hasMoreThanOneOntology())
        label = charField.getName();
      else
        label = charField.getFirstOntology().getName();
    }
    return label;
  }

  private void initCombo(CharField charField) { //, Container parent) {
    isCombo = true;

    String name = charField.getFirstOntology().getName();
    JLabel label = termPanel.addLabel(getLabel(),charField.hasMoreThanOneOntology());

    // if has more than one ontology(entity) then add ontology choose list
    if (charField.hasMoreThanOneOntology())
      initOntologyChooser(charField);

    // editModel - if false then ACB wont directly edit model (post comp)
    comboBox = new AutoComboBox(charField.getFirstOntology(),
                                termPanel.getSearchParams(),editModel);
    termPanel.addFieldGui(comboBox);

    comboBox.setCharField(charField);

    // POST COMPOSITION button
    if (charField.postCompAllowed()) {
      JButton postCompButton = new JButton("Comp"); // ???
      postCompButton.addActionListener(new PostCompListener());
      termPanel.addPostCompButton(postCompButton);
      // keep this here for now - may reinstate - this is inpanel post comp
//       // todo -> get pc differentia ontologies from config, >1 add chooser
//       AutoComboBox postCompCombo =
//         new AutoComboBox(charField.getFirstOntology(),termPanel.getSearchParams());
//       // postCompCombo.setVisible(false); // initial state hidden - layout?
//       postCompCombo.setCharField(charField);
//       postCompCombo.setIsDifferentia(true); // differentia of genus in post comp
//       termPanel.addFieldGui(postCompCombo);
    }
  }

  private void initOntologyChooser(CharField charField) {
    ontologyChooserCombo = new JComboBox();
    // add listener....
    for (Ontology o : charField.getOntologyList()) {
      ontologyChooserCombo.addItem(o.getName());
    }
    ontologyChooserCombo.addActionListener(new OntologyChooserListener());
    termPanel.addOntologyChooser(ontologyChooserCombo);
  }

  private void initTextField(String label) {
    isCombo = false;
    termPanel.addLabel(label);
    textField = new JTextField(25);
    textField.setEditable(true);
    // addGenericDocumentListener...
    textField.getDocument().addDocumentListener(new TextFieldDocumentListener());
    termPanel.addFieldGui(textField);

    textField.addKeyListener(new TextKeyListener());

  }

  /** key listener for free text fields */
  private class TextKeyListener extends java.awt.event.KeyAdapter {
    public void keyPressed(java.awt.event.KeyEvent e) {
      // on a mac Command-V is paste. this aint so with java/metal look&feel
      if (e.getKeyChar() == 'v' 
          && e.getKeyModifiersText(e.getModifiers()).equals("Command")) {
        System.out.println("got cmd V paste");
        //System.getClipboard
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
          Transferable t = c.getContents(null); // null?
          Object s = t.getTransferData(DataFlavor.stringFlavor);
          // this isnt quite right as it should just insert the text not wipe
          // it out - but probably sufficient for now?
          if (s != null)
            setText(s.toString());
        } catch (Exception ex) { System.out.println("failed paste "+ex); }
      }
    }
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
  /** This is where the model gets updated (for free text fields) */
  private class TextFieldDocumentListener implements DocumentListener {
    private String previousVal = null;
    public void changedUpdate(DocumentEvent e) { updateModel(); }
    public void insertUpdate(DocumentEvent e) { updateModel(); }
    public void removeUpdate(DocumentEvent e) { updateModel(); }
    private void updateModel() {
      // on delete last pheno row clearing of text will trigger this
      //if (!characterTablePanel.hasRows()) return;
      //String genotype = lumpField.getText();
      //characterTablePanel.setSelectedGenotype(genotype);
      CharacterI c = SelectionManager.inst().getSelectedCharacter();
      // i believe this isnt using oboClass as we just have string
      // of course it isnt this is free text
      String v = getText();
      UpdateTransaction ut = new UpdateTransaction(c,getCharFieldEnum(),v,previousVal);
      EditManager.inst().updateModel(CharFieldGui.this,ut);
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

  /** I think post-comp should only be closeable if its empty (in expand collapse
   inframe case - now window) */
  private class PostCompListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new PostCompGui(charField,termPanel.getSearchParams());
    }
  }

  /** This is for ontology char fields, freetext returns null. returns obo class
      selected in AutoComboBox if there is one */
  OBOClass getSelectedOboClass() throws Exception {
    if (!isCombo) throw new Exception("Free text field has no OBO Class");
    OBOClass term = comboBox.getSelectedCompListOboClass();
    if (term == null) throw new Exception("No term selected");
    return term;
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
