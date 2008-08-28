package phenote.datamodel;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.obo.annotation.datamodel.Annotation;
import org.obo.datamodel.OBOSession;
import org.obo.util.AnnotationUtil;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.main.Phenote;

/**
 * This test is "disabled" at the moment because it has not been maintained 
 * and the required config file ("ncbo-test.cfg") no longer exists.
 */
public class EnvironmentalAssociationDriverTest {

	protected static OBOSession session;

	protected static String getConfigFileName() { return "ncbo-test.cfg"; }
	protected static String getDataFilePath() { return "test/testfiles/Sox9-human-bbop.tab";}

	//@BeforeClass
	public static void initialize() throws ConfigException {
		Phenote.resetAllSingletons();
		Config.inst().setConfigFile("ncbo-test.cfg");
		Phenote phenote = Phenote.getPhenote();
		phenote.initOntologies();
		phenote.initGui();
		DelimitedFileAdapter ad = new DelimitedFileAdapter();
		CharacterListI clist = 
			ad.load(new File("test/testfiles/Sox9-human-bbop.tab"));
		System.err.println("clist size: "+clist.size());
		session = CharFieldManager.inst().getOboSession();
	}

	@Ignore @Test
	public void checkAnnotations()   {
		Collection<Annotation> annots = AnnotationUtil.getAnnotations(session);
		System.err.println("# annots = "+annots.size());
		for (Annotation annot : annots) {
			System.err.println("annot: "+annot+":: "+annot.getSubject()+" -"+annot.getRelationship()+"-> "+annot.getObject());

		}
		Assert.assertTrue(annots.size() > 0);
	}

	public void testMe() throws Exception {
	  throw new Exception();
	}

}
