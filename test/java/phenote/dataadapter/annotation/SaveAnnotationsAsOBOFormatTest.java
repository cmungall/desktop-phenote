package phenote.dataadapter.annotation;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obd.model.Graph;
import org.obd.model.Statement;
import org.obd.model.bridge.OBOBridge;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;
import org.obo.annotation.datamodel.Annotation;
import org.obo.datamodel.IdentifiedObject;
import org.obo.util.AnnotationUtil;
import org.obo.util.TermUtil;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.datamodel.AbstractAnnotationModelTest;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharacterListI;
import phenote.main.Phenote;

public class SaveAnnotationsAsOBOFormatTest extends AbstractAnnotationModelTest {


	AnnotationOBOFileAdapter adapter = new AnnotationOBOFileAdapter();
	static CharacterListI clist;

	protected static String getConfigFileName() { return "ncbo-test.cfg"; }
	protected static String getDataFilePath() { return "test/testfiles/Sox9-human-bbop.tab";}

	@BeforeClass public static void initialize() throws ConfigException {
		Phenote.resetAllSingletons();
		Config.inst().setConfigFile("ncbo-test.cfg");
		Phenote phenote = Phenote.getPhenote();
		phenote.initOntologies();
		phenote.initGui();
		DelimitedFileAdapter ad = new DelimitedFileAdapter();
		clist = 
			ad.load(new File(getDataFilePath()));
		System.err.println("clist size: "+clist.size());
		testCharacterList(clist);
		session = CharFieldManager.inst().getOboSession();
	}

	@BeforeClass public static void setUp() throws Exception  {

	}

	public static void testCharacterList(CharacterListI clist) {
		for (CharacterI c : clist.getList()) {
			System.out.println("character ="+c);
			for (CharField cf : c.getAllCharFields()) {
				System.out.println("  "+cf.getName()+" "+c.getValueString(cf));
			}

		}
	}

	@Before public void setup() {
	}

	@Test public void saveAnnotations()   {
		adapter.setAdapterValue("/tmp/foo.obo");
		adapter.commit(clist);


	}



}
