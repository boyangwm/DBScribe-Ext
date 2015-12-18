/*
 * Node.java
 *
 * Created on October 16, 2001, 2:17 PM
 */

package fina2.ui.tree;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author  David Shalamberidze
 * @version
 */

public class Node implements Serializable, Comparable {

    private Object pk;
    private Object parentPK;
    private Vector children;
    private Object type;
    private String label;
    private Hashtable properties;
    private boolean checked;
    private boolean defaultNode;

    public Node(Object pk, String label, Object type) {
        children = new Vector();
        properties = new Hashtable();
        this.pk = pk;
        this.label = label;
        this.type = type;
    }

    public void setParentPK(Object parentPK) {
        this.parentPK = parentPK;
    }

    public Object getParentPK() {
        return parentPK;
    }

    public Object getPrimaryKey() {
        return pk;
    }

    public Vector getChildren() {
        return children;
    }

    public Object getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public boolean isChecked() {
        return checked;
    }

    public boolean isDefaultNode() {
        return defaultNode;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setDefaultNode(boolean defaultNode) {
        this.defaultNode = defaultNode;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public String toString() {
        return label;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public void putProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public boolean equals(Object obj) {
        boolean retVal = false;
        if (obj != null) {
            return this.getPrimaryKey().equals(obj);
        }
        return retVal;
    }

    public int compareTo(Object obj) {
        int retVal = 0;
        Node n = (Node) obj;
        if (!this.equals(n)) {
            retVal = this.getLabel().compareTo(n.getLabel());
        }
        return retVal;
    }
    
    
}
