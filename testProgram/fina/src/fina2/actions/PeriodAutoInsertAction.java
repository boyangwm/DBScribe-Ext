/*
 * PeriodAutoInsertAction.java
 *
 * Created on November 9, 2001, 12:27 AM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.period.PeriodAutoInsertFrame;

/**
 * 
 * @author vasop
 * @version
 */
public class PeriodAutoInsertAction extends AbstractAction implements FinaAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	// private PeriodAutoInsertDialog dialog;
	private PeriodAutoInsertFrame frame;

	/** Creates new PeriodAutoInsertAction */
	public PeriodAutoInsertAction() {
		super();

		fina2.Main.main.setLoadingMessage(ui.getString("fina2.period.periodAutoInsert"));
		putValue(AbstractAction.NAME, ui.getString("fina2.period.periodAutoInsert"));

		// dialog = new PeriodAutoInsertDialog(null, true);
		frame = new PeriodAutoInsertFrame();
		ui.loadIcon("fina2.period.periodAuto", "period_auto.gif");
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.period.periodAuto"));

		ui.addAction("fina2.period.periodAutoInsert", this);
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

			frame.setIcon(false);
			frame.setSelected(true);
			frame.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		frame.requestFocus();
	}

	public String[] getPermissions() {
		return new String[] { "fina2.periods.amend" };
	}

}
