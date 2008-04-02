package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import org.obo.datamodel.OBOSession;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.OntologyDataAdapter2;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.main.Phenote;
import phenote.main.Phenote2;
import phenote.util.FileUtil;
import phenote.config.Config;
import phenote.config.xml.OntologyFileDocument.OntologyFile;
import phenote.config.xml.TerminologyDefinitionsDocument.TerminologyDefinitions;
import phenote.config.OntologyConfig;


/**
 * This class will show the details of what's going on in Phenote, including
 * the details of the ontologies that are loaded, and some general preference 
 * settings.  New tabbed panels can certainly be added in the future.
 * 
 * @author Nicole Washington
 *
 */
public class GetInfo extends JDialog {

	private JList ontologyList;
	private JEditorPane ontologyDetails;
	private JEditorPane generalInfoPanel;
	private Config config;
	private OntologyFile[] ontologyFiles;
	private TerminologyDefinitions termDefs;
	private List<OntologyConfig> ontologyConfig;
	private OntologyFile[] ontologies;
	private OBOSession oboSession;
	private int selectedOntology = 0;
  private CharFieldManager charFieldManager = CharFieldManager.inst();
  private CharacterListManager charListManager = CharacterListManager.inst();
	List<Ontology> loadedOntologies = charFieldManager.getAllOntologies();


	
	/**
	 * Initialize the Frame
	 */
	public GetInfo() {
		super();
		init();
	}
	
	private void init() {
		config = Config.inst();
		ontologies = config.getOntologyList();
		setTitle("Phenote Properties");
		setName("PhenoteInfoDialog");
		setModal(true);
		setLayout(new BorderLayout());

		final JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane);
		tabbedPane.setPreferredSize(new Dimension(600, 300));

		final JPanel preferencesPanel = new JPanel();
		tabbedPane.addTab("General", null, preferencesPanel, null);
		
		generalInfoPanel = new JEditorPane();
		generalInfoPanel.setContentType("text/html");
		generalInfoPanel.setEditable(false);
		final JScrollPane generalScrollPane = new JScrollPane();
		preferencesPanel.add(generalScrollPane);
		generalScrollPane.setViewportView(generalInfoPanel);
		setGeneralInfo();
		
		final JPanel ontologyInfo = new JPanel();
		ontologyInfo.setLayout(new BorderLayout());
		tabbedPane.addTab("Ontologies", null, ontologyInfo, null);

		final JSplitPane splitPane = new JSplitPane();
		ontologyInfo.add(splitPane);

		final JPanel ontologyListPanel = new JPanel();
		ontologyListPanel.setMinimumSize(new Dimension(100, 0));
		ontologyListPanel.setLayout(new BorderLayout());
		splitPane.setLeftComponent(ontologyListPanel);

		final JScrollPane scrollPane = new JScrollPane();
		ontologyListPanel.add(scrollPane);
		scrollPane.setViewportView(ontologyList);

		ontologyList = getOntologyList();
		ontologyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ontologyList.addListSelectionListener(new OntologyListSelectionListener());

		scrollPane.setViewportView(ontologyList);

		final JLabel ontologyLabel = new JLabel();
		ontologyLabel.setText("Ontology:");
		ontologyListPanel.add(ontologyLabel, BorderLayout.NORTH);

		final JPanel ontologyDetailsPanel = new JPanel();
		ontologyDetailsPanel.setLayout(new BorderLayout());
		splitPane.setRightComponent(ontologyDetailsPanel);

		final JScrollPane scrollPane_1 = new JScrollPane();
		ontologyDetailsPanel.add(scrollPane_1);

		ontologyDetails = new JEditorPane();
		ontologyDetails.setContentType("text/html");
		ontologyDetails.setEditable(false);

		
		scrollPane_1.setViewportView(ontologyDetails);
		setOntologyDetails("testing");  //initially get first ontology in list

		final JLabel detailsLabel = new JLabel();
		detailsLabel.setText("Details:");
		ontologyDetailsPanel.add(detailsLabel, BorderLayout.NORTH);

		final JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

//		final JButton cancelButton = new JButton();
//		cancelButton.setText("Cancel");
//		buttonPanel.add(cancelButton);

		final JButton okButton = new JButton();
		okButton.setText("OK");
		buttonPanel.add(okButton);
		//
		setFirstOntologySelected();
		setEnabled(true);
		setAlwaysOnTop(true);
		validate();
		pack();
		repaint();
		setVisible(true);
	}
	
	private void setGeneralInfo() {
		String info = "<html>";
		info+="Phenote+ version 1.5<br>";  //figure out what variable this is
		info+="# ontologies loaded: " + loadedOntologies.size()+"<br>";
//		info+="# annotations: "+charListManager.getCharList().size();
		try {
			info+="Configuration Name: "+config.getConfigName()+": "+config.getMyPhenoteConfigString()+"<br>";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generalInfoPanel.setText(info);

	}
	
	private JList getOntologyList () {
		Vector<String> listData = new Vector<String>();
		OntologyDataAdapter2 oda = null;
    OntologyFile[] ontologies = config.getOntologyList();
    //in this paradigm, utilize the ontology defs to get the files
    //note, in this new way, all files will already be updated, simply load locally
//    for (OntologyFile ontology : ontologies) {
//    	listData.add(ontology.getHandle());
//    	System.out.println("added "+listData.lastElement());
//    }
		for (Ontology o : loadedOntologies) {
			listData.add(o.getName());
		}
    
		JList ontologyList = new JList(listData);
		
		return ontologyList;
	}
	
	private void setOntologyList(JList newList) {
	  ontologyList = newList;
	  return;
	}
	private void setFirstOntologySelected() {
		ontologyList.setSelectedIndex(0);
	}
	
	private String getOntologyDetails(String ontology) {
		String details = "";
		details = "<html>";
		details += "will set details for "+ontology;
		details+= "</html>";
		return details;
	}
	private String getOntologyDetails(int index) {
		String details = "";
		String namespaces = "";
		details = "<html>";
		boolean found = false;
		URL localUrl = null;
		Ontology selectedOntology = null;
		File localFile = null;
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
		DateFormat tf = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		long date=0;
//		List<Ontology> loadedOntologies = charFieldManager.getAllOntologies();

		
		for (Ontology o : loadedOntologies) {
			if (o.getName().equals(ontologyList.getSelectedValue().toString())) {
				selectedOntology = o;
				found = true;
				for (OntologyFile ontology : ontologies) {  //find the ontology file
					if (ontology.getHandle().equals(ontologyList.getSelectedValue().toString())) {
						List<OntologyConfig> oc = config.getAllOntologyConfigs();
						for (OntologyConfig c : oc) {
							if (ontology.getHandle().equals(c.getName())) {
								if (c.hasNamespace()) {
									namespaces+=c.getNamespace()+" ";
								}
								localUrl = c.getLocalUrl();
								date = c.getUpdateDate();
								break;
							}
						}    			    	      
						details+="Ontology Name: "+ontology.getHandle()+"<br>";
						details+="Repository URL: "+ontology.getLocation()+"<br>";
						if (localUrl==null) {
								localFile = new File(FileUtil.getDotPhenoteOboDir(),ontology.getFilename());
								try {
									localUrl = localFile.toURL();
								} catch (MalformedURLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
						details+="Local cache: "+localUrl+"<br>";
						
						details+="Filename: "+ontology.getFilename()+"<br>";
						details+="Format: "+ontology.getType()+"<br>";
//						details+="Source: "+selectedOntology.getSource()+"<br>";
//						details+="Version: "+selectedOntology.getVersion()+"<br>";
						details+="Date in Header: "+df.format(selectedOntology.getOntologyDate())+"<br>";
						date = localFile.lastModified();
						Date d = new Date(date);
//						long now = new Date().getTime();
						details+="Last Download: "+df.format(d)+"<br>";
						details+="<br>";
						details+="# terms: "+selectedOntology.getSortedTerms().size()+"<br>";
						details+="# obsoletes: "+ selectedOntology.getSortedObsoleteTerms().size()+"<br>";
//						for (CharacterI c : charListManager.getCharList()) {
//							for (CharField cf : c.getAllCharFields()) {
//								if (cf.isTerm())
//									if (c.getValue(cf).getOboClass().get)
//							}
//						}
//						details+="# annotations: "+"<br>";

						details+="<br>Automatically update when Phenote starts? "+ontology.getAutoUpdate()+"<br>";
						break;
					}
				}
			}
		}
		if (!found) {
			details +="Ontology defined in configuration, but not mapped to any field.  No details retrieved.";
		}
		details+= "</html>";
		return details;
	}
	private void setOntologyDetails(String ontology) {
		ontologyDetails.setText(getOntologyDetails(ontology));
	}
	private void setOntologyDetails(int index) {
		ontologyDetails.setText(getOntologyDetails(index));
		pack();
		validate();
		repaint();
	}
	
  private class OntologyListSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      setOntologyDetails(ontologyList.getSelectedIndex());      
      
    }
  }

}
