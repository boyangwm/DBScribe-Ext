package fina2.actions;

import java.util.Collection;

import javax.swing.AbstractAction;

import fina2.FinaTypeException;
import fina2.Main;
import fina2.FinaTypeException.Type;
import fina2.returns.ImportManagerFrame;
import fina2.returns.ReturnVersionSession;
import fina2.returns.ReturnVersionSessionHome;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;

public class ImportManagerAction extends AbstractAction implements FinaAction {

	private fina2.Main main = fina2.Main.main;
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ImportManagerFrame frame;

	public ImportManagerAction() {

		super();

		main.setLoadingMessage(ui
				.getString("fina2.returns.importManagerAction"));
		putValue(AbstractAction.NAME,
				ui.getString("fina2.returns.importManagerAction"));

		frame = new ImportManagerFrame();
		ui.loadIcon("fina2.returns.importManager", "import_manager.gif");
		frame.setFrameIcon(ui.getIcon("fina2.returns.importManager"));
		putValue(AbstractAction.SMALL_ICON,
				ui.getIcon("fina2.returns.importManager"));

		ui.addAction("fina2.actions.importManager", this);
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

		Thread thread = new Thread() {
			public void run() {
				boolean contains = false;
				javax.swing.JInternalFrame[] frames = main.getMainFrame()
						.getDesktop().getAllFrames();
				for (int i = 0; i < frames.length; i++) {
					if (frames[i].equals(frame))
						contains = true;
				}
				if (!contains)
					main.getMainFrame().getDesktop().add(frame);
				frames = null;
				try {
					frame.setIcon(false);
					frame.setSelected(true);
					IndeterminateLoading loading = ui
							.createIndeterminateLoading(main.getMainFrame());
					loading.start();
					frame.show();
					loading.stop();
					frame.setVisible(true);
					frame.requestFocus();
				} catch (Exception e) {
					Main.generalErrorHandler(e);
				}
			}
		};

		thread.start();

	}

	/** Checks whether user has any return versions for amend */
	public static boolean checkUserReturnVersions() {

		boolean result = false;

		try {
			ReturnVersionSessionHome home = (ReturnVersionSessionHome) Main
					.getRemoteObject("fina2/returns/ReturnVersionSession",
							ReturnVersionSessionHome.class);
			ReturnVersionSession session = home.create();

			// Return versions for amend only
			Collection versions = session.getReturnVersions(
					Main.main.getLanguageHandle(), Main.main.getUserHandle(),
					true);

			if (versions.size() == 0) {
				// Current user has no return version for amend
				throw new FinaTypeException(
						Type.SECURITY_NO_RETURN_VERSION_AMEND_FOR_USER);
			}
			// OK
			result = true;
		} catch (Exception ex) {
			Main.generalErrorHandler(ex);
		}

		return result;
	}

	public String[] getPermissions() {
		return new String[] { "fina2.returns.process" };
	}
}
