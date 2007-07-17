package phenote.gui;

import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListDataListener;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import phenote.config.Config;
import phenote.config.FieldConfig;
import phenote.config.OntologyConfig;
import phenote.config.ConfigException;
import phenote.config.ConfigFileQueryGui; // phase out

public class ConfigGui {

  private final static String NO_NAME = "No Name";
  private final static String NO_NAME_ITAL = 
    "<html><i><font color=gray>"+NO_NAME+"</font></i></html>";

  private JList configList;
  private Config currentConfig; // ???

  private JList fieldList;
  private JTextField configNameInput;

  private JTextField fieldNameInput;
  private JCheckBox fieldEnable;
  private JList ontologyList;

  private JTextField ontolNameInput;
  private JTextField ontolFile;
  private JTextField namespace;
  private JTextField slim;
  private JCheckBox ontolPostComp;

  private boolean userEditing = true;

  public ConfigGui() { init(); }

  private void init() {
    JFrame frame = new JFrame("Phenote Configuration(under development)");

    JComponent allConfigs = makeAllConfigsPanel();
    allConfigs.setPreferredSize(new Dimension(300,300));
    allConfigs.setMinimumSize(new Dimension(250,250));
//     Config c = Config.newInstance("hmm","dipsla");//NO_NAME,noNameItalics); //??
//       //configList.setModel(getConfigListModel()); // ????
//       getConfigListModel().list.add(c);

    JComponent config = makeConfigPanel();

    // true -> continous layout (???)
    JSplitPane configSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,
                                            allConfigs,config);

    JComponent field = makeFieldPanel();

    JComponent ontol = makeOntologyPanel();
    ontol.setPreferredSize(new Dimension(300,300));
    // min size?
 
    JSplitPane fieldOntSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,field,
                                              ontol);
    //fieldOntSplit.setPreferredSize(new Dimension(600,300));

    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,configSplit,
                                          fieldOntSplit);
    mainSplit.setDividerLocation(0.5); // halfway?

    frame.add(mainSplit);

    frame.pack();
    configSplit.setDividerLocation(0.5);
    fieldOntSplit.setDividerLocation(0.5);
    mainSplit.setDividerLocation(0.5); // halfway?
    frame.pack();
    frame.setVisible(true);

      Config c2 = Config.newInstance("h2","2");//NO_NAME,noNameItalics); //??
      //configList.setModel(getConfigListModel()); // ????
      //getConfigListModel().list.add(c2);
      configList.repaint();
  }

  private JComponent makeAllConfigsPanel() {
    JPanel allConfigsPanel = new JPanel(new GridBagLayout());
    JScrollPane allScroll = new JScrollPane(allConfigsPanel);
    GridBagConstraints g = GridBagUtil.initConstraint();
    configList = addList(null,g,allConfigsPanel,new AddConfigActionListener(),
                         new ConfigListSelectionListener());
    //configList.setPreferredSize(new Dimension(250,250));

    // populate configList with all configs...
    PhenoteListModel<Config> listModel = new PhenoteListModel<Config>();
    //configList.setModel(listModel);
    int debugShortcut=0;
    for (String filename : ConfigFileQueryGui.getConfigNames()) {
      try {
       Config c = Config.makeConfigFromFile(filename);
       listModel.add(c);
       System.out.println("added config "+c.toString());
      } catch (ConfigException e) {
        System.out.println("Failed to parse config "+filename); // LOG!!
      }
      //if (++debugShortcut == 4) break;
    }
    configList.setModel(listModel);

    return allScroll;
  }

  private JComponent makeConfigPanel() {
    JPanel configPanel = new JPanel(new GridBagLayout());
    JScrollPane configScroll = new JScrollPane(configPanel); // ?? why not

    GridBagConstraints g = GridBagUtil.initConstraint();

    // Title
    addTitle("CONFIG",g,configPanel);

   // NAME
    configNameInput = addNameInput(g,configPanel,new ConfigNameDocListener());

    // Field List
    fieldList = addList("Fields",g,configPanel,new AddFieldActionListener(),
                        new FieldListSelectionListener());
  
    return configScroll;
  }

  private void addTitle(String title,GridBagConstraints g,JComponent parent) {
    g.gridwidth = 2; g.anchor = GridBagConstraints.CENTER;
    parent.add(new JLabel(title),g); // ?
  }

  private JTextField addNameInput(GridBagConstraints g, JComponent parent,
                                  DocumentListener l) {
    
    return addInput(g,"Name",parent,l);
  }

  private JTextField addInput(GridBagConstraints g, String l,JComponent p) {
    return addInput(g,l,p,null);
  }

  private JTextField addInput(GridBagConstraints g,String l,JComponent prnt,
                              DocumentListener dl) {
    JLabel label = new JLabel(l+": ");
    ++g.gridy; g.gridwidth = 1; g.anchor = GridBagConstraints.WEST;
    prnt.add(label,g);
    JTextField input = new JTextField(17);
    if (dl != null) {
      input.getDocument().addDocumentListener(dl);
    }
    ++g.gridx;
    prnt.add(input,g);
    --g.gridx; // put it back
    return input;
  }
  
  private JList addList(String s,GridBagConstraints g, JComponent parent,
                        ActionListener al,ListSelectionListener lsl) {
    g.gridx = 1;
    if (s != null) {
      JLabel lab = new JLabel(s+": ");
      ++g.gridy;
      parent.add(lab,g);
    }
    JList l = new JList(new DefaultListModel());
    l.addListSelectionListener(lsl);
    l.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane p = new JScrollPane(l);
    ++g.gridy; g.gridwidth = 2; g.fill = GridBagConstraints.BOTH;
    g.weighty = 10;
    parent.add(p,g);
    JButton add = new JButton("+"); // presumptious?
    add.addActionListener(al);
    ++g.gridy; g.fill = GridBagConstraints.NONE; g.weighty = 1;
    parent.add(add,g);
    return l;
  }

  private JComponent makeFieldPanel() {
    
    JPanel fieldPanel = new JPanel(new GridBagLayout());
    JScrollPane fieldScroll = new JScrollPane(fieldPanel); //??

    GridBagConstraints g = GridBagUtil.initConstraint();

    // Title
    addTitle("FIELD",g,fieldPanel);

    // NAME
    fieldNameInput = addNameInput(g,fieldPanel,new FieldNameDocumentListener());

    // ENABLE
    fieldEnable = new JCheckBox("Enable",true); // selected
    g.gridx = 1; ++g.gridy;
    fieldPanel.add(fieldEnable,g);

    // ONTOLOGY LIST
    ListSelectionListener l = new OntologyListSelectionListener();
    ontologyList = addList("Ontologies",g,fieldPanel,new AddOntActionListener(),l);

    return fieldScroll;
  }

  private JComponent makeOntologyPanel() {
    JPanel ontolPanel = new JPanel(new GridBagLayout());
    JScrollPane ontolScroll = new JScrollPane(ontolPanel); //??

    GridBagConstraints g = GridBagUtil.initConstraint();

    // Title
    addTitle("ONTOLOGY",g,ontolPanel);

    // NAME
    ontolNameInput = addNameInput(g,ontolPanel,new OntNameDocumentListener());

    // FILE/URL
    ontolFile = addInput(g,"File/URL",ontolPanel,new OntFileDocLis());

    namespace = addInput(g,"Namespace",ontolPanel);

    slim = addInput(g,"Slim",ontolPanel);

    // POST COMP
    ontolPostComp = new JCheckBox("Is Post Comp Relationship?",false); // selected
    g.gridx = 1; ++g.gridy; g.gridwidth = 2;
    ontolPanel.add(ontolPostComp,g);
    
    return ontolScroll;
  }



  private PhenoteListModel<Config> getConfigListModel() {
    return getPhenoteListModel(configList);
  }

  private Config getConfig(int i) {
    return getConfigListModel().get(i);
  }

  private FieldConfig getFieldConfig(int i) { return getFieldListModel().get(i); }

  private OntologyConfig getOntConfig(int i) { return getOntListModel().get(i); }

  private PhenoteListModel<FieldConfig> getFieldListModel() {
    return getPhenoteListModel(fieldList);
  }

  private PhenoteListModel<OntologyConfig> getOntListModel() {
    return getPhenoteListModel(ontologyList);
  }

  private PhenoteListModel getPhenoteListModel(JList l) {
    ListModel m = l.getModel();
    if (m instanceof PhenoteListModel)
      return (PhenoteListModel)m;
    else {
      System.out.println("list model "+m+" not instance of phenote list model ");
      return null; // shouldnt happen
    }
  }

  /** + button pressed in config fields - add noname field to list */
  private class AddConfigActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String noNameItalics = "<html><i><font color=gray>"+NO_NAME+"</font></i></html>";
      Config c = Config.newInstance(NO_NAME,noNameItalics); //??
      getConfigListModel().add(c);
      int newIndex = getConfigListModel().getSize() - 1;
      configList.setSelectedIndex(newIndex);
      configList.repaint();
    }
  }

  /** + button pressed in config fields - add noname field to list */
  private class AddFieldActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (!hasSelectedConfig()) {
        System.out.println("Error: no config selected");
        return;
      }
      FieldConfig fc = new FieldConfig(NO_NAME,NO_NAME_ITAL,getSelectedConfig());
      getFieldListModel().add(fc);
      int newIndex = fieldList.getModel().getSize() - 1;
      fieldList.setSelectedIndex(newIndex);
      fieldList.repaint();
    }
  }

  private class AddOntActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (!hasSelectedField()) {
        System.out.println("Error: no field selected");
        return;
      }
      OntologyConfig oc =
        new OntologyConfig(NO_NAME,NO_NAME_ITAL,getSelectedFieldConfig()); 
      getOntListModel().add(oc);
      int newIndex = ontologyList.getModel().getSize() - 1;
      ontologyList.setSelectedIndex(newIndex);
      ontologyList.repaint();
    }
  }


  private class ConfigNameDocListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { updateConfigListWithName(); }
    public void insertUpdate(DocumentEvent e) { updateConfigListWithName(); }
    public void removeUpdate(DocumentEvent e) { updateConfigListWithName(); }
  }

  private void updateConfigListWithName() {
    // if change comes from selection no need to update config list
    if (!userEditing) return; 
    int i = configList.getSelectedIndex();
    if (i == -1) return; // nothing selected
    // nameable interface???
    //Config c = (Config)getConfigListModel().remove(i);
    //c.setConfigName(configNameInput.getText());
    //getConfigDefaultListModel().add(i,c);
    //getConfigListModel().setName(i,configNameInput.getText());
    getConfig(i).setConfigName(configNameInput.getText());
    configList.repaint();// repaint list?
    //configList.setSelectedIndex(i); // ??
  }

  private class FieldNameDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { updateFieldListWithName(); }
    public void insertUpdate(DocumentEvent e) { updateFieldListWithName(); }
    public void removeUpdate(DocumentEvent e) { updateFieldListWithName(); }
  }

  private void updateFieldListWithName() {
    if (!userEditing) return;
    int i = fieldList.getSelectedIndex();
    if (i == -1) return; // nothing selected
    //FieldConfig fc = (FieldConfig)getFieldDefaultListModel().remove(i);
    //fc.setLabel(fieldNameInput.getText());
    //getFieldDefaultListModel().add(i,fc);
    //fieldList.setSelectedIndex(i);
    getFieldConfig(i).setLabel(fieldNameInput.getText());
    fieldList.repaint();
  }

  private class OntNameDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { updateOntListWithName(); }
    public void insertUpdate(DocumentEvent e) { updateOntListWithName(); }
    public void removeUpdate(DocumentEvent e) { updateOntListWithName(); }
  }

  private void updateOntListWithName() {
    if (!userEditing) return;
    int i = ontologyList.getSelectedIndex();
    if (i == -1) return; // nothing selected
    getOntConfig(i).setName(ontolNameInput.getText());
    ontologyList.repaint();
  }
  private class OntFileDocLis implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { updateOntFile(); }
    public void insertUpdate(DocumentEvent e) { updateOntFile(); }
    public void removeUpdate(DocumentEvent e) { updateOntFile(); }
  }

  private void updateOntFile() {
    getSelectedOntologyConfig().setFile(ontolFile.getText());
  }

  private class OntologyListSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) return; // only need 1 event/sel
      OntologyConfig oc = getSelectedOntologyConfig();
      if (oc == null) return;
      setOntologyConfig(oc);
    }
  }
  private class FieldListSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) return; // only need 1 event/sel
      FieldConfig fc = getSelectedFieldConfig();
      if (fc == null) return;
      setFieldConfig(fc);
    }
  }
  private class ConfigListSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) return; // only need 1 event/sel
      if (!hasSelectedConfig()) return; // shouldnt happen
      Config c = getSelectedConfig();
      setConfig(c);
    }
  }

  private boolean hasSelectedField() {
    return getSelectedFieldConfig() != null;
  }

  private FieldConfig getSelectedFieldConfig() {
    return (FieldConfig)fieldList.getSelectedValue(); // instanceof check?
  }

  private OntologyConfig getSelectedOntologyConfig() {
    return (OntologyConfig)ontologyList.getSelectedValue();
  }

  private boolean hasSelectedConfig() {
    return getSelectedConfig() != null;
  }
    
  private Config getSelectedConfig() {
    return (Config)configList.getSelectedValue();
  }

//   private Object getSelectedItem(JList l) {
// //     int i = l.getSelectedIndex();
// //     if (i == -1) return null; // ex?
// //     Object o = getDefaultListModel(l).getElementAt(i);
// //    return o;
//     return l.getSelectedValue();
//   }

  private void setOntologyConfig(OntologyConfig oc) {
    if (oc == null) return;
    userEditing = false; // this is not a user edit
    ontolNameInput.setText(oc.getName());
    ontolFile.setText(oc.getFile());
    namespace.setText(oc.getNamespace());
    slim.setText(oc.getSlim());
    ontolPostComp.setSelected(oc.isPostCompRel());
    userEditing = true; // reset
  }

  private void setFieldConfig(FieldConfig fc) {
    // oh boy need to turn off listener... endless loop?
    //if (currentFieldConfig 
    if (fc == null) return;
    userEditing = false; // this is not a user edit
    fieldNameInput.setText(fc.getLabel());
    fieldEnable.setSelected(fc.isEnabled());
    ontologyList.setModel(new PhenoteListModel(fc.getOntologyConfigList()));
    selectFirst(ontologyList);
    userEditing = true; // reset
  }

  /** Set current config in config box */
  private void setConfig(Config c) {
    // oh boy need to turn off listener... endless loop? yes - this is not an edit 
    if (currentConfig == c) return; // ??? already set
    currentConfig = c;
    userEditing = false;
    configNameInput.setText(c.getConfigName());
    fieldList.setModel(new PhenoteListModel<Config>(c.getAllFieldCfgs()));
    selectFirst(fieldList);
    userEditing = true;
  }

  private void selectFirst(JList l) {
    // make sure there is something to select...
    if (getPhenoteListModel(l).isEmpty()) return;
    l.setSelectedIndex(0);
  }

  //private interface ListModelI<Type> {}  // ??

  // generic? for configs, fields, & ontols?
  private class PhenoteListModel<Type> implements ListModel {

    List<Type> list;
    private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

    private PhenoteListModel(List l) { list = l; }
    private PhenoteListModel() { list = new ArrayList(); }
    
    public Type getElementAt(int index) { return list.get(index); }
    private boolean isEmpty() { return list == null || getSize() == 0; }
    public int getSize() { return list.size(); }
    public void removeListDataListener(ListDataListener l) {}
    public void addListDataListener(ListDataListener l) {
      listeners.add(l);
    }
    // i should type this <T>!!
    private void add(Type o) {
      System.out.println("adding "+o);
      list.add(o);
      fireChange();
    }
    private void fireChange() {
      for (ListDataListener l : listeners)
        l.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,list.size()));

    }

    private Type get(int i) { return list.get(i); }
    //private void setName(int i, String name) { }
  }

}
