package phenote.gui.field;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;

import phenote.config.Config;

/** couldnt this just be a FreeTextField with editable set to false?
// or vice versa
this is really read only free text field
separate class for read only term/rel field???
hmmmmmm....
this is backwards - - free text field should probably sublcass read only
actually this should just be a parameter to free text field - refactor!
also would there ever be a case of having a term read only?
*/

public class ReadOnlyFieldGui extends FreeTextField { // CharFieldGui
  private JLabel readOnlyLabel = new JLabel(); // uneditable JText?

  public ReadOnlyFieldGui(CharField charfield) {
    super(charfield);
//     this.getReadOnlyLabel().setPreferredSize(new Dimension(200,20));
//     this.getReadOnlyLabel().setBorder(new javax.swing.border.LineBorder(java.awt.Color.BLACK) );
    getTextField().setEditable(false);
  }

  /** enables to listen to character drag & drop - for comparisons
      may want to migrate this to super class if needed for other things?? */
  public void enableCharDropListening(boolean enable) {
    getTextField().setTransferHandler(new CharDropHandler());
  }

  /** gives thumbs up to CHAR_FLAVOR data - character drag & drop from table
      and imports it on dropping */
  private class CharDropHandler extends TransferHandler {
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
      for (DataFlavor f : flavors)
        if (f.equals(CharacterI.CHAR_FLAVOR)) return true;
      return false;
    }
    public boolean importData(JComponent comp, Transferable t) {
      if (!canImport(comp,t.getTransferDataFlavors())) return false;
      try {
        Object o = t.getTransferData(CharacterI.CHAR_FLAVOR);
        if (!(o instanceof CharacterI)) return false;
        setCharacter((CharacterI) o);
        return true;
      }
      catch (UnsupportedFlavorException e) { return false; }
      catch (java.io.IOException e) { return false; }
    }
  }

  /** hmmmmm - not sure about this */
  public void setCharacter(CharacterI c) {
    setText(charString(c));
  }
  // util fn?
  public static String charString(CharacterI c) {
    if (c == null) return "";
    StringBuffer sb = new StringBuffer();
    for (CharField cf : c.getAllCharFields()) {
      if (!Config.inst().isVisible(cf)) continue;
      String val = c.getValueString(cf);
      if (val == null || val.trim().equals("")) continue;
      sb.append(val).append(" ");
    }
    return sb.toString().trim();
  }

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

  protected void updateModel() {}
  
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

  private Logger log;
  protected Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
