package phenote.datamodel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.annotation.datamodel.Annotation;
import org.obo.util.AnnotationUtil;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.main.Phenote;

public class PhenotypeAssociationDriverTest extends AbstractAnnotationModelTest {
    
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
    	ad.load(new File("test/testfiles/Sox9-human-bbop.tab"));
    System.err.println("clist size: "+clist.size());
    testCharacterList(clist);
    session = CharFieldManager.inst().getOboSession();
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
  
  @Test public void checkAnnotations()   {
	  Collection<Annotation> annots = AnnotationUtil.getAnnotations(session);
	  System.err.println("# annots = "+annots.size());
	  for (Annotation annot : annots) {
		  System.err.println("annot: "+annot+":: "+annot.getSubject()+" -"+annot.getRelationship()+"-> "+annot.getObject());
		  
	  }
	  Assert.assertTrue(annots.size() > 0);
  }
  
  @Test public void testWriteToDatabase() throws DataAdapterException, IOException {
	   //writeTempOBOFile();
	   //writeToDatabase();
	   Assert.assertTrue(true);
  }

 
}
