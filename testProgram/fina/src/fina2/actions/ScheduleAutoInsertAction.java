/*
 * ScheduelAutoInsertAction.java
 *
 * Created on November 14, 2001, 9:46 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.returns.ScheduleAutoInsertFrame;
import fina2.ui.UIManager.IndeterminateLoading;

/**
 * 
 * @author vasop
 * @version
 */
public class ScheduleAutoInsertAction extends AbstractAction implements
		FinaAction {

	private fina2.ui.UIManager ui = fina2.Main.main.ui;
	private fina2.Main main = fina2.Main.main;

	private ScheduleAutoInsertFrame frame;

	private IndeterminateLoading loading;

	/** Creates new ScheduelAutoInsertAction */
	public ScheduleAutoInsertAction() {
		super();

		fina2.Main.main.setLoadingMessage(ui
				.getString("fina2.returns.autoSchedule"));
		putValue(AbstractAction.NAME,
				ui.getString("fina2.returns.autoSchedule"));

		frame = new ScheduleAutoInsertFrame();
		ui.loadIcon("fina2.returns.scheduleAuto", "schedule_auto.gif");
		frame.setFrameIcon(ui.getIcon("fina2.returns.scheduleAuto"));
		putValue(AbstractAction.SMALL_ICON,
				ui.getIcon("fina2.returns.scheduleAuto"));

		ui.addAction("fina2.returns.autoSchedule", this);
		loading = ui.createIndeterminateLoading(main.getMainFrame());
	}

	public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
		Thread th = new Thread() {
			public void run() {
				try {
					loading.start();
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

					frame.setIcon(false);
					frame.setSelected(true);
					frame.show();
					frame.setVisible(true);

					loading.stop();

				} catch (Exception e) {
					e.printStackTrace();
					main.generalErrorHandler(e);
				}
				frame.requestFocus();
			}
		};
		th.start();

	}

	public String[] getPermissions() {
		return new String[] { "fina2.returns.schedule.amend" };
	}

}
