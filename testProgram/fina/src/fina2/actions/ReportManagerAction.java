/*
 * ReportManagerAction.java
 *
 * Created on January 8, 2002, 12:06 AM
 */

package fina2.actions;

import javax.swing.AbstractAction;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ReportManagerAction extends AbstractAction implements FinaAction {

    private fina2.Main main = fina2.Main.main;
    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    //private fina2.report.ReportManagerFrame frame;
    private fina2.reportoo.ReportManagerFrame frame;

    /** Creates new ReportManagerAction */
    public ReportManagerAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.report.reportManager"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.report.reportManager"));

        frame = new fina2.reportoo.ReportManagerFrame();
        ui.loadIcon("fina2.report.reportManager", "report_manager.gif");
        frame.setFrameIcon(ui.getIcon("fina2.report.reportManager"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.report.reportManager"));

        ui.addAction("fina2.actions.reportManager", this);
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
        /*frame.show();
        frame.setState(java.awt.Frame.NORMAL);
        frame.requestFocus();*/
    }

    public String[] getPermissions() {
        return new String[] { "fina2.report.amend", "fina2.report.generate" };
    }

}
