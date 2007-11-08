package phenote.dataadapter.ncbi;

import java.util.regex.*;

// add http://jdbc.postgresql.org/download/postgresql-8.2-504.jdbc4.jar to trunk/jars/ directory

import java.util.ArrayList;
import java.util.List;

import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;

import phenote.edit.EditManager;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;

import java.net.URL;
import java.net.URLConnection;

/**
 * This adapter will utilize the NCBI RESTful toolkit to query OMIM IDs and
 * return some formatted information.</p>
 * In the future we should probably use their SOAP toolkit.<p>
 * This is the basis for what will become a generic class for ncbi lookup tools<p>
 * Probably should be OMIMAdapter extends NCBIDataAdapterI or something<p>
 * 
 * @author Nicole Washington
 *
 */
public class OMIMAdapter implements QueryableDataAdapterI {

	private static final String OMIMurlBase = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=omim";
	private static final String returnMode = "&retmode=xml";
	private static final String testURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=omim&id=601653&retmode=xml";
	//SNOMED access via id=http://terminology.vetmed.vt.edu/SCT/isa.cfm?SCT_ConceptID=190787008
	//External links for GeneRIF: http://www.ncbi.nlm.nih.gov/sites/entrez?cmd_current=&db=gene&orig_db=genome&term=GeneRIF%3A12551903&cmd=Search
	
	private List<String> queryableFields = new ArrayList<String>(2);
  private List<String> queryableGroups = new ArrayList<String>(2);

  public OMIMAdapter() { init(); }

  public List<String> getQueryableGroups() {
    return queryableGroups;
  }

  public String getOMIMbyID(String id) {
//  public String getOMIMbyID(String id) throws DataAdapterEx {
  	String omimURL = null;
  	URL u = null;
    StringBuffer sb = new StringBuffer();
//  	return "SOME OMIM STUFF HERE";
    try {
      omimURL = OMIMurlBase+"&id="+id+returnMode;
    	u = new URL(omimURL); // throws MalformedURLEx
    } catch (MalformedURLException e) {}
    try {
    URLConnection uc = u.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(
        uc.getInputStream()));
    String inputLine;
    while ((inputLine = in.readLine()) != null) {
    	System.out.println(inputLine);
    	sb.append(inputLine);
    }
    in.close();
    } catch (IOException e) {} ;
    return sb.toString();
   }


  private void init() {
    // dont HAVE to use CharFieldEnum but it does enforce using same strings
    // across different data adapters which is good to enforce
    // the worm config needs to have "Pub" and "Object Name"
  	//might eventually add other ncbi options here
//  	queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
    queryableFields.add("OMIM");
  }
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field) {
    return queryableFields.contains(field);
  }

//  private Connection connectToDB() {
//    Connection c = null;
//    try {
//    	System.out.println("checking to see if online");    	
//    	//ping for internet connection; try the one above?
//    } catch (SQLException se) {
//    	System.out.println("System if offline.  Cannot retrieve information from NCBI");
//      se.printStackTrace();
//      System.out.println("Couldn't connect: stack trace done.");
//      String getMessage = se.getMessage();
//    }
//    if (c != null)
//      System.out.println("Hooray! Connection Available!");
//    else
//      System.out.println("We should never get here.");
//    return c; 
//  } // private Connection connectToDB

//      title = queryPostgresTitle(s, "wpa_title", pubID);
//      name = queryPostgresName(s, "two_standardname", personID);


  private void parseOMIMxml(String omimXML) {
  	
  }
  public CharacterListI query(String group, String field, String query) {
  	return null;
  }

  public void commit(CharacterListI charList) { return;};
  /** The label that gets displayed on the db commit button */
  public String getCommitButtonLabel() { return "NA";}

}


     // public class WormAdapter implements QueryableDataAdapterI
