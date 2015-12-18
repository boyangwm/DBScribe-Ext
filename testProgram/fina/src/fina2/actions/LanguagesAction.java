package fina2.actions;

import javax.swing.AbstractAction;

import fina2.i18n.LanguagesFrame;

public class LanguagesAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private LanguagesFrame frame;

    /** Creates new LanguagesAction */
    public LanguagesAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui.getString("fina2.i18n.languages"));
        putValue(AbstractAction.NAME, ui.getString("fina2.i18n.languages"));

        frame = new LanguagesFrame();
        ui.loadIcon("fina2.i18n.languages", "languages.gif");
        frame.setFrameIcon(ui.getIcon("fina2.i18n.languages"));
        putValue(AbstractAction.SMALL_ICON, ui.getIcon("fina2.i18n.languages"));

        ui.addAction("fina2.actions.languages", this);
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
