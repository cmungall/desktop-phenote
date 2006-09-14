package phenote.main;

import com.townleyenterprises.command.CommandOption;
import com.townleyenterprises.command.CommandParser;
import com.townleyenterprises.command.DefaultCommandListener;

import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.phenosyntax.PhenoSyntaxFileAdapter;
import phenote.dataadapter.phenoxml.PhenoXmlAdapter;

/** a lot of this is copied from apollo.main.CommandLine - theres potential for 
    some generic super class but where would it go? in a jar file with one class?
    org.bdgp? */
public class CommandLine {

  /** the class that parses all the options */
  private CommandParser parser;
  /** stores adapter class, filenames, etc. for reading data */
  private IOOptions readOptions;
  /** stores adapter class, filenames, etc. for writing data */
  private IOOptions writeOptions;
  private CommandOption inputFile = new InputFileCommandOption();
  //private CommandOption inputFmtOption = new InputFormatCommandOption();
  private CommandOption writeFile = new WriteFileCommandOption();

  CommandOption[] options = new CommandOption[] { inputFile, writeFile };

  // this guarantees that we get the right classes (compile time check)
  private final static String PHENOXML = PhenoXmlAdapter.class.getName();
  private final static String PHENOSYNTAX = PhenoSyntaxFileAdapter.class.getName();

  // there can be only one instance of this class (per JVM)
  private static CommandLine commandLineSingleton;

  public static CommandLine inst() {
    if (commandLineSingleton == null)
      commandLineSingleton = new CommandLine();
    return commandLineSingleton;
  }

  /** 
   * Set command-line arguments passed along from main().
   *
   * @param args argStrings from command line 
   */
  public void setArgs(String[] args) throws Exception {
    if (args.length == 0)
      return;
    if (parser != null) {
      System.out.println("CommandLine: WARNING multiple calls to setArgs(), ignoring");
      return; // ?
    }
    parser = new CommandParser("Phenote"); // help text?
    parser.addCommandListener(new DefaultCommandListener("Options",options));
    parser.parse(args);
    //addConstraints(parser);
    // does execute() on CommandOptions, throws generic Exception
    //try { // should i wrap the exception like apollo?
    parser.executeCommands();
    //} catch (Exception e) {  throw new ApolloAdapterException(e);  }
  }

  /** 
   * If input & output (or batch) is fully specified, then we are in command line
   * mode; no need for GUI.
   */
  public static boolean isInCommandLineMode() {
    return inst().writeIsSpecified(); //|| inst().isBatchMode();
  }

  /** if read has been (correctly) specified, read adapter is non null */
  boolean readIsSpecified() {
    return getReadOptions().getSpecifiedState();
  }

  boolean writeIsSpecified() {
    return getWriteOptions().getSpecifiedState();
  }

  private IOOptions getReadOptions() {
    if (readOptions == null)
      readOptions = new IOOptions(true);
    return readOptions;
  }

  private IOOptions getWriteOptions() {
    if (writeOptions == null)
      writeOptions = new IOOptions(false);
    return writeOptions;
  }

  /** 
   * If input/read was specified on the command line this returns the 
   * correctly-initialized read data adapter for it.  Note that
   * adapter returned may be the same object as that returned by 
   * <code>getWriteAdapter()</code>
   * @return null if no read adapter specified on command line. 
   */
  DataAdapterI getReadAdapter() throws Exception {
    IOOptions ioo = getReadOptions();
    DataAdapterI adapter = ioo.getAdapter();
    adapter.setAdapterValue(ioo.getAdapterValue());
    return adapter;
  }

  /** If output/write was specified on the command line this returns the 
   * correctly-initialized write data adapter for it.  Note that the
   * adapter returned may be the same object as that returned by 
   * <code>getReadAdapter()</code>
   * @return null if no read adapter specified on command line. */
  DataAdapterI getWriteAdapter() throws Exception {
    // LOG.debug("CommandLine: getWriteAdapter called\n");
    IOOptions ioo = getWriteOptions();
    DataAdapterI adapter = ioo.getAdapter();
    adapter.setAdapterValue(ioo.getAdapterValue());
    return adapter;
  }


  /** INPUT FILE COMMAND OPTION */
  private class InputFileCommandOption extends CommandOption {
    private final static String help =
      "Specify filename to read in (phenoxml.xml,phenosyntax.syn)";
    private InputFileCommandOption() {
      // true -> has argument
      super("inputFile",'f',true,"filename",help);
    }
    public void execute() throws Exception {
      //System.out.println("executing input file command option "+getArg());
      getReadOptions().setFilename(getArg());
      setAdapterForFile(getReadOptions(), false);
    }
  }

  /**
   * OUTPUT FILE COMMAND OPTION Specifies name of target/output file.
   */
  private class WriteFileCommandOption extends CommandOption {
    private WriteFileCommandOption() {
      super("writeFile",'w',true,"filename","Filename to write to");
    }
    public void execute() throws Exception {
      getWriteOptions().setFilename(getArg());
      setAdapterForFile(getWriteOptions(), false); // sets data adapter from file suffix
    }
  }


  /** 
   * Read & write file method. Retrieves data adapter either from format option or
   * suffix and sets its DataInput with filename 
   */
  private void setAdapterForFile(IOOptions options, boolean setAdapterInput)
    throws Exception 
  {
    DataAdapterI adapter = getAdapterForFile(options);
    if (!options.hasAdapter())
      options.setAdapter(adapter);
    // is it scandalous to use DataInput for output?? should be called DataInfo?
    // of DataSpecifitation?
    String inputFile = options.getFilename();
    //DataInputType inputType = DataInputType.FILE;
    //if (inputFile.startsWith("http:") || inputFile.startsWith("file:")) {
    //inputType = DataInputType.URL;  }
    //DataInput di = new DataInput(inputType, inputFile);
    // gff input may have seq file.
    //if (haveSequenceFilename()) di.setSequenceFilename(getSequenceFilename());
    //options.setDataInput(di);
    adapter.setAdapterValue(inputFile);
    options.setSpecifiedState(true);
    //if (setAdapterInput) adapter.setDataInput(di);
  }

  private DataAdapterI getAdapterForFile(IOOptions options) throws Exception {
    //DataAdapterI adapter; 
    // first see if input format explicitly specified 
    if (options.hasAdapter())
      return options.getAdapter();

    // if nothing specified try to get format from file suffix
    try { return getDataAdapterFromSuffix(options.getFilename()); }
    catch (AdapterEx e) { 
      throw new AdapterEx("No input format specified and "+e.getMessage());
    }

    //return adapter;
  }

  private DataAdapterI getDataAdapterFromSuffix(String filename) throws AdapterEx {
    String suffix = getFileSuffix(filename);
    // anything with game in it??
    if (suffix.matches(".*syn.*|psx")) // ???
      return getDataAdapter(PHENOSYNTAX);
    if (suffix.matches(".*xml.*"))
      return getDataAdapter(PHENOXML);
    // configuration can specify what to do with .xml suffix (game or chado)
    //else if (suffix.equals("xml") && xmlSuffixIsConfigged())
    //  return getConfiggedXmlDataAdapter();
    throw new AdapterEx("Suffix "+suffix+" doesnt map to known format"); 
  }

  private class AdapterEx extends Exception {
    private AdapterEx(String m) { super(m); }
    private AdapterEx(Exception e) { super(e.getMessage()); } // save e?
  }

  /** should this include the '.'? probably not. changing it to not include . */
  private String getFileSuffix(String filename) {
    int index = filename.lastIndexOf('.');
    return filename.substring(index+1); // 1 past .
  }

  private DataAdapterI getDataAdapter(String classString) throws AdapterEx {
    try {
      // introspection? or switch on string?
      Class c = Class.forName(classString);
      Object o = c.newInstance();
      if (!(o instanceof DataAdapterI)) // shouldnt happen
        System.out.println("Class string is not data adapter "+o);
      return (DataAdapterI)o;
    } catch (Exception e) { throw new AdapterEx(e); }
  }

  // eventually - multiple xmls?
//   private boolean xmlSuffixIsConfigged() {
//     return Config.commandLineXmlFileFormatIsConfigged();
//   }

//   private ApolloDataAdapterI getConfiggedXmlDataAdapter() {
//     String config = Config.getCommandLineXmlFileFormat();
//     if (config.equalsIgnoreCase("game"))
//       return getDataAdapter(GAME);
//     if (config.equalsIgnoreCase("chado"))
//       return getDataAdapter(CHADOXML);
//     return null;
//   }

  void printHelp() {
    parser.help();
  }

  private void error(String m) throws Exception {
    System.out.println(m);
    printHelp();
    throw new Exception(m);
  }


  // -----------------------------------------------------------------------
  // IOOptions inner class
  // -----------------------------------------------------------------------

  /** 
   * IOOptions holds state for either read or write 
   */
  private class IOOptions {
    private DataAdapterI adapter;
    private String filename;
    private boolean specified = false;
    private boolean isRead;
    private String adapterValue;


    // ----------------------------------------------
    // Constructor
    // ----------------------------------------------

    private IOOptions(boolean isRead) {
      this.isRead = isRead;
    }

    // ----------------------------------------------
    // CommandLine - simple getters/setters
    // ----------------------------------------------

    private String getAdapterValue() { return adapterValue; }

    private void setFilename(String filename) {
      this.filename = filename;
      adapterValue = filename;
    }
    private String getFilename() { return filename; }

    private void setSpecifiedState(boolean specified) { this.specified = specified; }
    private boolean getSpecifiedState() { return specified; }

    // ----------------------------------------------
    // CommandLine
    // ----------------------------------------------

    private void setAdapter(DataAdapterI adapter) {
      this.adapter = adapter;
    }
    private DataAdapterI getAdapter() throws Exception {
      if (adapter == null) {
        //System.out.println("dont have adapter");
        // if not set from file extension get from format option
//         if (isRead) {
//           adapter = inputFmtOption.getAdapter();
//         } else {
//           adapter = outputFmtOption.getAdapter();
//         }
      }
      return adapter;
    }
    private boolean hasAdapter() throws Exception { return getAdapter() != null; }

    /** type of input - type only needed for input */
    //private void setInputType(String inputTypeString) {
      // actually i think this is adapter dependent - for game want to do 
      // stringToType - for chado its a so type
      //try {inputType = DataInputType.stringToType(inputTypeString); }
      //catch (UnknownTypeException e) {
      //         System.out.println(e.getMessage()+" Can not set input type"); }
    //}
    //private DataInputType getInputType() { return inputType; }
    //private boolean haveInputType() { return inputType != null; }

    //private void setDataInput(DataInput dataInput) { this.dataInput = dataInput; }
    //private DataInput getDataInput() { return this.dataInput; }

  }
}
