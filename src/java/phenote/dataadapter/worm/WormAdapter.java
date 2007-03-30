package phenote.dataadapter.worm;

import java.util.regex.*;

// add http://jdbc.postgresql.org/download/postgresql-8.2-504.jdbc4.jar to trunk/jars/ directory

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterList;
import phenote.datamodel.Character;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.TermNotFoundException;
import phenote.datamodel.CharField;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;





public class WormAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);

  public WormAdapter() { init(); }


  public void commit(CharacterListI charList) {
    // if alleleQueried... wipe out allele and insert
    // else if Pub queried... wipe out pub and insert
    // else  - new insert & deletes(from transactions)
  }

  // Returns the first substring in input that matches the pattern.  Returns null if no match found.
  // lifted from  http://www.exampledepot.com/egs/java.util.regex/Greedy.html?l=rel
  public static String find(String patternStr, CharSequence input) {
      Pattern pattern = Pattern.compile(patternStr);
      Matcher matcher = pattern.matcher(input);
      if (matcher.find()) { return matcher.group(); }
      return null;
  }

  private void init() {
    // dont HAVE to use CharFieldEnum but it does enforce using same strings
    // across different data adapters which is good to enforce
    // the worm config needs to have "Pub" and "Allele"
    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
    queryableFields.add(CharFieldEnum.ALLELE.getName()); // "Allele"
    // should their be a check that the current char fields have pub & allele?
  }
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field) {
    return queryableFields.contains(field);
  }

  private Connection connectToDB() {
    System.out.println("Checking if Driver is registered with DriverManager.");	// open postgres database connection
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException cnfe) {
      System.out.println("Couldn't find the driver!");
      System.out.println("Let's print a stack trace, and exit.");
      cnfe.printStackTrace();
      System.exit(1);
    }
    System.out.println("Registered the driver ok, so let's make a connection.");
    Connection c = null;
    try {
      // The second and third arguments are the username and password,
      // respectively. They should be whatever is necessary to connect
      // to the database.
      // c = DriverManager.getConnection("jdbc:postgresql://andiamo.caltech.edu/testdb", "postgres", "");
      // c = DriverManager.getConnection("jdbc:postgresql://131.215.52.86:5432/testdb", "postgres", "");  // andiamo, if postgres is running there
      c = DriverManager.getConnection("jdbc:postgresql://131.215.52.76:5432/testdb", "postgres", "");     // tazendra
      // c = DriverManager.getConnection("jdbc:postgresql://localhost/testdb", "postgres", "");           // with /usr/local/pgsql/data/postgresql.conf set to localhost
      //c = DriverManager.getConnection("jdbc:postgresql://localhost/booktown", "username", "password");        // sample
    } catch (SQLException se) {
      System.out.println("Couldn't connect: print out a stack trace and exit.");
      se.printStackTrace();
      System.exit(1);
    }
    if (c != null)
      System.out.println("Hooray! We connected to the database!");
    else
      System.out.println("We should never get here.");
    return c; 
  } // private Connection connectToDB

  private String queryPostgresCharacter(Statement s, String postgres_table, String default_value, String query, int boxI, int colI) {
    // get the value corresponding to a phenote cell from a postgres table by box and column
    ResultSet rs = null;	// intialize postgres query result
      // get the phenotype term in timestamp order where the allele and box and column number match
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' AND alp_column='"+colI+"' ORDER BY alp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { default_value = rs.getString(4); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
//    System.out.println("Added in function charList term "+query+" column "+colI+" box "+boxI+".");		// comment out later
    return default_value; 
  }

  private CharacterListI queryPostgresCharacterList(CharacterListI charList, Statement s, String joinkey, int boxI) {
      // populate a phenote character based on postgres value by joinkey and box, then append to character list
    String paper = "No Paper assigned";			// initialize paper value
    ResultSet rs = null;	// intialize postgres query result
      // get the papers in timestamp order where the allele and box number match
    try { rs = s.executeQuery("SELECT * FROM alp_paper WHERE joinkey = '"+joinkey+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our joinkey: that probably means our pap SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { paper = rs.getString(3); } } 	// assign the paper
    catch (SQLException se) {
      System.out.println("We got an exception while getting a paper joinkey "+joinkey+" box "+boxI+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

    String curator = "No Curator assigned";
    rs = null;
    try { rs = s.executeQuery("SELECT * FROM alp_curator WHERE joinkey = '"+joinkey+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our query: that probably means our cur SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { curator = rs.getString(3); } } 
    catch (SQLException se) {
      System.out.println("We got an exception while getting a curator joinkey "+joinkey+" box "+boxI+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

    String person = "No Person assigned";
    rs = null;
    try { rs = s.executeQuery("SELECT * FROM alp_person WHERE joinkey = '"+joinkey+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our query: that probably means our per SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { person = rs.getString(3); } } 
    catch (SQLException se) {
      System.out.println("We got an exception while getting a person joinkey "+joinkey+" box "+boxI+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

    String phen_text_remark = "No Phenotype Text Remark assigned";
    rs = null;
    try { rs = s.executeQuery("SELECT * FROM alp_phenotype WHERE joinkey = '"+joinkey+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our query: that probably means our ptr SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { phen_text_remark = rs.getString(3); } } 
    catch (SQLException se) {
      System.out.println("We got an exception while getting a phenotype joinkey "+joinkey+" box "+boxI+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

    String remark = "No Other Remark assigned";
    rs = null;
    try { rs = s.executeQuery("SELECT * FROM alp_remark WHERE joinkey = '"+joinkey+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our query: that probably means our orem SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { remark = rs.getString(3); } } 
    catch (SQLException se) {
      System.out.println("We got an exception while getting a remark joinkey "+joinkey+" box "+boxI+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

    String genetic_interaction = "No Genetic Interaction assigned";
    rs = null;
    try { rs = s.executeQuery("SELECT * FROM alp_intx_desc WHERE joinkey = '"+joinkey+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our query: that probably means our gint SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { genetic_interaction = rs.getString(3); } } 
    catch (SQLException se) {
      System.out.println("We got an exception while getting a intx_desc joinkey "+joinkey+" box "+boxI+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

    int columns = 0;		// the number of columns in that box
    rs = null;
      // grab the highest number column for that box and that allele
    try { rs = s.executeQuery("SELECT alp_column FROM alp_term WHERE joinkey = '"+joinkey+"' AND alp_box = '"+boxI+"' ORDER BY alp_column DESC"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our query: that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { if (rs.next()) { { columns = rs.getInt(1); } } }		// assign the highest number column for that box and that allele to the number of columns
    catch (SQLException se) {
      System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" box "+boxI+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

//    System.out.println("We have "+columns+" columns in box "+boxI+".");		// comment out later

    try {
    for (int colI=1; colI<columns+1; colI++) {					// for each of those columns
      Character c1 = new Character();						// create a new character for a phenote row
      c1.setValue("Allele",joinkey);						// assign the allele
      c1.setValue("Pub",paper);
      c1.setValue("Curator",curator);
      c1.setValue("Person",person);
      c1.setValue("Phenotype Text Remark",phen_text_remark);
      c1.setValue("Other Remark",remark);
      c1.setValue("Genetic Interaction",genetic_interaction);
      String postgres_value = "No Phenotype assigned";
      String postgres_table = "alp_term";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Phenotype",postgres_value);					// assign the queried value
      postgres_table = "alp_phen_remark"; postgres_value = "No Phenotype Remark assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Phenotype Remark",postgres_value);				// assign the queried value
      postgres_table = "alp_anat_term"; postgres_value = "No Anatomy assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
//      c1.setValue("Anatomy",postgres_value);					// this doesn't work, assigning whatever term name(s) is in postgres
//      c1.setValue("Anatomy","WBbt:0004758");			 		// this works, assigning a term ID
      postgres_table = "alp_lifestage"; postgres_value = "No Stage assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Stage",postgres_value);					// assign the queried value
      postgres_table = "alp_nature"; postgres_value = "No Nature of Allele assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Allele Nature",postgres_value);				// assign the queried value
      postgres_table = "alp_func"; postgres_value = "No Functional Change assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Functional Change",postgres_value);				// assign the queried value
      postgres_table = "alp_temperature"; postgres_value = "No Temperature assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Temperature",postgres_value);				// assign the queried value
      postgres_table = "alp_preparation"; postgres_value = "No Preparation assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Preparation",postgres_value);				// assign the queried value
      postgres_table = "alp_penetrance"; postgres_value = "No Penetrance assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Penetrance",postgres_value);				// assign the queried value
      postgres_table = "alp_percent"; postgres_value = "No Penetrance Percent assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Penetrance Percent",postgres_value);				// assign the queried value
      postgres_table = "alp_range"; postgres_value = "No Penetrance Range assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Penetrance Range",postgres_value);				// assign the queried value
      postgres_table = "alp_quantity_remark"; postgres_value = "No Quantity Remark assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Quantity Remark",postgres_value);				// assign the queried value
      postgres_table = "alp_heat_sens"; postgres_value = "No Heat Sensitive assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Heat Sensitive",postgres_value);				// assign the queried value
      postgres_table = "alp_heat_degree"; postgres_value = "No Heat Sensitive Degree assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Heat Sensitive Degree",postgres_value);				// assign the queried value
      postgres_table = "alp_cold_sens"; postgres_value = "No Cold Sensitive assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Cold Sensitive",postgres_value);				// assign the queried value
      postgres_table = "alp_cold_degree"; postgres_value = "No Cold Sensitive Degree assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Cold Sensitive Degree",postgres_value);			// assign the queried value
      postgres_table = "alp_mat_effect"; postgres_value = "No Maternal Effect assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Maternal Effect",postgres_value);				// assign the queried value
      postgres_table = "alp_pat_effect"; postgres_value = "No Paternal Effect assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Paternal Effect",postgres_value);				// assign the queried value
      postgres_table = "alp_genotype"; postgres_value = "No Genotype assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Genotype",postgres_value);				// assign the queried value
      postgres_table = "alp_strain"; postgres_value = "No Strain assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Strain",postgres_value);				// assign the queried value
      postgres_table = "alp_delivered"; postgres_value = "No Delivered By assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Delivered By",postgres_value);				// assign the queried value
      charList.add(c1);								// add the character to the character list
    } // for (int colI=1; colI<columns; colI++)
        }
        catch (TermNotFoundException e) {
          System.out.println("Term Not Found Exception, assigning characters by Allele."); }
        catch (CharFieldException e) {
          System.out.println("Char Field Exception, assigning characters by Allele."); }
    return charList; 
  } // private CharacterListI queryPostgresCharacterList(CharacterList charList, Statement s, String joinkey, int boxI)

  public CharacterListI query(String field, String query) throws DataAdapterEx {
//    String m = "Worm adapter query not yet implemented. field: "+field+" query: "+query;
//    JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);

    String alleleString = "Allele";			// the query could be for Allele or Pub
    String pubString = "Pub";
//    System.out.println("Querying field "+field+" query "+query+" end");
//    if (field.equals(alleleString)) { System.out.println("Yes Allele"); } else { System.out.println("Not Allele"); }

    CharacterListI charList = new CharacterList();	// create the CharacterList that we will return

    if (field.equals(alleleString)) {			// if querying the allele, get allele data
      Connection c = connectToDB();
      Statement s = null;
      try { s = c.createStatement(); }
        catch (SQLException se) {
        System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }

      ResultSet rs = null;	// intialize postgres query result
      String joinkey = query;
      try { rs = s.executeQuery("SELECT * FROM alp_tempname WHERE alp_tempname = '"+joinkey+"'");	}	// find the allele that matches the queried allele
      catch (SQLException se) {
        System.out.println("We got an exception while executing our query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      int foundAllele = 0;
      try {
        while (rs.next()) {
//            System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3));	// comment out later
            foundAllele++; } }		// if there's a result in postgres we have found an allele
      catch (SQLException se) {
        System.out.println("We got an exception while getting a tempname joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
        se.printStackTrace(); System.exit(1); }

      if (foundAllele <= 0) { throw new DataAdapterEx("Worm query of "+joinkey+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
        else {		// if there is a match
          int boxes = 0;	// the number of boxes for that allele
          rs = null;		// init result from postgres query
          try { rs = s.executeQuery("SELECT alp_box FROM alp_curator WHERE joinkey = '"+joinkey+"' ORDER BY alp_box DESC"); }	// grab the highest number box for that allele
          catch (SQLException se) {
            System.out.println("We got an exception while executing our query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { rs.next(); { boxes = rs.getInt(1); } }		// assign the highest number box for that joinkey to the number of boxes
          catch (SQLException se) {
            System.out.println("We got an exception while getting a curator joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); System.exit(1); }
//          System.out.println("We have "+boxes+" boxes");	// comment out later
          for (int boxI=1; boxI<boxes+1; boxI++) {		// for each of those boxes
            charList = queryPostgresCharacterList(charList, s, joinkey, boxI);
          } // for (int boxI=1; boxI<boxes; boxI++)
          return charList; 
      } // end -- if there's an allele found in postgres

    } else if (field.equals(pubString)) {						// if querying the publication, get paper data
      Connection c = connectToDB();
      Statement s = null;
      try { s = c.createStatement(); }
       catch (SQLException se) {
        System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }
      Statement s2 = null;
      try { s2 = c.createStatement(); }
       catch (SQLException se) {
        System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }

      String match = find("WBPaper[0-9]*", query);  	// Find a WBPaper followed by any amount of digits
      if (match != null) { query = match; }		// query for this, otherwise keep the default value
System.out.println("Going to query for "+query+".");
      int foundPaper = 0;				// flag if there are any papers in postgres that match, otherwise give an error warning
      ResultSet rs = null;				// initialize result of query
      try { rs = s2.executeQuery("SELECT DISTINCT(joinkey), alp_box FROM alp_paper WHERE alp_paper ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
      catch (SQLException se) {
        System.out.println("We got an exception while executing our query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      try { if (rs.next()) { foundPaper++; } }
      catch (SQLException se) {
        System.out.println("We got an exception while getting a publication query result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
      if (foundPaper <= 0) { throw new DataAdapterEx("Worm query of "+query+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
        else {
          ResultSet rs2 = null;				// initialize result of query
          try { rs2 = s2.executeQuery("SELECT DISTINCT(joinkey), alp_box FROM alp_paper WHERE alp_paper ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
          catch (SQLException se) {
            System.out.println("We got an exception while executing our query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { while (rs2.next()) {								// while there's data in postgres
            String joinkey = "No Allele assigned for Publication "+query+".";			// initialize allele in each phenote character Row
            int boxI = 0;
            joinkey = rs2.getString(1); 							// get the allele from postgres
            boxI = rs2.getInt(2);								// get the big box from postgres
            charList = queryPostgresCharacterList(charList, s, joinkey, boxI);
          } }
          catch (SQLException se) {
            System.out.println("We got an exception while getting a publication query result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
          return charList; 
      } // end -- if there's a publication found in postgres

    } else {
      // if query has failed...
      throw new DataAdapterEx("Worm query of "+query+" of field "+field+" failed");
    }
  } // public CharacterListI query(String field, String query) throws DataAdapterEx

}


//    for (CharacterI chr : charList.getList()) {
//      System.out.println("Chr "+chr+" Phenoset "+phenoset+" end");
//        
//      // builds Phenoset from characters
//      addCharAndGenotypeToPhenoset(chr,phenoset);
//    }

     // public class WormAdapter implements QueryableDataAdapterI
