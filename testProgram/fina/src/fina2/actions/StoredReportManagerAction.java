package fina2.actions;

import javax.swing.AbstractAction;

import fina2.reportoo.StoredReportManagerFrame;

public class StoredReportManagerAction extends AbstractAction implements
        FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private StoredReportManagerFrame frame;

    public StoredReportManagerAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.report.storedReportManager"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.report.storedReportManager"));

        frame = new StoredReportManagerFrame();
        ui.loadIcon("fina2.report.storedReportManager",
                "stored_report_manager.gif");
        frame.setFrameIcon(ui.getIcon("fina2.report.storedReportManager"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.report.storedReportManager"));

        ui.addAction("fina2.actions.storedReportManager", this);
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
        return new String[] { "fina2.reports.stored.manager" };
    }
}
