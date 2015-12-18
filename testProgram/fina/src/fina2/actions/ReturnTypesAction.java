package fina2.actions;

import javax.swing.AbstractAction;

import fina2.returns.ReturnTypesFrame;

public class ReturnTypesAction extends AbstractAction implements FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private ReturnTypesFrame frame;

    public ReturnTypesAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.returns.returnTypesAction"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.returns.returnTypesAction"));

        frame = new ReturnTypesFrame();
        ui.loadIcon("fina2.returns.returnTypes", "return_types.gif");
        frame.setFrameIcon(ui.getIcon("fina2.returns.returnTypes"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.returns.returnTypes"));

        ui.addAction("fina2.actions.returnTypes", this);
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
        return new String[] { "fina2.returns.definition.amend",
                "fina2.returns.definition.delete",
                "fina2.returns.definition.review",
                "fina2.returns.definition.format" };
    }

}
