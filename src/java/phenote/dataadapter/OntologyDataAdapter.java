package phenote.dataadapter;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOSession;
import org.geneontology.oboedit.datamodel.impl.OBOSessionImpl;
import org.geneontology.dataadapter.DataAdapterException;
import org.geneontology.dataadapter.FileAdapterConfiguration;
import org.geneontology.dataadapter.IOOperation;
import org.geneontology.oboedit.dataadapter.OBOFileAdapter;

import phenote.datamodel.CharField;
import phenote.datamodel.CharField.CharFieldEnum;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;
import phenote.config.Config;
import phenote.config.FieldConfig;
import phenote.config.OntologyConfig;

/** is this really a data adapter? - OntologyLoader? this isnt a data adapter
    it doesnt load & commit character data - just loads ontologies. rename OntologyLoader
    for now can stay in dataadapter package */
public class OntologyDataAdapter {

  private Config config;
  private OntologyManager ontologyManager = OntologyManager.inst();

  public OntologyDataAdapter() {
    config = Config.inst();
  }

  public void loadOntologies() {

    for (FieldConfig fieldConfig : config.getFieldConfigList()) {
      CharFieldEnum fce = fieldConfig.getCharFieldEnum();
      CharField cf;
      if (fieldConfig.hasOntology()) {
        Ontology o = loadOntology(fieldConfig.getOntologyConfig());
        cf = new CharField(fce,o);
      }
      else {
        cf = new CharField(fce,fieldConfig.getLabel());
      }
      ontologyManager.addField(cf);
    }

  }


  /** Load up/cache Sets for all ontologies used, anatomyOntologyTermSet
   * and patoOntologyTermSet -- move to dataadapter/OntologyDataAdapter... */
  private Ontology loadOntology(OntologyConfig ontCfg) {
    OBOSession oboSession = getOboSession(findFile(ontCfg.ontologyFile));
    Ontology ontology = new Ontology(ontCfg.name,oboSession);
    return ontology;
  }

  

  /** Look for file in current directory (.) and jar file */
  private URL findFile(String fileName) {
    String oboFileDir = "obo-files/";
    // try current directory + obo-file dir
    String currentDir = "./" + oboFileDir + fileName;
    File file = new File(currentDir);
    if (file.exists())
      return makeUrl(currentDir);

    // try jar - hopefully this works... jar files have to have '/' prepended
    // first try without obo-files dir (in jar)
    String jarFile = "/" + fileName;
    URL url = Ontology.class.getResource(jarFile); // looks in jar
    // 2nd try with obo-files dir in jar file (i used to do it this way)
    if (url == null) {
      jarFile = "/" + oboFileDir + fileName;
      url = Ontology.class.getResource(jarFile); // looks in jar
    }

    if (url == null) {
      System.out.println("No file found in pwd or jar for "+fileName);
      return null;
    }
    return url;
  }
  
  private URL makeUrl(String file) {
    try {
      return new URL("file:"+file);
    }
    catch (MalformedURLException e) {
      System.out.println("malformed url "+file+" "+e);
      return null;
    }
  }


  // String -> url to handle web start jar obo files
  private OBOSession getOboSession(URL oboUrl) {
    if (oboUrl == null)
      return new OBOSessionImpl(); // ??

    OBOFileAdapter fa = new OBOFileAdapter();
    FileAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
    Collection fileList = new ArrayList();
    fileList.add(oboUrl.toString());
    cfg.setReadPaths(fileList);
    try { // throws data adapter exception
      OBOSession os = (OBOSession)fa.doOperation(IOOperation.READ,cfg,null);
      return os;
    }
    catch (DataAdapterException e) {
      System.out.println("got data adapter exception: "+e);
      return null; // empty session?
    }
  }

}

//     Ontology pato = loadOntology(config.getPatoOntologyConfig());
//     ontologyManager.setPatoOntology(pato);

//     if (config.hasLumpOntology()) {
//       Ontology lump = loadOntology(config.getLumpOntologyConfig());
//       ontologyManager.setLumpOntology(lump);
//     }
    
//     List<OntologyConfig> entities = config.getEntityOntologyConfigs();
//     Iterator<OntologyConfig> it = entities.iterator();
//     while(it.hasNext()) {
//       Ontology o = loadOntology(it.next());
//       ontologyManager.addEntityOntology(o);
//     }

//     if (config.hasGeneticContextField()) {
//       FieldConfig fc = config.getGeneticContextConfig();
//       Ontology o = loadOntology(fc.getOntologyConfig());
//       //ontologyManager.setGeneticContextOntology(o);
//       // alternatively... ... // or (new CharField(cfe,o)) ??
//       //ontologyManager.addOntology(fc.getCharFieldEnum(),o);
//       ontologyManager.addField(new CharField(fc.getCharFieldEnum(),o));
//     }
