package phenote.dataadapter.ncbi;

import java.util.List;

import phenote.dataadapter.DataAdapterEx;

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
  
  /** The label that gets displayed on the lookup button */
  public String getLookupButtonImage();

  public String getName();
  
}

