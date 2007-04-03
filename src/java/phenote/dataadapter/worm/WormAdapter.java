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
import java.sql.PreparedStatement;





public class WormAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);

  public WormAdapter() { init(); }


  public String getCommitButtonLabel() {
    return "Commit To Worm DB";
  }

  public void commit(CharacterListI charList) {
//    String m = "Worm adapter commit not yet implemented.";
//    JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);
    Connection c = connectToDB();
    Statement s = null;
    try { s = c.createStatement(); }
      catch (SQLException se) {
      System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }
    for (CharacterI chr : charList.getList()) {
//      System.out.println("Chr "+chr+" end");
      try {
        String allele = chr.getValueString("Allele");
//         System.out.println( "Allele : "+allele+" end.");
        String paper = chr.getValueString("Pub");
//         System.out.println( "Pub : "+paper+" end.");
        String curator = chr.getValueString("Curator");
//         System.out.println( "Curator : "+curator+" end.");
        String person = chr.getValueString("Person");
//         System.out.println( "Person : "+person+" end.");
        String phen_text_remark = chr.getValueString("Phenotype Text Remark");
//         System.out.println( "Phenotype Text Remark : "+phen_text_remark+" end.");
        String remark = chr.getValueString("Other Remark");
//         System.out.println( "Other Remark : "+remark+" end.");
        String genetic_interaction = chr.getValueString("Genetic Interaction");
//         System.out.println( "Genetic Interaction : "+genetic_interaction+" end.");
        String app_term = chr.getValueString("Phenotype");
//         System.out.println( "Phenotype : "+app_term+" end.");
        String app_phen_remark = chr.getValueString("Phenotype Remark");
//         System.out.println( "Phenotype Remark : "+app_phen_remark+" end.");
        String app_anat_term = chr.getValueString("Anatomy");
//         System.out.println( "Anatomy : "+app_anat_term+" end.");
        String entity = chr.getValueString("Entity");
//         System.out.println( "Entity : "+entity+" end.");
        String quality = chr.getValueString("Quality");
//         System.out.println( "Quality : "+quality+" end.");
        String app_lifestage = chr.getValueString("Stage");
//         System.out.println( "Stage : "+app_lifestage+" end.");
        String app_nature = chr.getValueString("Allele Nature");
//         System.out.println( "Allele Nature : "+app_nature+" end.");
        String app_func = chr.getValueString("Functional Change");
//         System.out.println( "Functional Change : "+app_func+" end.");
        String app_temperature = chr.getValueString("Temperature");
//         System.out.println( "Temperature : "+app_temperature+" end.");
        String app_preparation = chr.getValueString("Preparation");
//         System.out.println( "Preparation : "+app_preparation+" end.");
        String app_penetrance = chr.getValueString("Penetrance");
//         System.out.println( "Penetrance : "+app_penetrance+" end.");
        String app_percent = chr.getValueString("Penetrance Percent");
//         System.out.println( "Penetrance Percent : "+app_percent+" end.");
        String app_range = chr.getValueString("Penetrance Range");
//         System.out.println( "Penetrance Range : "+app_range+" end.");
        String app_quantity_remark = chr.getValueString("Quantity Remark");
//         System.out.println( "Quantity Remark : "+app_quantity_remark+" end.");
        String app_heat_sens = chr.getValueString("Heat Sensitive");
//         System.out.println( "Heat Sensitive : "+app_heat_sens+" end.");
        String app_heat_degree = chr.getValueString("Heat Sensitive Degree");
//         System.out.println( "Heat Sensitive Degree : "+app_heat_degree+" end.");
        String app_cold_sens = chr.getValueString("Cold Sensitive");
//         System.out.println( "Cold Sensitive : "+app_cold_sens+" end.");
        String app_cold_degree = chr.getValueString("Cold Sensitive");
//         System.out.println( "Cold Sensitive Degree : "+app_cold_degree+" end.");
        String app_mat_effect = chr.getValueString("Maternal Effect");
//         System.out.println( "Maternal Effect : "+app_mat_effect+" end.");
        String app_pat_effect = chr.getValueString("Paternal Effect");
//         System.out.println( "Paternal Effect : "+app_pat_effect+" end.");
        String app_genotype = chr.getValueString("Genotype");
//         System.out.println( "Genotype : "+app_genotype+" end.");
        String app_strain = chr.getValueString("Strain");
//         System.out.println( "Strain : "+app_strain+" end.");
        String app_delevered = chr.getValueString("Delivered By");
//         System.out.println( "Delivered By : "+app_delevered+" end.");
        if (allele != null) {
            int found_allele = 0;
            ResultSet rs = null;	// intialize postgres query result
            String joinkey = allele;
            try { rs = s.executeQuery("SELECT * FROM app_tempname WHERE app_tempname = '"+joinkey+"'");	}	// find the allele that matches the queried allele
            catch (SQLException se) {
              System.out.println("We got an exception while executing our app_tempname query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { while (rs.next()) { 
//              default_value = rs.getString(4); 
//               System.out.println("Found "+joinkey+" allele in app_tempname.");
              found_allele++; } }
            catch (SQLException se) {
              System.out.println("We got an exception while getting a app_tempname result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
            if (found_allele > 0) {
	      PreparedStatement ps = null;	// intialize postgres insert 
              try { ps = c.prepareStatement("INSERT INTO app_tempname VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, allele); }
              catch (SQLException se) {
                System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
	      try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
              catch (SQLException se) { System.out.println("We got an exception while executing an update: possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); } } } 
          else {
            System.out.println("There is no allele for "+chr+" Character"); } 
      } catch (Exception e) {
        System.out.println("Could not get terms from character: " + e);
      }

    } // for (CharacterI chr : charList.getList())
    // if alleleQueried... wipe out allele and insert
    // else if Pub queried... wipe out pub and insert
    // else  - new insert & deletes(from transactions)
  } // public void commit(CharacterListI charList)

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

  private String queryPostgresCharacter(Statement s, String postgres_table, String default_value, String query, int colI) {
    // get the value corresponding to a phenote cell from a postgres table by column
    ResultSet rs = null;	// intialize postgres query result
      // get the phenotype term in timestamp order where the allele and column number match
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+query+"' AND app_column='"+colI+"' ORDER BY app_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { default_value = rs.getString(3); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
//    System.out.println("Added in function charList term "+query+" column "+colI+".");		// comment out later
    return default_value; 
  }

  private CharacterListI queryPostgresCharacterList(CharacterListI charList, Statement s, String joinkey) {
      // populate a phenote character based on postgres value by joinkey, then append to character list
    int columns = 0;		// the number of columns 
    ResultSet rs = null;	// grab the highest number column for that allele
    try { rs = s.executeQuery("SELECT app_column FROM app_term WHERE joinkey = '"+joinkey+"' ORDER BY app_column DESC"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our app_term query: that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { if (rs.getInt(1) > columns) { columns = rs.getInt(1); } } }		// assign the highest number column for that allele to the number of columns
    catch (SQLException se) {
      System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

    try {
    for (int colI=1; colI<columns+1; colI++) {					// for each of those columns
      Character c1 = new Character();						// create a new character for a phenote row
      c1.setValue("Allele",joinkey);						// assign the allele
      String postgres_value = "No Phenotype assigned";
      String postgres_table = "app_term";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Phenotype",postgres_value);					// assign the queried value
      postgres_table = "app_curator"; postgres_value = "No Curator assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Curator",postgres_value);					// assign the queried value
      postgres_table = "app_paper"; postgres_value = "No Pub assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Pub",postgres_value);					// assign the queried value
      postgres_table = "app_person"; postgres_value = "No Person assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Person",postgres_value);					// assign the queried value
      postgres_table = "app_phenotype"; postgres_value = "No Phenotype Text Remark assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Phenotype Text Remark",postgres_value);					// assign the queried value
      postgres_table = "app_remark"; postgres_value = "No Other Remark assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Other Remark",postgres_value);					// assign the queried value
      postgres_table = "app_intx_desc"; postgres_value = "No Genetic Interaction assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Genetic Interaction",postgres_value);					// assign the queried value
      postgres_table = "app_phen_remark"; postgres_value = "No Phenotype Remark assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Phenotype Remark",postgres_value);				// assign the queried value
      postgres_table = "app_anat_term"; postgres_value = "No Anatomy assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
//      c1.setValue("Anatomy",postgres_value);					// this doesn't work, assigning whatever term name(s) is in postgres
//      c1.setValue("Anatomy","WBbt:0004758");			 		// this works, assigning a term ID
      postgres_table = "app_lifestage"; postgres_value = "No Stage assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Stage",postgres_value);					// assign the queried value
      postgres_table = "app_nature"; postgres_value = "No Nature of Allele assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Allele Nature",postgres_value);				// assign the queried value
      postgres_table = "app_func"; postgres_value = "No Functional Change assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Functional Change",postgres_value);				// assign the queried value
      postgres_table = "app_temperature"; postgres_value = "No Temperature assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Temperature",postgres_value);				// assign the queried value
      postgres_table = "app_preparation"; postgres_value = "No Preparation assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Preparation",postgres_value);				// assign the queried value
      postgres_table = "app_penetrance"; postgres_value = "No Penetrance assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Penetrance",postgres_value);				// assign the queried value
      postgres_table = "app_percent"; postgres_value = "No Penetrance Percent assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Penetrance Percent",postgres_value);				// assign the queried value
      postgres_table = "app_range"; postgres_value = "No Penetrance Range assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Penetrance Range",postgres_value);				// assign the queried value
      postgres_table = "app_quantity_remark"; postgres_value = "No Quantity Remark assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Quantity Remark",postgres_value);				// assign the queried value
      postgres_table = "app_heat_sens"; postgres_value = "No Heat Sensitive assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Heat Sensitive",postgres_value);				// assign the queried value
      postgres_table = "app_heat_degree"; postgres_value = "No Heat Sensitive Degree assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Heat Sensitive Degree",postgres_value);				// assign the queried value
      postgres_table = "app_cold_sens"; postgres_value = "No Cold Sensitive assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Cold Sensitive",postgres_value);				// assign the queried value
      postgres_table = "app_cold_degree"; postgres_value = "No Cold Sensitive Degree assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Cold Sensitive Degree",postgres_value);			// assign the queried value
      postgres_table = "app_mat_effect"; postgres_value = "No Maternal Effect assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Maternal Effect",postgres_value);				// assign the queried value
      postgres_table = "app_pat_effect"; postgres_value = "No Paternal Effect assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Paternal Effect",postgres_value);				// assign the queried value
      postgres_table = "app_genotype"; postgres_value = "No Genotype assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Genotype",postgres_value);				// assign the queried value
      postgres_table = "app_strain"; postgres_value = "No Strain assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Strain",postgres_value);				// assign the queried value
      postgres_table = "app_delivered"; postgres_value = "No Delivered By assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
      c1.setValue("Delivered By",postgres_value);				// assign the queried value
      charList.add(c1);								// add the character to the character list
    } // for (int colI=1; colI<columns; colI++)
        }
        catch (TermNotFoundException e) {
          System.out.println("Term Not Found Exception, assigning characters by Allele."); }
        catch (CharFieldException e) {
          System.out.println("Char Field Exception, assigning characters by Allele."); }
    return charList; 
  } // private CharacterListI queryPostgresCharacterList(CharacterList charList, Statement s, String joinkey)

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
      try { rs = s.executeQuery("SELECT * FROM app_tempname WHERE app_tempname = '"+joinkey+"'");	}	// find the allele that matches the queried allele
      catch (SQLException se) {
        System.out.println("We got an exception while executing our app_tempname query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      int foundAllele = 0;
      try {
        while (rs.next()) {
//            System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3));	// comment out later
            foundAllele++; } }		// if there's a result in postgres we have found an allele
      catch (SQLException se) {
        System.out.println("We got an exception while getting a tempname joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
        se.printStackTrace(); System.exit(1); }

      if (foundAllele <= 0) { throw new DataAdapterEx("Worm query of "+joinkey+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
        else { charList = queryPostgresCharacterList(charList, s, joinkey); return charList; }	// if there is a match

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
      int foundPaper = 0;				// flag if there are any papers in postgres that match, otherwise give an error warning
      ResultSet rs = null;				// initialize result of query
      try { rs = s2.executeQuery("SELECT DISTINCT(joinkey) FROM app_paper WHERE app_paper ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
      catch (SQLException se) {
        System.out.println("We got an exception while executing our app_paper query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      try { if (rs.next()) { foundPaper++; } }
      catch (SQLException se) {
        System.out.println("We got an exception while getting a publication query result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
      if (foundPaper <= 0) { throw new DataAdapterEx("Worm query of "+query+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
        else {
          ResultSet rs2 = null;				// initialize result of query
          try { rs2 = s2.executeQuery("SELECT DISTINCT(joinkey) FROM app_paper WHERE app_paper ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
          catch (SQLException se) {
            System.out.println("We got an exception while executing our app_paper query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { while (rs2.next()) {								// while there's data in postgres
            String joinkey = "No Allele assigned for Publication "+query+".";			// initialize allele in each phenote character Row
            joinkey = rs2.getString(1); 							// get the allele from postgres
            charList = queryPostgresCharacterList(charList, s, joinkey);
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
