package phenote.dataadapter.birn;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;
import org.apache.log4j.Logger;

/** birn soap class */
//import edu.PhenoWSService;
import edu.ucsd.ccdb.PhenoWS;
import edu.ucsd.ccdb.PhenoWSService;
import edu.ucsd.ccdb.PhenoWSServiceLocator;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharFieldException;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;

public class SoapAdapter implements QueryableDataAdapterI {


  public void commit(CharacterListI charList) {
    PhenoWSService soap = new PhenoWSServiceLocator();
    try {
      PhenoWS ws = soap.getphenoWSPort();
      for (CharacterI c : charList.getList()) {
        try {
          boolean isCoronal = isCoronal(c); // need config field for this?
          String poly = get(c,"coordinates");
          String comm = get(c,"comm");
          String own = get(c,"userName");
          String e = get(c,"E");
          String q = get(c,"Q");
          String e2 = get(c,"E2");
          String rawIm = get(c,"imagename"); // imagename?
          String warpIm = null; // add to config
          String thumb = null;
          int sliceId = getSliceId(c);
          String imRegId = get(c,"regions"); // regions? int?

          String id = ws.createPhenoTypeFromSmartAtlas
            (poly,isCoronal,comm,own,e,q,e2,rawIm,warpIm,thumb,0,imRegId);
          log().info("Commit ok, got id "+id);
        }
        catch (RemoteException e) { log().error("soap failed "+e); }
      }
    } 
    catch (ServiceException e) {
      log().error("soap failed "+e);
    }
  }

  private String get(CharacterI c, String fieldName) {
    try { return c.getValueString(fieldName);}
    catch (CharFieldException e) { log().error("Config error "+e); return null; }
  }
  /** makes number out of slicenumber for slice id
      slice number chould probably be renamed slice id?
      when int type implemented this should be int type
      returns -1 if its null of not a number */
  private int getSliceId(CharacterI c) {
    String idString = get(c,"slicenumber");
    if (idString == null) return -1; // ??
    try { return Integer.valueOf(idString).intValue(); }
    catch (NumberFormatException e) {
      log().error("id not int "+idString);
      return -1;
    }
  }

  /** default true or false? true? this is a workaround around a lack of 
   boolean type - add boolean! */
  private boolean isCoronal(CharacterI c) {
    String type = get(c,"slicetype");
    if (type == null) return true;
    return type.equalsIgnoreCase("coronal");
  }


  public boolean isFieldQueryable(String field) { return false; }

  /** returns a list of group strings of groups that can be queried for with query
      method */
  public List<String> getQueryableGroups() { return new ArrayList<String>(); }

  /** Throws exception if query fails, and no data to return
      Query with query string of type field for group */
  public CharacterListI query(String group, String field, String query)
    throws DataAdapterEx { return null; }

  /** The label that gets displayed on the db commit button */
  public String getCommitButtonLabel() { return "Commit to CKB"; }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}