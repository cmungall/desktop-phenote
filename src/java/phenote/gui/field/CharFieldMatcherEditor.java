package phenote.gui.field;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.swixml.SwingEngine;

import phenote.config.xml.FieldDocument.Field.Type;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.selection.SelectionManager;
import phenote.util.FileUtil;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

/**
 * Provides a search panel for filtering character lists by charfield values.
 * @author Jim Balhoff
 */
public class CharFieldMatcherEditor extends AbstractMatcherEditor<CharacterI> {
  
  private JPanel filterContainer; // initialized by swix
  private JComboBox charFieldPopup; // initialized by swix
  private JPanel inputFieldContainer; // initialized by swix
  private JRadioButton exactModeRadioButton;
  private JRadioButton broadModeRadioButton;
  private TermCompList ontologyInputField;
  private JTextField textInputField;
  private List<CharField> charFields;
  private CharField editedCharField;
  private Mode filterMode;
  private String filter;
  protected static CharField ANY_FIELD = new CharField("Simple Filter", "", Type.FREE_TEXT);
  private boolean ignoreActions = false;
  
  
  /**
   * Creates a new CharFieldMatcherEditor which will filter characters composed of the CharFields in charFields.
   */
  public CharFieldMatcherEditor(List<CharField> charFields) {
    if (charFields.size() < 1) log().error("Filter field initialized with no charfields");
    this.loadPanelLayout();
    this.editedCharField = CharFieldMatcherEditor.ANY_FIELD;
    this.filterMode = Mode.BROAD;
    this.ontologyInputField = (TermCompList)(CharFieldGui.makePostCompTermList(this.editedCharField, "", 0));
    this.ontologyInputField.setSelectionManager(new SelectionManager()); // make sure the TermCompList doesn't update the term info panel
    this.ontologyInputField.getJComboBox().setToolTipText(this.filterContainer.getToolTipText());
    this.ontologyInputField.getJComboBox().addActionListener(new FilterActionListener());
    this.textInputField = new JTextField();
    this.textInputField.setToolTipText(this.filterContainer.getToolTipText());
    this.textInputField.getDocument().addDocumentListener(new FilterDocumentListener());
    this.charFieldPopup.setRenderer(new CharFieldRenderer());
    this.setCharFields(charFields);
  }
  
  /**
   * Sets the charfields which the user can choose from for filtering characters.
   */
  public void setCharFields(List<CharField> charFields) {
    this.charFields = new ArrayList<CharField>(charFields);
    this.charFields.add(0, CharFieldMatcherEditor.ANY_FIELD);
    this.setEditedCharField(this.charFields.get(0));
    this.updateGUI();
  }
  
  /**
   * Sets the charfield used to filter the characters
   */
  public void setEditedCharField(CharField charField) {
    this.editedCharField = charField;
    this.updateGUI();
    this.fireChange();
  }
  
  /**
   * Sets the edited charfield according to the currently selected charfield in interface.
   * Called by charfield popup button.
   */
  public void updateEditedCharField() {
    if (this.ignoreActions) return;
    this.setEditedCharField((CharField)(this.charFieldPopup.getSelectedItem()));
  }
  
  /**
   * Sets the filter value according to the current input in interface.
   * Called by the free text or term filter input field.
   */
  public void updateFilter() {
    if (this.ignoreActions) return;
    if (this.editedCharField.isTerm()) {
      try {
        final OBOClass term = this.ontologyInputField.getCurrentOboClass();
        this.setFilter(term.getID(), this.ontologyInputField);
      } catch (CharFieldGuiEx e) {
        Object item = this.ontologyInputField.getJComboBox().getSelectedItem();
        this.setFilter((item == null ? null : item.toString()), this.ontologyInputField);
      }
    } else {
      this.setFilter(this.textInputField.getText(), this.textInputField);
    }
  }
  
  /**
   * Returns the current filter value.  For an ontology term this will be an ID.
   */
  public String getFilter() {
    return this.filter;
  }
  
  /**
   * Sets the current text value to filter on.
   * @param text The filter text.  For an ontology term this should be the ID.
   * @param source The source of the filter input.
   */
  public void setFilter(String text, Object source) {
    this.filter = text;
    this.fireChange();
    if (!((source.equals(this.ontologyInputField)) || (source.equals(this.textInputField)))) {
      this.updateGUI();
    }
  }
  
  /**
   * Sets the filter mode to EXACT.
   * This is called by the mode radio button.
   */
  public void setExactFilterMode() {
    if (this.ignoreActions) return;
    this.setFilterMode(Mode.EXACT);
  }
  
  /**
   * Sets the filter mode to INHERIT.
   * This is called by the mode radio button.
   */
  public void setInheritFilterMode() {
    if (this.ignoreActions) return;
    this.setFilterMode(Mode.BROAD);
  }
  
  /**
   * Sets the filter mode used for matching ontology terms.
   */
  public void setFilterMode(Mode mode) {
    this.filterMode = mode;
    this.fireChange();
    this.updateGUI();
  }
  
  /**
   * Returns the filter mode used for matching ontology terms.
   */
  public Mode getFilterMode() {
    return this.filterMode;
  }
  
  /**
   * Returns the panel containing the filter interface.
   */
  public JComponent getComponent() {
    return this.filterContainer;
  }
  
  private void fireChange() {
    this.fireChanged(new CharFieldMatcher(this.editedCharField, this.filter, this.filterMode.equals(Mode.BROAD)));
  }
  
  private void updateGUI() {
    this.ignoreActions = true;
    this.charFieldPopup.setModel(new DefaultComboBoxModel(new Vector<CharField>(this.charFields)));
    this.charFieldPopup.setSelectedItem(this.editedCharField);
    this.exactModeRadioButton.setSelected(this.filterMode.equals(Mode.EXACT));
    this.broadModeRadioButton.setSelected(this.filterMode.equals(Mode.BROAD));
    this.inputFieldContainer.removeAll();
    if (this.editedCharField.isTerm()) {
      this.broadModeRadioButton.setText("Inherit");
      this.inputFieldContainer.add(this.ontologyInputField.getJComboBox());
      this.ontologyInputField.setText(this.labelForTermID(this.getFilter()));
      this.ontologyInputField.getCompListSearcher().setOntologies(this.editedCharField.getOntologyList());
    } else {
      this.broadModeRadioButton.setText("Partial");
      this.textInputField.setText(this.getFilter());
      this.inputFieldContainer.add(this.textInputField);
    }
    this.inputFieldContainer.validate();
    this.inputFieldContainer.repaint();
    this.ignoreActions = false;
  }
  
  
  /** Instantiates interface objects from Swixml file */
  private void loadPanelLayout() {
    SwingEngine swix = new SwingEngine(this);
    try {
      swix.render(FileUtil.findUrl("filter_field.xml"));
    } catch (Exception e) {
      log().fatal("Unable to render character table interface", e);
    }
  }
  
  private String labelForTermID(String id) {
    try {
      return CharFieldManager.inst().getOboClass(id).getName();
    } catch (TermNotFoundException e) {
      return id;
    }
  }
  
  /**
   * Mode for filtering characters with ontology terms.
   * EXACT matches only the same term.
   * BROAD matches the same term and any that are descendants, or partial text.
   */
  protected static enum Mode {EXACT, BROAD}
  
  /**
   * Listens to actions from JComboBox of ontologyInputField.
   * Triggers filtering based on changed term selection. 
   */
  private class FilterActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      // need to make sure the TermCompList receives the ActionEvent and updates its OBOClass before we do anything
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          CharFieldMatcherEditor.this.updateFilter();
        }
      });
    }
    
  }
  
  /**
   * Listens to textInputField to filter while the user types.
   */
  private class FilterDocumentListener implements DocumentListener {
    
    public void changedUpdate(DocumentEvent e) {
      this.update();
    }

    public void insertUpdate(DocumentEvent e) {
      this.update();
    }

    public void removeUpdate(DocumentEvent e) {
      this.update();
    }
    
    private void update() {
      CharFieldMatcherEditor.this.updateFilter();
    }
    
  }
  
  /**
   * Allows CharFields to be used as elements for a JList.
   * Displays the name of the CharField.
   */
  @SuppressWarnings("serial")
  private static class CharFieldRenderer extends BasicComboBoxRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof CharField) {
        final CharField charField = (CharField)value;
        return super.getListCellRendererComponent(list, charField.getName(), index, isSelected, cellHasFocus);
      } else {
        log().error("List item is not a charfield");
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    }
    
  }
  
  private static Logger log() {
    return Logger.getLogger(CharFieldMatcherEditor.class);
  }

}
