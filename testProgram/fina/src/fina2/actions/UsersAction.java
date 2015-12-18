/*
 * UsersAction.java
 *
 * Created on October 29, 2001, 12:23 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.security.users.UsersFrame;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public class UsersAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private UsersFrame frame;

    /** Creates new UsersAction */
    public UsersAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.security.usersAction"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.security.usersAction"));

        frame = new UsersFrame();
        ui.loadIcon("fina2.security.role", "role.gif");
        frame.setFrameIcon(ui.getIcon("fina2.security.role"));
        putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.security.role"));

        ui.addAction("fina2.actions.users", this);
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
        return new String[] { "fina2.security.amend" };
    }

}
