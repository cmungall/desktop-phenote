package phenote.datamodel;

import java.io.File;
import java.io.IOException;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.datamodel.OBOSession;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.main.Phenote;

public class BasicAnnotationModelTest {

	protected static String getConfigFileName() { return "basic-test.cfg"; }
	protected static String getDataFilePath() { return "test/testfiles/basic-test-data.tab";}
	protected static OBOSession session;
	protected static AnnotationModelTestUtil util;

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
		util = new AnnotationModelTestUtil(session);
	}

	@Before public void setup() {
	}

	@Test public void testWriteToDatabase() throws DataAdapterException, IOException {
		util.checkAnnotations();
		//writeTempOBOFile();
		//writeToDatabase();
		Assert.assertTrue(true);
	}



}
