package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import phenote.datamodel.OntologyManager;
import phenote.datamodel.Ontology;


public class TermRequestGUI extends JFrame {

	private ButtonGroup buttonGroup = new ButtonGroup();
	private JComboBox ontologyComboBox;
	private JRadioButton newRadioButton;
  private JTextField newtermName;
  private JTextArea newtermDef;
  private JTextArea newtermComment;
  


  private JFrame thisframe = this;

	
	/**
	 * Launch the application
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			TermRequestGUI frame = new TermRequestGUI();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the frame
	 */
	public TermRequestGUI() {
		super();
		setTitle("Term Request");
		setBounds(100, 100, 500, 375);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		final JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);

		JLabel termLabel;
		termLabel = new JLabel();
		termLabel.setText("Term:");

//		JRadioButton newRadioButton;
		newRadioButton = new JRadioButton();
		newRadioButton.setSelected(true);
		buttonGroup.add(newRadioButton);
		newRadioButton.setText("New");

		JRadioButton modifyRadioButton;
		modifyRadioButton = new JRadioButton();
		buttonGroup.add(modifyRadioButton);
		modifyRadioButton.setText("Modify");

		JLabel ontologyLabel;
		ontologyLabel = new JLabel();
		ontologyLabel.setText("Ontology");

		ontologyComboBox = new JComboBox();
  	for (Ontology o : OntologyManager.inst().getAllOntologies()) {
      ontologyComboBox.addItem(o.getName());
    }
//    ontologyComboBox.addActionListener(new OntologyChooserListener());


		JLabel nameLabel;
		nameLabel = new JLabel();
		nameLabel.setText("Name");

		JLabel definitionLabel;
		definitionLabel = new JLabel();
		definitionLabel.setText("Definition");

		JLabel commentsLabel;
		commentsLabel = new JLabel();
		commentsLabel.setText("Comments");

//		JTextArea newtermDef;
		newtermDef = new JTextArea();
		newtermDef.setWrapStyleWord(true);
		newtermDef.setLineWrap(true);
		newtermDef.setBorder(new BevelBorder(BevelBorder.LOWERED));

//		JTextField newtermName;
		newtermName = new JTextField();

//		JTextArea newtermComment;
		newtermComment = new JTextArea();
		newtermComment.setWrapStyleWord(true);
		newtermComment.setLineWrap(true);
		newtermComment.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JButton cancelButton;
		cancelButton = new JButton();
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				thisframe.setVisible(false);
			}		});
		cancelButton.setText("Cancel");

		final GroupLayout groupLayout = new GroupLayout(panel);;
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(GroupLayout.LEADING)
					.add(groupLayout.createSequentialGroup()
						.addContainerGap()
						.add(groupLayout.createParallelGroup(GroupLayout.LEADING)
							.add(groupLayout.createSequentialGroup()
								.add(ontologyLabel)
								.add(20, 20, 20)
								.add(ontologyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addContainerGap())
							.add(groupLayout.createParallelGroup(GroupLayout.LEADING)
								.add(groupLayout.createSequentialGroup()
									.add(termLabel)
									.add(42, 42, 42)
									.add(newRadioButton)
									.add(28, 28, 28)
									.add(modifyRadioButton)
									.add(252, 252, 252))
								.add(groupLayout.createSequentialGroup()
									.add(groupLayout.createParallelGroup(GroupLayout.LEADING)
										.add(definitionLabel)
										.add(nameLabel)
										.add(commentsLabel))
									.addPreferredGap(LayoutStyle.RELATED)
									.add(groupLayout.createParallelGroup(GroupLayout.TRAILING)
										.add(GroupLayout.LEADING, newtermName, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
										.add(GroupLayout.LEADING, newtermComment, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
										.add(GroupLayout.LEADING, newtermDef, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE))
									.add(82, 82, 82)))))
			);
			groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(GroupLayout.LEADING)
					.add(groupLayout.createSequentialGroup()
						.addContainerGap()
						.add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
							.add(termLabel)
							.add(newRadioButton)
							.add(modifyRadioButton))
						.add(22, 22, 22)
						.add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
							.add(ontologyLabel)
							.add(ontologyComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.add(18, 18, 18)
						.add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
							.add(nameLabel)
							.add(newtermName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.RELATED)
						.add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
							.add(definitionLabel)
							.add(newtermDef, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.add(groupLayout.createParallelGroup(GroupLayout.BASELINE)
							.add(commentsLabel)
							.add(newtermComment, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE))
						.add(71, 71, 71))
			);

		panel.setLayout(groupLayout);

		final JPopupMenu popupMenu = new JPopupMenu();
		addPopup(panel, popupMenu);

		final JMenuItem copyMenuItem = new JMenuItem();
		copyMenuItem.setText("Copy");
		popupMenu.add(copyMenuItem);

		final JMenuItem cutMenuItem = new JMenuItem();
		cutMenuItem.setText("Cut");
		popupMenu.add(cutMenuItem);

		final JMenuItem pasteMenuItem = new JMenuItem();
		pasteMenuItem.setText("Paste");
		popupMenu.add(pasteMenuItem);
//D_SIZE))
//					.add(71, 71, 71
		final JPanel buttons = new JPanel();
		getContentPane().add(buttons, BorderLayout.SOUTH);

		JButton submitButton;
		submitButton = new JButton();
		submitButton.addActionListener(new submitButtonActionListener());
//nebmilBu);
		submitButton.setText("Submit");

		final GroupLayout groupLayout_1 = new GroupLayout(buttons); 		
		
		groupLayout_1.setHorizontalGroup(
				groupLayout_1.createParallelGroup(GroupLayout.TRAILING)
				.add(groupLayout_1.createSequentialGroup()
						.addContainerGap(278, Short.MAX_VALUE)
						.add(cancelButton)
						.add(21, 21, 21)
						.add(submitButton)
						.add(51, 51, 51))
		);
		groupLayout_1.setVerticalGroup(
				groupLayout_1.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout_1.createSequentialGroup()
						.addContainerGap(17, Short.MAX_VALUE)
						.add(groupLayout_1.createParallelGroup(GroupLayout.BASELINE)
								.add(submitButton)
								.add(cancelButton))
								.addContainerGap())
		);
		buttons.setLayout(groupLayout_1);
//tton))
//					.addContainerGap())
				//
	}
	


	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e);
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e);
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	private class submitButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			String m= ("Will submit term via http request\n");
	    m="[TYPE] ";
			if (newRadioButton.isSelected()) {
				m += "NEW\n";
			} else {
				m += "MODIFY\n";
			}
			m+="[ONTOLOGY] "+ontologyComboBox.getSelectedItem().toString()+"\n";
		  m+="[NAME] "+newtermName.getText()+"\n";
		  m+="[DEF] "+ newtermDef.getText()+"\n";
		  m+="[COMMENT] "+ newtermComment.getText()+"\n";
		  String newtermID = submitNewTermViaREST();
		  if (newRadioButton.isSelected()) {
		  	m = "<html><body>Your term <bold>"+newtermName.getText()+"</bold> has been successfully created.\n";
		  	m+= "It has been given a temporary ID <font color=red>"+newtermID+"</font></body></html>";
		  }
		  if (newtermID!=null) {
		  }
      JOptionPane.showMessageDialog(null, m, "Submission Successful", JOptionPane.PLAIN_MESSAGE);
			thisframe.setVisible(false);
		}
	}
	private String submitNewTermViaREST() {
		return "nlw_temp:1234567";
	}
}
