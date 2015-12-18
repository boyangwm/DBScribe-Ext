package fina2.login;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import fina2.ui.UIManager;

public class SettingsDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private String homeDir = ".";
	private Properties jndiProperties;

	private boolean ok;

	public SettingsDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.cancel", "cancel.gif");
		ui.loadIcon("fina2.help", "help.gif");
		initComponents();
	}

	public void show() {

		homeDir = System.getProperty("FINA2_HOME");
		if (homeDir == null) {
			homeDir = ".";
		}
		try {
			jndiProperties = new Properties();
			FileInputStream fi = new FileInputStream(homeDir
					+ "/conf/jndi.properties");
			jndiProperties.load(fi);
			fi.close();
			System.getProperties().putAll(jndiProperties);
			addressText.setText(jndiProperties
					.getProperty("java.naming.provider.url"));
		} catch (FileNotFoundException e) {
			ui.showMessageBox(null, "FinA International",
					"Configuration File Not Found.\n" + homeDir
							+ "/conf/jndi.properties");
		} catch (IOException e) {
			ui.showMessageBox(null, "Unable to load configuration file.\n"
					+ homeDir + "/conf/jndi.properties\n" + e.getMessage());
		}

		setLocationRelativeTo(getParent());
		super.show();
	}

	public boolean isOk() {
		return ok;
	}

	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		addressText = new javax.swing.JTextField();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jButton2 = new javax.swing.JButton();
		jButton3 = new javax.swing.JButton();

		setTitle("Settings");
		setFont(ui.getFont());
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jLabel1.setText("Server Address");
		jLabel1.setFont(ui.getFont());
		jPanel1.add(jLabel1, UIManager.getGridBagConstraints(-1, -1, -1, -1,
				-1, -1, -1, -1, new java.awt.Insets(30, 80, 30, 5)));

		addressText.setColumns(15);
		addressText.setFont(ui.getFont());
		jPanel1.add(addressText, UIManager.getGridBagConstraints(1, 0, -1, -1,
				-1, -1, -1, -1, new java.awt.Insets(30, 0, 30, 80)));

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setFont(ui.getFont());
		helpButton.setText("Help");
		helpButton.setEnabled(false);
		jPanel3.add(helpButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.WEST);

		jButton2.setIcon(ui.getIcon("fina2.ok"));
		jButton2.setFont(ui.getFont());
		jButton2.setText("OK");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});

		jPanel4.add(jButton2);

		jButton3.setIcon(ui.getIcon("fina2.cancel"));
		jButton3.setFont(ui.getFont());
		jButton3.setText("Cancel");
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});

		jPanel4.add(jButton3);

		jPanel2.add(jPanel4, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		pack();
	}// GEN-END:initComponents

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-
		// FIRST
		// :
		// event_jButton2ActionPerformed
		jndiProperties.put("java.naming.provider.url", addressText.getText());
		try {
			FileOutputStream fo = new FileOutputStream(homeDir
					+ "/conf/jndi.properties");
			jndiProperties.save(fo, "");
			fo.close();
			System.getProperties().putAll(jndiProperties);

			main.getProperties().put("java.naming.provider.url",
					addressText.getText());
		} catch (FileNotFoundException e) {
			ui.showMessageBox(null, "FinA International",
					"Configuration File Not Found.\n" + homeDir
							+ "/conf/jndi.properties");
		} catch (IOException e) {
			ui.showMessageBox(null, "FinA International",
					"Unable to write configuration file.\n" + homeDir
							+ "/conf/jndi.properties\n" + e.getMessage());
		}

		ok = true;
		setVisible(false);
		dispose();
	}// GEN-LAST:event_jButton2ActionPerformed

	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-
		// FIRST
		// :
		// event_jButton3ActionPerformed
		ok = false;

		setVisible(false);
		dispose();
	}// GEN-LAST:event_jButton3ActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_closeDialog
		ok = false;

		setVisible(false);
		dispose();
	}// GEN-LAST:event_closeDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanel1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JTextField addressText;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JButton helpButton;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	// End of variables declaration//GEN-END:variables
}
