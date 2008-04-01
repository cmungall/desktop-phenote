package phenote.gui.field;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;


/** couldnt this just be a FreeTextField with editable set to false? yes!
// or vice versa
this is really read only free text field
separate class for read only term/rel field???
hmmmmmm....
this is backwards - - free text field should probably sublcass read only
actually this should just be a parameter to free text field - refactor!
also would there ever be a case of having a term read only?
ive put drop recipient for character data in here for now for comparison gui
see CharDropHandler inner class
if other subclasses need this, this can migrate to superclass
*/

public class ReadOnlyFieldGui extends FreeTextField { // CharFieldGui

  /** for now - refactor */
  //private CharacterI character;

  public ReadOnlyFieldGui(CharField charfield) {
    super(charfield);
//     this.getReadOnlyLabel().setPreferredSize(new Dimension(200,20));
//     this.getReadOnlyLabel().setBorder(new javax.swing.border.LineBorder(java.awt.Color.BLACK) );
    getTextField().setEditable(false);
  }
  protected void updateModel() {}
  private Logger log;
  protected Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}

// GARBAGE

//   /** enables to listen to character drag & drop - for comparisons
//       may want to migrate this to super class if needed for other things?? */
//   public void enableCharDropListening(boolean enable) {
//     getTextField().setTransferHandler(new CharDropHandler());
//   }


  /** hmmmmm - not sure about this - used for comparison gui where jamming a character
      into a field */
//   public void setCharacter(CharacterI c) {
//     character = c;
//     setText(charString(c));
//   }
//   /** should their be a separate subclass for CharacterGui? */
//   public CharacterI getCharacter() {
//     return character;
//   }

  // util fn?

//   protected void setValueFromChar(CharacterI chr) {
//     if (chr == null) {
//       log().error("ERROR: attempt to set fields from null character"); // ex?
//       return;
//     }
//     if (!chr.hasValue(getCharField())) {
//       setText(""); // ?? null?
//       return;
//     }
//     String v = chr.getValue(getCharField()).getName();
//     setText(v);
//   }

//   protected void setCharFieldValue(CharFieldValue value) {
//     this.setText(value.getName());
//   }
  
//   @Override
//   protected CharFieldValue getCharFieldValue() {
//     try {
//       return this.getCharField().makeValue(null, this.getText());
//     } catch (CharFieldException e) {
//       log().error("Couldn't create charfieldvalue", e);
//     }
//     return CharFieldValue.emptyValue(null, this.getCharField());
//   }

  
//   protected boolean hasFocus() {
//     return this.readOnlyLabel.hasFocus();
//   }


//   protected void setText(String s) { 
//     readOnlyLabel.setText(s); 
//     readOnlyLabel.repaint(); }
//   protected String getText() { return readOnlyLabel.getText(); }

  
//   protected JComponent getUserInputGui() {
//     return this.getReadOnlyLabel();
//   }
  
//   private JLabel getReadOnlyLabel() {
//     if (this.readOnlyLabel == null) {
//       this.readOnlyLabel = new JLabel();
//     }
//     return this.readOnlyLabel;
//   }

