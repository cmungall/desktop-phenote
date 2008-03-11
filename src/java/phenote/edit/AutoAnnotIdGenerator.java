package phenote.edit;


/** produces unique annot ids for session of form _:# */
public class AutoAnnotIdGenerator {

  private static int currentIdNumber = 1;

  public static String getNewId() {
    String id = "_:"+currentIdNumber;
    currentIdNumber++;
    return id;
  }

  /** should reset number with new data */

}
