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

public class OBDSavePhenotypeTest extends AbstractAnnotationModelTest {



	protected static Shard shard;
	static String jdbcPath = "jdbc:postgresql://localhost:5432/obdtest";
	// Set these values if you don't want to use the environmental user / no password database connection defaults. 
	static String dbUsername;
	static String dbPassword;


	protected static String getConfigFileName() { return "ncbo-test.cfg"; }
	protected static String getDataFilePath() { return "test/testfiles/Sox9-human-bbop.tab";}

	@BeforeClass public static void initialize() throws ConfigException {
		Phenote.resetAllSingletons();
		Config.inst().setConfigFile("ncbo-test.cfg");
		Phenote phenote = Phenote.getPhenote();
		phenote.initOntologies();
		phenote.initGui();
		DelimitedFileAdapter ad = new DelimitedFileAdapter();
		CharacterListI clist = 
			ad.load(new File(getDataFilePath()));
		System.err.println("clist size: "+clist.size());
		testCharacterList(clist);
		session = CharFieldManager.inst().getOboSession();
	}

	@BeforeClass public static void setUp() throws Exception  {
		OBDSQLShard obd = new OBDSQLShard();
		obd.connect(jdbcPath,dbUsername,dbPassword);
		shard = obd;

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
		Graph g = OBOBridge.getAnnotationGraph(session);
		for (IdentifiedObject io : session.getObjects()) {
			if (io.isBuiltIn())
				continue;
			if (io.getNamespace() == null || io.isAnonymous()) {
				if (io instanceof Annotation) {
					// we should already have this
				}
				else {
					g.addNode(OBOBridge.obj2node(io));
				}
			}
		}
		shard.putGraph(g);

	}

	@Test public void checkAnnotationsInDatabase() throws DataAdapterException, IOException {
		String id = "OMIM:608160.0002";
		Collection<Statement> stmts = shard.getAnnotationStatementsForAnnotatedEntity(id, null, null);
		for (Statement s : stmts) {
			System.out.println(s);
		}
		Assert.assertTrue(stmts.size() > 0);
	} 


}
