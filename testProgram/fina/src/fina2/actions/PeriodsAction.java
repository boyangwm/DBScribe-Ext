/*
 * PeriodAction.java
 *
 * Created on October 30, 2001, 11:22 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.period.PeriodFrame;

/**
 *
 * @author  vasop
 * @version 
 */
public class PeriodsAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private PeriodFrame frame;

    /** Creates new PeriodTypeAction */
    public PeriodsAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui.getString("fina2.period.periods"));
        putValue(AbstractAction.NAME, ui.getString("fina2.period.periods"));

        frame = new PeriodFrame();
        ui.loadIcon("fina2.period.periods", "periods.gif");
        frame.setFrameIcon(ui.getIcon("fina2.period.periods"));
        putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.period.periods"));

        ui.addAction("fina2.actions.periods", this);
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
