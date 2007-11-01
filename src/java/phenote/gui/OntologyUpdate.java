package phenote.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;



/**
 * @author Nicole Washington
 * 
 * @see This class is for presenting a gui to the user given a list of 
 * ontologies that has updates available.  The user will select
 * some or all of the items, which will be fetched from their respective
 * repository locations (as contained in the config file).  Progress
 * bars will refresh to indicate the progress.  Any errors in download
 * or other messages will be presented for the user. 
 * Extensions to this (in the future) might include the ability for the user
 * to view differences between their old version of the ontology and the
 * new one, or other smart updates.
 *
 */

public class OntologyUpdate extends JFrame {

	/**
	 * Launch the application
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			OntologyUpdate frame = new OntologyUpdate();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the frame
	 */
	public OntologyUpdate() {
		super();
		setTitle("Ontology Update");
		setAlwaysOnTop(true);
		setBounds(100, 100, 500, 375);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		getContentPane().add(contentPanel);

		JLabel statusLabel;
		statusLabel = new JLabel();
		statusLabel.setText("Status");

		JLabel updateLabel;
		updateLabel = new JLabel();
		updateLabel.setText("Update");

		JLabel nameLabel;
		nameLabel = new JLabel();
		nameLabel.setText("Name");

		JLabel versionLabel;
		versionLabel = new JLabel();
		versionLabel.setText("Version");

		JLabel sizeLabel;
		sizeLabel = new JLabel();
		sizeLabel.setText("Size");

		JCheckBox installOntology;
		installOntology = new JCheckBox();
		installOntology.setToolTipText("Select to update ontology from repository");

		JLabel ontologyName;
		ontologyName = new JLabel();
		ontologyName.setText("Ontology Name");

		JLabel versionInfo;
		versionInfo = new JLabel();
		versionInfo.setText("1.0");

		JLabel fileSize;
		fileSize = new JLabel();
		fileSize.setText("0.0");

		JLabel statusIcon;
		statusIcon = new JLabel();
		statusIcon.setToolTipText("Repository location");
		statusIcon.setIcon(new ImageIcon("images/OK.GIF"));
//		statusIcon.setText("OK");

		JButton infoButton;
		infoButton = new JButton();
		infoButton.setToolTipText("Get more information");
		infoButton.setText("i");
		
		//layout for all center components
		final GroupLayout groupLayout = new GroupLayout((JComponent) contentPanel);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout.createSequentialGroup()
					.add(groupLayout.createParallelGroup(GroupLayout.LEADING)
						.add(groupLayout.createSequentialGroup()
							.add(10, 10, 10)
							.add(statusLabel)
							.add(17, 17, 17)
							.add(updateLabel))
						.add(groupLayout.createSequentialGroup()
							.addContainerGap()
							.add(statusIcon)
							.addPreferredGap(LayoutStyle.RELATED)
							.add(installOntology)))
					.add(28, 28, 28)
					.add(groupLayout.createParallelGroup(GroupLayout.LEADING)
						.add(groupLayout.createSequentialGroup()
							.add(nameLabel)
							.addPreferredGap(LayoutStyle.RELATED, 222, Short.MAX_VALUE)
							.add(versionLabel))
						.add(groupLayout.createSequentialGroup()
							.add(ontologyName, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.RELATED)
							.add(infoButton, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.RELATED, 39, Short.MAX_VALUE)
							.add(versionInfo)))
					.addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.add(groupLayout.createParallelGroup(GroupLayout.TRAILING)
						.add(sizeLabel)
						.add(fileSize))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout.createSequentialGroup()
					.addContainerGap()
					.add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
						.add(statusLabel)
						.add(nameLabel)
						.add(versionLabel)
						.add(updateLabel)
						.add(sizeLabel))
					.add(12, 12, 12)
					.add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
						.add(versionInfo)
						.add(statusIcon)
						.add(installOntology)
						.add(fileSize)
						.add(ontologyName)
						.add(infoButton))
					.addContainerGap(138, Short.MAX_VALUE))
		);
		contentPanel.setLayout(groupLayout);

		final JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JProgressBar progressBar;
		progressBar = new JProgressBar();

		JButton cancel;
		cancel = new JButton();
		cancel.setText("Cancel");

		JButton updateButton;
		updateButton = new JButton();
		updateButton.addActionListener(new UpdateActionListener());
		updateButton.setText("Update");

		JButton updateAllButton;
		updateAllButton = new JButton();
		updateAllButton.setText("Update All");

		JCheckBox autoupdateCheckBox;
		autoupdateCheckBox = new JCheckBox();
		autoupdateCheckBox.setText("Automatically update in the future");

		JTextPane messagePane;
		messagePane = new JTextPane();

		JButton settingsButton;
		settingsButton = new JButton();
		settingsButton.setText("Settings...");
		
		//layout for button panel
		final GroupLayout groupLayout_1 = new GroupLayout((JComponent) buttonPanel);
		groupLayout_1.setHorizontalGroup(
			groupLayout_1.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout_1.createSequentialGroup()
					.add(42, 42, 42)
					.add(groupLayout_1.createParallelGroup(GroupLayout.LEADING)
						.add(groupLayout_1.createSequentialGroup()
							.add(messagePane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.RELATED, 30, Short.MAX_VALUE)
							.add(settingsButton)
							.add(25, 25, 25))
						.add(progressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.add(37, 37, 37)
					.add(groupLayout_1.createParallelGroup(GroupLayout.LEADING)
						.add(autoupdateCheckBox)
						.add(groupLayout_1.createSequentialGroup()
							.add(cancel)
							.addPreferredGap(LayoutStyle.RELATED)
							.add(updateButton)
							.addPreferredGap(LayoutStyle.RELATED)
							.add(updateAllButton)))
					.addContainerGap(20, Short.MAX_VALUE))
		);
		groupLayout_1.setVerticalGroup(
			groupLayout_1.createParallelGroup(GroupLayout.TRAILING)
				.add(groupLayout_1.createSequentialGroup()
					.addContainerGap(23, Short.MAX_VALUE)
					.add(groupLayout_1.createParallelGroup(GroupLayout.TRAILING)
						.add(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.add(autoupdateCheckBox))
					.addPreferredGap(LayoutStyle.RELATED)
					.add(groupLayout_1.createParallelGroup(GroupLayout.TRAILING)
						.add(groupLayout_1.createSequentialGroup()
							.add(groupLayout_1.createParallelGroup(GroupLayout.LEADING)
								.add(groupLayout_1.createParallelGroup(GroupLayout.BASELINE)
									.add(cancel)
									.add(updateButton)
									.add(updateAllButton))
								.add(messagePane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.add(26, 26, 26))
						.add(groupLayout_1.createSequentialGroup()
							.add(settingsButton)
							.addContainerGap())))
		);
		buttonPanel.setLayout(groupLayout_1);

		final JPanel messagePanel = new JPanel();
		getContentPane().add(messagePanel, BorderLayout.NORTH);

		final JLabel updatesForTheLabel = new JLabel();
		updatesForTheLabel.setText("Updates for the following ontologies are available!");
		messagePanel.add(updatesForTheLabel);
		//
	}
	
  private class UpdateActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	//will tell the system to update all the ontologies
    	//will also handle all the progress bar progress for download!
      String m = "You've selected Update ";
      JOptionPane.showMessageDialog(null, m, "Phenote Message",
                                    JOptionPane.INFORMATION_MESSAGE);
    	
    }
  }
  private class UpdateAllActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	//will tell the system to update all the ontologies
    	//will also handle all the progress bar progress for download!
      String m = "You've selected Update All";
      JOptionPane.showMessageDialog(null, m, "Phenote Message",
                                    JOptionPane.INFORMATION_MESSAGE);
    	
    }
  }
  private class CancelUpdateActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	//will tell the system to update all the ontologies
    	//will also handle all the progress bar progress for download!
      String m = "You've selected Cancel Update";
      JOptionPane.showMessageDialog(null, m, "Phenote Message",
                                    JOptionPane.INFORMATION_MESSAGE);
    	
    }
  }

}
