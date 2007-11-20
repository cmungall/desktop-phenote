package phenote.datamodel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.annotation.datamodel.Annotation;
import org.obo.dataadapter.OBDSQLDatabaseAdapter;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration;
import org.obo.datamodel.OBOSession;
import org.obo.util.AnnotationUtil;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.datamodel.TermNotFoundException;
import phenote.main.Phenote;

public abstract class AbstractAnnotationModelTest {
  
   protected static OBOSession session;
   protected static String getConfigFileName() { return ""; }
   protected static String getDataFilePath() { return "";}
   
	String jdbcPath = "jdbc:postgresql://localhost:5432/obdtest";
  
  @BeforeClass public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile(getConfigFileName());
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
    phenote.initGui();
    DelimitedFileAdapter ad = new DelimitedFileAdapter();
    CharacterListI clist = 
    	ad.load(new File(getDataFilePath()));
    System.err.println("clist size: "+clist.size());
    session = CharFieldManager.inst().getOboSession();
  }
  
  @Before public void setup() {
   }
  
  @Test public void checkAnnotations()   {
	  Collection<Annotation> annots = AnnotationUtil.getAnnotations(session);
	  System.err.println("# annots = "+annots.size());
	  for (Annotation annot : annots) {
		  System.err.println("annot: "+annot+":: "+annot.getSubject()+" -"+annot.getRelationship()+"-> "+annot.getObject());
		  
	  }
	  Assert.assertTrue(annots.size() > 0);
  }
  
  public void writeToDatabase() throws DataAdapterException, IOException {
		OBDSQLDatabaseAdapterConfiguration wconfig = 
			new OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration();
		wconfig.setSaveImplied(false);
		
		wconfig.setWritePath(jdbcPath);
		wconfig.setAnnotationMode(OBDSQLDatabaseAdapter.
				OBDSQLDatabaseAdapterConfiguration.AnnotationMode.ANNOTATIONS_ONLY);
		OBDSQLDatabaseAdapter wadapter = new OBDSQLDatabaseAdapter();
		//ReasonedLinkDatabase reasoner = rf.createReasoner();
		//reasoner.setLinkDatabase(linkDatabase);
		//wadapter.setReasoner(reasoner);
		//reasoner.recache();
		
		wadapter.doOperation(OBOAdapter.WRITE_ONTOLOGY, wconfig, session);
  }	
 
  // lifted from OE. Can we reuse from test
	public File writeTempOBOFile() throws IOException, DataAdapterException {
		
		OBOFileAdapter adapter = new OBOFileAdapter();
		OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
		File outFile = File.createTempFile("foo", "bar");
		//outFile.deleteOnExit();
		config.setWritePath(outFile.getAbsolutePath());
		adapter.doOperation(OBOAdapter.WRITE_ONTOLOGY, config, session);
		return outFile;
	}

}
