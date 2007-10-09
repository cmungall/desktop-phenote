package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import phenote.main.PhenoteVersion;
import phenote.util.FileUtil;


public class LoadingScreen extends JFrame {

	/**
	 * Launch the application
	 * @param args
	 */
//	public static void main(String args[]) {
//		try {
//			LoadingScreen frame = new LoadingScreen();
//			frame.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	JProgressBar ontologyProgressBar;
	JProgressBar startupProgressBar;
	JLabel message;

	/**
	 * Create the frame
	 */
	public LoadingScreen() {
		super();
		setBackground(Color.WHITE);
		getContentPane().setBackground(Color.WHITE);
		setBounds(100, 100, 500, 375);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		final JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		getContentPane().add(panel, BorderLayout.CENTER);

		ImageIcon imageIcon = new ImageIcon(FileUtil.findUrl(
"images/elephant_header.jpg"));
		
		JLabel logo = new JLabel();
//		logo.createTitle("");
		logo.setIcon(imageIcon);
		//		logo.setIcon(SwingResourceManager.getIcon(LoadingScreen.class, "images/elephant_header.jpg"));


		startupProgressBar = new JProgressBar();
		startupProgressBar.setString("");
		startupProgressBar.setStringPainted(true);
		startupProgressBar.setMaximum(100);
		startupProgressBar.setValue(0);

		message = new JLabel();
		message.setHorizontalAlignment(SwingConstants.CENTER);
		message.setText("Initializing...");

		JLabel versionLabel;
		versionLabel = new JLabel();
		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		versionLabel.setText("version "+ PhenoteVersion.versionString());

		
		final GroupLayout groupLayout_1 = new GroupLayout((JComponent) panel);
		groupLayout_1.setHorizontalGroup(
			groupLayout_1.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout_1.createSequentialGroup()
					.add(87, 87, 87)
					.add(logo)
					.addContainerGap(87, Short.MAX_VALUE))
				.add(groupLayout_1.createSequentialGroup()
					.add(96, 96, 96)
					.add(versionLabel, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(95, Short.MAX_VALUE))
				.add(GroupLayout.TRAILING, groupLayout_1.createSequentialGroup()
					.add(43, 43, 43)
					.add(message, GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
					.add(40, 40, 40))
				.add(groupLayout_1.createSequentialGroup()
					.add(64, 64, 64)
					.add(startupProgressBar, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
					.add(63, 63, 63))
		);
		groupLayout_1.setVerticalGroup(
			groupLayout_1.createParallelGroup(GroupLayout.TRAILING)
				.add(GroupLayout.LEADING, groupLayout_1.createSequentialGroup()
					.add(24, 24, 24)
					.add(logo)
					.addPreferredGap(LayoutStyle.RELATED)
					.add(versionLabel)
					.addPreferredGap(LayoutStyle.RELATED, 83, Short.MAX_VALUE)
					.add(startupProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.RELATED)
					.add(message))
		);
		panel.setLayout(groupLayout_1);

		final JPanel footerPanel = new JPanel();
		footerPanel.setBackground(Color.WHITE);
		getContentPane().add(footerPanel, BorderLayout.SOUTH);

		JTextPane footerTextPane;
		footerTextPane = new JTextPane();
		footerTextPane.setContentType("text/html");
		footerTextPane.setFont(new Font("Helvetica", Font.PLAIN, 9));
		footerTextPane.setAutoscrolls(false);
		footerTextPane.setEditable(false);
		footerTextPane.setText("<html><body><center><font size=2>Phenote is part of the <a href='http://www.bioontology.org'>" +
				"National Center for Biomedical Ontology</a>, <br>" +
				"a part of the <a href='http://www.bisti.nih.gov/ncbc/index.cfm'> National " +
				"Centers for Biomedical Computing</a>, and is supported by the <a href='http://nihroadmap.nih.gov/'> NIH Roadmap</a>.<br> " +
				"Developed at the Berkeley Bioinformatics and Ontologies Project (<a href='http://www.berkeleybop.org'>BBOP</a>)</font></center></body></html>");

		final GroupLayout groupLayout = new GroupLayout((JComponent) footerPanel);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout.createSequentialGroup()
					.add(13, 13, 13)
					.add(footerTextPane, GroupLayout.PREFERRED_SIZE, 475, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(12, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(GroupLayout.TRAILING)
				.add(groupLayout.createSequentialGroup()
					.addContainerGap(70, Short.MAX_VALUE)
					.add(footerTextPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		footerPanel.setLayout(groupLayout);
		//
		pack();
	}
	
	public void setMessageText(String m) {
		message.setText(m);
		pack();
	}

  public void setProgressMax(int maxProgress)
  {
//    ontologyProgressBar.setMaximum(maxProgress);
  }

  public void setProgress(int progress)
  {
    final int theProgress = progress;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        startupProgressBar.setValue(theProgress);
      }
    });
  }
  
  public int getStartupProgress() {
  	return startupProgressBar.getValue();
  }

  public void setProgress(String message, int progress)
  {
    final int theProgress = progress;
    final String theMessage = message;
    setProgress(progress);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
//        ontologyProgressBar.setValue(theProgress);
//        ontologyProgressBar.setString(theMessage);
      }
    });
  }

  public void setScreenVisible(boolean b)
  {
    final boolean boo = b;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setVisible(boo);
      }
    });
  }

  public void setMessage(String message)
  {
    if (message==null)
    {
      message = "";
//      ontologyProgressBar.setStringPainted(false);
    }
    else
    {
//      ontologyProgressBar.setStringPainted(true);
    }
//    ontologyProgressBar.setString(message);
//    messagePanel.setText(message);
  }
  	
}
