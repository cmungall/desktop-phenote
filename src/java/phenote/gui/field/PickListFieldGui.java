package phenote.gui.field;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;
import phenote.edit.UpdateTransaction;
import phenote.util.EverythingEqualComparator;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

public class PickListFieldGui extends CharFieldGui {
  
  private JPanel userInputGui;
  private JTextField textField;
  private CharFieldValue currentValue;

  public PickListFieldGui(CharField charField) {
    super(charField);
    this.initializeGui();
  }

  @Override
  protected JComponent getUserInputGui() {
    return this.userInputGui;
  }

  @Override
  protected boolean hasFocus() {
    return this.textField.hasFocus();
  }

  @Override
  protected void setCharFieldValue(CharFieldValue value) {
    this.currentValue = value;
    final List<CharFieldValue> values = value.getCharFieldValueList();
    if (values == null) return;
    final StringBuffer sb = new StringBuffer();
    boolean removeTrailingSeparator = false;
    for (CharFieldValue item : values) {
      sb.append(item.getValueAsString());
      sb.append("; ");
      removeTrailingSeparator = true;
    }
    if (removeTrailingSeparator) sb.delete(sb.length() - 2, sb.length());
    textField.setText(sb.toString());
  }
  
  protected CharFieldValue getCharFieldValue() {
    return this.currentValue.cloneCharFieldValue(null, this.currentValue.getCharField());
  }
  
  @Override
  protected String getText() {
    return null;
  }

  @Override
  protected void setText(String text) {}

  @Override
  protected void updateModel() {
    // TODO Auto-generated method stub
  }
  
  private void updateModel(List<CharFieldValue> chosen) {
    if (this.getSelectedChars().size() != 1) return;
    final CharFieldValue newValue = this.currentValue.cloneCharFieldValue(null, this.getCharField());
    newValue.removeAllKids();
    for (CharFieldValue item : chosen) {
      final CharFieldValue clonedValue = item.cloneCharFieldValue(null, null);
      clonedValue.setIsList(false);
      clonedValue.setOverridePickList(true);
      newValue.addKid(clonedValue);
    }
    UpdateTransaction ut = new UpdateTransaction(this.currentValue, newValue);
    this.getEditManager().updateModel(this, ut);
  }
  
  private List<Map<String, Object>> getChoicesList() {
    final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    if (this.currentValue == null) return list;
    for (CharFieldValue value : this.currentValue.getValuePickList()) {
      final Map<String, Object> map = new HashMap<String, Object>();
      map.put("value", value);
      map.put("chosen", this.currentValue.getCharFieldValueList().contains(value));
      list.add(map);
    }
    return list;
  }
  
  private void showListDialog() {
    final JDialog dialog = this.getDialog();
    dialog.setVisible(true);
  }
  
  private void endListDialog(JDialog dialog, boolean okay, List<Map<String, Object>> choicesList) {
    dialog.dispose();
    if (okay) {
      log().debug(choicesList);
      this.updateModel(this.getChosen(choicesList));
    }
  }
  
  private List<CharFieldValue> getChosen(List<Map<String, Object>> choicesList) {
    final List<CharFieldValue> list = new ArrayList<CharFieldValue>();
    for (Map<String, Object> item : choicesList) {
      if (item.get("chosen").equals(true)) {
        list.add((CharFieldValue)(item.get("value")));
      }
    }
    return list;
  }
  
  @SuppressWarnings("serial")
  private void initializeGui() {
    this.userInputGui = new JPanel(new BorderLayout());
    this.textField = new JTextField();
    this.textField.setEditable(false);
    this.userInputGui.add(this.textField, BorderLayout.CENTER);
    this.userInputGui.add(new JButton(new AbstractAction("Edit") {
      public void actionPerformed(ActionEvent e) { showListDialog(); }
    }), BorderLayout.EAST);
  }
  
  private JFrame getFrame(Component c) {
    return (c.getParent() instanceof JFrame) ? (JFrame)(c.getParent()) : this.getFrame(c.getParent());
  }
  
  @SuppressWarnings("serial")
  private JDialog getDialog() {
    final EventList<Map<String, Object>> choicesList = GlazedLists.eventList(this.getChoicesList());
    
    final JDialog dialog = new JDialog(this.getFrame(this.getUserInputGui()), true);
    dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    dialog.setLayout(new BorderLayout());
    dialog.setSize(400, 600);
    
    final EventTableModel<Map<String, Object>> model = new EventTableModel<Map<String, Object>>(GlazedLists.eventList(choicesList), new PickTableFormat());
    final JTable table = new JTable(model);
    new TableComparatorChooser<Map<String, Object>>(table, new SortedList<Map<String, Object>>(choicesList, new EverythingEqualComparator<Map<String, Object>>()), false);
    final JScrollPane scrollPane = new JScrollPane(table);
    dialog.add(scrollPane, BorderLayout.CENTER);
    
    final JPanel buttonPanel = new JPanel();
    buttonPanel.add(new JButton(new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) { endListDialog(dialog, false, choicesList); }
    }));
    buttonPanel.add(new JButton(new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) { endListDialog(dialog, true, choicesList); }
      }));
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    return dialog;
  }
  
  private static class PickTableFormat implements AdvancedTableFormat<Map<String, Object>>, WritableTableFormat<Map<String, Object>> {

    public Class<?> getColumnClass(int column) {
      if (column == 0) {
        return Boolean.class;
      } else {
        return CharFieldValue.class;
      }
    }

    public Comparator<?> getColumnComparator(int column) {
      if (column == 0) {
        return GlazedLists.booleanComparator();
      } else {
        return new Comparator<CharFieldValue>() {
          public int compare(CharFieldValue o1, CharFieldValue o2) {
            return o1.getValueAsString().compareToIgnoreCase(o2.getValueAsString());
          }};
      }
    }
     
    public int getColumnCount() {
      return 2;
    }

    public String getColumnName(int column) {
      if (column == 0) {
        return "Selected";
      } else {
        return "";
      }
    }

    public Object getColumnValue(Map<String, Object> baseObject, int column) {
      if (column == 0) {
        return baseObject.get("chosen");
      } else {
        return baseObject.get("value");
      }
    }

    public boolean isEditable(Map<String, Object> baseObject, int column) {
      return column == 0;
    }

    public Map<String, Object> setColumnValue(Map<String, Object> baseObject, Object editedValue, int column) {
      baseObject.put("chosen", editedValue);
      return baseObject;
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
