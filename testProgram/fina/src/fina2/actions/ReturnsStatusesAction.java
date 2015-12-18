package fina2.actions;

import javax.swing.AbstractAction;

public class ReturnsStatusesAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private fina2.returns.ReturnsStatusesFrame frame;

    public ReturnsStatusesAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.returns.statuses"));
        putValue(AbstractAction.NAME, ui.getString("fina2.returns.statuses"));

        frame = new fina2.returns.ReturnsStatusesFrame();
        ui.loadIcon("fina2.returns.statuses", "return_statuses.gif");
        frame.setFrameIcon(ui.getIcon("fina2.returns.statuses"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.returns.statuses"));

        ui.addAction("fina2.actions.returnsStatuses", this);
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
        return new String[] { "fina2.returns.statuses" };
    }
}
