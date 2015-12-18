package fina2.actions;

import javax.swing.AbstractAction;

import fina2.metadata.FormulaRepositoryFrame;
import fina2.ui.tree.Node;

public class FormulaRepositoryAction extends AbstractAction implements
        FinaAction, FormulaRepositoryInterface {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private FormulaRepositoryFrame frame;
    private Node node = null;

    /** Creates a new instance of FormulaRepositoryAction */
    public FormulaRepositoryAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.metadata.frAction"));
        putValue(AbstractAction.NAME, ui.getString("fina2.metadata.frAction"));

        frame = new FormulaRepositoryFrame();
        ui.loadIcon("fina2.metadata.formulaRepository", "mdtree.gif");
        frame.setFrameIcon(ui.getIcon("fina2.metadata.formulaRepository"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.metadata.formulaRepository"));

        ui.addAction("fina2.actions.formulaRepository", this);
    }

    public void setNode(Node node) {
        this.node = node;
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
            frame.show(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
        frame.requestFocus();
    }

    public String[] getPermissions() {
        return new String[] { "fina2.metadata.amend", "fina2.metadata.review" };
    }
}
