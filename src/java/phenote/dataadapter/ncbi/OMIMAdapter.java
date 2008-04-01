package phenote.dataadapter.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceLocator;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceSoap;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.EFetchRequest;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.MimEntryType;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.MimEntry_clinicalSynopsisType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.obo.annotation.datamodel.AnnotationOntology;
import org.obo.datamodel.Instance;
import org.obo.datamodel.OBOSession;

import phenote.datamodel.CharFieldManager;

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
	public class OMIMAdapter implements NCBIDataAdapterI {

//	private static final String OMIMurlBase = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=omim";
		private static final String database = "omim";
	private static final String OMIMurlBase = ncbiURL+"db="+database;
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
    String[] splitID=id.split(":");
    String idSpace= splitID[0];
    String idNum = splitID[1];
    try {
      omimURL = OMIMurlBase+"&id="+idNum+returnMode;
      System.out.println(omimURL);
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

  public String query(String id, String database) {
  	String omimURL = null;
  	URL u = null;
    StringBuffer sb = new StringBuffer();
    String[] splitID=id.split(":");
    String idSpace= splitID[0];
    String idNum = splitID[1];
    if (database.equalsIgnoreCase(idSpace)) {
    	//make sure the idspace and database match
    	System.out.println("same database!");
    }
    try {
      omimURL = OMIMurlBase+"&id="+idNum+returnMode;
      System.out.println(omimURL);
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
  
  public Instance query(String id) {
  	Instance pubInstance=null;
		OBOSession session = CharFieldManager.inst().getOboSession();
    String idSpace = null;
    String idNum = null;
    if (id!=null) {
    	String[] splitID=id.split(":");
    	if (splitID.length>1) {
    		idSpace= splitID[0];
    		idNum = splitID[1];
    	}
    }
    try
    {
        EUtilsServiceLocator service = new EUtilsServiceLocator();
        EUtilsServiceSoap utils = service.geteUtilsServiceSoap();
        // call NCBI EFetch utility
        EFetchRequest parameters = new EFetchRequest();
        parameters.setDb(database);
        parameters.setId(idNum);
        EFetchResult res = utils.run_eFetch(parameters);
        // results output
        MimEntryType mimEntry = null;
        if (res!=null && res.getMimEntries()!=null) {
        	//just get the first one.
        	for(int i=0; i<res.getMimEntries().getMimEntry().length; i++)
        	{	
        		mimEntry = res.getMimEntries().getMimEntry()[i];
        		pubInstance = (Instance)session.getObjectFactory().createObject(id, AnnotationOntology.PUBLICATION(), false);        	
        		if (mimEntry.getMimEntry_title()!=null)
        			pubInstance.setName(mimEntry.getMimEntry_title());
        		
        		if (mimEntry.getMimEntry_synonyms()!=null) {
        			for (int j=0; j<mimEntry.getMimEntry_synonyms().getMimEntry_synonyms_E().length; j++) {
//        			pubInstance.addSynonym(mimEntry.getMimEntry_synonyms().getMimEntry_synonyms_E()[j]);
        			}
        		}
        		if (mimEntry.getMimEntry_clinicalSynopsis()!=null)
        			pubInstance.setDefinition(mimEntry.getMimEntry_clinicalSynopsis().toString());
        		if (mimEntry.getMimEntry_summary()!=null)
        			pubInstance.setDefinition(mimEntry.getMimEntry_summary().getMimText(0).getMimText_text());   
        		pubInstance.setComment(makeCitationString(mimEntry));
        	}
        } else { pubInstance=null; }
    }
    catch(Exception e) { System.out.println(e.toString()); pubInstance=null;}
    
    //add the instance

    return pubInstance;
  }


  public String getLookupButtonImage() { return "omim_logo.png";}

  public String getName() { return database; }
  
  private String makeCitationString (MimEntryType mimEntry) {
  	//this ought to e a util
  	String citation = "";
  	String synonyms="";
  	String synopsis="";
  	String summary="";
  	String symbol=" ";
  	String title="";
  	String allelicVariants="";
  	MimEntry_clinicalSynopsisType clinicalSynopsis = mimEntry.getMimEntry_clinicalSynopsis();

  	if (mimEntry.getMimEntry_symbol()!=null)
  		symbol = mimEntry.getMimEntry_symbol();
  	if (mimEntry.getMimEntry_title()!=null)
  		title = mimEntry.getMimEntry_title();
  	
  	if (mimEntry.getMimEntry_synonyms()!=null) {
  		for (int j=0; j<mimEntry.getMimEntry_synonyms().getMimEntry_synonyms_E().length; j++) {
  			synonyms+=mimEntry.getMimEntry_synonyms().getMimEntry_synonyms_E()[j]+", ";
  		}
  	}
  	if (clinicalSynopsis!=null) {
  		for (int j=0; j<clinicalSynopsis.getMimIndexTerm().length; j++) {
  			String key = clinicalSynopsis.getMimIndexTerm()[j].getMimIndexTerm_key();
  			String terms = "";
  			for (int k=0; k<clinicalSynopsis.getMimIndexTerm()[j].getMimIndexTerm_terms().getMimIndexTerm_terms_E().length; k++) {
  				terms+=clinicalSynopsis.getMimIndexTerm()[j].getMimIndexTerm_terms().getMimIndexTerm_terms_E()[k]+", ";
  			}
  			synopsis+=key+": "+terms+"<br>";
  		}
  	}
  	if (mimEntry.getMimEntry_summary()!=null) {
  		for (int j=0; j<mimEntry.getMimEntry_summary().getMimText().length; j++) {
  			summary+=mimEntry.getMimEntry_summary().getMimText()[j].getMimText_label()+": "+
  				mimEntry.getMimEntry_summary().getMimText()[j].getMimText_text()+"<br>";
  		}
  	}
  	if (mimEntry.getMimEntry_text()!=null) {
  		for (int j=0; j<mimEntry.getMimEntry_text().getMimText().length; j++) {
  			summary+=mimEntry.getMimEntry_text().getMimText()[j].getMimText_label()+": "+
				mimEntry.getMimEntry_text().getMimText()[j].getMimText_text()+"<br>";
  			//in here should create links for any omim or ref link...
  		}  		
  	}
  	if (mimEntry.getMimEntry_allelicVariants()!=null) {
  		for (int j=0; j<mimEntry.getMimEntry_allelicVariants().getMimAllelicVariant().length; j++) {
  			allelicVariants+=mimEntry.getMimEntry_allelicVariants().getMimAllelicVariant()[j].getMimAllelicVariant_name()+
  			" ("+mimEntry.getMimEntry_allelicVariants().getMimAllelicVariant()[j].getMimAllelicVariant_number()+")<br> ";
  		}
  	}

		citation = "<b>"+title + "("+ symbol+ ")</b><br>"+synonyms+"<br><br>Summary:<br>"+summary+"Allelic Variants:<br>"+allelicVariants+"<br>Clinical Synopsis:<br>"+synopsis;

  	return citation;
  } 
}