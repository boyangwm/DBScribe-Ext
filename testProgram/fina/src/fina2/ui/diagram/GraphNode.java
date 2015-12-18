package fina2.ui.diagram;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;

import org.tigris.gef.base.Layer;
import org.tigris.gef.graph.presentation.NetNode;
import org.tigris.gef.presentation.FigNode;

public class GraphNode extends NetNode implements Serializable {

    public GraphPort north, east, west, south, n;
    protected int _number;
    protected int x = 0, y = 0;
    protected String str = " ";
    protected Object pk;
    protected int level = 999;
    protected Hashtable parents = new Hashtable();
    protected boolean hasChilds = false;
    protected boolean hasParents = false;

    protected FigGraphNode fnF;

    public void initialize(Hashtable args) {
        addPort(north = new GraphPort(this));
        addPort(south = new GraphPort(this));

        parents.clear();
        _number = _NextNumber++;
    }

    static int _NextNumber = 1;

    public int getNumber() {
        return _number;
    }

    public String getId() {
        return "" + _number;
    }

    public Object getPK() {
        return pk;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setHasParents(boolean has) {
        this.hasParents = has;
    }

    public void setHasChilds(boolean has) {
        this.hasChilds = has;
    }

    public boolean getHasParents() {
        return hasParents;
    }

    public boolean getHasChilds() {
        return hasChilds;
    }

    public void addParent(Object node) {
        parents.put(node, node);
    }

    public Collection getParents() {
        return parents.values();
    }

    public void Up() {

    }

    public void Down() {
        setLocation(getX(), getY() + 50);
        y = y + 50;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
        fnF.setX(x);
        fnF.setY(y);
    }

    public void setTitle(String str) {
        this.str = str;
    }

    public void setPK(Object pk) {
        this.pk = pk;
    }

    public void fill(Color c) {
        fnF.setFillColor(c);
    }

    public void unfill() {
        fnF.setFillColor(Color.white);
    }

    public FigNode makePresentation(Layer lay) {
        FigGraphNode fn = new FigGraphNode();
        fn.setOwner(this, str);
        fn.setX(x);
        fn.setY(y);
        this.fnF = fn;
        return fn;
    }

    /** Sample event handler: prints a message to the console. */
    public void mouseEntered(MouseEvent e) {
        //   System.out.println("sample node got mouseEnter");
    }

    /** Sample event handler: prints a message to the console. */
    public void mouseExited(MouseEvent e) {
        //    System.out.println("sample node got mouseExit");
    }

    /** Sample event handler: prints a message to the console. */
    public void mouseReleased(MouseEvent e) {
        //  System.out.println("sample node got mouseUp");
    }

    /** Sample event handler: prints a message to the console. */
    public void mousePressed(MouseEvent e) {
        //    System.out.println("sample node got mouseDown");
    }

    /** Sample event handler: prints a message to the console. */
    public void mouseClicked(MouseEvent e) {
        //    System.out.println("sample node got mouseDown");
    }

    /** Sample event handler: prints a message to the console. */
    public void mouseDragged(MouseEvent e) {
        //    System.out.println("sample node got mouseDrag");
    }

    /** Sample event handler: prints a message to the console. */
    public void mouseMoved(MouseEvent e) {
        //    System.out.println("sample node got mouseMove");
    }

    /** Sample event handler: prints a message to the console. */
    public void keyTyped(KeyEvent e) {
        //    System.out.println("sample node got keyUp");
    }

    /** Sample event handler: prints a message to the console. */
    public void keyReleased(KeyEvent e) {
        //    System.out.println("sample node got keyUp");
    }

    /** Sample event handler: prints a message to the console. */
    public void keyPressed(KeyEvent e) {
        //    System.out.println("sample node got keyDown");
    }
}
