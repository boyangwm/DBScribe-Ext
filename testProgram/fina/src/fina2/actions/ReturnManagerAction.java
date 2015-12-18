/*
 * BankDefinitionsAction.java
 *
 * Created on October 19, 2001, 8:49 PM
 */

package fina2.actions;

import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;

import fina2.returns.ReturnManagerFrame;
import fina2.ui.UIManager;
import fina2.ui.UIManager.IndeterminateLoading;

/**
 * 
 * @author Sh Shalamberidze
 * @version
 */
public class ReturnManagerAction extends AbstractAction implements FinaAction {

	private fina2.Main main = fina2.Main.main;
	private fina2.ui.UIManager ui = fina2.Main.main.ui;

	private ReturnManagerFrame frame;

	private IndeterminateLoading loading;

	/** Creates new LanguagesAction */
	public ReturnManagerAction() {
		super();

		fina2.Main.main.setLoadingMessage(ui.getString("fina2.returns.returnManagerAction"));
		putValue(AbstractAction.NAME, ui.getString("fina2.returns.returnManagerAction"));

		frame = new ReturnManagerFrame();
		ui.loadIcon("fina2.returns.returnManager", "return_manager.gif");
		frame.setFrameIcon(ui.getIcon("fina2.returns.returnManager"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.returns.returnManager"));

		ui.addAction("fina2.actions.returnManager", this);

		loading = ui.createIndeterminateLoading(main.getMainFrame());
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		try {
			boolean contains = false;
			javax.swing.JInternalFrame[] frames = main.getMainFrame().getDesktop().getAllFrames();
			for (int i = 0; i < frames.length; i++) {
				if (frames[i].equals(frame))
					contains = true;
			}
			if (!contains)
				main.getMainFrame().getDesktop().add(frame);
			frames = null;

			Thread t = new Thread() {
				public void run() {
					loading.start();
					try {
						frame.setIcon(false);
						frame.setSelected(true);
						frame.show();
						frame.setVisible(true);
					} catch (PropertyVetoException e) {
						main.generalErrorHandler(e);
						e.printStackTrace();
					}
					loading.stop();
				}
			};
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		frame.requestFocus();
		/*
		 * frame.show(); frame.setState(java.awt.Frame.NORMAL);
		 * frame.requestFocus();
		 */
	}

	public String[] getPermissions() {
		return new String[] { "fina2.returns.amend", "fina2.returns.delete", "fina2.returns.process", "fina2.returns.review", "fina2.returns.accept", "fina2.returns.reset", "fina2.returns.reject" };
	}

}
