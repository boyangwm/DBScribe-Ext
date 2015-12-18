/*
 * ExcelAction.java
 *
 * Created on January 5, 2002, 5:01 PM
 */

package fina2.nbg;

import javax.swing.AbstractAction;

/**
 *
 * @author  Administrator
 * @version 
 */
public class ExcelAction extends AbstractAction {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    private ExcelSelectDialog dlg;

    /** Creates new ExcelAction */
    public ExcelAction() {
        super();

        dlg = new ExcelSelectDialog(null, true);

        putValue(AbstractAction.NAME, "Excel source");

        ui.addAction("fina2.nbg.excel", this);
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        dlg.show();
    }

}
