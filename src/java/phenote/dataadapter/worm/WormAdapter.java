package phenote.dataadapter.worm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.TermNotFoundException;
import phenote.edit.EditManager;






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


  public void delete(Connection c, Statement s) {
    List<CharacterI> l = EditManager.inst().getDeletedAnnotations();
    try {
      for (CharacterI chr : l) {
        String joinkey = chr.getValueString("PgdbId");
        if (joinkey == null) continue;
        System.out.println("Delete "+joinkey+" end"); 
        String blank = "";
        String postgres_table = "app_type"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_tempname"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_paper"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_person"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_laboratory"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_intx_desc"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_nbp"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_curator"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_not"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_term"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_phen_remark"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_anat_term"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_entity"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_quality"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_suggested"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_lifestage"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_nature"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_func"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_temperature"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_treatment"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_penetrance"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_percent"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_range_start"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_range_end"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "app_quantity_remark"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "app_quantity"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_heat_sens"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_heat_degree"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_cold_sens"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_cold_degree"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_mat_effect"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_pat_effect"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_haplo"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_genotype"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_strain"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "app_obj_remark"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
      }
    } catch (Exception e) {
      System.out.println("Could not delete character: " + e);
    }
  }

  private void updatePostgresVal(Connection c, String postgres_table, String postgres_value, String joinkey, String value) {
    PreparedStatement ps = null;	// intialize postgres insert 
    try { ps = c.prepareStatement("INSERT INTO "+postgres_table+"_hst VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
    catch (SQLException se) {
      System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
    catch (SQLException se) { System.out.println("We got an exception while executing a history insert in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); }
    
    String pgisnull = "null";
    String pgblank = "";
//System.out.println("pgblank "+pgblank+" postgres_value "+postgres_value+" end");
    if ( postgres_value.equals(pgisnull) ) {			// no values in postgres
//        System.out.println("INSERT INTO "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("INSERT INTO "+postgres_table+" VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
        try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
        catch (SQLException se) { System.out.println("We got an exception while executing an insert update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); } }
      else { 							// some value in postgres, update it
//        System.out.println("UPDATING "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET "+postgres_table+" = ? WHERE joinkey = '"+joinkey+"'"); ps.setString(1, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
        try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
        catch (SQLException se) { System.out.println("We got an exception while executing a data update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); }
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET app_timestamp = CURRENT_TIMESTAMP WHERE joinkey = '"+joinkey+"'"); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
        try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
        catch (SQLException se) { System.out.println("We got an exception while executing a timestamp update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); } }
  } // private void updatePostgresVal(String postgres_table, String joinkey, int colI, String value)

  private void insertPostgresHistVal(Connection c, Statement s, String postgres_table, String joinkey, String value) {
    Integer hasMatch = 0;
    ResultSet rs = null;
    System.out.println("SELECT joinkey FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' AND "+postgres_table+"_hst = '"+value+"' "); 
    try { rs = s.executeQuery("SELECT joinkey FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' AND "+postgres_table+"_hst = '"+value+"' "); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { if (rs.next()) { hasMatch++; } }
      // get the next highest number joinkey for that character
    catch (SQLException se) {
      System.out.println("We got an exception while getting a insertPostgresHistVal joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }
    if (hasMatch == 0) {
      PreparedStatement ps = null;	// intialize postgres insert 
      System.out.println("INSERT INTO "+postgres_table+"_hst VALUES "+joinkey+" and "+value+" end");
      try { ps = c.prepareStatement("INSERT INTO "+postgres_table+"_hst VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
      catch (SQLException se) {
        System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
      catch (SQLException se) { System.out.println("We got an exception while executing a history insert in insertPostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); } } }
    


  private void updateNormalField(Connection c, Statement s, String joinkey, String postgres_table, String tag_name, String tag_value) {
//    String postgres_value = "No postgres value assigned";
//    String postgres_value = "";
//System.out.println("table "+postgres_table+" joinkey "+joinkey+" checking updateNormalField");
    String postgres_value = queryPostgresCharacterNull(s, postgres_table, joinkey);
    String pgisnull = "null";
    String pg_value = postgres_value;
    if (postgres_value.equals(pgisnull)) { pg_value = ""; }	// pg_value converts postgres null into blank to check against tag value
    if (pg_value.equals(tag_value)) { 
//System.out.println("table "+postgres_table+" joinkey "+joinkey+" has equal values");
} else { updatePostgresVal(c, postgres_table, postgres_value, joinkey, tag_value); }	// pass the pgisnull string if pg value is null
    System.out.println( tag_name+" : "+tag_value+" end.");
  }

  private void updateListField(Connection c, Statement s, String joinkey, String postgres_table, String tag_name, CharacterI chr) {
    try {
      StringBuilder sb = new StringBuilder(); Integer loopcount = 0;
      CharFieldValue list = chr.getValue(chr.getCharFieldForName(tag_name));
      List<CharFieldValue> valList = list.getCharFieldValueList();
      for (CharFieldValue kid : valList) { String kidId = kid.getID(); loopcount++; if (loopcount > 1) { sb.append(","); } sb.append("\"").append(kidId).append("\""); }
      if (sb != null) { String tag_value = sb.toString(); updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value); } }
    catch (Exception e) {
      System.out.println("Could not get terms from character in updateListField : " + e); e.printStackTrace(); }
  }

  public void commit(CharacterListI charList) {
    Connection c = connectToDB();
    Statement s = null;

//    Constraint const;
//    ConstraintManager.addConstraint(const);

    try { s = c.createStatement(); }
      catch (SQLException se) {
      System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }
    for (CharacterI chr : charList.getList()) {
//      System.out.println("Chr "+chr+" end");
      try {
        String allele = chr.getValueString("Object Name");	// get the allele value from the character, currently could have a column number
        String pgdbid = chr.getValueString("PgdbId");		// get the allele value from the character, currently could have a column number
        String joinkey = pgdbid;
//System.out.println("pgdbid "+pgdbid+" end");
        if ( (pgdbid == null) || (pgdbid == "") ) { 
//          Integer joinkeyInt = Integer.parseInt(pgdbid);	// this can't do anything since pgdbid must be blank
          Integer joinkeyInt = 0;
          ResultSet rs = null;
          try { rs = s.executeQuery("SELECT joinkey FROM app_tempname "); }
          catch (SQLException se) {
            System.out.println("We got an exception while executing our app_tempname query: that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { while (rs.next()) { if (rs.getInt(1) > joinkeyInt) { joinkeyInt = rs.getInt(1); } } joinkeyInt++; joinkey = Integer.toString(joinkeyInt); }
            // get the next highest number joinkey for that character
          catch (SQLException se) {
            System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); System.exit(1); } }
//System.out.println("joinkey "+joinkey+" end");

        chr.setValue("PgdbId",joinkey);					// assign the allele and the column

        String postgres_table = "app_type"; String tag_name = "Type"; String tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_tempname"; tag_name = "Name"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_paper"; tag_name = "Pub"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//	ADD paper_remark
System.out.println("paper tag_value "+tag_value+" for postgres");
	if (tag_value != null) {  
          postgres_table = "app_paper_remark"; tag_name = "Paper Remark"; String paprem_value = chr.getValueString(tag_name);
System.out.println("paprem_value "+paprem_value+" for postgres");
	  if ( (paprem_value != null) && (paprem_value != "") ) { insertPostgresHistVal(c, s, postgres_table, tag_value, paprem_value); }
          postgres_table = "app_curation_status"; tag_name = "Curation Status"; String papsta_value = chr.getValueString(tag_name);
System.out.println("papsta_value "+papsta_value+" for postgres");
	  if ( (papsta_value != null) && (papsta_value != "") ) { updateNormalField(c, s, tag_value, postgres_table, tag_name, papsta_value); } }
        postgres_table = "app_person"; tag_name = "Person"; 
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "app_laboratory"; tag_name = "Laboratory Evidence"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_intx_desc"; tag_name = "Genetic Intx Desc"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_curator"; tag_name = "Curator"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_not"; tag_name = "Not"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_term"; tag_name = "Phenotype"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_phen_remark"; tag_name = "Phenotype Remark"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_anat_term"; tag_name = "Anatomy";
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "app_entity"; tag_name = "Entity"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_quality"; tag_name = "Quality"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_suggested"; tag_name = "Suggested"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_lifestage"; tag_name = "Life Stage"; 
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "app_nature"; tag_name = "Allele Nature"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_func"; tag_name = "Functional Change"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_temperature"; tag_name = "Temperature"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_treatment"; tag_name = "Treatment"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_penetrance"; tag_name = "Penetrance"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_percent"; tag_name = "Penetrance Remark"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_range_start"; tag_name = "Penetrance Range Start"; tag_value = chr.getValueString(tag_name);
        String range_start = tag_value;
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_range_end"; tag_name = "Penetrance Range End"; tag_value = chr.getValueString(tag_name);
        if ( ( (tag_value == "") || (tag_value == null) ) && (range_start != null) && (range_start != "")) { tag_value = range_start; 
          String m = "Penetrance range for ID : "+joinkey+" has no end value, using start value : "+range_start;
          JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "app_quantity_remark"; tag_name = "Quantity Remark"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);	// not for phenote Karen 2008 01 28
//        postgres_table = "app_quantity"; tag_name = "Quantity"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);	// not for phenote Karen 2008 01 28
        postgres_table = "app_heat_sens"; tag_name = "Heat Sensitive"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_heat_degree"; tag_name = "Heat Sensitive Degree"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_cold_sens"; tag_name = "Cold Sensitive"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_cold_degree"; tag_name = "Cold Sensitive Degree"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_mat_effect"; tag_name = "Maternal Effect"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_pat_effect"; tag_name = "Paternal Effect"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_pat_effect"; tag_name = "Haplo"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_genotype"; tag_name = "Genotype"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_strain"; tag_name = "Strain"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_obj_remark"; tag_name = "Object Remark"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "app_allele_status"; tag_name = "Allele Status"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);

      } catch (Exception e) {
        System.out.println("Could not get terms from character: " + e);
        e.printStackTrace(); // helpful for debugging
      }

    } // for (CharacterI chr : charList.getList())

    delete(c, s);
  } // public void commit(CharacterListI charList)

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
    // for now adding constraints here - take this out when configging is working
    // done now in config
    //ConstraintManager.inst().addConstraint(new WormConstraint());
    
    // dont HAVE to use CharFieldEnum but it does enforce using same strings
    // across different data adapters which is good to enforce
    // the worm config needs to have "Pub" and "Object Name"
    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
//    queryableFields.add(CharFieldEnum.ALLELE.getName()); // "Allele"
    queryableFields.add("Object Name"); // "Object Name"
    queryableFields.add("Phenotype"); 
    queryableFields.add("Phenotype Remark"); 
    // should their be a check that the current char fields have pub & allele?
    queryableFields.add("NBP Date"); 
//    queryableGroups.add("referenceMaker");		// populate reference obo for the main
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

  private String queryPostgresCharacterNull(Statement s, String postgres_table, String joinkey) {
    // see if the postgres value corresponding to a phenote cell has an entry at all (@row) ; returns ``null'' if no row
    String default_value = null;
    ResultSet rs = null;	// intialize postgres query result
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } 
    if (default_value == null) { default_value = "null"; }
    return default_value; 
  }

  private String queryPostgresCharacter(Statement s, String postgres_table, String default_value, String joinkey) {
    // get the value corresponding to a phenote cell from a postgres table by column
//System.out.println( "queryPostgresCharacter for "+postgres_table+" "+joinkey+" with default_value "+default_value+" end");
    ResultSet rs = null;	// intialize postgres query result
      // get the phenotype term in timestamp order where the allele and column number match
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } 
//  System.out.println("Added in function charList term "+query+" column "+colI+".");		// comment out later
//    if (default_value == null) { default_value = "postgres value is null"; }
//    if (default_value == "") { default_value = "postgres value is blank"; }
    if (default_value == null) { default_value = ""; }
//System.out.println( "queryPostgresCharacter for "+postgres_table+" "+joinkey+" gives "+default_value+" end");
    return default_value; 
  }

  private String queryPostgresCharacterDate(Statement s, String postgres_table, String default_value, String joinkey) {
    ResultSet rs = null;	// intialize postgres query result
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { default_value = rs.getString(3); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacterDate "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
//    if (default_value == null) { default_value = "postgres value is null"; }
//    if (default_value == "") { default_value = "postgres value is blank"; }
    if (default_value == null) { default_value = ""; }
    String date_match = find("([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9])", default_value);		// Find a WBPaper followed by any amount of digits
    if (date_match != null) { default_value = date_match; } 						// query for this, otherwise keep the default value
    return default_value; 
  }

  private String queryPostgresPapAll(Statement s, String postgres_table, String joinkey) {
    StringBuilder sb = new StringBuilder();
    ResultSet rs = null;	// intialize postgres query result
//    System.out.println("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); 
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+"_hst query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { sb.append(rs.getString(2)).append(" -- "); } }		// append the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresPapAll"+postgres_table+"_hst result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } 
    String pap_hist = sb.toString();
//    System.out.println("pap_hist "+pap_hist+" is not null"); 
//    if (pap_hist == null) { pap_hist = "postgres value is null"; }
//    if (pap_hist == "") { pap_hist = "postgres value is blank"; }
    if (pap_hist == null) { pap_hist = ""; }
    return pap_hist; 
  }

  private String queryPostgresPap(Statement s, String postgres_table, String joinkey) {
    String pap_latest = "";
    ResultSet rs = null;	// intialize postgres query result
//    System.out.println("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); 
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+"_hst query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
    try { while (rs.next()) { pap_latest = rs.getString(2); } }		// append the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresPap"+postgres_table+"_hst result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } 
//System.out.println("pap_latest "+pap_latest+" for joinkey "+joinkey+" end");
    if (pap_latest == null) { pap_latest = ""; }
    return pap_latest; 
  }

  private CharacterListI queryPostgresCharacterMainList(CharacterListI charList, Statement s, String joinkey) {
      // populate a phenote character based on postgres value by joinkey, then append to character list
    try {
      CharacterI c1 = CharacterIFactory.makeChar();				// create a new character for a phenote row

//System.out.println("set PgdbId to "+joinkey+" END");
      c1.setValue("PgdbId",joinkey);					// assign the allele and the column
      String postgres_table = "app_type"; String postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      c1.setValue("Object Type",postgres_value);					// assign the queried value
      postgres_table = "app_tempname"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      c1.setValue("Object Name",postgres_value);					// assign the allele and the column

      postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_table = "app_term";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      String phenotype_match = find("(WBPhenotype[0-9]*)", postgres_value);  	// Find a WBPhenotype followed by any amount of digits
      if (phenotype_match != null) { postgres_value = phenotype_match; }		// query for this, otherwise keep the default value
//      if (postgres_value == "No postgres value assigned") { } else { c1.setValue("Phenotype",postgres_value); }					// assign the queried value
      if (postgres_value == "") { } else { c1.setValue("Phenotype",postgres_value); }					// assign the queried value
//System.out.println("set Phenotype to "+postgres_value+" END");
      postgres_table = "app_intx_desc"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Genetic Intx Desc",postgres_value); }					// assign the queried value
      postgres_table = "app_curator"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Curator",postgres_value); }					// assign the queried value
//System.out.println("set Curator to "+postgres_value+" END");
      postgres_table = "app_not"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Not",postgres_value); }					// assign the queried value
      postgres_table = "app_phen_remark"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Phenotype Remark",postgres_value); }				// assign the queried value
//System.out.println("set PhenotypeRemark to "+postgres_value+" END");
      postgres_table = "app_anat_term"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//System.out.println("queryied anat_term to "+postgres_value+" END");
      if (postgres_value == "") { } else { c1.setValue("Anatomy",postgres_value); }					// assign the queried IDs
//      c1.setValue("Anatomy","WBbt:0004758");			 		// this works, assigning a term ID
      postgres_table = "app_entity"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Entity",postgres_value); }					// assign the queried value
      postgres_table = "app_quality"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Quality",postgres_value); }					// assign the queried value
      postgres_table = "app_suggested"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Suggested",postgres_value); }					// assign the queried value
      postgres_table = "app_lifestage"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//System.out.println("queryied lifestage to "+postgres_value+" END");
      if (postgres_value != null) {
      if (postgres_value == "") { } else { c1.setValue("Life Stage",postgres_value); }					// assign the queried value
}
//System.out.println("set LifeStage to "+postgres_value+" END");
      postgres_table = "app_nature"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Allele Nature",postgres_value); }				// assign the queried value
      postgres_table = "app_func"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Functional Change",postgres_value); }				// assign the queried value
      postgres_table = "app_temperature"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Temperature",postgres_value); }				// assign the queried value
//System.out.println("set Temperature to "+postgres_value+" END");
      postgres_table = "app_treatment"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Treatment",postgres_value); }				// assign the queried value
      postgres_table = "app_penetrance"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Penetrance",postgres_value); }				// assign the queried value
      postgres_table = "app_percent"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Penetrance Remark",postgres_value); }				// assign the queried value
//System.out.println("set PenetranceRemark to "+postgres_value+" END");
      postgres_table = "app_range_start"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Penetrance Range Start",postgres_value); }				// assign the queried value
      postgres_table = "app_range_end"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Penetrance Range End",postgres_value); }				// assign the queried value
//      postgres_table = "app_quantity"; postgres_value = ""; // postgres_value = "No postgres value assigned";	// not for phenote, Karen 2008 01 28
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      c1.setValue("Quantity",postgres_value);					// assign the queried value
////System.out.println("set Quantity to "+postgres_value+" END");
//      postgres_table = "app_quantity_remark"; postgres_value = ""; // postgres_value = "No postgres value assigned";	// not for phenote, Karen 2008 01 28
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      c1.setValue("Quantity Remark",postgres_value);				// assign the queried value
      postgres_table = "app_heat_sens"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Heat Sensitive",postgres_value); }				// assign the queried value
      postgres_table = "app_heat_degree"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Heat Sensitive Degree",postgres_value); }				// assign the queried value
//System.out.println("set HeatSens to "+postgres_value+" END");
      postgres_table = "app_cold_sens"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Cold Sensitive",postgres_value); }				// assign the queried value
      postgres_table = "app_cold_degree"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Cold Sensitive Degree",postgres_value); }			// assign the queried value
      postgres_table = "app_mat_effect"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Maternal Effect",postgres_value); }				// assign the queried value
      postgres_table = "app_pat_effect"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Paternal Effect",postgres_value); }				// assign the queried value
//System.out.println("set PatEffect to "+postgres_value+" END");
      postgres_table = "app_haplo"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Haplo",postgres_value); }				// assign the queried value
      postgres_table = "app_genotype"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Genotype",postgres_value); }				// assign the queried value
      postgres_table = "app_strain"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Strain",postgres_value); }				// assign the queried value
      postgres_table = "app_obj_remark"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Object Remark",postgres_value); }				// assign the queried value
//System.out.println("set ObjRem to "+postgres_value+" END");
      postgres_table = "app_allele_status"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Allele Status",postgres_value); }				// assign the queried value

      postgres_table = "app_paper"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Pub",postgres_value); }					// assign the queried value
//System.out.println("set Pub to "+postgres_value+" END");
      if ( (postgres_value != null) && (postgres_value != "") ) { 
        String pap_value = postgres_value;
        postgres_table = "app_paper_remark";
        postgres_value = queryPostgresPapAll(s, postgres_table, pap_value);
        if (postgres_value == "") { } else { c1.setValue("Paper Remark History",postgres_value); }		// assign the queried value
        postgres_table = "app_curation_status";
        postgres_value = queryPostgresPap(s, postgres_table, pap_value);
        if (postgres_value == "") { } else { c1.setValue("Curation Status",postgres_value); } }			// assign the queried value
      postgres_table = "app_person"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Person",postgres_value); }				// assign the queried value
//System.out.println("set Person to "+postgres_value+" END");
      postgres_table = "app_laboratory"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Laboratory Evidence",postgres_value); }				// assign the queried value
      postgres_table = "app_nbp"; postgres_value = ""; // postgres_value = "No postgres value assigned";		// NBP is nbp, not intx_desc
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("NBP",postgres_value); }					// assign the queried value
      postgres_table = "app_nbp"; postgres_value = ""; // postgres_value = "No postgres value assigned";		
      postgres_value = queryPostgresCharacterDate(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("NBP Date",postgres_value); }					// assign the queried value

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
    String nbpString = "NBP Date";
    String phenotypeString = "Phenotype";
    String phenotypeRemarkString = "Phenotype Remark";

    CharacterListI charList = new CharacterList();	// create the CharacterList that we will return

    Connection c = connectToDB();
    Statement s = null;
    try { s = c.createStatement(); }
      catch (SQLException se) { System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }

    ResultSet rs = null;	// intialize postgres query result
    List<String> joinkeys = new ArrayList<String>(2);
    int foundAllele = 0;

    if (field.equals(alleleString)) {			// if querying the allele, get allele data
      try { rs = s.executeQuery("SELECT * FROM app_tempname WHERE app_tempname ~ '"+query+"' ORDER BY joinkey"); }	// find the allele that matches the queried allele  switch to substring search for Karen 2008 02 21
      catch (SQLException se) { System.out.println("Exception while executing app_tempname alleleString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
    } else if (field.equals(pubString)) {						// if querying the publication, get paper data
      try { rs = s.executeQuery("SELECT * FROM app_paper WHERE app_paper ~ '"+query+"' ORDER BY joinkey"); }	// find the allele that matches the queried allele
      catch (SQLException se) { System.out.println("Exception while executing app_paper pubString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
    } else if (field.equals(phenotypeString)) {						// if querying the publication, get paper data
      try { rs = s.executeQuery("SELECT * FROM app_term WHERE app_term ~ '"+query+"' ORDER BY joinkey"); }	// find the allele that matches the queried allele
      catch (SQLException se) { System.out.println("Exception while executing app_term phenotypeString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
    } else if (field.equals(phenotypeRemarkString)) {
      try { rs = s.executeQuery("SELECT * FROM app_phen_remark WHERE app_phen_remark ~ '"+query+"' ORDER BY joinkey"); }	// find the allele that matches the queried allele
      catch (SQLException se) { System.out.println("Exception while executing app_phen_remark phenotypeRemarkString "+query+" query: that probably means our SQL is invalid"); 
        se.printStackTrace(); System.exit(1); }
    } else if (field.equals(nbpString)) {	
      try { rs = s.executeQuery("SELECT * FROM app_nbp WHERE app_timestamp ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
      catch (SQLException se) { System.out.println("Exception while executing app_nbp nbpString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
    } else {
      // if query has failed...
      throw new DataAdapterEx("Worm query of "+query+" of field "+field+" failed");
    }

    try {
      while (rs.next()) {
        joinkeys.add(rs.getString(1));			
        foundAllele++; } }
    catch (SQLException se) {
      System.out.println("We got an exception while getting a query catch while rs.next tempname joinkey "+query+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); System.exit(1); }

      for (String joinkey : joinkeys) {
//        System.out.println("J "+joinkey+" List");
        charList = queryPostgresCharacterMainList(charList, s, joinkey); 
//        System.out.println("END "+joinkey+" List");
      }
    if (foundAllele <= 0) { throw new DataAdapterEx("Worm query of "+query+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
      else { return charList; } 	// if there is a match

  } // public CharacterListI query(String field, String query) throws DataAdapterEx


} // public class WormAdapter implements QueryableDataAdapterI




// __END__
