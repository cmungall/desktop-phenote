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






public class WormTransgeneAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);
  private List<String> queryableGroups = new ArrayList<String>(2);

  public WormTransgeneAdapter() { init(); }

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
        String blank = "";
        if (joinkey.equals(blank)) { continue; }
        System.out.println("Delete "+joinkey+" end"); 
        String postgres_table = "trp_name"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_summary"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_driven_by_gene"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_reporter_product"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_other_reporter"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_gene"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_integrated_by"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_particle_bombardment"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_strain"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_map"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_map_paper"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_map_person"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_marker_for"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_marker_for_paper"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_reference"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_remark"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_species"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_synonym"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_driven_by_construct"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_location"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_movie"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "trp_picture"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
      }
    } catch (Exception e) {
      System.out.println("Could not delete character: " + e);
    }
  }

  private void updatePostgresVal(Connection c, String postgres_table, String postgres_value, String joinkey, String value) {
    PreparedStatement ps = null;	// intialize postgres insert 
    try { ps = c.prepareStatement("INSERT INTO "+postgres_table+"_hst VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
    catch (SQLException se) {
      System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal INSERT "+postgres_table+"_hst "); return; }
    try { ps.executeUpdate(); }
    catch (SQLException se) { System.out.println("We got an exception while executing a history insert in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate() "+postgres_table+" "); return; }
    
    String pgisnull = "null";
    String pgblank = "";
//System.out.println("pgblank "+pgblank+" postgres_value "+postgres_value+" end");
    if ( postgres_value.equals(pgisnull) ) {			// no values in postgres
//        System.out.println("INSERT INTO "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("INSERT INTO "+postgres_table+" VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal INSERT "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing an insert update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate() "+postgres_table+" "); return; } }
      else { 							// some value in postgres, update it
//        System.out.println("UPDATING "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET "+postgres_table+" = ? WHERE joinkey = '"+joinkey+"'"); ps.setString(1, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal UPDATE "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing a data update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate "+postgres_table+" "); return; }
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET trp_timestamp = CURRENT_TIMESTAMP WHERE joinkey = '"+joinkey+"'"); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal UPDATE timestamp "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing a timestamp update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate timestamp "+postgres_table+" "); return; } }
  } // private void updatePostgresVal(String postgres_table, String joinkey, int colI, String value)


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
    boolean bool = true;


//    Constraint const;
//    ConstraintManager.addConstraint(const);

    try { s = c.createStatement(); }
      catch (SQLException se) {
      System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); errorPopup("commit createStatement"); return; }
    try { bool = s.execute("BEGIN WORK"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our int_name query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit BEGIN WORK"); return ; }
    for (CharacterI chr : charList.getList()) {
//      System.out.println("Chr "+chr+" end");
      try {
        String name = chr.getValueString("Name");	// get the name value from the character, currently could have a column number
        String pgdbid = chr.getValueString("PgdbId");		// get the name value from the character, currently could have a column number
        String joinkey = pgdbid;
//System.out.println("pgdbid "+pgdbid+" end");
        if ( (pgdbid == null) || (pgdbid == "") ) { 
//          Integer joinkeyInt = Integer.parseInt(pgdbid);	// this can't do anything since pgdbid must be blank
          Integer joinkeyInt = 0;
          ResultSet rs = null;
          try { rs = s.executeQuery("SELECT joinkey FROM trp_name "); }
          catch (SQLException se) {
            System.out.println("We got an exception while executing our trp_name query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit SELECT trp_name"); return; }
          try { while (rs.next()) { if (rs.getInt(1) > joinkeyInt) { joinkeyInt = rs.getInt(1); } } joinkeyInt++; joinkey = Integer.toString(joinkeyInt); }
            // get the next highest number joinkey for that character
          catch (SQLException se) {
            System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); errorPopup("commit rs.next()"); return; } }
//System.out.println("joinkey "+joinkey+" end");

        chr.setValue("PgdbId",joinkey);					// assign the pgdbid and the column

        String postgres_table = "trp_name"; String tag_name = "Name"; String tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_summary"; tag_name = "Summary"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_driven_by_gene"; tag_name = "Driven by Gene"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_reporter_product"; tag_name = "Reporter Product";
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "trp_other_reporter"; tag_name = "Other Reporter"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_gene"; tag_name = "Gene"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_integrated_by"; tag_name = "Integrated by"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_particle_bombardment"; tag_name = "Particle Bombardment"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_strain"; tag_name = "Strain"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_map"; tag_name = "Map";
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "trp_map_paper"; tag_name = "Map Paper"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_map_person"; tag_name = "Map Person"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_marker_for"; tag_name = "Marker for Paper"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_reference"; tag_name = "Reference"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_remark"; tag_name = "Remark"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_species"; tag_name = "Species"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_synonym"; tag_name = "Synonym"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_driven_by_construct"; tag_name = "Driven by Construct"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_location"; tag_name = "Location"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_movie"; tag_name = "Movie"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "trp_picture"; tag_name = "Picture"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);

      } catch (Exception e) {
        System.out.println("Could not get terms from character: " + e);
        e.printStackTrace(); // helpful for debugging
      }

    } // for (CharacterI chr : charList.getList())
    try { bool = s.execute("COMMIT WORK"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our int_name query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit COMMIT WORK"); return ; }

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
//    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
//    queryableFields.add(CharFieldEnum.ALLELE.getName()); // "Allele"
    queryableFields.add("Name"); // "Object Name"
    queryableFields.add("Reference"); 
//    queryableFields.add("Phenotype Remark"); 
//    // should their be a check that the current char fields have pub & allele?
//    queryableFields.add("NBP Date"); 
//    queryableFields.add("Genetic Intx Desc"); 
//    queryableFields.add("Suggested"); 
//    queryableFields.add("Suggested Definition"); 
//    queryableGroups.add("referenceMaker");		// populate reference obo for the main
    queryableGroups.add("default");			// default last
  }
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field) {
    return queryableFields.contains(field);
  }

  public void errorPopup(String errType) {
    String errText = errType + " : invalid SQL problem";
    JOptionPane.showMessageDialog(null,errText,errText,JOptionPane.INFORMATION_MESSAGE);
  }

  private Connection connectToDB() {
    System.out.println("Checking if Driver is registered with DriverManager.");	// open postgres database connection
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException cnfe) {
      System.out.println("Couldn't find the driver!");
      System.out.println("Let's print a stack trace, and exit.");
      cnfe.printStackTrace();
      errorPopup("connectToDB"); return null;
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
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY trp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); errorPopup("queryPostgresCharacterNull "+postgres_table+" "); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); errorPopup("queryPostgresCharacterNull rs.next()"); } 
    if (default_value == null) { default_value = "null"; }
    return default_value; 
  }

  private String queryPostgresCharacter(Statement s, String postgres_table, String default_value, String joinkey) {
    // get the value corresponding to a phenote cell from a postgres table by column
//System.out.println( "queryPostgresCharacter for "+postgres_table+" "+joinkey+" with default_value "+default_value+" end");
    ResultSet rs = null;	// intialize postgres query result
      // get the phenotype term in timestamp order where the allele and column number match
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY trp_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); errorPopup("queryPostgresCharacter "+postgres_table+" "); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); errorPopup("queryPostgresCharacter rs.next()"); } 
//  System.out.println("Added in function charList term "+query+" column "+colI+".");		// comment out later
//    if (default_value == null) { default_value = "postgres value is null"; }
//    if (default_value == "") { default_value = "postgres value is blank"; }
    if (default_value == null) { default_value = ""; }
//System.out.println( "queryPostgresCharacter for "+postgres_table+" "+joinkey+" gives "+default_value+" end");
    return default_value; 
  }

//  private String queryPostgresCharacterDate(Statement s, String postgres_table, String default_value, String joinkey) {
//    ResultSet rs = null;	// intialize postgres query result
//    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY trp_timestamp"); }
//    catch (SQLException se) {
//      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); errorPopup("queryPostgresCharacterDate "+postgres_table+" "); }
//    try { while (rs.next()) { default_value = rs.getString(3); } }		// assign the new term value
//    catch (SQLException se) {
//      System.out.println("We got an exception while getting a queryPostgresCharacterDate "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); errorPopup("queryPostgresCharacterDate rs.next()"); }
////    if (default_value == null) { default_value = "postgres value is null"; }
////    if (default_value == "") { default_value = "postgres value is blank"; }
//    if (default_value == null) { default_value = ""; }
//    String date_match = find("([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9])", default_value);		// Find a WBPaper followed by any amount of digits
//    if (date_match != null) { default_value = date_match; } 						// query for this, otherwise keep the default value
//    return default_value; 
//  }


  private CharacterListI queryPostgresCharacterMainList(CharacterListI charList, Statement s, String joinkey) {
      // populate a phenote character based on postgres value by joinkey, then append to character list
    try {
      CharacterI c1 = CharacterIFactory.makeChar();				// create a new character for a phenote row

//System.out.println("set PgdbId to "+joinkey+" END");
      c1.setValue("PgdbId",joinkey);					// assign the allele and the column
      String postgres_table = "trp_name"; String postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Name",postgres_value);
      postgres_table = "trp_summary"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Summary",postgres_value);
      postgres_table = "trp_driven_by_gene"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Driven by Gene",postgres_value);
      postgres_table = "trp_reporter_product"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Reporter Product",postgres_value);
      postgres_table = "trp_other_reporter"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Other Reporter",postgres_value);
      postgres_table = "trp_gene"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Gene",postgres_value);
      postgres_table = "trp_integrated_by"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Integrated by",postgres_value);
      postgres_table = "trp_particle_bombardment"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Particle Bombardment",postgres_value);
      postgres_table = "trp_strain"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Strain",postgres_value);
      postgres_table = "trp_map"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Map",postgres_value);
      postgres_table = "trp_map_paper"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Map Paper",postgres_value);
      postgres_table = "trp_map_person"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Map Person",postgres_value);
      postgres_table = "trp_marker_for"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Marker for",postgres_value);
      postgres_table = "trp_marker_for_paper"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Marker for Paper",postgres_value);
      postgres_table = "trp_reference"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Reference",postgres_value);
      postgres_table = "trp_remark"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Remark",postgres_value);
      postgres_table = "trp_species"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Species",postgres_value);
      postgres_table = "trp_synonym"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Synonym",postgres_value);
      postgres_table = "trp_driven_by_construct"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Driven by Construct",postgres_value);
      postgres_table = "trp_location"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Location",postgres_value);
      postgres_table = "trp_movie"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Movie",postgres_value);
      postgres_table = "trp_picture"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey); c1.setValue("Picture",postgres_value);

//      postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_table = "trp_term";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      String phenotype_match = find("(WBPhenotype:[0-9]*)", postgres_value);  	// Find a WBPhenotype followed by any amount of digits
//      if (phenotype_match != null) { postgres_value = phenotype_match; }		// query for this, otherwise keep the default value
//      if (postgres_value == "") { } else { c1.setValue("Phenotype",postgres_value); }					// assign the queried value
//System.out.println("set Phenotype to "+postgres_value+" END");

      charList.add(c1);								// add the character to the character list
    }
    catch (TermNotFoundException e) {
      System.out.println("Term Not Found Exception, assigning characters in queryPostgresCharacterMainList "+e.getMessage()); }
    catch (CharFieldException e) {
      System.out.println("Char Field Exception, assigning characters "+e.getMessage()); }
    return charList; 
  } // private CharacterListI queryPostgresCharacterMainList(CharacterList charList, Statement s, String joinkey)


  public CharacterListI query(String group, String field, String query) throws DataAdapterEx {
//    String m = "Worm adapter query not yet implemented. field: "+field+" query: "+query;
//    JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);

    String nameString = "Name";			// the query could be for Allele or Pub
    String referenceString = "Reference";

    CharacterListI charList = new CharacterList();	// create the CharacterList that we will return

    Connection c = connectToDB();
    Statement s = null;
    try { s = c.createStatement(); }
      catch (SQLException se) { System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); errorPopup("query createStatement"); }

    ResultSet rs = null;	// intialize postgres query result
    List<String> joinkeys = new ArrayList<String>(2);
    int foundName = 0;

    if (field.equals(nameString)) {			// if querying the name, get name data
      try { rs = s.executeQuery("SELECT * FROM trp_name WHERE trp_name ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried name 
      catch (SQLException se) { System.out.println("Exception while executing trp_name nameString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query SELECT trp_name"); }
    } else if (field.equals(referenceString)) {						// if querying the publication, get paper data
      try { rs = s.executeQuery("SELECT * FROM trp_reference WHERE trp_reference ~ '"+query+"' ORDER BY joinkey"); }	// find the allele that matches the queried allele
      catch (SQLException se) { System.out.println("Exception while executing trp_reference referenceString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query SELECT trp_reference"); }
    } else {
      // if query has failed...
      throw new DataAdapterEx("Worm query of "+query+" of field "+field+" failed");
    }

    try {
      while (rs.next()) {
        joinkeys.add(rs.getString(1));			
        foundName++; } }
    catch (SQLException se) {
      System.out.println("We got an exception while getting a query catch while rs.next tempname joinkey "+query+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); errorPopup("query rs.next()"); }

      for (String joinkey : joinkeys) {
//        System.out.println("J "+joinkey+" List");
        charList = queryPostgresCharacterMainList(charList, s, joinkey); 
//        System.out.println("END "+joinkey+" List");
      }
    if (foundName <= 0) { throw new DataAdapterEx("Worm query of "+query+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
      else { return charList; } 	// if there is a match

  } // public CharacterListI query(String field, String query) throws DataAdapterEx


} // public class WormTransgeneAdapter implements QueryableDataAdapterI




// __END__
