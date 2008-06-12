package phenote.dataadapter.birn;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;
import org.apache.log4j.Logger;

/** birn soap class */
import edu.PhenoWSService;
import edu.PhenoWS;
import edu.Exception_Exception;
//import edu.ucsd.ccdb.PhenoWS;
//import edu.ucsd.ccdb.PhenoWSService;
//import edu.ucsd.ccdb.PhenoWSServiceLocator;

import org.obo.owl.dataadapter.OWLAdapter;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.TermNotFoundException;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.edit.EditManager;

public class SoapAdapter implements QueryableDataAdapterI {

  private static final String CKB_ID_FIELD = "CKB_ID";

  /** for converting obo ids to owly uris for ckb */ 
  private OWLAdapter owlAdapter = new OWLAdapter();

  public void commit(CharacterListI charList) {

    //    try {
      //PhenoWSService soap = new PhenoWSServiceLocator();
      PhenoWSService soap = new PhenoWSService();
      //PhenoWS ws = soap.getphenoWSPort();
      PhenoWS ws = soap.getPhenoWSPort();
      doDeletes(ws); // go thru transaction list
      doInsertsAndUpdates(ws,charList);
      //   }
//     catch (ServiceException e) {
//       log().error("soap failed "+e);
//     }
    
    // clear out transactions!

  }

  /** do inserts & updates to database with soap */
  private void doInsertsAndUpdates(PhenoWS ws, CharacterListI charList) {
    for (CharacterI c : charList.getList()) {
      // new character -> insert
      if (isNew(c)) insertNewCharacter(ws,c);
      // existing character -> update, check if character has actually changed?
      else updateCharacter(ws,c);
    }
  }

  private boolean isNew(CharacterI c) {
    return !c.hasValue(CKB_ID_FIELD);
  }

  /** new character (no db id), insert into ckb via soap */
  private void insertNewCharacter(PhenoWS ws, CharacterI c) {
    try {
      String poly = get(c,"coordinates");
      boolean isCoronal = isCoronal(c); // need config field for this?
      String comm = get(c,"comm");
      String own = get(c,"userName");
      //String dummy = "http://purl.org/obo/owl/CL#CL_0000161";
      String e = get(c,"E");
      String q = get(c,"Q");
      //String q = dummy;
      String e2 = get(c,"E2");
      //String e2 = dummy;
      String rawIm = get(c,"imagename"); // imagename?
      String warpIm = null; // add to config
      String thumb = null;
      int sliceId = getSliceId(c);
      String imRegId = get(c,"regions"); // regions? int?
      

      String id = ws.createPhenoTypeFromSmartAtlas
        (poly,isCoronal,comm,own,e,q,e2,rawIm,warpIm,thumb,0,imRegId);
      if (id==null) {
        log().error("Commit failed, got null id from ckb/soap");
        return;
      }
      else {
        log().info("Commit ok, got id "+id);
        setId(id,c);
      }
      
    }
    catch (Exception_Exception e) { log().error("soap failed "+e); }
  }


  /** character has db id, therefore already exists in database, so update it
      should we check if theres actually been changes to char? */
  private void updateCharacter(PhenoWS ws, CharacterI c) {
    //log().info("Updates not yet implemented");
    String ckbID = get(c,CKB_ID_FIELD);
    String poly = get(c,"coordinates");
    boolean isCoronal = isCoronal(c); // need config field for this?
    String comm = get(c,"comm");
    String own = get(c,"userName");
    //String dummy = "http://purl.org/obo/owl/CL#CL_0000161";
    String e = get(c,"E");
    String q = get(c,"Q");
    //String q = dummy;
    String e2 = get(c,"E2");
    //String e2 = dummy;
    String rawIm = get(c,"imagename"); // imagename?
    String warpIm = null; // add to config
    String thumb = null;
    int sliceId = getSliceId(c);
    String imRegId = get(c,"regions"); // regions? int?
    try {
      ws.updatePhenoType(ckbID,poly, isCoronal, comm, own, e, q, e2,rawIm,
                         warpIm,thumb,sliceId,imRegId);
      // no exception - success?
      log.info("Update for "+ckbID+" seems to have succeeded");
    } catch (Exception_Exception x) {
      log().error("Update failed for CKB ID "+ckbID);
    }
  }

  /** go thru delete transactions and send delete to soap if there is a valid 
      ckb id */
  private void doDeletes(PhenoWS ws) {
    for (CharacterI del : EditManager.inst().getDeletedAnnotations()) {
      if (del.hasValue(CKB_ID_FIELD)) { // dont delete if doesnt have id
        String id = get(del,CKB_ID_FIELD);
        try {
          ws.deletePhenotype(id);
          log().info("Deleted "+id);
        }
        catch (Exception_Exception e) {
          log().error("Delete failed for "+id);
        }
      }
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
      
      if (v==null || v.equals("")) v = null;
      
      log().debug("field "+ fieldName+" val "+v);
      return v;
    }
    catch (CharFieldException e) { log().error("Config error "+e); return null; }
  }

  /** commit suceeded, got id from commit, set it in phenote datamodel */
  private void setId(String id, CharacterI c) {
    try { c.setValue(CKB_ID_FIELD,id); }
    catch (CharFieldException e) { log().error("Failed to set ckb id in phenote "+e); }
    catch (TermNotFoundException x) { log().error(x); } // shouldnt happen
  }

  private String oboToCkbId(String oboId) {
    if (oboId==null || oboId.equals("")) return null; // oboId; // ??
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
