package fina2.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class LogoutAction extends AbstractAction {
	private Logger log = Logger.getLogger(ExitAction.class);

	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	public LogoutAction() {
		super();
		ui.loadIcon("fina2.logout", "logout.png");
		putValue(AbstractAction.NAME, ui.getString("fina2.logout"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.logout"));
		ui.addAction("fina2.actions.logout", this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		log.info("logout FinA!");
		try {
			javax.swing.UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			Runtime run = Runtime.getRuntime();
			run.exec(createCmdString());
			System.exit(1);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public static String[] createCmdString() {
		String[] cmd = new String[5];
		cmd[0] = "cmd.exe";
		cmd[1] = "/C";
		cmd[2] = "start";
		cmd[3] = "/min";
		cmd[4] = "run.bat";
		return cmd;
	}
}
