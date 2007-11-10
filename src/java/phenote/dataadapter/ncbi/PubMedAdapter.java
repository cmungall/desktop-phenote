package phenote.dataadapter.ncbi;

import java.util.regex.*;

// add http://jdbc.postgresql.org/download/postgresql-8.2-504.jdbc4.jar to trunk/jars/ directory

import java.util.ArrayList;
import java.util.List;

import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.dataadapter.ncbi.NCBIDataAdapterI;

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
//public class OMIMAdapter implements QueryableDataAdapterI {
	public class PubMedAdapter implements NCBIDataAdapterI {

//	private static final String OMIMurlBase = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=omim";
		private static final String database = "pubmed";
	private static final String PubMedurlBase = ncbiURL+"db="+database;
	private static final String returnMode = "&retmode=xml&rettype=abstract";
	private static final String testURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=omim&id=601653&retmode=xml";
	//SNOMED access via id=http://terminology.vetmed.vt.edu/SCT/isa.cfm?SCT_ConceptID=190787008
	//External links for GeneRIF: http://www.ncbi.nlm.nih.gov/sites/entrez?cmd_current=&db=gene&orig_db=genome&term=GeneRIF%3A12551903&cmd=Search
	private List<String> queryableFields = new ArrayList<String>(2);
  private List<String> queryableGroups = new ArrayList<String>(2);

  public PubMedAdapter() { init(); }

  public List<String> getQueryableGroups() {
    return queryableGroups;
  }

  public String getOMIMbyID(String id) {
//  public String getOMIMbyID(String id) throws DataAdapterEx {
  	String urlString = null;
  	URL u = null;
    StringBuffer sb = new StringBuffer();
//  	return "SOME OMIM STUFF HERE";
    String[] splitID=id.split(":");
    String idSpace= splitID[0];
    String idNum = splitID[1];
    try {
      urlString = PubMedurlBase+"&id="+idNum+returnMode;
      System.out.println(urlString);
    	u = new URL(urlString); // throws MalformedURLEx
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
  	//should i add charfieldenum type here?  
  	//should there be an enumerated ncbi type  ...that is defined
  	//by their list of databases?  how to generalize?
  	//  	queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
    queryableFields.add("PUB");
  }
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field) {
    return queryableFields.contains(field);
  }

  private void parseOMIMxml(String omimXML) {
  	
  }
  public String query(String id, String database) {
  	String urlString = null;
  	URL u = null;
    StringBuffer sb = new StringBuffer();
    String idSpace = null;
    String idNum = null;
    if (id!=null) {
    	String[] splitID=id.split(":");
    	if (splitID.length>1) {
    		idSpace= splitID[0];
    		idNum = splitID[1];
    	}
    }
    if (database.equalsIgnoreCase(idSpace)) {
    	//make sure the idspace and database match
    	System.out.println("same database!");
    }
    try {
      urlString = PubMedurlBase+"&id="+idNum+returnMode;
      System.out.println(urlString);
    	u = new URL(urlString); // throws MalformedURLEx
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

  public String getLookupButtonImage() { return "omim_logo.png";}

  public String getName() { return database; }
  
}