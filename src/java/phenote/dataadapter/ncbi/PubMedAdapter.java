package phenote.dataadapter.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceLocator;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceSoap;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.ArticleType;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.AuthorListType;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.EFetchRequest;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.JournalType;

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

  public String query(String id, String database) {

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

    //this is the eutils soapy code
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
        if (res!=null) {
        	for(int i=0; i<res.getPubmedArticleSet().getPubmedArticle().length; i++)
        	{
        		ArticleType article = res.getPubmedArticleSet().getPubmedArticle()[i].getMedlineCitation().getArticle();
        		sb.append(article.getAuthorList().getAuthor(0).getCollectiveName()+" et al.; "+article.getArticleTitle()+"; "+article.getJournal().getTitle());
//      		pubInstance.addPropertyValue(OBOProperty.IS_A, Value<String>article.getAuthorList());
        		System.out.println(id+" added: "+sb.toString());

        	}
        } else { sb.append("result is null"); }
    }
    catch(Exception e) { System.out.println("Caught: "+e.toString()); }
    
    return sb.toString();  	
  }
  
  public Instance query (String id) {
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
        if (res!=null) {
        	//just get the first one.
        	for(int i=0; i<res.getPubmedArticleSet().getPubmedArticle().length; i++)
        	{
        		ArticleType article = res.getPubmedArticleSet().getPubmedArticle()[i].getMedlineCitation().getArticle();
        		pubInstance = (Instance)session.getObjectFactory().createObject(id, AnnotationOntology.PUBLICATION(), false);        	
        		pubInstance.setName(article.getArticleTitle());
        		pubInstance.setDefinition(article.get_abstract().getAbstractText());
        		
        		pubInstance.setComment(makeCitationString(article));
        	}
        } else { pubInstance=null; }
    }
    catch(Exception e) { System.out.println(e.toString()); pubInstance=null;}
    
    //add the instance

    return pubInstance;
  }

    private String makeCitationString (ArticleType article) {
    	//this ought to e a util
    	String citation = "";
    	String authors="";
    	String year="";
    	String title="";
    	String journalName="";
    	String volPages="";

    	AuthorListType authorList = article.getAuthorList();
    	JournalType journal = article.getJournal();
  		for (int j=0; j<authorList.getAuthor().length; j++) { //grab each author
  			authors+=authorList.getAuthor()[j].getInitials()+" "+authorList.getAuthor()[j].getLastName();
  			if (j<authorList.getAuthor().length-1) 
  				authors+=", ";
  		}
  		
  		if (journal.getJournalIssue()!=null) {
    		year=article.getJournal().getJournalIssue().getPubDate().getYear();
  			volPages=article.getJournal().getJournalIssue().getVolume()+":";
  			if (article.getPagination()!=null)
  				volPages+=article.getPagination().getMedlinePgn();
  			else
  				volPages+="(unknown pages)";
  		} else {year="(no date on record)";}
  		if (journal!=null)
  			journalName = article.getJournal().getTitle();
  		title = article.getArticleTitle();

  		citation = authors+". "+year+". <b>"+title+"</b>. <i>"+journalName+"</i> "+volPages;

    	return citation;
    } 
  
  public String getLookupButtonImage() { return "omim_logo.png";}

  public String getName() { return database; }
  
  
}
  //this is the old restful code
//try {
//urlString = PubMedurlBase+"&id="+idNum+returnMode;
//    System.out.println(urlString);
//  	u = new URL(urlString); // throws MalformedURLEx
//  } catch (MalformedURLException e) {}
//  try {
//  URLConnection uc = u.openConnection();
//  BufferedReader in = new BufferedReader(new InputStreamReader(
//      uc.getInputStream()));
//  String inputLine;
//  while ((inputLine = in.readLine()) != null) {
//  	System.out.println(inputLine);
//  	sb.append(inputLine);
//  }
//  in.close();
//  } catch (IOException e) {} ;
