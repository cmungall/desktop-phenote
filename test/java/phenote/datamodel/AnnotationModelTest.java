package phenote.datamodel;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.annotation.datamodel.Annotation;
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

public class AnnotationModelTest {
  
   protected static OBOSession session;
   
  @BeforeClass public static void initialize() throws ConfigException {
    Phenote.resetAllSingletons();
    Config.inst().setConfigFile("ncbo-test.cfg");
    Phenote phenote = Phenote.getPhenote();
    phenote.initOntologies();
    phenote.initGui();
    DelimitedFileAdapter ad = new DelimitedFileAdapter();
    ad.load(new File("test/testfiles/Sox9-human-bbop.tab"));
    session = CharFieldManager.inst().getOboSession();
  }
  
  @Before public void setup() {
   }
  
  @Test public void checkAnnotations()   {
	  Collection<Annotation> annots = AnnotationUtil.getAnnotations(session);
	  for (Annotation annot : annots) {
		  System.err.println("annot: "+annot);
	  }
	  Assert.assertTrue(annots.size() > 0);
  }
  
 
}
