package phenote.gui.field;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Dimension;
import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import org.apache.log4j.Logger;


class ReadOnlyFieldGui extends CharFieldGui {
  private JLabel readOnlyLabel = new JLabel();

  ReadOnlyFieldGui(CharField charfield) {
    super(charfield);
    readOnlyLabel.setPreferredSize(new Dimension(200,20));
    readOnlyLabel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.BLACK) );
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



  protected void setText(String s) { 
    readOnlyLabel.setText(s); 
    readOnlyLabel.repaint(); }
  protected String getText() { return readOnlyLabel.getText(); }

  protected void setGuiForMultiSelect() { }
  protected Component getUserInputGui() { return readOnlyLabel; }

  private Logger log;
  protected Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
