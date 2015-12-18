/*
 * DNDTree.java
 *
 * Created on 18 Èþëü 2002 ã., 14:40
 */

package fina2.ui.tree;

import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class DNDTree extends EJBTree implements DragSourceListener,
        DragGestureListener {

    DragSource dragSource;

    /** Creates new DNDTree */
    public DNDTree() {
        super();

        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this,
                DnDConstants.ACTION_COPY, this);
    }

    public void dragOver(java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    public void dragExit(java.awt.dnd.DragSourceEvent dragSourceEvent) {
    }

    public void dropActionChanged(
            java.awt.dnd.DragSourceDragEvent dragSourceDragEvent) {
    }

    public void dragEnter(java.awt.dnd.DragSourceDragEvent event) {
    }

    public void dragDropEnd(java.awt.dnd.DragSourceDropEvent dragSourceDropEvent) {
    }

    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent event) {
        Node node = getSelectedNode();
        if (node != null) {
            StringSelection text = new StringSelection("\""
                    + node.getProperty("code") + "\"");

            dragSource.startDrag(event, DragSource.DefaultCopyDrop, text, this);
        }
    }

}
