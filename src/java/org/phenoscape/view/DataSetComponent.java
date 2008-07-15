package org.phenoscape.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.swing.PlaceholderText;

public class DataSetComponent extends PhenoscapeGUIComponent {
  
  private JTextField publicationField;

  public DataSetComponent(String id, PhenoscapeController controller) {
    super(id, controller);
  }
  
  @Override
  public void init() {
    super.init();
    this.initializeInterface();
  }

  private void initializeInterface() {
    this.setLayout(new GridBagLayout());
    this.publicationField = new JTextField();
    new PlaceholderText(this.publicationField, "None");
    this.publicationField.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) { updatePublication(); }
    });
    final GridBagConstraints labelConstraints = new GridBagConstraints();
    this.add(new JLabel("Publication:"), labelConstraints);
    final GridBagConstraints fieldConstraints = new GridBagConstraints();
    fieldConstraints.gridx = 1;
    fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
    fieldConstraints.weightx = 1.0;
    this.add(this.publicationField, fieldConstraints);
    this.updateInterface();
  }
  
  private void updateInterface() {
    this.publicationField.setText(this.getController().getDataSet().getPublication());
  }

  private void updatePublication() {
    this.getController().getDataSet().setPublication(this.publicationField.getText());
  }
  
}
