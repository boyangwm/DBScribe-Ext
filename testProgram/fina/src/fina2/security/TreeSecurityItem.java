package fina2.security;

/**
 * The security item, which can be used in hierarchy data storing. Contains data
 * about security options for users and roles.
 */
public class TreeSecurityItem extends SecurityItem {

    /** Parent item id */
    private int parentId = -1;

    /** Defines whether the item is leaf */
    private boolean isLeafItem = false;

    /** Creates the instance of the class */
    public TreeSecurityItem(int id, String text, int parentId,
            boolean isLeafItem) {
        this(id, text, parentId, isLeafItem, Status.NO);
    }

    /** Creates the instance of the class */
    public TreeSecurityItem(int id, String text, int parentId,
            boolean isLeafItem, Status review) {
        this(id, text, parentId, isLeafItem, review, Status.NO);
    }

    /** Creates the instance of the class */
    public TreeSecurityItem(int id, String text, int parentId,
            boolean isLeafItem, Status review, Status amend) {
        super(id, text, review, amend);

        this.parentId = parentId;
        this.isLeafItem = isLeafItem;
    }

    /** Returns parent id of the current item */
    public int getParentId() {
        return parentId;
    }

    /** String representation of this class */
    public String toString() {
        return getText();
    }

    /** Checks whether the item is leaf */
    public boolean isLeaf() {
        return isLeafItem;
    }

}
