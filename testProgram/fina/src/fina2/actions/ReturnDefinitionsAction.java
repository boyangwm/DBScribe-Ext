/*
 * BankDefinitionsAction.java
 *
 * Created on October 19, 2001, 8:49 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.returns.ReturnDefinitionsFrame;

/**
 *
 * @author  Sh Shalamberidze
 * @version 
 */
public class ReturnDefinitionsAction extends AbstractAction implements
        FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private ReturnDefinitionsFrame frame;

    /** Creates new LanguagesAction */
    public ReturnDefinitionsAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.returns.returnDefinitionsAction"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.returns.returnDefinitionsAction"));

        frame = new ReturnDefinitionsFrame();
        ui
                .loadIcon("fina2.returns.returnDefinitions",
                        "return_definitions.gif");
        frame.setFrameIcon(ui.getIcon("fina2.returns.returnDefinitions"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.returns.returnDefinitions"));

        ui.addAction("fina2.actions.returnDefinitions", this);
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
