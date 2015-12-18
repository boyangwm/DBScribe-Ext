/*
 * PeriodTypeAction.java
 *
 * Created on October 23, 2001, 12:45 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.period.PeriodTypesFrame;

/**
 *
 * @author  vasop
 * @version 
 */
public class PeriodTypesAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private PeriodTypesFrame frame;

    /** Creates new PeriodTypeAction */
    public PeriodTypesAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.period.periodTypes"));
        putValue(AbstractAction.NAME, ui.getString("fina2.period.periodTypes"));

        frame = new PeriodTypesFrame();
        ui.loadIcon("fina2.period.periodTypes", "period_types.gif");
        frame.setFrameIcon(ui.getIcon("fina2.period.periodTypes"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.period.periodTypes"));

        ui.addAction("fina2.actions.periodTypes", this);
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
        return new String[] { "fina2.periods.amend", "fina2.periods.delete",
                "fina2.periods.review" };
    }

}
