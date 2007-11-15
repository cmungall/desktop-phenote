package phenote.dataadapter.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceLocator;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceSoap;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.EFetchRequest;
import gov.nih.nlm.ncbi.www.soap.eutils.efetch.EFetchResult;

import java.util.List;

import phenote.dataadapter.DataAdapterEx;
import org.obo.datamodel.Instance;

/**
 * Note, for retmode, see http://www.ncbi.nlm.nih.gov/entrez/query/static/efetchlit_help.html for options
 * @author Nicole
 *
 */
public interface NCBIDataAdapterI {
	
	public static String ncbiURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?";
	
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field);

  /** returns a list of group strings of groups that can be queried for with query
      method */
  public List<String> getQueryableGroups();

  /** Throws exception if query fails, and no data to return
      Query with query string of type field for group */
  public String query(String id, String database)
  	throws DataAdapterEx;
  
  public Instance query(String id);
//This function tends to have thse type of calls:
//  EUtilsServiceLocator service = new EUtilsServiceLocator();
//  EUtilsServiceSoap utils = service.geteUtilsServiceSoap();
//  // call NCBI EFetch utility
//  EFetchRequest parameters = new EFetchRequest();
//  parameters.setDb(database);
//  parameters.setId(idNum);
//  EFetchResult res = utils.run_eFetch(parameters);
//  // results output
//  if (res!=null) {
//  for(int i=0; i<res.getPubmedArticleSet().getPubmedArticle().length; i++)
//  {
//  	sb.append("ID: "+res.getPubmedArticleSet().getPubmedArticle()[i].getMedlineCitation().getPMID());
//    sb.append("Abstract: "+res.getPubmedArticleSet().getPubmedArticle()[i].getMedlineCitation().getArticle().get_abstract().getAbstractText());
//    sb.append("--------------------------\n");
//   
//      System.out.println("ID: "+res.getPubmedArticleSet().getPubmedArticle()[i].getMedlineCitation().getPMID());
//      System.out.println("Abstract: "+res.getPubmedArticleSet().getPubmedArticle()[i].getMedlineCitation().getArticle().get_abstract().getAbstractText());
//      System.out.println("--------------------------\n");
//      
//  }
//  } else { sb.append("result is null"); }
//}
//catch(Exception e) { System.out.println(e.toString()); }

  
  /** The label that gets displayed on the lookup button */
  public String getLookupButtonImage();

  public String getName();
  
  
  
}

