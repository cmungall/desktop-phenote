package phenote.charactertemplate;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import phenote.config.Config;
import phenote.dataadapter.CharacterListManager;
import phenote.edit.EditManager;
import phenote.gui.CharacterTablePanel;
import phenote.gui.GridBagUtil;
import phenote.gui.MenuManager;
import phenote.gui.TermInfo;
import phenote.gui.field.FieldPanel;
import phenote.gui.selection.SelectionManager;

public class CharacterTemplateController implements ActionListener {

  public static final String SHOW_CHARACTER_TEMPLATE_ACTION = "showCharacterTemplate";
  private final String groupName;
  private CharacterListManager characterListManager;
  private EditManager editManager;
  private SelectionManager selectionManager; 
  private JFrame window;
  
  public CharacterTemplateController(String groupName) {
    super();
    this.characterListManager = new CharacterListManager();
    this.editManager = new EditManager(this.characterListManager);
    this.selectionManager = new SelectionManager();
    this.groupName = groupName;
    this.createMenu();
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().equals(CharacterTemplateController.SHOW_CHARACTER_TEMPLATE_ACTION)) {
      this.showCharacterTemplate();
    }
  }
  
  public void showCharacterTemplate() {
    this.getWindow().setVisible(true);
  }
  
  private String getGroupTitle() {
    return Config.inst().getTitleForGroup(this.groupName);
  }

  private void createMenu() {
    JMenuItem menuItem = new JMenuItem(this.getGroupTitle());
    menuItem.setActionCommand(CharacterTemplateController.SHOW_CHARACTER_TEMPLATE_ACTION);
    menuItem.addActionListener(this);
    MenuManager.inst().addViewMenuItem(menuItem);
  }
  
  private JFrame getWindow() {
    if (this.window == null) {
      this.window = new JFrame(this.getGroupTitle());
      this.window.getContentPane().add(this.createPanel());
    }
    return this.window;
  }
  
  private JPanel createPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    JPanel upperPanel = new JPanel(new GridBagLayout());
    
    FieldPanel fieldPanel = new FieldPanel(true, false, this.groupName, this.selectionManager, this.editManager);
    GridBagConstraints upperPanelConstraints = GridBagUtil.makeFillingConstraint(0,0);
    upperPanelConstraints.weightx = 1;
    upperPanel.add(fieldPanel, upperPanelConstraints);
    
    TermInfo termInfo = new TermInfo(this.selectionManager);
    upperPanelConstraints.gridx++;
    upperPanelConstraints.weightx = 5;
    upperPanel.add(termInfo.getComponent(), upperPanelConstraints);
    
    GridBagConstraints gridBagConstraints = GridBagUtil.makeFillingConstraint(0,0);
    gridBagConstraints.weighty = 1;
    panel.add(upperPanel, gridBagConstraints);
    
    JPanel tablePanel = new CharacterTablePanel(this.characterListManager, this.editManager, this.selectionManager);
    gridBagConstraints.gridy++;
    gridBagConstraints.weighty = 10;
    panel.add(tablePanel, gridBagConstraints);
    return panel;
  }
  
}
