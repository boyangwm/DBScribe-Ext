/*
 * BankDefinitionsAction.java
 *
 * Created on October 19, 2001, 8:49 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.metadata.ComparisonsAmendFrame;

/**
 *
 * @author  Sh Shalamberidze
 * @version 
 */
public class ComparisonsAmendAction extends AbstractAction implements
        FinaAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private ComparisonsAmendFrame frame;

    /** Creates new LanguagesAction */
    public ComparisonsAmendAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.metadata.comparisonrules"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.metadata.comparisonrules"));

        frame = new ComparisonsAmendFrame();
        ui.loadIcon("fina2.metadata.comparisonRules", "comparison_rules.gif");
        frame.setFrameIcon(ui.getIcon("fina2.metadata.comparisonRules"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.metadata.comparisonRules"));

        ui.addAction("fina2.actions.metadataComparisons", this);
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
        return new String[] { "fina2.metadata.amend", "fina2.metadata.delete",
                "fina2.metadata.review" };
    }

}
