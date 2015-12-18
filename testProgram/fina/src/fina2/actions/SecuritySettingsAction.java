package fina2.actions;

import javax.swing.AbstractAction;

import fina2.Main;
import fina2.security.SettingsDialog;

@SuppressWarnings("serial")
public class SecuritySettingsAction extends AbstractAction implements FinaAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;
	private SettingsDialog dialog;

	public SecuritySettingsAction() {
		super();
		ui.loadIcon("fina2.settings", "settings.gif");
		ui.addAction("fina2.security.settings", this);

		putValue(AbstractAction.NAME, ui.getString("fina2.security.settings"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.settings"));

		this.dialog = new SettingsDialog(main.getMainFrame(), ui.getString("fina2.security.settings"), true);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			this.dialog.setSize(460, 560);
			this.dialog.setLocationRelativeTo(main.getMainFrame());
			this.dialog.setVisible(true);
		} catch (Exception e) {
			Main.generalErrorHandler(e);
		}
	}

	public String[] getPermissions() {
		return new String[] { "fina2.security.settings", };
	}
}
