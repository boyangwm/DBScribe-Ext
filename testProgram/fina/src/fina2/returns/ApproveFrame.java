package fina2.returns;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class ApproveFrame extends Frame {
    public ApproveFrame() {
        try {
            jbInit();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setFocusable(false);
        this
                .setTitle("Some Returns Are Already Imported. Please Choose Return(s) To Overwrite");
        this.setLayout(borderLayout1);
        jButton1.setToolTipText("");
        jButton1.setText("Cancel");
        jButton2.setText("Continue");
        jLabel1.setFont(new java.awt.Font("Tahoma", Font.BOLD, 12));
        jPanel2.setLayout(borderLayout2);
        this.add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jButton2);
        jPanel1.add(jButton1);
        jPanel2.add(jLabel1, java.awt.BorderLayout.NORTH);
        jScrollPane1.getViewport().add(jTable1);
        this.add(jPanel2, java.awt.BorderLayout.WEST);
        this.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jTable1.setPreferredScrollableViewportSize(new Dimension(500, 70));
        //        jTable1.setFillsViewportHeight(true);
    }

    public static void main(String[] args) {
        ApproveFrame approveframe = new ApproveFrame();
        approveframe.setSize(400, 600);
        approveframe.setVisible(true);
    }

    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JPanel jPanel2 = new JPanel();
    JLabel jLabel1 = new JLabel();
    BorderLayout borderLayout2 = new BorderLayout();
    JTable jTable1 = new JTable(new MyTableModel());
    JScrollPane jScrollPane1 = new JScrollPane();
}

class MyTableModel extends AbstractTableModel {

    String[] columnNames = { "Report Code", "Bank Code", "Period Start",
            "Period End", "Version", "Overwrite" };

    Object[][] data = {
            { "RP01", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(false) },
            { "RP02", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(true) },
            { "RP03", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(false) },
            { "RP04", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(true) },
            { "RP05", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(false) },
            { "RP06", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(false) },
            { "RP07", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(true) },
            { "RP08", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(true) },
            { "RP09", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(false) },
            { "RP010", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(false) },
            { "RP011", "BANK1", "01/08/2007", "01/09/2007", "ORG",
                    new Boolean(false) },
            { "RP012", "BANK1", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(false) },
            { "RP01", "BANK2", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(true) },
            { "RP02", "BANK2", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(false) },
            { "RP03", "BANK2", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(true) },
            { "RP04", "BANK2", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(false) },
            { "RP05", "BANK2", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(false) },
            { "RP06", "BANK2", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(false) },
            { "RP07", "BANK2", "01/08/2007", "01/09/2007", "EDT",
                    new Boolean(false) } };

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 2) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}
