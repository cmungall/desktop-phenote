package phenote.main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import phenote.dataadapter.importer.AnnotationImporterGeneric;




public class AnnotationImporterCLI{
	
	public static void main(String[] args){
		
		Options options = new Options();
		
		Option dbnameOption = new Option("d", "databaseName", true, "Specify the target database name (Required).");
		dbnameOption.setRequired(true);
		options.addOption(dbnameOption);
		
		Option inputFileOption = new Option("i","input_file",true,"Specify the input file (Required).");
		inputFileOption.setRequired(true);
		options.addOption(inputFileOption);
		
		Option configurationFileOption = new Option("c","configuration_file",true,"Specify the configuration file to be used (Required)");
		configurationFileOption.setRequired(true);
		options.addOption(configurationFileOption);
		
		options.addOption("h","hostname", true, "Specify the target hostname.\nDefault: localhost.");
		options.addOption("u","db_username",true,"Specify the target database username.\nDefault: Environmental username.");
		options.addOption("p","password",true,"Specify the target database user password.\nDefault: NULL");
		options.addOption("P","port",true,"Specify the target database port.\nDefault: 5432.");
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (Exception e) {
			System.out.println("Usage Error: " + e.getMessage());
			showUsage(options);
		}
		
		try {
			importAnnotation(cmd);
		} catch (Exception e){
			System.out.println("Annotation Import Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	private static void showUsage(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "AnnotationImporter", options );
	}
	
	
	private static void importAnnotation(CommandLine cmd) throws Exception{

		AnnotationImporterGeneric aig = new AnnotationImporterGeneric();
		aig.configureShard(getJDBCConnectionString(cmd), cmd.getOptionValue("u", null), cmd.getOptionValue("p", null));
		aig.importAnnotation(cmd.getOptionValue("c"),cmd.getOptionValue("i"));
		System.out.println("DONE!");
		
	}
	
	
	private static String getJDBCConnectionString(CommandLine cmd){
		String jdbcPath = "jdbc:postgresql://";
		jdbcPath += cmd.getOptionValue("h","localhost");
		jdbcPath +=":";
		jdbcPath += cmd.getOptionValue("P","5432");
		jdbcPath += "/";
		jdbcPath += cmd.getOptionValue("d");
		return jdbcPath;
	}
	
	
}