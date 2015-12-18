package fina2.returns;

import java.io.Serializable;

import fina2.metadata.MDTNodePK;
import fina2.ui.table.TableRow;

public class DefinitionTable implements Serializable, TableRow {

    private String nodeName;

    private MDTNodePK node;
    private String code;
    private int nodeVisible;
    private int type;
    private int level;
    private int evalType;
    private int sequence;
    private boolean blank;

    public DefinitionTable() {
    }

    public Object getPrimaryKey() {
        return null;
    }

    public void setDefaultCol(int defaultCol) {
    }

    public void setValue(int column, String value) {

    }

    public String getValue(int column) {
        switch (column) {
        case 0:
            return code;
        case 1:
            return nodeName;
        case 2:
            switch (type) {
            case ReturnConstants.TABLETYPE_MULTIPLE:
                return "Multiple";
            case ReturnConstants.TABLETYPE_NORMAL:
                return "Normal";
            case ReturnConstants.TABLETYPE_VARIABLE:
                return "Variable";
            }
        }
        return "";
    }

    public int getColumnCount() {
        return 3;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public MDTNodePK getNode() {
        return node;
    }

    public void setNode(MDTNodePK node) {
        this.node = node;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getNodeVisible() {
        return nodeVisible;
    }

    public void setNodeVisible(int nodeVisible) {
        this.nodeVisible = nodeVisible;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getEvalType() {
        return evalType;
    }

    public void setEvalType(int evalType) {
        this.evalType = evalType;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Object getProperty(String key) {
        return null;
    }

    public void putProperty(String key, Object value) {
    }

    public boolean isBlank() {
        return this.blank;
    }

    public void setBlank(boolean blank) {
        this.blank = blank;
    }
}
