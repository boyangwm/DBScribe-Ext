package fina2.actions;

import javax.swing.AbstractAction;

import fina2.reportoo.ReportSchedulerManagerFrame;

public class ReportSchedulerManagerAction extends AbstractAction implements
        FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private ReportSchedulerManagerFrame frame;

    public ReportSchedulerManagerAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.report.reportSchedulerManager"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.report.reportSchedulerManager"));

        frame = new ReportSchedulerManagerFrame();
        ui.loadIcon("fina2.report.reportSchedulerManager",
                "report_scheduler_manager.gif");
        frame.setFrameIcon(ui.getIcon("fina2.report.reportSchedulerManager"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.report.reportSchedulerManager"));

        ui.addAction("fina2.actions.reportSchedulerManager", this);
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.requestFocus();
    }

    public String[] getPermissions() {
        return new String[] { "fina2.reports.scheduler.manager",
                "fina2.reports.scheduler.add" };
    }
}
