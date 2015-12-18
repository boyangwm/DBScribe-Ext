package fina2.reportoo;

import java.text.SimpleDateFormat;

import fina2.reportoo.server.StoredReportInfo;
import fina2.ui.treetable.AbstractTreeTableModel;

public class TreeTableModel extends AbstractTreeTableModel {

    private fina2.ui.UIManager ui = fina2.Main.main.ui;

    // Names of the columns.
    static protected String[] columnCodes = { "fina2.report.reportName",
            "fina2.report.storeDate", "fina2.report.storeUser",
            "fina2.report.language" };

    // Types of the columns.
    static protected Class[] cTypes = {
            fina2.ui.treetable.TreeTableModel.class, String.class,
            String.class, String.class };

    private String pattern;

    public TreeTableModel(StoredReportInfo root) {
        super(root);
    }

    public void setRoot(StoredReportInfo root) {
        super.root = root;
    }

    /**
     * Returns the child of <code>parent</code> at index <code>index</code>
     * in the parent's child array.
     *
     * @param parent a node in the tree, obtained from this data source
     * @param index int
     * @return the child of <code>parent</code> at index <code>index</code>
     * @todo Implement this javax.swing.tree.TreeModel method
     */
    public Object getChild(Object parent, int index) {
        StoredReportInfo reportInfo = (StoredReportInfo) parent;
        return reportInfo.getChildren().get(index);
    }

    /**
     * Returns the number of children of <code>parent</code>.
     *
     * @param parent a node in the tree, obtained from this data source
     * @return the number of children of the node <code>parent</code>
     * @todo Implement this javax.swing.tree.TreeModel method
     */
    public int getChildCount(Object parent) {
        StoredReportInfo reportInfo = (StoredReportInfo) parent;
        return (reportInfo.isFolder()) ? reportInfo.getChildren().size() : 0;
    }

    /**
     * Returns the value to be displayed for node <code>node</code>, at
     * column number <code>column</code>.
     *
     * @param node Object
     * @param column int
     * @return Object
     * @todo Implement this test.TreeTableModel method
     */
    public Object getValueAt(Object node, int column) {

        Object result = "";
        StoredReportInfo reportInfo = (StoredReportInfo) node;

        switch (column) {
        case 0:
            result = reportInfo.getName();
            break;
        case 1:
            if (!reportInfo.isFolder()) {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                result = format.format(reportInfo.getStoreDate());
            }
            break;
        case 2:
            if (!reportInfo.isFolder()) {
                result = reportInfo.getCreatorUser();
            }
            break;
        case 3:
            if (!reportInfo.isFolder()) {
                result = reportInfo.getLanguageName();
            }
            break;
        }

        return result;
    }

    /**
     * Returns the number ofs availible column.
     *
     * @return int
     * @todo Implement this test.TreeTableModel method
     */
    public int getColumnCount() {
        return columnCodes.length;
    }

    /**
     * Returns the name for column number <code>column</code>.
     *
     * @param column int
     * @return String
     * @todo Implement this test.TreeTableModel method
     */
    public String getColumnName(int column) {
        return ui.getString(columnCodes[column]);
    }

    public Class getColumnClass(int column) {
        return cTypes[column];
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}