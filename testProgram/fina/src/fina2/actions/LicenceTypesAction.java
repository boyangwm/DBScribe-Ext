/*
 * LicenceTypesAction.java
 *
 * Created on October 19, 2001, 8:49 PM
 */

package fina2.actions;

import javax.swing.AbstractAction;

import fina2.bank.LicenceTypesFrame;

/**
 *
 * @author  Administrator
 * @version 
 */
public class LicenceTypesAction extends AbstractAction implements FinaAction {

    /** Creates new LicenceTypesAction */
    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    private LicenceTypesFrame frame;

    /** Creates new LicenceAction */
    public LicenceTypesAction() {
        super();

        fina2.Main.main.setLoadingMessage(ui
                .getString("fina2.bank.licenceTypesAction"));
        putValue(AbstractAction.NAME, ui
                .getString("fina2.bank.licenceTypesAction"));

        frame = new LicenceTypesFrame();
        ui.loadIcon("fina2.bank.licenseTypes", "license_types.gif");
        frame.setFrameIcon(ui.getIcon("fina2.bank.licenseTypes"));
        putValue(AbstractAction.SMALL_ICON, ui
                .getIcon("fina2.bank.licenseTypes"));

        ui.addAction("fina2.actions.licences", this);
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
