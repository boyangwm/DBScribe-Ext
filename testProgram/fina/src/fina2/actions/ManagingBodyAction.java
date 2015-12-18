/*
 * ManagingBodyAction.java
 *
 * Created on 2 јпрель 2002 г., 11:51
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.bank.ManagingBodiesFrame;

/**
 *
 * @author  Vasop
 * @version 
 */
public class ManagingBodyAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private ManagingBodiesFrame frame;

    /** Creates new ManagingBodyAction */
    public ManagingBodyAction() {
        super();

        putValue(AbstractAction.NAME, ui
                .getString("fina2.bank.managingBodyAction"));

        frame = new ManagingBodiesFrame();
        ui.loadIcon("fina2.bank.managingBody", "managing_body.gif");
        frame.setFrameIcon(ui.getIcon("fina2.bank.managingBody"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.bank.managingBody"));

        ui.addAction("fina2.actions.managingBody", this);
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
        return new String[] { "fina2.bank.amend", "fina2.bank.delete",
                "fina2.bank.review" };
    }

}
