package phenote.jobs;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import phenote.dataadapter.OntologyDataAdapter;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;

import java.io.File;
import java.util.Date;

/**
 * Job that reloads and archives a specified ontology. This job is scheduled via Quartz.
 * The job is defined in the spring configuration file.
 */
public class OntologyUpdateJob extends QuartzJobBean {

  public static final Logger LOG = Logger.getLogger(OntologyUpdateJob.class);
  public static final long MILLISECONDS_PER_DAY = (24 * 60 * 60 * 1000);

  // set by spring
  private String ontologyName;
  private String archiveDirectory;
  private double purgePeriodInDays;


  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    LOG.info("Started Ontology Update Job: " + ontologyName);


    Ontology ontology = OntologyManager.inst().getOntologyForName(ontologyName);
    if (ontology == null) {
      // In case no ontology has ever been loaded.
      // we assume we run in lazy initialization mode
      // and need to initialize first.
      OntologyDataAdapter.initialize();
      ontology = OntologyManager.inst().getOntologyForName(ontologyName);
      // Check if ontology by this name is found
      if (ontology == null)
        LOG.warn("No Ontology with name :" + ontologyName + " found!");
      // we can return here in either case: If ontology was found it read the latest version
      // if it could not be found no more action needed.
      return;
    }

    try {
      File newFile = newOntologyFile(ontology);
      if (newFile != null) {
        OntologyDataAdapter ontReader = OntologyDataAdapter.getInstance();
        //ontReader.reloadOntology(ontology);
         // have to now reload all of the ontologies as they are all in 1 oboSession
        ontReader.reloadOntologies();

/*
        File archiveFile = FileUtil.archiveFile(newFile, PhenoteWebConfiguration.getInstance().getWebRoot(), archiveDirectory);
        long purgePeriodInMilliseconds = (long) (purgePeriodInDays * MILLISECONDS_PER_DAY );
        FileUtil.purgeArchiveDirectory(archiveFile.getParentFile(), purgePeriodInMilliseconds);
*/
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /*
   * Retrieve the new ontology file if available.
   * It checks the time stamps of the new file and the file
   * from which it was loaded (save in the Ontology object)
   */
  private File newOntologyFile(Ontology ontology) {
    String fileName = ontology.getSource();
    File file = new File(fileName);
    long timestamp = file.lastModified();
    long timestampOldFile = ontology.getTimestamp();
    if (timestamp > timestampOldFile) {
      LOG.debug("New File  " + file.getAbsolutePath() + " found. ");
      return file;
    } else {
      Date date = new Date(timestampOldFile);
      LOG.debug("The file  " + file.getAbsolutePath() + " has not changed since the last load: " + date);
      return null;
    }
  }

  public String getOntologyName() {
    return ontologyName;
  }

  public void setOntologyName(String ontologyName) {
    this.ontologyName = ontologyName;
  }

  public String getArchiveDirectory() {
    return archiveDirectory;
  }

  public void setArchiveDirectory(String archiveDirectory) {
    this.archiveDirectory = archiveDirectory;
  }

  public double getPurgePeriodInDays() {
    return purgePeriodInDays;
  }

  public void setPurgePeriodInDays(double purgePeriodInDays) {
    this.purgePeriodInDays = purgePeriodInDays;
  }
}

