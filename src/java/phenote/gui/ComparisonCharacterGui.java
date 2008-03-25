package phenote.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;

import phenote.config.Config;

/** a gui box for characters - cant type in it but can drop characters from main table
    in it. this is for subject & object characters of comparison gui */

class ComparisonCharacterGui {
  
  private JTextField textField = new JTextField(25); // 40 columns
  private String name;
  private CharacterI character;
  private CharDropObservable observable;

  ComparisonCharacterGui(String name) {
    this.name = name;
    textField.setEditable(false);
    textField.setTransferHandler(new CharDropHandler());
  }
  
  JComponent getComponent() { return textField; }

  CharacterI getCharacter() { return character; }
  void setCharacter(CharacterI c) {
    character = c;
    textField.setText(charString(c));
    getObservable().setChanged();
    getObservable().notifyObservers(); // notify CompGui controller
  }


  /** gives thumbs up to CHAR_FLAVOR data - character drag & drop from table
      and imports it on dropping
      this is actually controller stuff that could be put in separate class? */
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

  void addObserver(Observer o) { getObservable().addObserver(o); }

  /** Send out notification when a new char has been dropped, used by ComparisonGui
      which then updates comparison model */
  public CharDropObservable getObservable() {
    if (observable==null) observable = new CharDropObservable();
    return observable;
  }

  /** just need accesible setChanged method */
  private class CharDropObservable extends Observable {
    protected void setChanged() { super.setChanged(); }
  }



  public static String charString(CharacterI c) {
    if (c == null) return "";
    // if has annot id use that (???)
    if (c.hasAnnotId())
      return c.getAnnotId();
    // otherwise string together char fields
    StringBuffer sb = new StringBuffer();
    for (CharField cf : c.getAllCharFields()) {
      if (!Config.inst().isVisible(cf)) continue;
      String val = c.getValueString(cf);
      if (val == null || val.trim().equals("")) continue;
      sb.append(val).append(" ");
    }
    return sb.toString().trim();
  }


}
