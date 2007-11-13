package phenote.datamodel;

import java.io.File;
import java.io.IOException;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.dataadapter.OBDSQLDatabaseAdapter;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.main.Phenote;

public class BasicAnnotationModelTest extends AbstractAnnotationModelTest {
  
	String jdbcPath = "jdbc:postgresql://localhost:5432/obdtest";

   protected static String getConfigFileName() { return "basic-test.cfg"; }
   protected static String getDataFilePath() { return "test/testfiles/basic-test-data.tab";}
  
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

   @Test public void testWriteToDatabase() throws DataAdapterException, IOException {
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
		
		writeTempOBOFile();

	   Assert.assertTrue(true);
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
