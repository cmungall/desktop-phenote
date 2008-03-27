package phenote.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractGUIComponent;

import phenote.dataadapter.ScratchGroup;
import phenote.dataadapter.ScratchGroupsManager;
import phenote.util.FileUtil;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.eekboom.utils.Strings;

/**
 * Displays editable list of existing scratch groups.
 * @author Jim Balhoff
 */
@SuppressWarnings("serial")
public class ScratchGroupsView extends AbstractGUIComponent {
  
  private JButton addButton;
  private JButton deleteButton;
  private JButton showTableButton;
  private EventSelectionModel<ScratchGroup> selectionModel;

  public ScratchGroupsView(String id) {
    super(id);
  }

  @Override
  public void init() {
    super.init();
    this.setLayout(new BorderLayout());
    this.add(this.createTable(), BorderLayout.CENTER);
    this.add(this.createToolBar(), BorderLayout.NORTH);
  }
  
  public void addNewScratchGroup() {
    ScratchGroupsManager.inst().newScratchGroup();
  }
  
  public void deleteSelectedScratchGroups() {
    ScratchGroupsManager.inst().deleteScratchGroups(this.selectionModel.getSelected());
  }
  
  public void showSelectedScratchGroups() {
    ScratchGroupsManager.inst().showScratchGroups(this.selectionModel.getSelected());
  }
  
  private JComponent createTable() {
    this.selectionModel = new EventSelectionModel<ScratchGroup>(ScratchGroupsManager.inst().getScratchGroups());
    final EventTableModel<ScratchGroup> model = new EventTableModel<ScratchGroup>(ScratchGroupsManager.inst().getScratchGroups(), new ScratchGroupsTableFormat());
    final JTable table = new JTable(model);
    table.putClientProperty("Quaqua.Table.style", "striped");
    table.setSelectionModel(this.selectionModel);
    final JScrollPane scrollPane = new JScrollPane(table);
    return scrollPane;
  }
  
  @SuppressWarnings("serial")
  private JToolBar createToolBar() {
    final JToolBar toolBar = new JToolBar("Default Toolbar");
    
    try {
      this.addButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-add.png"))) {
          public void actionPerformed(ActionEvent e) {
            addNewScratchGroup();
          }
        });
      this.addButton.setToolTipText("Add");
      toolBar.add(this.addButton);
      
      this.deleteButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/list-remove.png"))) {
          public void actionPerformed(ActionEvent e) {
            deleteSelectedScratchGroups();
          }
        });
      this.deleteButton.setToolTipText("Delete");
      toolBar.add(this.deleteButton);
      
      this.showTableButton = new JButton(new AbstractAction(null, new ImageIcon(FileUtil.findUrl("images/table.png"))) {
        public void actionPerformed(ActionEvent e) {
          showSelectedScratchGroups();
        }
      });
      this.showTableButton.setToolTipText("Show Table");
      toolBar.add(this.showTableButton);

    } catch (FileNotFoundException e) {
      log().error("Couldn't find toolbar icons", e);
    }
    
    toolBar.setFloatable(false);
    return toolBar;
  }
  
  private static class ScratchGroupsTableFormat implements AdvancedTableFormat<ScratchGroup>, WritableTableFormat<ScratchGroup> {

    public Class<?> getColumnClass(int column) {
      return String.class;
    }

    public Comparator<?> getColumnComparator(int column) {
      return Strings.getNaturalComparator();
    }
     
    public int getColumnCount() {
      return 1;
    }

    public String getColumnName(int column) {
        return "Scratch Lists";
    }

    public Object getColumnValue(ScratchGroup baseObject, int column) {
      return baseObject.getTitle();
    }

    public boolean isEditable(ScratchGroup baseObject, int column) {
      return true;
    }

    public ScratchGroup setColumnValue(ScratchGroup baseObject, Object editedValue, int column) {
      baseObject.setTitle((String)editedValue);
      return baseObject;
    }
    
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
