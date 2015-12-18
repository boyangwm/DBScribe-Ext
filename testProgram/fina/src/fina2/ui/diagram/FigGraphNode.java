package fina2.ui.diagram;

import java.awt.Color;
import java.util.StringTokenizer;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigCircle;
import org.tigris.gef.presentation.FigNode;
import org.tigris.gef.presentation.FigText;

public class FigGraphNode extends FigNode {
    Fig obj1, obj2, obj3, obj4, obj5, obj6;
    FigText obj7;
    private fina2.ui.UIManager ui = fina2.Main.main.ui;
    private fina2.Main main = fina2.Main.main;

    public FigGraphNode() {
        super();

        obj1 = new FigCircle(-30, -20, 78, 40, Color.black, Color.white);
        obj3 = new FigCircle(10, -15, 0, 0, Color.black, Color.blue);
        obj4 = new FigCircle(10, 15, 0, 0, Color.black, Color.blue);

        obj7 = new FigText(0, -15, 20, 20);
        obj7.setLineWidth(0);
        obj7.setJustification(FigText.JUSTIFY_CENTER);
        obj7.setFont(ui.getFont());
        obj7.setTextColor(Color.black);

        addFig(obj1);
        addFig(obj3);
        addFig(obj4);
        addFig(obj7);
    }

    public String getPrivateData() {
        return "text=\"" + obj7.getText() + "\"";
    }

    public void setOwner(Object own, String str) {
        super.setOwner(own);
        if (!(own instanceof GraphNode))
            return;
        GraphNode node = (GraphNode) own;
        obj7.setText(str);

        bindPort(node.north, obj3);
        bindPort(node.south, obj4);
    }

    public void setPrivateData(String data) {
        StringTokenizer tokenizer = new StringTokenizer(data, "=\"' ");

        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            if (tok.equals("text")) {
                String s = tokenizer.nextToken();
                obj7.setText(s);
            }
        }
    }

    public String toString() {
        return obj7.getText();
    }
}
