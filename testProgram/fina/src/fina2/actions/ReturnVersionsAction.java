package fina2.actions;

import javax.swing.AbstractAction;

import fina2.returns.ReturnVersionFrame;

public class ReturnVersionsAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private ReturnVersionFrame frame;

    public ReturnVersionsAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.returns.returnVersionsAction"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.returns.returnVersionsAction"));

        frame = new ReturnVersionFrame();
        ui.loadIcon("fina2.returns.returnVersions", "return_vesions.gif");
        frame.setFrameIcon(ui.getIcon("fina2.returns.returnVersions"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.returns.returnVersions"));

        ui.addAction("fina2.actions.returnVersions", this);
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
        return new String[] { "fina2.returns.version.amend",
                "fina2.returns.version.delete", "fina2.returns.version.review", };
    }
}
