/*
 * MenuAmendAction.java
 *
 * Created on October 17, 2001, 12:29 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.ui.menu.MenuAmendFrame;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class MenuAmendAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private MenuAmendFrame frame;

    /** Creates new MenuAmendAction */
    public MenuAmendAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.ui.menu.menuAmendAction"));

        //ui.loadIcon("fina2.exit", "exit.gif");
        putValue(AbstractAction.NAME, ui
                .getString("fina2.ui.menu.menuAmendAction"));
        //putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.exit"));

        frame = new MenuAmendFrame();
        ui.loadIcon("fina2.ui.menu.menuManager", "menu_manager.gif");
        frame.setFrameIcon(ui.getIcon("fina2.ui.menu.menuManager"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.ui.menu.menuManager"));

        ui.addAction("fina2.actions.menuAmend", this);
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
        return new String[] { "fina2.menu.amend" };
    }

}
