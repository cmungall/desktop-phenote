package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import phenote.config.Config;
import phenote.config.xml.OntologyFileDocument;
import phenote.config.xml.OntologyFileDocument.OntologyFile;
import phenote.dataadapter.OntologyDataAdapter;
import phenote.dataadapter.OntologyDataAdapter2;
import phenote.datamodel.OntologyException;
import phenote.util.FileUtil;



/**
 * @author Nicole Washington
 * 
 * This class is for presenting a gui to the user given a list of 
 * ontologies that has updates available.  The user will select
 * some or all of the items, which will be fetched from their respective
 * repository locations (as contained in the config file).  Progress
 * bars will refresh to indicate the progress.  Any errors in download
 * or other messages will be presented for the user. 
 * Extensions to this (in the future) might include the ability for the user
 * to view differences between their old version of the ontology and the
 * new one, or other smart updates. <br>
 * Note that some of the methods were helped by code from 
 * http://www.devx.com/getHelpOn/10MinuteSolution/20425<br>
 * Right now this is called from the main Phenote class, but i think it should
 * possibly be reordered and called from the ontologyDataAdapter. <br>
 */

//public class OntologyUpdate extends JDialog {
public class OntologyUpdate {
	
	private final static String newline = "\n";
	private JTextArea messageTextArea;
	JScrollPane messageScrollPane = new JScrollPane(messageTextArea); 
	StringBuffer sb = new StringBuffer();
	private TableCellRenderer defaultRenderer;
	private JTable ontologyTable;
	private JPanel contentPanel = new JPanel(new BorderLayout());
	List<String> ontsToUpdate = null;
  private static final Logger LOG = Logger.getLogger(OntologyDataAdapter.class);
  private java.awt.Frame f = phenote.main.Phenote.getPhenote().getFrame();
  // true -> modal -> this is crucial! 
  private JDialog dialog = new JDialog(f, true);
  private Object[] options = {"Update All","Update","Cancel"};
  private Object selection = options[0];  //Update All, Update, Cancel
  int numUpdates = 0;
  private OntologyFile[] ontologies = Config.inst().getTerminologyDefs().getOntologyFileArray();

	
	public static Object queryForOntologyUpdate() {
		OntologyUpdate o = new OntologyUpdate();
		return o.getDialog();
	}

	private Object getDialog() {
		createWindow();
		if (numUpdates>0) {
			dialog.setContentPane(contentPanel);
			dialog.setTitle("Ontology Update");
			dialog.setBounds(100, 100, 500, 300);
			dialog.setResizable(true);
			dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      dialog.setLocationRelativeTo(null); // centers panel on screen
      dialog.setEnabled(true);
			dialog.setVisible(true);
			dialog.pack();
		}
		return selection;
	}
	
	private void createWindow() {

		ontologyTable = new JTable() {
			//Implement table row tool tips.
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				URL localUrl = null;
				URL reposUrl = null;
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				String handle = getValueAt(rowIndex, 2).toString();
				OntologyFile ontology = Config.inst().getOntologyFileByHandle(handle);
				try {
					if (ontology.getLocation()!=null) {
						reposUrl = new URL(ontology.getLocation()+ontology.getFilename()); 
					} else {
						reposUrl = new URL("no repository location");
					}
				} catch(MalformedURLException mue) {}
				localUrl = getLocalUrl(ontology.getFilename());
					tip = reposUrl.toString();  //show the repository URL
					String oldDate = FileUtil.getLastModifiedDate(localUrl);
					if (oldDate!=null) {//Version - show last date downloaded
						tip += "<br>Last downloaded: "+oldDate;
					}
				return "<html>"+tip+"</html>";
			}
		};
		ontologyTable.setModel(new OntologyUpdateTableModel());
    defaultRenderer = ontologyTable.getDefaultRenderer(JButton.class);
    ontologyTable.setDefaultRenderer(JButton.class,
			       new JTableButtonRenderer(defaultRenderer));
    ontologyTable.addMouseListener(new JTableButtonMouseListener(ontologyTable));
//		ontologyTable.setMaximumSize(new Dimension(300,200));
		ontologyTable.setMinimumSize(new Dimension(300,100));
		ontologyTable.setPreferredSize(new Dimension(300,200));
		TableColumn column = null;
		//				"Update","Status", "Name", "Version", "Info","Size"
		//set the column widths
		for (int i = 0; i < ontologyTable.getColumnCount(); i++) {
		    column = ontologyTable.getColumnModel().getColumn(i);
//		    int[] widths = {20,20,150,30,30,20};
		    int[] widths = {20,20,150,30,20};
		    column.setPreferredWidth(widths[i]);
		}
		
		//throw it in a scrollpane
		final JScrollPane scrollPane = new JScrollPane(ontologyTable);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(ontologyTable);
		contentPanel.setMinimumSize(new Dimension(200,100));
		contentPanel.setPreferredSize(new Dimension(200,200));
		contentPanel.add(scrollPane);
		contentPanel.setVisible(true);

		//loop through all the ontologies in the configuration and throw 'em in the table
		//right now this lists everything, regardless of whether or not it should be updated.
		addOntologiesToTable();

		final JPanel buttonPanel = new JPanel();
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);

		JButton cancelButton;
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		cancelButton.setToolTipText("Cancel update and continue.");
		cancelButton.addActionListener(new CancelUpdateActionListener());

		JButton updateButton;
		updateButton = new JButton();
		updateButton.addActionListener(new UpdateActionListener());
		updateButton.setToolTipText("Update selected ontologies");
		updateButton.setText("Update");

		JButton updateAllButton;
		updateAllButton = new JButton();
		updateAllButton.setText("Update All");
		updateAllButton.setToolTipText("Update ALL ontologies");
		updateAllButton.addActionListener(new UpdateAllActionListener());

		JButton settingsButton;
		settingsButton = new JButton();
		settingsButton.setText("Settings...");
		settingsButton.setEnabled(false);

		messageTextArea = new JTextArea();
		messageTextArea.setEditable(false);
		messageTextArea.setBorder(new LineBorder(Color.GRAY, 1, true));
		
		//layout for button panel
		final GroupLayout groupLayout_1 = new GroupLayout((JComponent) buttonPanel);
		groupLayout_1.setHorizontalGroup(
			groupLayout_1.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, groupLayout_1.createSequentialGroup()
					.add(26, 26, 26)
					.add(groupLayout_1.createParallelGroup(GroupLayout.TRAILING)
						.add(GroupLayout.LEADING, messageTextArea, GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
						.add(groupLayout_1.createSequentialGroup()
							.add(settingsButton)
							.add(18, 18, 18)
							.add(cancelButton)
							.addPreferredGap(LayoutStyle.RELATED, 75, Short.MAX_VALUE)
							.add(updateButton)
							.addPreferredGap(LayoutStyle.RELATED)
							.add(updateAllButton)))
					.add(41, 41, 41))
		);
		groupLayout_1.setVerticalGroup(
			groupLayout_1.createParallelGroup(GroupLayout.TRAILING)
				.add(groupLayout_1.createSequentialGroup()
					.addContainerGap()
					.add(messageTextArea, GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
					.addPreferredGap(LayoutStyle.RELATED)
					.add(groupLayout_1.createParallelGroup(GroupLayout.BASELINE)
						.add(cancelButton)
						.add(settingsButton)
						.add(updateButton)
						.add(updateAllButton))
					.addContainerGap())
		);
		buttonPanel.setLayout(groupLayout_1);

		final JPanel messagePanel = new JPanel();
		contentPanel.add(messagePanel, BorderLayout.NORTH);

		final JLabel updatesForTheLabel = new JLabel();
		updatesForTheLabel.setText("Updates for the following ontologies are available!");
		messagePanel.add(updatesForTheLabel);
		
		contentPanel.validate();
		contentPanel.repaint();
	}
	
	private void closeWindow() {
		dialog.dispose();
	}
	

  
  /**
   * This is where the meat will occur.  It will call on the ontologyDataAdapter to
   * (1) determine what ontologies there are
   * (2) determine if there's newer versions available
   * (3) flag if they are to be autodownloaded
   * (4) disable the row if the file is up-to-date, and add that as a tooltip
   */
  private void addOntologiesToTable() {
		//first, add all the ontologies to the table
  	//loop through each ontology, place those in the table for which there's an update
		for (int i=0; i<ontologies.length; i++) {
			createOntologyRow(ontologies[i],i);  //i'm using a global to do this...yucky!
		}
		
  }



	/**
   * This function creates a new row in the Ontology Update frame, based
   * on (1) if the ontology needs to be updated, (2) sets the default checkbox 
   * behavior basedon the autoupdate flag.  it will check only once per each
   * ontology.  it will be disabled if the ontology is current.<br>
   * First iteration, it will display all ontologies, regardless whether or
   * not they need to be updated.
   * @param ontology The current ontology being looped through in the calling
   * function
   */
  private void createOntologyRow (OntologyFileDocument.OntologyFile ontology, int row) {
  	//				"Update","Status", "Name", "Version", "Info","Size"

		JButton infoButton = new JButton();
		//need to create an imageicon button here
//		infoButton.setSize(1, 1);
		infoButton.setToolTipText("Get more information for "+ ontology.getHandle());
		infoButton.addMouseListener(new InfoMouseListener(ontology));
		boolean status = false;
		try {
			//if there's a new ontology available, it will flag it here.
			status = OntologyDataAdapter2.getInstance().checkForUpdate(ontology);
			if (status) {
				System.out.println("Update available for "+ontology.getFilename());
				numUpdates++;
			}
		} catch (OntologyException e) {
			e.printStackTrace();
		}

		//temp throwing in the OK icon, but will be other icons, depending on status
		//throw it into the table only if there's an update or a need to download
//		if (status) {
		URL localUrl = getLocalUrl(ontology.getFilename());
		URL remoteUrl = getRemoteUrl(ontology);
			String fileSize = FileUtil.getFileSize(localUrl);
//			String fileSize = FileUtil.getRemoteFileSize(remoteUrl); //need to do this eventually
//			Object[] rowData = new Object[]{status, " ", 
//					ontology.getHandle(), ontology.getVersion(), infoButton, fileSize+" Kb" };
			Object[] rowData = new Object[]{status, " ", 
					ontology.getHandle(), ontology.getVersion(), fileSize};

			for (int i=0; i<rowData.length; i++) {
				ontologyTable.getModel().setValueAt(rowData[i], row, i);
//			}
		}
  	return;
  }

  /**helper function for getting column index by name */
  public int getColumnByName (String name) {
		for (int i=0; i<ontologyTable.getColumnCount(); i++) {
			if (ontologyTable.getColumnName(i).equals(name)) {
				return i;
			}
		}
		return -1; //column name not found
	}
  
	/* ********************** TABLE METHODS ***************************/
  // might move these into their own class?
	class JTableButtonRenderer implements TableCellRenderer {
	  private TableCellRenderer __defaultRenderer;

	  public JTableButtonRenderer(TableCellRenderer renderer) {
	    __defaultRenderer = renderer;
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value,
							 boolean isSelected,
							 boolean hasFocus,
							 int row, int column)
	  {
	    if(value instanceof Component)
	      return (Component)value;
	    return __defaultRenderer.getTableCellRendererComponent(
		   table, value, isSelected, hasFocus, row, column);
	  }
	}
	
	class JTableProgressBarRenderer implements TableCellRenderer {
	  private TableCellRenderer __defaultRenderer;

	  public JTableProgressBarRenderer(TableCellRenderer renderer) {
	    __defaultRenderer = renderer;
	  }

	  public Component getTableCellRendererComponent(JTable table, Object value,
							 boolean isSelected,
							 boolean hasFocus,
							 int row, int column)
	  {
	    if(value instanceof Component)
	      return (Component)value;
	    return __defaultRenderer.getTableCellRendererComponent(
		   table, value, isSelected, hasFocus, row, column);
	  }
	}
	
//	public class FileSizeCellRenderer extends DefaultTableCellRenderer {
//
//		public FileSizeCellRenderer() {
//		}
//
//		public void setHorizontalAlignment(int alignment) {
//			setHorizontalAlignment(RIGHT);
//		}
//		
//		}
	
	class OntologyUpdateTableModel extends AbstractTableModel {
		public Vector<Object[]> data = initializeData();
//		public final String[] COLUMN_NAMES = new String[] {
//				"Update","Status", "Name", "Version", "Info","Size"
//		};
		public final String[] COLUMN_NAMES = new String[] {
				"Update","Status", "Name", "Version", "Size (kb)"
		};
		
		public int getRowCount() {
			return ontologies.length;
		}
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public Class<?> getColumnClass(int c) {
			if (getValueAt(0,c)==null)
				return String.class;
			else
				return getValueAt(0, c).getClass();
		}

		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex>data.size() || columnIndex>getColumnCount())
				return " ";
			if (data.get(rowIndex)[columnIndex]==null)
				return (" "); //put a blank in the table
			else
				return data.get(rowIndex)[columnIndex];				
		}
		
		public void setValueAt(Object value, int row, int col) {
			//only the update value should be changed and updated
			Object[] rowData = data.get(row);
			rowData[col] = value;
			data.set(row,rowData);
			fireTableCellUpdated(row, col);
		}
		
		public boolean isCellEditable(int row, int col) {
      //Note that the data/cell address is constant,
      //no matter where the cell appears onscreen.
      if ((col == 0)) { //can change the value of the checkbox
          return true;
      } else {
          return false;
      }
		}
		
		private Vector<Object[]> initializeData() {
			Vector<Object[]> allData = new Vector<Object[]>();
			for (int i=0; i<getRowCount(); i++) {
//				Object[] rowData = {"a","b","c","d","e","f"};				
				Object[] rowData = {"a","b","c","d","e"};				
				allData.add(i,rowData);
			}
			return allData;
		}
		
	}
	
	private void addUpdateMessage(String message) {
		sb.append(message);
		messageTextArea.setText(sb.toString());
		contentPanel.validate();
		contentPanel.repaint();
		return;
	}

	

  
  /* ************************ LISTENERS *****************************/

  private class UpdateActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	//will tell the system to update all the selected ontologies
    	//right now this will download an update if the user selects, even if
    	//current.  will change this so it won't even show up as a possibility
      String m ="";
      int up = 0;

//      BackgroundEventQueue queue = new BackgroundEventQueue();
//			BackgroundUtil.scheduleTask(queue, null, false, null);
//			queue.die();
//			if (eventTask.getResults())
//				return null;

			TableCellRenderer defaultRenderer = ontologyTable.getDefaultRenderer(JProgressBar.class);
			ontologyTable.setDefaultRenderer(JProgressBar.class,
		       new JTableProgressBarRenderer(defaultRenderer));

      for (int i=0; i<ontologyTable.getRowCount(); i++) {
      	OntologyFile ontology = Config.inst().getOntologyFileByHandle(ontologyTable.getValueAt(i,2).toString());
      	try {
      		//download those that are checked, need updating, and also if they don't exist
      		if ((ontologyTable.getValueAt(i,0).equals(true) && !OntologyDataAdapter2.getInstance().checkForUpdate(ontology)) ||
      				!OntologyDataAdapter2.getInstance().checkForLocalFileExists(ontology.getFilename())) {
      			m+= ontologyTable.getValueAt(i, 2)+ " ";
      			try {
      				ontologyTable.setValueAt("...", i, 1); //would like this to be a progress bar in here.
      				contentPanel.validate();
      				contentPanel.repaint();
      				addUpdateMessage("Downloading: "+ontologyTable.getValueAt(i,2));
      				OntologyDataAdapter2.getInstance().downloadUpdate(ontologyTable.getValueAt(i, 2).toString());
      				ontologyTable.setValueAt(new ImageIcon(FileUtil.findUrl("images/OK.GIF")), i, 1);
      				contentPanel.validate();
      				contentPanel.repaint();

      				addUpdateMessage(" DONE."+newline);
      				up++;
      			} catch(OntologyException oe) {
      				addUpdateMessage(oe.getMessage()+"error with updating ontology");
      				LOG.error(oe.getMessage()+"error with updating ontology");
      			} catch (FileNotFoundException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
      		}
      	} catch (OntologyException oe) {
      		addUpdateMessage(oe.getMessage()+"error with updating ontology");
  				LOG.error(oe.getMessage()+"error with updating ontology");
      	}
      }
      if (up==0) {
      	LOG.info("no ontologies needed updating");
      	m="no ontologies needed updating";
      } else {
      	m = "Only some ontologies needed updating.";
        m += up +" of " + numUpdates + " updated";
      }
      	//here, i should just update the window, and switch the button to "done"?
      //or just go on.  perhaps close the window, then display the update
      closeWindow();
      JOptionPane.showMessageDialog(null, m, "Ontology Update",
          JOptionPane.INFORMATION_MESSAGE);

    }
  }
  private class UpdateAllActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	//regardless of what the user selects, all ontologies that need
    	//updating will be updated
      String m = "You've selected Update All";
      boolean status = false;
      int row = 0;
      int up=0;
      //reselect those in the table that have updates.
      for (OntologyFile ontology : ontologies) {
      	try {
      		//if there's a new ontology available, it will snag it.
      		status = OntologyDataAdapter2.getInstance().checkForUpdate(ontology);
      		if (status) {
      			addUpdateMessage("Downloading: "+ontology.getHandle());
      			OntologyDataAdapter2.getInstance().downloadUpdate(ontology.getHandle());
      			up++;
      			for (int i=row; i<ontologyTable.getRowCount(); i++) {
      				if (ontologyTable.getValueAt(i, 2).equals(ontology.getHandle())) {
      					ontologyTable.setValueAt(new ImageIcon(FileUtil.findUrl("images/OK.GIF")), i, 1);
      					addUpdateMessage(" DONE."+newline);
      					break;
      				} 
      			}
//      			numUpdates++;
      		}
  				row++;
  			} catch(OntologyException oe) {
    			addUpdateMessage(oe.getMessage()+"error with updating ontology");
    			LOG.error(oe.getMessage()+"error with updating ontology");
    		} catch (FileNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
      }
        if (up==0) {
        	LOG.info("no ontologies updated");
        	m="no ontologies needed updating";
        } else {
          m = up +" of " + numUpdates + " ontologies have been updated";
        }

      JOptionPane.showMessageDialog(null, m, "Phenote Message",
                                    JOptionPane.INFORMATION_MESSAGE);
      closeWindow();
    	
    }
  }
  private class CancelUpdateActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	//This will continue loading without updating any ontologies
    	//actually, this needs to check to see if the file is on the local drive,
    	//if its not, then it needs to download anyway.
    	String m = "You've selected Cancel Update";
    	boolean exists = false;
    	int up=0;
    	int row=0;
      for (OntologyFile ontology : ontologies) {
      	try {
      		//if there's a new ontology available, it will snag it.
      		exists = OntologyDataAdapter2.getInstance().checkForLocalFileExists(ontology);
      		if (!exists) {
      			addUpdateMessage(ontology.getHandle()+" not found locally.  Downloading.");
      			OntologyDataAdapter2.getInstance().downloadUpdate(ontology.getHandle());
      			up++;
      			if (ontologyTable.getValueAt(row, 2).equals(ontology.getHandle())) {
      				ontologyTable.setValueAt(new ImageIcon(FileUtil.findUrl("images/OK.GIF")), row, 1);
      				addUpdateMessage(".....DONE."+newline);
      			} else {
      				row++; //this is a hack...these *should* be done in order
      			}
//    				numUpdates++;
      		}
  				row++;
  			} catch(OntologyException oe) {
    			addUpdateMessage(oe.getMessage()+"error with downloading initial copy of ontology");
    			LOG.error(oe.getMessage()+"error with downloading initial copy of ontology");
    		} catch (FileNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
      }
        if (up==0) {
        	LOG.info("no ontologies downloaded");
        	m="no ontologies downloaded";
        } else {
          m = up +" ontologies were not found locally and downloaded.";
        }

      JOptionPane.showMessageDialog(null, m, "Phenote Message",
                                    JOptionPane.INFORMATION_MESSAGE);
      closeWindow();
    	
    }
  }
  
  private class InfoMouseListener implements MouseListener {
  	OntologyFile o = null;
  	public InfoMouseListener(OntologyFile ontology) {
  		o = ontology;
    }

		public void mouseClicked(MouseEvent arg0) {
    String m = "<html>Additional information on <b>"+o.getHandle()+"</b>:<br>";
    m+= "Filename: "+o.getFilename()+"<br>";
     m+= "Location: "+o.getLocation()+"<br></html>";
    JOptionPane.showMessageDialog(null, m, "Ontology Information",
                                  JOptionPane.INFORMATION_MESSAGE);
			
		}
		public void mouseEntered(MouseEvent arg0) {
		}
		public void mouseExited(MouseEvent arg0) {
		}
		public void mousePressed(MouseEvent arg0) {
		}
		public void mouseReleased(MouseEvent arg0) {
		}
  }
  
  class JTableButtonMouseListener implements MouseListener {
    private JTable __table;

    private void __forwardEventToButton(MouseEvent e) {
      TableColumnModel columnModel = __table.getColumnModel();
      int column = columnModel.getColumnIndexAtX(e.getX());
      int row    = e.getY() / __table.getRowHeight();
      Object value;
      JButton button;
      MouseEvent buttonEvent;
      
      if(row >= __table.getRowCount() || row < 0 ||
         column >= __table.getColumnCount() || column < 0)
        return;

      value = __table.getValueAt(row, column);

      if(!(value instanceof JButton))
        return;

      button = (JButton)value;

      buttonEvent =
        (MouseEvent)SwingUtilities.convertMouseEvent(__table, e, button);
      button.dispatchEvent(buttonEvent);
      // This is necessary so that when a button is pressed and released
      // it gets rendered properly.  Otherwise, the button may still appear
      // pressed down when it has been released.
      __table.repaint();
    }

    public JTableButtonMouseListener(JTable table) {
      __table = table;
    }

    public void mouseClicked(MouseEvent e) {
      __forwardEventToButton(e);
    }

    public void mouseEntered(MouseEvent e) {
      __forwardEventToButton(e);
    }

    public void mouseExited(MouseEvent e) {
      __forwardEventToButton(e);
    }

    public void mousePressed(MouseEvent e) {
      __forwardEventToButton(e);
    }

    public void mouseReleased(MouseEvent e) {
      __forwardEventToButton(e);
    }
  }

  private URL getLocalUrl (String filename) {
  	URL localUrl;
  	try {
			localUrl = FileUtil.findUrl(filename);
			return localUrl;
  	} catch (FileNotFoundException fnfe) {}
  	return null;
  }

  private URL getRemoteUrl (OntologyFile ontology) {
  	URL remoteUrl=null;
  	try {
  		if (ontology.getLocation()!=null) {
  			remoteUrl = new URL(ontology.getLocation()+ontology.getFilename());
  		}
			return remoteUrl;
  	} catch (MalformedURLException mue) {}
  	return null;
  }
  
}
