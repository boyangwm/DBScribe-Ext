/*
 * ScheduleAction.java
 *
 * Created on November 6, 2001, 3:08 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.returns.SchedulesFrame;
import fina2.ui.UIManager.IndeterminateLoading;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public class SchedulesAction extends AbstractAction implements FinaAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private SchedulesFrame frame;

	private IndeterminateLoading loading;

	/** Creates new ScheduleAction */
	public SchedulesAction() {
		super();

		fina2.Main.main.setLoadingMessage(ui.getString("fina2.returns.schedulesAction"));
		putValue(AbstractAction.NAME, ui.getString("fina2.returns.schedulesAction"));

		frame = new SchedulesFrame();
		ui.loadIcon("fina2.returns.schedules", "schedules.gif");
		frame.setFrameIcon(ui.getIcon("fina2.returns.schedules"));
		putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.returns.schedules"));

		ui.addAction("fina2.actions.schedules", this);
		loading = ui.createIndeterminateLoading(main.getMainFrame());
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
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

				} catch (Exception e) {
					e.printStackTrace();
				}
				loading.stop();
				frame.requestFocus();
			}
		};
		t.start();
	}

	public String[] getPermissions() {
		return new String[] { "fina2.returns.schedule.amend", "fina2.returns.schedule.delete", "fina2.returns.schedule.review" };
	}

}
