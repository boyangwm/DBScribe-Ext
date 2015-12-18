/*
 * CreateFolderDialog.java
 *
 * Created on January 3, 2002, 10:08 PM
 */

package fina2.reportoo;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.swing.BorderFactory;
import javax.swing.JWindow;

import fina2.Main;
import fina2.reportoo.server.Report;
import fina2.reportoo.server.ReportHome;
import fina2.reportoo.server.ReportPK;
import fina2.returns.ReturnManagerFrame;

/**
 * 
 * @author David Shalamberidze
 */
public class AmendFolderDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private boolean ok;
	private ReportPK pk;
	private ReportPK parentPK;
	private String name;

	private JWindow errorWindow;
	private String errorString = ui.getString("fina2.report.invalidName");

	private ReportManagerFrame reportManagerFrame;

	/** Creates new form CreateFolderDialog */
	public AmendFolderDialog(java.awt.Frame parent, boolean modal, ReportManagerFrame reportManagerFrame) {
		super(parent, modal);
		this.reportManagerFrame = reportManagerFrame;
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.ok", "ok.gif");

		initComponents();
	}

	public void show(ReportPK pk, ReportPK parentPK) {
		this.pk = pk;
		this.parentPK = parentPK;

		if (pk != null) {
			try {
				InitialContext jndi = fina2.Main.getJndiContext();

				Object ref = jndi.lookup("fina2/reportoo/server/Report");
				ReportHome home = (ReportHome) PortableRemoteObject.narrow(ref, ReportHome.class);

				Report report = home.findByPrimaryKey(pk);
				folderNameText.setText(report.getDescription(main.getLanguageHandle()));
			} catch (Exception e) {
				Main.generalErrorHandler(e);
				return;
			}
		} else {
			folderNameText.setText("");
		}
		setLocationRelativeTo(getParent());
		super.show();
	}

	public boolean isOk() {
		return ok;
	}

	public String getName() {
		return name;
	}

	public ReportPK getPK() {
		return pk;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		folderNameText = new javax.swing.JTextField();
		textFildBorder = folderNameText.getBorder();

		folderNameText.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!ui.isValidReportName(folderNameText.getText())) {
					if (errorWindow == null) {
						errorWindow = ui.showErrorWindow(jPanel2, errorString, folderNameText.getLocationOnScreen());
					} else {
						if (!errorWindow.isVisible()) {
							errorWindow = ui.showErrorWindow(jPanel2, errorString, folderNameText.getLocationOnScreen());
						}
					}
					folderNameText.setBorder(BorderFactory.createLineBorder(Color.RED));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				folderNameText.setBorder(textFildBorder);
				if (errorWindow != null) {
					errorWindow.dispose();
				}
			}
		});

		setTitle(ui.getString("fina2.report.folder"));
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText(ui.getString("fina2.help"));
		helpButton.setEnabled(false);
		jPanel3.add(helpButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.WEST);

		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setFont(ui.getFont());
		okButton.setText(ui.getString("fina2.ok"));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (ui.isValidReportName(folderNameText.getText())) {
					okButtonActionPerformed(evt);
					if (errorWindow != null) {
						errorWindow.dispose();
					}
				} else {
					if (errorWindow == null) {
						errorWindow = ui.showErrorWindow(jPanel2, errorString, folderNameText.getLocationOnScreen());
					} else {
						if (!errorWindow.isVisible()) {
							errorWindow = ui.showErrorWindow(jPanel2, errorString, folderNameText.getLocationOnScreen());
						}
					}
				}

			}
		});

		jPanel4.add(okButton);

		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setFont(ui.getFont());
		cancelButton.setText(ui.getString("fina2.cancel"));
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel4.add(cancelButton);

		jPanel1.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		jPanel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(25, 70, 25, 70)));
		jLabel1.setText(ui.getString("fina2.report.folder"));
		jLabel1.setFont(ui.getFont());
		jPanel2.add(jLabel1);

		folderNameText.setColumns(15);
		folderNameText.setFont(ui.getFont());
		jPanel2.add(folderNameText);

		getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

		pack();
	}// GEN-END:initComponents

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
		ok = false;
		dispose();
	}// GEN-LAST:event_cancelButtonActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
		try {
			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/reportoo/server/Report");
			ReportHome home = (ReportHome) PortableRemoteObject.narrow(ref, ReportHome.class);

			Report report = null;
			if (pk == null) {
				report = home.create(fina2.Main.main.getUserHandle(), parentPK);
				pk = (ReportPK) report.getPrimaryKey();
			} else {
				report = home.findByPrimaryKey(pk);
			}
			report.setDescription(main.getLanguageHandle(), folderNameText.getText());
			name = folderNameText.getText();
			ok = true;
			reportManagerFrame.addFolderNode();
		} catch (Exception e) {
			Main.generalErrorHandler(e);
			return;
		}
		dispose();
	}// GEN-LAST:event_okButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_closeDialog
		ok = false;
		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.border.Border textFildBorder;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JTextField folderNameText;
	// End of variables declaration//GEN-END:variables

}
