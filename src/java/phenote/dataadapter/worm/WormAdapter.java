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

import phenote.edit.EditManager;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;





public class WormAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);
  private List<String> queryableGroups = new ArrayList<String>(2);

  public WormAdapter() { init(); }

  public List<String> getQueryableGroups() {
    return queryableGroups;
  }

  public String getCommitButtonLabel() {
    return "Commit To Worm DB";
  }

  public void delete() {
    List<CharacterI> l = EditManager.inst().getDeletedAnnotations();
    try {
      for (CharacterI chr : l) {
        String pgdbid = chr.getValueString("PGDBID");
        if (pgdbid == null) continue;
        System.out.println("Delete "+pgdbid+" end"); }
    } catch (Exception e) {
      System.out.println("Could not delete character: " + e);
    }
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
        String allele = chr.getValueString("Object Name");	// get the allele value from the character, currently could have a column number
        String pgdbid = chr.getValueString("Object Name");	// get the allele value from the character, currently could have a column number
        int colI = 0; int boxI = 0;					// initialize column to zero
        String match = find(".* - ([0-9]+) - [0-9]+", pgdbid);  	// Find a tempname followed by space - space number
        if (match != null) { boxI = Integer.parseInt(match); }	// get the column number if there is one
        match = find(".* - [0-9]+ - ([0-9]+)", pgdbid);  	// Find a tempname followed by space - space number
        if (match != null) { colI = Integer.parseInt(match); }	// get the column number if there is one
        match = find("(.*) - [0-9]+ - [0-9]+", pgdbid);  	// Find a tempname followed by space - space number
        if (match != null) { pgdbid = match; }		// query for this, otherwise keep the default value
        String joinkey = pgdbid;			// assign the tempname / allele to the joinkey 
        if (colI < 1) {					// if there are no columns, find the highest existing column and add 1 to make the next one
          ResultSet rs = null;
          try { rs = s.executeQuery("SELECT app_column FROM app_term WHERE joinkey = '"+joinkey+"' ORDER BY app_column DESC"); }
          catch (SQLException se) {
            System.out.println("We got an exception while executing our app_term query: that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { if (rs.next()) { if (rs.getInt(1) > colI) { colI = rs.getInt(1); } } colI++; }	// get the next highest number column for that allele
          catch (SQLException se) {
            System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); System.exit(1); } }
//         System.out.println( "Allele : "+allele+" end.");
//         System.out.println( "Column : "+colI+" end.");
//         String app_paper = chr.getValueString("Pub");
        String app_paper = chr.getTerm("Pub").getID();
        String postgres_table = "app_paper"; String postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_paper)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_paper); }
//         System.out.println( "Pub : "+app_paper+" end.");
        String app_curator = chr.getValueString("Curator");
        postgres_table = "app_curator"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_curator)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_curator); }
//         System.out.println( "Curator : "+curator+" end.");
        String app_person = chr.getValueString("Person");
        postgres_table = "app_person"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_person)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_person); }
//         System.out.println( "Person : "+person+" end.");
        String app_phenotype = chr.getValueString("NBP");
        postgres_table = "app_phenotype"; postgres_value = "No postgres value assigned";
//         System.out.println( "Phenotype Text Remark : "+app_phenotype+" end.");
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
//         System.out.println( "Phenotype Text Remark Postgres : "+postgres_value+" end.");
        if (postgres_value.equals(app_phenotype)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_phenotype); }
//         System.out.println( "Phenotype Text Remark : "+app_phenotype+" end.");
        String app_remark = chr.getValueString("Reference Remark");
        postgres_table = "app_remark"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_remark)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_remark); }
//         System.out.println( "Reference Remark : "+remark+" end.");
        String app_intx_desc = chr.getValueString("Genetic Interaction");
        postgres_table = "app_intx_desc"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_intx_desc)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_intx_desc); }
//         System.out.println( "Genetic Interaction : "+genetic_interaction+" end.");
        String app_term = chr.getValueString("Phenotype");
        postgres_table = "app_term"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_term)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_term); }
//         System.out.println( "Phenotype : "+app_term+" end.");
        String app_phen_remark = chr.getValueString("Phenotype Remark");
        postgres_table = "app_phen_remark"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_phen_remark)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_phen_remark); }
//         System.out.println( "Phenotype Remark : "+app_phen_remark+" end.");
        String app_anat_term = chr.getValueString("Anatomy");
        postgres_table = "app_anat_term"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_anat_term)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_anat_term); }
//         System.out.println( "Anatomy : "+app_anat_term+" end.");
//         String app_entity = chr.getValueString("Entity");
//         postgres_table = "app_entity"; postgres_value = "No postgres value assigned";
//         postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
//         if (postgres_value.equals(app_entity)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_entity); }
//         System.out.println( "Entity : "+app_entity+" end.");
//         String app_quality = chr.getValueString("Quality");
//         postgres_table = "app_quality"; postgres_value = "No postgres value assigned";
//         postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
//         if (postgres_value.equals(app_quality)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_quality); }
//         System.out.println( "Quality : "+app_quality+" end.");
        String app_lifestage = chr.getValueString("Life Stage");
        postgres_table = "app_lifestage"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_lifestage)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_lifestage); }
//         System.out.println( "Life Stage : "+app_lifestage+" end.");
        String app_nature = chr.getValueString("Allele Nature");
        postgres_table = "app_nature"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_nature)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_nature); }
//         System.out.println( "Allele Nature : "+app_nature+" end.");
        String app_func = chr.getValueString("Functional Change");
        postgres_table = "app_func"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_func)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_func); }
//         System.out.println( "Functional Change : "+app_func+" end.");
        String app_temperature = chr.getValueString("Temperature");
        postgres_table = "app_temperature"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_temperature)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_temperature); }
//         System.out.println( "Temperature : "+app_temperature+" end.");
        String app_preparation = chr.getValueString("Treatment");
        postgres_table = "app_preparation"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_preparation)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_preparation); }
//         System.out.println( "Treatment : "+app_preparation+" end.");
        String app_penetrance = chr.getValueString("Penetrance");
        postgres_table = "app_penetrance"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_penetrance)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_penetrance); }
//         System.out.println( "Penetrance : "+app_penetrance+" end.");
        String app_percent = chr.getValueString("Penetrance Remark");
        postgres_table = "app_percent"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_percent)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_percent); }
//         System.out.println( "Penetrance Remark : "+app_percent+" end.");
        String app_range = chr.getValueString("Penetrance Range Start");
        postgres_table = "app_range"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_range)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_range); }
// //         System.out.println( "Penetrance Range  Start: "+app_range+" end.");
//         String app_range = chr.getValueString("Penetrance Range End");
//         postgres_table = "app_range"; postgres_value = "No postgres value assigned";
//         postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, colI);
//         if (postgres_value.equals(app_range)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_range); }
//         System.out.println( "Penetrance Range End : "+app_range+" end.");
        String app_quantity = chr.getValueString("Quantity");
        postgres_table = "app_quantity"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_quantity)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_quantity); }
//         System.out.println( "Quantity : "+app_quantity+" end.");
        String app_quantity_remark = chr.getValueString("Quantity Remark");
        postgres_table = "app_quantity_remark"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_quantity_remark)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_quantity_remark); }
//         System.out.println( "Quantity Remark : "+app_quantity_remark+" end.");
        String app_heat_sens = chr.getValueString("Heat Sensitive");
        postgres_table = "app_heat_sens"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_heat_sens)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_heat_sens); }
//         System.out.println( "Heat Sensitive : "+app_heat_sens+" end.");
        String app_heat_degree = chr.getValueString("Heat Sensitive Degree");
        postgres_table = "app_heat_degree"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_heat_degree)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_heat_degree); }
//         System.out.println( "Heat Sensitive Degree : "+app_heat_degree+" end.");
        String app_cold_sens = chr.getValueString("Cold Sensitive");
        postgres_table = "app_cold_sens"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_cold_sens)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_cold_sens); }
//         System.out.println( "Cold Sensitive : "+app_cold_sens+" end.");
        String app_cold_degree = chr.getValueString("Cold Sensitive");
        postgres_table = "app_cold_degree"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_cold_degree)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_cold_degree); }
//         System.out.println( "Cold Sensitive Degree : "+app_cold_degree+" end.");
        String app_mat_effect = chr.getValueString("Maternal Effect");
        postgres_table = "app_mat_effect"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_mat_effect)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_mat_effect); }
//         System.out.println( "Maternal Effect : "+app_mat_effect+" end.");
        String app_pat_effect = chr.getValueString("Paternal Effect");
        postgres_table = "app_pat_effect"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_pat_effect)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_pat_effect); }
//         System.out.println( "Paternal Effect : "+app_pat_effect+" end.");
        String app_genotype = chr.getValueString("Genotype");
        postgres_table = "app_genotype"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_genotype)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_genotype); }
//         System.out.println( "Genotype : "+app_genotype+" end.");
        String app_strain = chr.getValueString("Strain");
        postgres_table = "app_strain"; postgres_value = "No postgres value assigned";
        postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
        if (postgres_value.equals(app_strain)) { } else { updatePostgresCol(c, postgres_table, joinkey, colI, app_strain); }
//         System.out.println( "Strain : "+app_strain+" end.");
        if (allele != null) {
            int found_allele = 0;
            ResultSet rs = null;	// intialize postgres query result
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

    delete();
    // if alleleQueried... wipe out allele and insert
    // else if Pub queried... wipe out pub and insert
    // else  - new insert & deletes(from transactions)
  } // public void commit(CharacterListI charList)

  private void updatePostgresCol(Connection c, String postgres_table, String joinkey, int colI, String value) {
    PreparedStatement ps = null;	// intialize postgres insert 
    try { ps = c.prepareStatement("INSERT INTO "+postgres_table+" VALUES (?, ?, ?)"); ps.setString(1, joinkey); ps.setInt(2, colI); ps.setString(3, value); }
    catch (SQLException se) {
      System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
    catch (SQLException se) { System.out.println("We got an exception while executing an update: possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); }
  } // private void updatePostgresCol(String postgres_table, String joinkey, int colI, String value)

  // Returns the first substring in input that matches the pattern.  Returns null if no match found.
  // lifted from  http://www.exampledepot.com/egs/java.util.regex/Greedy.html?l=rel
  public static String find(String patternStr, CharSequence input) {
      Pattern pattern = Pattern.compile(patternStr);
      Matcher matcher = pattern.matcher(input);
//       System.out.println("Pattern "+pattern+" end");
      if (matcher.find()) { return matcher.group(1); }
      return null;
  }

  private void init() {
    // dont HAVE to use CharFieldEnum but it does enforce using same strings
    // across different data adapters which is good to enforce
    // the worm config needs to have "Pub" and "Object Name"
    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
//    queryableFields.add(CharFieldEnum.ALLELE.getName()); // "Allele"
    queryableFields.add("Object Name"); // "Object Name"
    // should their be a check that the current char fields have pub & allele?
    queryableGroups.add("referenceMaker");		// populate reference obo for the main
    queryableGroups.add("default");			// default last
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
      System.out.println("Couldn't connect: stack trace done.");
      String getMessage = se.getMessage();
      getMessage = getMessage + " \nYour IP is not recognized by the postgres database, please contact help@wormbase.org if you're a valid Phenotype WormBase curator.";
      JOptionPane.showMessageDialog(null,getMessage,"Postgres connection error",JOptionPane.INFORMATION_MESSAGE);
//       Throwable throwCause = se.getCause();
//       String getCause = throwCause.toString();
//       JOptionPane.showMessageDialog(null,getCause,"getCause",JOptionPane.INFORMATION_MESSAGE);
//       String m = "Your IP is not recognized by the postgres database, please contact help@wormbase.org if you're a valid Phenotype WormBase curator.";
//       JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);
      // System.exit(1);
      // System.out.println("Couldn't connect: exit done.");
    }
    if (c != null)
      System.out.println("Hooray! We connected to the database!");
    else
      System.out.println("We should never get here.");
    return c; 
  } // private Connection connectToDB

//      title = queryPostgresTitle(s, "wpa_title", pubID);
//      name = queryPostgresName(s, "two_standardname", personID);
  private String queryPostgresName(Statement s, String postgres_table, String personID) {
    String joinkey = find("([0-9]+)", personID);		// Find a WBPaper followed by any amount of digits
    String name = null;
    ResultSet rs = null;	// intialize postgres query result
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY two_timestamp "); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { name = rs.getString(3); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
    return name;
  }
  private String queryPostgresTitle(Statement s, String postgres_table, String pubID) {
    String joinkey = find("([0-9]+)", pubID);		// Find a WBPaper followed by any amount of digits
    String title = null;
    ResultSet rs = null;	// intialize postgres query result
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY wpa_timestamp "); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { title = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } 
    return title;
  }

  private String queryPostgresCharacter(Statement s, String postgres_table, String default_value, String query, int boxI, int colI) {
    // get the value corresponding to a phenote cell from a postgres table by column
    ResultSet rs = null;	// intialize postgres query result
      // get the phenotype term in timestamp order where the allele and column number match
    if (colI > 0) {
      try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+query+"' AND app_column='"+colI+"' AND app_box='"+boxI+"' ORDER BY app_timestamp"); }
      catch (SQLException se) {
        System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
      try { while (rs.next()) { default_value = rs.getString(4); } }		// assign the new term value
      catch (SQLException se) {
        System.out.println("We got an exception while getting a "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } }
    else if (boxI > 0) {
      try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+query+"' AND app_box='"+boxI+"' ORDER BY app_timestamp"); }
      catch (SQLException se) {
        System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
      try { while (rs.next()) { default_value = rs.getString(3); } }		// assign the new term value
      catch (SQLException se) {
        System.out.println("We got an exception while getting a "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } }
    else {
      try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+query+"' ORDER BY app_timestamp"); }
      catch (SQLException se) {
        System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
      try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
      catch (SQLException se) {
        System.out.println("We got an exception while getting a "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } }
//    System.out.println("Added in function charList term "+query+" column "+colI+".");		// comment out later
    if (default_value == null) { default_value = "postgres value is null"; }
    if (default_value == "") { default_value = "postgres value is blank"; }
    return default_value; 
  }

  private CharacterListI queryPostgresCharacterReferenceList(String group, CharacterListI charList, Statement s, String joinkey, int boxI, int colI) {
      // populate a phenote character based on postgres value by joinkey, then append to character list
    try {
      Character c1 = new Character();						// create a new character for a phenote row

      String pubID = null; String title = null; String personID = null; String name = null; String nbp = null;

      String postgres_table = "app_paper"; String postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);
      String paper_match = find("(WBPaper[0-9]*)", postgres_value);		// Find a WBPaper followed by any amount of digits
      if (paper_match != null) { 
         postgres_value = paper_match; 						// query for this, otherwise keep the default value
         pubID = paper_match; 
         title = queryPostgresTitle(s, "wpa_title", pubID); }
//       c1.setValue("Pub",postgres_value);					// assign the queried value
      if (postgres_value == "No postgres value assigned") { } else { c1.setValue("Pub",postgres_value); }					// assign the queried value
      postgres_table = "app_person"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);
      String person_match = find("(WBPerson[0-9]*)", postgres_value);	// Find a WBPerson followed by any amount of digits
      if (person_match != null) { 
        postgres_value = person_match; 					 	// query for this, otherwise keep the default value
        personID = person_match; 
        name = queryPostgresName(s, "two_standardname", personID); }
      if (postgres_value == "No postgres value assigned") { } else { c1.setValue("Person",postgres_value); }					// assign the queried value
      postgres_table = "app_phenotype"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);
      if (postgres_value != null) { nbp = postgres_value; }
      c1.setValue("NBP",postgres_value);					// assign the queried value
      postgres_table = "app_remark"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);
      c1.setValue("Reference Remark",postgres_value);					// assign the queried value

      String refID = WormReferenceGroupAdapter.makeNameFromPubPersonNBP(pubID, title, personID, name, nbp);
      refID = ":" + refID;
      c1.setValue("RefID",refID);				// assign the queried value

      charList.add(c1);								// add the character to the character list
    }
    catch (TermNotFoundException e) {
      System.out.println("Term Not Found Exception, assigning characters in queryPostgresCharacterReferenceList "+e.getMessage()); }
    catch (CharFieldException e) {
      System.out.println("Char Field Exception, assigning characters "+e.getMessage()); }
    return charList; 
  } // private CharacterListI queryPostgresCharacterReferenceList(CharacterList charList, Statement s, String joinkey)

  private CharacterListI queryPostgresCharacterMainList(String group, CharacterListI charList, Statement s, String joinkey, int boxI, int colI) {
      // populate a phenote character based on postgres value by joinkey, then append to character list
    try {
      Character c1 = new Character();						// create a new character for a phenote row

      c1.setValue("Object Name",joinkey);					// assign the allele and the column
      String alleleColumn = joinkey+" - "+boxI+" - "+colI;
      c1.setValue("PGDBID",alleleColumn);					// assign the allele and the column
      String postgres_table = "app_type"; String postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, 0, 0);
      c1.setValue("Object Type",postgres_value);					// assign the queried value

      postgres_value = "No postgres value assigned";
      postgres_table = "app_term";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      String phenotype_match = find("(WBPhenotype[0-9]*)", postgres_value);  	// Find a WBPhenotype followed by any amount of digits
      if (phenotype_match != null) { postgres_value = phenotype_match; }		// query for this, otherwise keep the default value
      if (postgres_value == "No postgres value assigned") { } else { c1.setValue("Phenotype",postgres_value); }					// assign the queried value
//     c1.setValue("Phenotype",postgres_value);					// assign the queried value
      postgres_table = "app_intx_desc"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);
      c1.setValue("Genetic Intx Desc",postgres_value);					// assign the queried value
      postgres_table = "app_curator"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Curator",postgres_value);					// assign the queried value
      postgres_table = "app_phen_remark"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Phenotype Remark",postgres_value);				// assign the queried value
      postgres_table = "app_anat_term"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
//      c1.setValue("Anatomy",postgres_value);					// this doesn't work, assigning whatever term name(s) is in postgres
//      c1.setValue("Anatomy","WBbt:0004758");			 		// this works, assigning a term ID
      postgres_table = "app_lifestage"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
//       c1.setValue("Life Stage",postgres_value);					// assign the queried value
      postgres_table = "app_nature"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Allele Nature",postgres_value);				// assign the queried value
      postgres_table = "app_func"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Functional Change",postgres_value);				// assign the queried value
      postgres_table = "app_temperature"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Temperature",postgres_value);				// assign the queried value
      postgres_table = "app_preparation"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Treatment",postgres_value);				// assign the queried value
      postgres_table = "app_penetrance"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Penetrance",postgres_value);				// assign the queried value
      postgres_table = "app_percent"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Penetrance Remark",postgres_value);				// assign the queried value
      postgres_table = "app_range"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Penetrance Range Start",postgres_value);				// assign the queried value
      postgres_table = "app_quantity"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Penetrance Range End",postgres_value);				// assign the queried value
      postgres_table = "app_quantity"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Quantity",postgres_value);					// assign the queried value
      postgres_table = "app_quantity_remark"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Quantity Remark",postgres_value);				// assign the queried value
      postgres_table = "app_heat_sens"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Heat Sensitive",postgres_value);				// assign the queried value
      postgres_table = "app_heat_degree"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Heat Sensitive Degree",postgres_value);				// assign the queried value
      postgres_table = "app_cold_sens"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Cold Sensitive",postgres_value);				// assign the queried value
      postgres_table = "app_cold_degree"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Cold Sensitive Degree",postgres_value);			// assign the queried value
      postgres_table = "app_mat_effect"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Maternal Effect",postgres_value);				// assign the queried value
      postgres_table = "app_pat_effect"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Paternal Effect",postgres_value);				// assign the queried value
      postgres_table = "app_genotype"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Genotype",postgres_value);				// assign the queried value
      postgres_table = "app_strain"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, colI);
      c1.setValue("Strain",postgres_value);				// assign the queried value


      String pubID = null;
      String title = null;
      String personID = null;
      String name = null;
      String nbp = null;
      postgres_table = "app_paper"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);
      String paper_match = find("(WBPaper[0-9]*)", postgres_value);		// Find a WBPaper followed by any amount of digits
      if (paper_match != null) { postgres_value = paper_match; }	// query for this, otherwise keep the default value
      if (postgres_value == "No postgres value assigned") { } else { pubID = postgres_value; }		// assign the queried value
      postgres_table = "app_person"; postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);
      String person_match = find("(WBPerson[0-9]*)", postgres_value);	// Find a WBPerson followed by any amount of digits
      if (person_match != null) { postgres_value = person_match; }	// query for this, otherwise keep the default value
      if (postgres_value == "No postgres value assigned") { } else { personID = postgres_value; }	// assign the queried value
      postgres_table = "app_phenotype"; postgres_value = "No postgres value assigned";
      nbp = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey, boxI, 0);

      if (pubID != null) { title = queryPostgresTitle(s, "wpa_title", pubID); }
      if (personID != null) { name = queryPostgresName(s, "two_standardname", personID); }

      String refID = WormReferenceGroupAdapter.makeNameFromPubPersonNBP(pubID, title, personID, name, nbp);
      refID = ":" + refID;
      c1.setValue("Ref",refID);				// assign the queried value

      charList.add(c1);								// add the character to the character list
    }
    catch (TermNotFoundException e) {
      System.out.println("Term Not Found Exception, assigning characters in queryPostgresCharacterMainList "+e.getMessage()); }
    catch (CharFieldException e) {
      System.out.println("Char Field Exception, assigning characters "+e.getMessage()); }
    return charList; 
  } // private CharacterListI queryPostgresCharacterMainList(CharacterList charList, Statement s, String joinkey)

  public CharacterListI query(String group, String field, String query) throws DataAdapterEx {

    // something like this....?
    // if (group.equals("default")) return queryForDefaultGroup(field,query)
    // else if (group.equals("referenceMaker")) return queryForReferenceMaker(field,query);
    // if (group.equals("default")) { System.out.println("Querying group default field "+field+" query "+query+" end"); }
    // else if (group.equals("referenceMaker")) { System.out.println("Querying group referenceMaker field "+field+" query "+query+" end"); }

//    String m = "Worm adapter query not yet implemented. field: "+field+" query: "+query;
//    JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);

    String alleleString = "Object Name";			// the query could be for Allele or Pub
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
//      String match = find("(.*) - [0-9]+ - [0-9]+", query);  	// Find a tempname followed by space - space number when query by postgres database ID instead of object name like it really should
//      if (match != null) { query = match; }		// query for this, otherwise keep the default value
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
        else { 
          rs = null; int boxes = 0; int columns = 0;		// the number of boxes ; columns 
          try { rs = s.executeQuery("SELECT app_box, app_column FROM app_term WHERE joinkey = '"+joinkey+"' ORDER BY app_column DESC"); }
          catch (SQLException se) {
            System.out.println("We got an exception while executing our app_term query: that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { if (rs.next()) { 
            if (rs.getInt(1) > boxes) { boxes = rs.getInt(1); } 		// assign the highest number boxes for that allele to the number of boxes 
            if (rs.getInt(2) > columns) { columns = rs.getInt(2); } } }		// assign the highest number column for that allele to the number of columns
          catch (SQLException se) {
            System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); System.exit(1); }
          for (int boxI=1; boxI<boxes+1; boxI++) {					// for each of those columns
            if (group.equals("default")) { 		// these values only go to the Main tab 
              for (int colI=1; colI<columns+1; colI++) {					// for each of those columns
                charList = queryPostgresCharacterMainList(group, charList, s, joinkey, boxI, colI); } }
            else if (group.equals("referenceMaker")) { 	// these values only go to the referenceMaker tab
              charList = queryPostgresCharacterReferenceList(group, charList, s, joinkey, boxI, 0); } }
          return charList; }	// if there is a match

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
      Statement s3 = null;
      try { s3 = c.createStatement(); }
       catch (SQLException se) {
        System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }

      String match = find("(WBPaper[0-9]*)", query);  	// Find a WBPaper followed by any amount of digits
      if (match != null) { query = match; }		// query for this, otherwise keep the default value
      int foundPaper = 0;				// flag if there are any papers in postgres that match, otherwise give an error warning
      ResultSet rs = null;				// initialize result of query
//       System.out.println("Paper "+query+" end");
      try { rs = s2.executeQuery("SELECT DISTINCT(joinkey) FROM app_paper WHERE app_paper ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
      catch (SQLException se) {
        System.out.println("We got an exception while executing our app_paper joinkey query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      try { if (rs.next()) { foundPaper++; } }
      catch (SQLException se) {
        System.out.println("We got an exception while getting a publication query result joinkey app_paper :this shouldn't happen: we've done something really bad."); 
        se.printStackTrace(); System.exit(1); }
      if (foundPaper <= 0) { throw new DataAdapterEx("Worm query of "+query+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
        else {
          ResultSet rs2 = null;				// initialize result of query
          try { rs2 = s2.executeQuery("SELECT DISTINCT(joinkey), app_box FROM app_paper WHERE app_paper ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
          catch (SQLException se) {
            System.out.println("We got an exception while executing our app_paper joinkey box query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { while (rs2.next()) {								// while there's data in postgres
            String joinkey = "No Allele assigned for Publication "+query+".";			// initialize allele in each phenote character Row
            joinkey = rs2.getString(1); 							// get the allele from postgres
            int boxI = rs2.getInt(2);								// get the box from postgres
            ResultSet rs3 = null;								// initialize result of query
            try { rs3 = s3.executeQuery("SELECT app_column FROM app_term WHERE app_box = '"+boxI+"' AND joinkey = '"+joinkey+"' ORDER BY joinkey;"); }	// get the columns from a paper
            catch (SQLException se) {
              System.out.println("We got an exception while executing our app_term query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }

            int columns = 0;
            try { while (rs3.next()) {								// while there's data in postgres
              if (rs3.getInt(1) > columns) { columns = rs3.getInt(1); } } } 		// assign the highest number column for that allele to the number of columns
            catch (SQLException se) {
              System.out.println("We got an exception while getting a publication query result column app_term :this shouldn't happen: we've done something really bad."); 
               se.printStackTrace(); System.exit(1); }
            if (group.equals("default")) { 		// these values only go to the Main tab 
              for (int colI=1; colI<columns+1; colI++) {					// for each of those columns
                charList = queryPostgresCharacterMainList(group, charList, s, joinkey, boxI, colI); } }
            else if (group.equals("referenceMaker")) { 	// these values only go to the referenceMaker tab
              charList = queryPostgresCharacterReferenceList(group, charList, s, joinkey, boxI, 0); }
          } }
          catch (SQLException se) {
            System.out.println("We got an exception while getting a publication query result joinkey, box app_paper :this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); System.exit(1); }
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
