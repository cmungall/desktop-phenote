package phenote.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * A TransferHandler that can use another transfer handler for default behavior.
 * For example, pass the default transfer handler from a JTextField when
 * constructing an instance of this class, to add drop support for a new data type
 * while keeping the existing support for text data.
 * @author Jim Balhoff
 */
@SuppressWarnings("serial")
public class DelegatingTransferHandler extends TransferHandler {
  
  private TransferHandler parent;
  
  public DelegatingTransferHandler(TransferHandler parentHandler) {
    super();
    this.parent = parentHandler;
  }

  @Override
  public void exportAsDrag(JComponent comp, InputEvent e, int action) {
    this.parent.exportAsDrag(comp, e, action);
  }

  @Override
  public void exportToClipboard(JComponent comp, Clipboard clip, int action)
      throws IllegalStateException {
    this.parent.exportToClipboard(comp, clip, action);
  }

  @Override
  public int getSourceActions(JComponent c) {
    return this.parent.getSourceActions(c);
  }

  @Override
  public Icon getVisualRepresentation(Transferable t) {
    return this.parent.getVisualRepresentation(t);
  }

  @Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    return parent.canImport(comp, transferFlavors);
  }

  @Override
  public boolean importData(JComponent comp, Transferable t) {
    return this.parent.importData(comp, t);
  }
  
}
