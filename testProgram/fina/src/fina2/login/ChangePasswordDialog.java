package fina2.login;

import java.awt.GridBagConstraints;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.security.User;
import fina2.system.PropertySession;
import fina2.ui.UIManager;

public class ChangePasswordDialog extends javax.swing.JDialog {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private User user;
	private boolean passwordChanged;

	public ChangePasswordDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.cancel", "cancel.gif");
		initComponents();
	}

	public void show(User user) {
		this.user = user;
		try {
			userText.setText(user.getLogin());
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
		passwordText1.requestFocusInWindow();
		setLocationRelativeTo(getParent());

		super.show();
	}

	private void initComponents() {
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		userText = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		passwordText1 = new javax.swing.JPasswordField();
		jLabel4 = new javax.swing.JLabel();
		passwordText2 = new javax.swing.JPasswordField();
		jPanel1 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		passwordText1.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				passwordTextKeyPressed(evt);
			}
		});

		passwordText2.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				passwordTextKeyPressed(evt);
			}
		});

		setTitle("Change Password");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		jPanel2.setLayout(new java.awt.GridBagLayout());

		jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
		jLabel1.setText("Login");
		jPanel2.add(jLabel1, UIManager.getGridBagConstraints(0, 0, 10, 15, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(10, 10, 0, 0)));

		userText.setColumns(20);
		userText.setEditable(false);
		userText.setFont(new java.awt.Font("Dialog", 0, 11));
		jPanel2.add(userText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(10, 0, 0, 10)));

		jLabel3.setFont(new java.awt.Font("Dialog", 0, 11));
		jLabel3.setText("New Password");
		jPanel2.add(jLabel3, UIManager.getGridBagConstraints(0, 2, 10, 15, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(5, 10, 0, 0)));

		passwordText1.setColumns(20);
		passwordText1.setFont(new java.awt.Font("Dialog", 0, 11));
		jPanel2.add(passwordText1, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(5, 0, 0, 10)));

		jLabel4.setFont(new java.awt.Font("Dialog", 0, 11));
		jLabel4.setText("Confirm new Password");
		jPanel2.add(jLabel4, UIManager.getGridBagConstraints(0, 3, 10, 15, -1, -1, GridBagConstraints.EAST, -1, new java.awt.Insets(5, 10, 10, 0)));

		passwordText2.setColumns(20);
		passwordText2.setFont(new java.awt.Font("Dialog", 0, 11));
		jPanel2.add(passwordText2, UIManager.getGridBagConstraints(1, 3, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(5, 0, 10, 10)));

		getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

		jPanel1.setLayout(new java.awt.BorderLayout());

		okButton.setFont(new java.awt.Font("Dialog", 0, 11));
		okButton.setIcon(ui.getIcon("fina2.ok"));
		okButton.setText("Ok");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		jPanel3.add(okButton);

		cancelButton.setFont(new java.awt.Font("Dialog", 0, 11));
		cancelButton.setIcon(ui.getIcon("fina2.cancel"));
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		jPanel3.add(cancelButton);

		jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

		pack();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN
		// -
		// FIRST
		// :
		// event_cancelButtonActionPerformed
		setVisible(false);
		dispose();
	}

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-
		// FIRST
		// :
		// event_okButtonActionPerformed

		String newP1 = passwordText1.getText().trim();
		String newP2 = passwordText2.getText().trim();
		try {
			if (newP1.equals(newP2)) {
				Integer length = Integer.parseInt(ui.getSysPropertiesValue(PropertySession.MINIMAL_PASSWORD_LENGTH));
				if (newP1.length() < length) {
					ui.showMessageBox(null, "Fina International", "Password minimal length is " + length);
					return;
				}
				user.setPassword(newP1);
				user.setChangePassword(false);
				passwordChanged = true;
				setVisible(false);
				dispose();
			} else {
				ui.showMessageBox(null, "Fina International", "Password does not match");
			}
		} 
		catch(FinaTypeException ex){
			ui.showMessageBox(null, "FinA International","Password already used");
		}
		catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}
	}

	private void closeDialog(java.awt.event.WindowEvent evt) { // GEN-FIRST:
		// event_closeDialog
		setVisible(false);
		dispose();
	} // GEN-LAST:event_closeDialog

	public boolean isPasswordChanged() {
		return passwordChanged;
	}

	public String getChangedPassword() {
		return passwordText1.getText();
	}

	private void passwordTextKeyPressed(java.awt.event.KeyEvent evt) { // GEN-
		// FIRST
		// :
		// event_passwordTextKeyPressed
		if (evt.getKeyCode() == evt.VK_ENTER) {
			okButtonActionPerformed(null);
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JTextField userText;
	private javax.swing.JPasswordField passwordText1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPasswordField passwordText2;
	// End of variables declaration//GEN-END:variables
}
