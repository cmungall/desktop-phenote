package phenote.dataadapter.annotation;

import java.io.File;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.obo.annotation.datamodel.Annotation;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.OBOSession;
import org.obo.util.AnnotationUtil;

import phenote.config.Config;
import phenote.dataadapter.AbstractFileAdapter;
import phenote.datamodel.AnnotationCharacter;
import phenote.datamodel.AnnotationMappingDriver;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.error.ErrorEvent;
import phenote.error.ErrorManager;

/** I think chris wrote this? reads & writes obo files with annotations,
 I believe this currently writes out the whole obo session terms and all,
 which need to be filtered out */

public class AnnotationOBOFileAdapter extends AbstractFileAdapter {

  private File previousFile;
  private File file;
  private static String[] extensions = {"obo", "annotation-obo"};
  private static String desc = "Annotation-OBO [.obo, .annotation-obo]";
  private static Logger LOG = Logger.getLogger(AnnotationOBOFileAdapter.class);

  public AnnotationOBOFileAdapter() {
    super(extensions,desc);
  }

  /** command line setting of file */
  public void setAdapterValue(String filename) {
    file = new File(filename);
  }

  /** this should return CharacterList and caller should load CharListMan
      or CLM makes the call itself? */
  public void load() {
//this doesn't seem to do anything
  }
  
  public CharacterListI load(File f) {
    CharacterListI charList = new CharacterList();
    OBOFileAdapter fa = new OBOFileAdapter();
    OBOFileAdapter.OBOAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
    cfg.getReadPaths().add(f.getAbsolutePath());
    cfg.setBasicSave(false);     
    cfg.setAllowDangling(true); 
    
    try { // throws data adapter exception
      
      OBOSession os = fa.doOperation(OBOAdapter.READ_ONTOLOGY,cfg,null);
      AnnotationMappingDriver driver = Config.inst().getAnnotMappingDriver();
      for (Annotation annot : AnnotationUtil.getAnnotations(os)) {
		AnnotationCharacter ac = new AnnotationCharacter(annot, driver);
    	  charList.add(ac);
      }
    }
    catch (DataAdapterException e) {
      String m = "got obo data adapter exception: "+e+" message "+e.getMessage()
      +" cause "+e.getCause(); // cause is crucial!
      ErrorManager.inst().error(new ErrorEvent(this,m));
      LOG.error(m); // error manager should do this for free 
      //throw new Exception(e);
      // TODO!!! URGENT!!! We must throw
    }
    return charList;
    
  }

  public void commit(CharacterListI charList) {
  
    if (file == null) {
      LOG.error("No file was specified");
      return;
    }
    this.commit(charList, file);
    previousFile = file;
    file = null;
  }
  
  public void commit(CharacterListI charList, File f) {
    if (charList.isEmpty()) {
      LOG.warn("No Data to save"); // popup!
      return;
    }
    OBOFileAdapter fa = new OBOFileAdapter();
    OBOFileAdapter.OBOAdapterConfiguration cfg =
      new OBOFileAdapter.OBOAdapterConfiguration();
    cfg.setWritePath(file.getAbsolutePath());
    //cfg.setBasicSave(false);     
    cfg.setAllowDangling(true); 
    cfg.setSerializer("OBO_1_2");
    try {
      fa.doOperation(OBOAdapter.WRITE_ONTOLOGY, cfg,
          CharFieldManager.inst().getOboSession());
    } catch (DataAdapterException e) {
      // TODO : !!!
      e.printStackTrace();
    }
  }
  
  // im changing my mind - should just go to log and have appender from log
  // rather than vice versa
//   private void debug(String s) {
//     phenote.error.ErrorManager.inst().debug(this,s);
//   }

//   public List<String> getExtensions() {
//     return Arrays.asList(extensions);
//   }

  /** should both be in DataAdapterI convenience and in an AbstactDataAdapter?
   */
//   public boolean hasExtension(String ext) {
//     for (String x : getExtensions())
//       if (x.equals(ext)) return true;
//     return false;
//   }
  
//   public String getDescription() {
//     return "Tab Delimited [.tab, .xls]";
//   }

}
