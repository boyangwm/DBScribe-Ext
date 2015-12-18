/*
 * Element.java
 *
 * Created on 25 Èþëü 2002 ã., 19:32
 */

package fina2.javascript.element;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import fina2.javascript.EOFException;
import fina2.javascript.Token;
import fina2.javascript.Tokenizer;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public abstract class Element {

    protected Vector children;
    protected int startPosition;
    protected int endPosition;

    public Element() {
        children = new Vector();
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition + 1;
    }

    public abstract String print(int level, boolean dummy);

    public abstract Token parse(Tokenizer tokenizer, Element parent)
            throws EOFException, fina2.javascript.ParseException;

    public void addChild(Element child) {
        children.add(child);
    }

    public void insertChildAt(int index, Element child) {
        children.insertElementAt(child, index);
    }

    public int getChildIndex(Element child) {
        return children.indexOf(child);
    }

    public int numberOfChildren() {
        return children.size();
    }

    public Element getChildAt(int index) {
        return (Element) children.get(index);
    }

    public void removeChildAt(int index) {
        children.remove(index);
    }

    public void removeChild(Element child) {
        children.remove(child);
    }

    public Element getValidElement() {
        return this;
    }

    public java.util.Iterator children() {
        return children.iterator();
    }

    public synchronized void removeChildren() {
        for (Iterator iter = children(); iter.hasNext();) {
            Element e = (Element) iter.next();
            //if( !(e instanceof Scope) && (!e.getNodeName().toLowerCase().equals("else")) ) {
            e.removeChildren();
            //    children.remove(e);
            //}
        }
        children.removeAllElements();
    }

    protected abstract String getNodeName();

    public abstract javax.swing.ImageIcon getNodeIcon();

    public DefaultMutableTreeNode getNode() {
        Element element = getValidElement();

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(element);

        if (element instanceof Function) {
            if (element instanceof Keyword) {
                if (!((Keyword) element).getName().equals("var")) {
                    node.add(((Function) element).getParameters().getNode());
                }
            } else {
                for (Iterator iter = ((Function) element).getParameters()
                        .children(); iter.hasNext();) {
                    Element e = (Element) iter.next();
                    node.add(e.getNode());
                }
                /*for(Enumeration enum=((Function)this).getParameters().getNode().children(); enum.hasMoreElements(); ) {
                    node.add(
                        (DefaultMutableTreeNode)enum.nextElement()
                    );
                }*/
            }
        }
        if ((element instanceof Equation) && (children.size() == 1)) {
            Element e = (Element) children.get(0);
            return e.getNode();
        } else {
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Element e = (Element) iter.next();
                if (!(e instanceof Operation) && !(e instanceof Break)) {
                    node.add(e.getNode());
                }
            }
        }
        return node;
    }

    public Function findByPosition(int pos) {
        return (Function) findByPosition(this, pos);
    }

    private Element findByPosition(Element element, int pos) {
        if (element instanceof Function) {
            if (((Function) element).getParameters().numberOfChildren() > 0) {
                for (int i = ((Function) element).getParameters()
                        .numberOfChildren() - 1; i >= 0; i--) {
                    Element e = (Element) ((Function) element).getParameters()
                            .getChildAt(i);
                    Element res = findByPosition(e, pos);
                    if (res != null)
                        return res;
                }
            }
        }
        if (element.numberOfChildren() > 0) {
            for (int i = element.numberOfChildren() - 1; i >= 0; i--) {
                Element e = (Element) element.getChildAt(i);
                Element res = findByPosition(e, pos);
                if (res != null)
                    return res;
            }
        }
        if (element instanceof Function) {
            Function f = (Function) element;
            if ((pos >= f.getStartPosition()) && (pos <= f.getEndPosition())) {
                return element;
            }
        }
        return null;
    }

    public String toString() {
        return getNodeName();
    }
}
