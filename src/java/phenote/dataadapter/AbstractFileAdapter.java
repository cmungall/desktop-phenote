package phenote.dataadapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractFileAdapter implements DataAdapterI {

  private List<String> extensions = new ArrayList<String>();
  private String description;
  protected File file;

  protected AbstractFileAdapter(String[] exts, String description) {
    setExtensions(exts);
    setDescription(description);
  }

  /** Set value to use for loading or writeback, for a file adapter this would be
      the file name - is there a better name for this method? 
      For now just doing String - which may be sufficient - may need an 
      AdapterValue/DataInput object if this gets more involved - but that may
      not be necasary - certianly string ok for now 
      rename setFilename???*/
  public void setAdapterValue(String filename) {
    file = new File(filename);    
  }
  
  protected void setExtensions(String[] exts) {
    if (exts == null) return;
    extensions = Arrays.asList(exts);
  }

  public List<String> getExtensions() {
    return extensions;
  }


  /** should both be in DataAdapterI convenience and in an AbstactDataAdapter?
   */
  public boolean hasExtension(String ext) {
    for (String x : getExtensions())
      if (x.equals(ext)) return true;
    return false;
  }
  protected void setDescription(String d) { description = d; }
  public String getDescription() { return description; }


}