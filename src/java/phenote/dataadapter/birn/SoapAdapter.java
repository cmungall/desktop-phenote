package phenote.dataadapter.birn;

import java.io.UnsupportedEncodingException;
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

import org.obo.owl.dataadapter.OWLAdapter;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharFieldException;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;

public class SoapAdapter implements QueryableDataAdapterI {

  private OWLAdapter owlAdapter = new OWLAdapter();

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
          //String q = get(c,"Q");
          String q = "http://purl.org/obo/owl/PATO#PATO_0000639";
          String e2 = get(c,"E2");
          String rawIm = get(c,"imagename"); // imagename?
          String warpIm = null; // add to config
          String thumb = null;
          int sliceId = getSliceId(c);
          String imRegId = get(c,"regions"); // regions? int?

          String id = ws.createPhenoTypeFromSmartAtlas
            (poly,isCoronal,comm,own,e,q,e2,rawIm,warpIm,thumb,0,imRegId);
          if (id==null)
            log().error("Commit failed, got null id from ckb/soap");
          else
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
    //try { log().debug("field "+ fieldName+" val "+c.getValueString(fieldName)); 
    // return c.getValueString(fieldName);}
    // gets id for terms, strings/value for free text
    try {
      CharField cf = CharFieldManager.inst().getCharFieldForName(fieldName);
      String v = c.getIdOrValue(fieldName);
      if (cf.isTerm())
        v = oboToCkbId(v);
      
      log().debug("field "+ fieldName+" val "+v);
      return v;
    }
    catch (CharFieldException e) { log().error("Config error "+e); return null; }
  }

  private String oboToCkbId(String oboId) {
    if (oboId==null || oboId.equals("")) return oboId; // ??
    try {
      String ckbId = owlAdapter.getURI(oboId).toString();
      log().debug("converted "+oboId+" to "+ckbId);
      return ckbId;
    }
    catch (UnsupportedEncodingException e) {
      log().error("Failed to convert obo id to owl uri "+e);
      return oboId; // ??
    }
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
