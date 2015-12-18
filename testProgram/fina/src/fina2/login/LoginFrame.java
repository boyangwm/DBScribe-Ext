/*
 * LoginFrame.java
 *
 * Created on October 15, 2001, 5:18 PM
 */

package fina2.login;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.FinderException;
import javax.naming.CommunicationException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

import fina2.Main;
import fina2.i18n.Language;
import fina2.i18n.LanguageHome;
import fina2.i18n.LanguagePK;
import fina2.i18n.LanguageSession;
import fina2.i18n.LanguageSessionHome;
import fina2.security.AuthenticatedModeSession;
import fina2.security.AuthenticatedModeSessionBean;
import fina2.security.AuthenticatedModeSessionHome;
import fina2.security.User;
import fina2.security.UserBean;
import fina2.security.UserHome;
import fina2.ui.UIManager;
import fina2.ui.table.TableRowImpl;

public class LoginFrame extends javax.swing.JFrame {

	private static Logger log = Logger.getLogger(LoginFrame.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private int selection;
	private SettingsDialog settingsDialog;

	/** Creates new form LoginFrame */
	public LoginFrame() {
		boolean err = false;
		settingsDialog = new SettingsDialog(this, true);
		setIconImage(ui.getIcon("fina2.icon").getImage());
		loadIcons();
		initComponents();

		changePassButton.setEnabled(false);
		loginButton.setEnabled(false);
		updateLoginContext("guest", "anonymous");

		Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setLocationRelativeTo(null);
		String fontFace = (String) ui.getConfigValue("fina2.login.language.FontFace");
		Integer fontSize = (Integer) ui.getConfigValue("fina2.login.language.FontSize");
		String langDesc = (String) ui.getConfigValue("fina2.login.language.Description");
		Object langId = ui.getConfigValue("fina2.login.language.ID");
		Integer langCount = (Integer) ui.getConfigValue("fina2.login.language.Count");

		if (fontFace == null || fontSize == null || langDesc == null || langId == null || langCount == null) {
			while (!err) {
				err = loadLanguageList();
				if (!err) {
					settingsDialog.show();
					if (!settingsDialog.isOk()) {
						System.exit(0);
					}
				}
			}

			Thread t = new Thread() {
				public void run() {
					try {
						InitialContext jndi = fina2.Main.getJndiContext();
						Object ref = jndi.lookup("fina2/i18n/Language");
						LanguageHome _home = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
						Language lang = _home.findByPrimaryKey((LanguagePK) ((TableRowImpl) languageList.getSelectedItem()).getPrimaryKey());

						fina2.Main.dateFormat = lang.getDateFormat();
						fina2.Main.main.setLanguageHandle(lang.getHandle());

						ui.createFont();
						new fina2.actions.ExitAction();
					} catch (Exception e) {
						Main.generalErrorHandler(e);
					}
				}
			};
			t.start();
		} else {
			TableRowImpl lang = new TableRowImpl(langId, 1);
			lang.setValue(0, langDesc);

			// Workaround of combo box bug
			for (int i = 0; i < langCount; i++) {
				languageList.addItem(lang);
			}

			ui.createFont(fontFace, fontSize);
			new fina2.actions.ExitAction();
		}

		if (ui.getConfigValue("fina2.login.LastLoggedUser") != null) {
			userText.setText(ui.getConfigValue("fina2.login.LastLoggedUser").toString());
			passwordText.requestFocus();
		}

	}

	private boolean loadLanguageList() {
		try {

			InitialContext jndi = fina2.Main.getJndiContext();

			Object ref = jndi.lookup("fina2/i18n/LanguageSession");
			LanguageSessionHome home = (LanguageSessionHome) PortableRemoteObject.narrow(ref, LanguageSessionHome.class);

			LanguageSession session = home.create();
			Collection rows = session.getLanguagesRows(null, null);

			selection = 0;
			int i = 0;
			String langCode = "en_US";
			if (ui.getConfigValue("fina2.login.language.Code") != null) {
				langCode = (String) ui.getConfigValue("fina2.login.language.Code");
			}

			for (Iterator iter = rows.iterator(); iter.hasNext(); i++) {
				TableRowImpl row = (TableRowImpl) iter.next();
				row.setDefaultCol(1);
				if (row.getValue(0).equals(langCode)) {
					selection = i;
				}
			}

			languageList.setModel(new DefaultComboBoxModel(new Vector(rows)));
			languageList.setSelectedIndex(selection);
		} catch (Exception ex) {
			log.error("Error connecting to the FinA server", ex);
			Main.generalErrorHandler(ex);
			return false;
		}

		return true;
	}

	private void loadIcons() {
		ui.loadIcon("fina2.ok", "ok.gif");
		ui.loadIcon("fina2.help", "help.gif");
		ui.loadIcon("fina2.amend", "amend.gif");
		ui.loadIcon("fina2.spinner", "spinner.gif");
		ui.loadIcon("fina2.check", "icon-check.gif");
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	private void initComponents() {// GEN-BEGIN:initComponents
		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		userText = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		passwordText = new javax.swing.JPasswordField();
		jLabel3 = new javax.swing.JLabel();
		languageList = new javax.swing.JComboBox();
		setupButton = new javax.swing.JButton();
		changePassButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jPanel3 = new javax.swing.JPanel();
		loginButton = new javax.swing.JButton();
		exitButton = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		helpButton = new javax.swing.JButton();

		setTitle("FinA International Login");
		setResizable(false);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(15, 70, 15, 70)));
		jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
		jLabel1.setText("Login");
		jPanel1.add(jLabel1, UIManager.getGridBagConstraints(0, 0, 10, 15, -1, -1, GridBagConstraints.EAST, -1, null));

		userText.setColumns(20);
		userText.setFont(new java.awt.Font("Dialog", 0, 11));
		userText.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				userTextKeyPressed(evt);
			}
		});

		jPanel1.add(userText, UIManager.getGridBagConstraints(1, 0, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel2.setFont(new java.awt.Font("Dialog", 0, 11));
		jLabel2.setText("Password");
		jPanel1.add(jLabel2, UIManager.getGridBagConstraints(0, 1, 10, 15, -1, -1, GridBagConstraints.EAST, -1, null));

		passwordText.setColumns(20);
		passwordText.setFont(new java.awt.Font("Dialog", 0, 11));
		passwordText.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				passwordTextKeyPressed(evt);
			}
		});

		jPanel1.add(passwordText, UIManager.getGridBagConstraints(1, 1, -1, -1, -1, -1, GridBagConstraints.WEST, -1, null));

		jLabel3.setFont(new java.awt.Font("Dialog", 0, 11));
		jLabel3.setText("Language");
		jPanel1.add(jLabel3, UIManager.getGridBagConstraints(0, 2, 10, 15, -1, -1, GridBagConstraints.EAST, -1, null));

		languageList.setFont(new java.awt.Font("Dialog", 0, 11));
		languageList.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				languagesPopupMenuWillBecomeVisible(e);
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		jPanel1.add(languageList, UIManager.getGridBagConstraints(1, 2, -1, -1, -1, -1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, null));

		setupButton.setFont(new java.awt.Font("Dialog", 0, 11));
		setupButton.setIcon(ui.getIcon("fina2.amend"));
		setupButton.setText("Settings...");
		setupButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				setupButtonActionPerformed(evt);
			}
		});

		jPanel1.add(setupButton, UIManager.getGridBagConstraints(1, 3, -1, -1, -1, -1, GridBagConstraints.WEST, -1, new java.awt.Insets(6, 0, 0, 0)));

		changePassButton.setFont(new java.awt.Font("Dialog", 0, 11));
		changePassButton.setText("Change...");
		changePassButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				changePassButtonActionPerformed(evt);
			}
		});
		jPanel1.add(changePassButton, UIManager.getGridBagConstraints(2, 1, -1, -1, -1, -1, -1, -1, new java.awt.Insets(0, 5, 0, 0)));

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

		jPanel2.setLayout(new java.awt.BorderLayout());

		loginButton.setFont(new java.awt.Font("Dialog", 0, 11));
		loginButton.setIcon(ui.getIcon("fina2.ok"));
		loginButton.setText("OK");
		loginButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loginButtonActionPerformed(evt);
			}
		});

		jPanel3.add(loginButton);

		exitButton.setFont(new java.awt.Font("Dialog", 0, 11));
		exitButton.setIcon(ui.getIcon("fina2.exit"));
		exitButton.setText("Exit");
		exitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				System.exit(0);
			}
		});

		jPanel3.add(exitButton);

		jPanel2.add(jPanel3, java.awt.BorderLayout.EAST);

		helpButton.setFont(new java.awt.Font("Dialog", 0, 11));
		helpButton.setIcon(ui.getIcon("fina2.help"));
		helpButton.setText("Help");
		helpButton.setEnabled(false);
		jPanel4.add(helpButton);

		jPanel2.add(jPanel4, java.awt.BorderLayout.WEST);

		getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

		passwordText.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {

				if (passwordText.getText().trim().length() == 0) {
					changePassButton.setEnabled(false);
					loginButton.setEnabled(false);

				} else if (userText.getText().trim().length() != 0) {
					changePassButton.setEnabled(true);
					loginButton.setEnabled(true);
				}

			}
		});
		userText.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {

				if (userText.getText().trim().length() == 0) {
					changePassButton.setEnabled(false);
					loginButton.setEnabled(false);

				} else if (passwordText.getText().trim().length() != 0) {
					changePassButton.setEnabled(true);
					loginButton.setEnabled(true);
				}

			}
		});
		pack();

	}// GEN-END:initComponents

	private void passwordTextKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST
		// :
		// event_passwordTextKeyPressed
		if (evt.getKeyCode() == evt.VK_ENTER) {
			loginButtonActionPerformed(null);
		}
	}// GEN-LAST:event_passwordTextKeyPressed

	private void userTextKeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:
		// event_userTextKeyPressed
		if (evt.getKeyCode() == evt.VK_ENTER) {
			if (userText.getText().trim().length() > 0)
				loginButtonActionPerformed(null);
		}
	}// GEN-LAST:event_userTextKeyPressed

	private void changePassButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_changePassButtonActionPerformed
		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ref = jndi.lookup("fina2/security/User");
			UserHome home = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);

			User user = home.findByLoginPassword(userText.getText(), passwordText.getText());

			updateLoginContext(userText.getText(), passwordText.getText());

			ChangePasswordDialog chPassDialog = new ChangePasswordDialog(this, true);
			chPassDialog.show(user);

			if (chPassDialog.isPasswordChanged()) {
				passwordText.setText(chPassDialog.getChangedPassword());
			}
		} catch (FinderException e) {
			Main.errorHandler(null, "Fina International", "Username or Password is not valid or account is blocked");
			e.printStackTrace();
		} catch (Exception ex) {
			log.error("Error changing password", ex);
			Main.errorHandler(null, "Fina International", "Username or Password is not valid or account is blocked");
		}
	}// GEN-LAST:event_changePassButtonActionPerformed

	private void setupButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_setupButtonActionPerformed
		settingsDialog.show();
	}// GEN-LAST:event_setupButtonActionPerformed

	private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		// -
		// FIRST
		// :
		// event_loginButtonActionPerformed

		try {
			InitialContext jndi = fina2.Main.getJndiContext();
			Object ldapRef = jndi.lookup("fina2/security/AuthenticatedModeSession");
			Object ref = jndi.lookup("fina2/security/User");
			UserHome home = (UserHome) PortableRemoteObject.narrow(ref, UserHome.class);
			AuthenticatedModeSessionHome authHome = (AuthenticatedModeSessionHome) PortableRemoteObject.narrow(ldapRef, AuthenticatedModeSessionHome.class);
			AuthenticatedModeSession authSession = null;
			authSession = authHome.create();
			String mode = authSession.loadProperties().get(AuthenticatedModeSessionBean.FINA_CURRENT_AUTHENTICATION);
			User user = null;

			if (mode.toLowerCase().equals("ldap")) {
				authSession.authenticateLdap(userText.getText(), passwordText.getText());
				user = home.findByLogin(userText.getText());
			} else {
				user = home.findByLoginPassword(userText.getText(), passwordText.getText());

			}
			updateLoginContext(userText.getText(), passwordText.getText());
			ChangePasswordDialog chPassDialog = new ChangePasswordDialog(this, true);
			if (user.getChangePassword()) {
				chPassDialog.show(user);
			}

			if (!user.getChangePassword() || chPassDialog.isPasswordChanged()) {

				jndi = fina2.Main.getJndiContext();

				ref = jndi.lookup("fina2/i18n/Language");
				LanguageHome _home = (LanguageHome) PortableRemoteObject.narrow(ref, LanguageHome.class);
				Language lang = _home.findByPrimaryKey((LanguagePK) ((TableRowImpl) languageList.getSelectedItem()).getPrimaryKey());
				fina2.Main.dateFormat = lang.getDateFormat();
				fina2.Main.main.loginOk(user.getHandle(), lang.getHandle(), false);

				ui.putConfigValue("fina2.login.LastLoggedUser", userText.getText());
				ui.putConfigValue("fina2.login.language.FontFace", lang.getFontFace());
				ui.putConfigValue("fina2.login.language.FontSize", lang.getFontSize());
				ui.putConfigValue("fina2.login.language.Description", lang.getDescription());
				ui.putConfigValue("fina2.login.language.ID", lang.getPrimaryKey());
				ui.putConfigValue("fina2.login.language.Count", languageList.getItemCount());
				ui.putConfigValue("fina2.login.language.Code", lang.getCode());

				ui.saveConfig();
			}

		} catch (CommunicationException ex) {
			ui.showMessageBox(this, "FinA International", "Error occured communicating FinA server");

		} catch (NullPointerException ex) {
			ui.showMessageBox(this, "FinA International", "There are not Authentication modes defined in database!");
		} catch (Exception ex) {
			if (ex.getMessage().contains(UserBean.errStatus)) {
				try {
					InitialContext jndi = fina2.Main.getJndiContext();
					Object ref = jndi.lookup("fina2/security/UserSession");
					fina2.security.UserSessionHome home = (fina2.security.UserSessionHome) PortableRemoteObject.narrow(ref, fina2.security.UserSessionHome.class);

					fina2.security.UserSession session = home.create();

					session.blockUserByLogin(userText.getText(), session.findByLogin(userText.getText()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ui.showMessageBox(this, "FinA International", "User name or password is not valid or account is blocked");
		}

	}

	public void languagesPopupMenuWillBecomeVisible(PopupMenuEvent e) {
		boolean err = false;
		while (!err) {
			err = loadLanguageList();
			if (!err) {
				settingsDialog.show();
				if (!settingsDialog.isOk()) {
					return;
				}
			}
		}
	}

	private void exitForm(java.awt.event.WindowEvent evt) {// GEN-FIRST:
		// event_exitForm
		System.exit(0);
	}

	private void updateLoginContext(String username, String password) {
		try {
			LoginContext lc = new LoginContext("client-login", new UsernamePasswordHandler(username, password.toCharArray()));
			lc.login();
		} catch (LoginException ex) {
			Main.errorHandler(null, "Fina International", "Can not update Login context");
			log.error("Error updating login context", ex);
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JComboBox languageList;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JButton loginButton;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPasswordField passwordText;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JButton exitButton;
	private javax.swing.JTextField userText;
	private javax.swing.JButton changePassButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JButton setupButton;
	private javax.swing.JButton helpButton;
	// End of variables declaration//GEN-END:variables
}
