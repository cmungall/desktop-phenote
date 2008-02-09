package phenote.gui.field;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;

/** couldnt this just be a FreeTextField with editable set to false?
// or vice versa
this is really read only free text field
separate class for read only term/rel field???
hmmmmmm....
*/

class ReadOnlyFieldGui extends CharFieldGui {
  private JLabel readOnlyLabel = new JLabel(); // uneditable JText?

  ReadOnlyFieldGui(CharField charfield) {
    super(charfield);
    this.getReadOnlyLabel().setPreferredSize(new Dimension(200,20));
    this.getReadOnlyLabel().setBorder(new javax.swing.border.LineBorder(java.awt.Color.BLACK) );
  }


  protected void setValueFromChar(CharacterI chr) {
    if (chr == null) {
      log().error("ERROR: attempt to set fields from null character"); // ex?
      return;
    }
    if (!chr.hasValue(getCharField())) {
      setText(""); // ?? null?
      return;
    }
    String v = chr.getValue(getCharField()).getName();
    setText(v);
  }

  protected void setCharFieldValue(CharFieldValue value) {
    this.setText(value.getName());
  }
  
  protected void updateModel() {}
  
  protected boolean hasFocus() {
    return this.readOnlyLabel.hasFocus();
  }


  protected void setText(String s) { 
    readOnlyLabel.setText(s); 
    readOnlyLabel.repaint(); }
  protected String getText() { return readOnlyLabel.getText(); }

  
  protected JComponent getUserInputGui() {
    return this.getReadOnlyLabel();
  }
  
  private JLabel getReadOnlyLabel() {
    if (this.readOnlyLabel == null) {
      this.readOnlyLabel = new JLabel();
    }
    return this.readOnlyLabel;
  }

  private Logger log;
  protected Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
