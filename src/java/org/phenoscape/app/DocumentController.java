package org.phenoscape.app;

import java.io.File;

import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;

public abstract class DocumentController {
  
  private File currentFile;
  
  public void open() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showOpenDialog(GUIManager.getManager().getFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      final boolean success = this.readData(fileChooser.getSelectedFile());
      if (!success) { this.runFileReadErrorMessage(); }
    }
  }
  
  public void save() {
    if (this.getCurrentFile() == null) {
      this.saveAs();
    } else {
      this.writeData(this.getCurrentFile());
    }
  }
  
  public void saveAs() {
    final JFileChooser fileChooser = new JFileChooser();
    final int result = fileChooser.showSaveDialog(GUIManager.getManager().getFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      final boolean success = this.writeData(fileChooser.getSelectedFile());
      if (!success) { this.runFileWriteErrorMessage(); }
    }
  }

  public File getCurrentFile() {
    return this.currentFile;
  }
  
  public void setCurrentFile(File aFile) {
    this.currentFile = aFile;
  }
  
  public abstract boolean readData(File aFile);
  
  public abstract boolean writeData(File aFile);
  
  private void runFileReadErrorMessage() {
    log().error("Failed to load file data");
    //TODO gui message
  }
  
  private void runFileWriteErrorMessage() {
    log().error("Failed to write file data");
    //TODO gui message
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
