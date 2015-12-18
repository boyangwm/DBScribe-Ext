package fina2.ui.diagram;

import org.tigris.gef.base.Layer;
import org.tigris.gef.graph.presentation.NetEdge;
import org.tigris.gef.presentation.ArrowHeadTriangle;
import org.tigris.gef.presentation.FigEdge;

public class GraphEdge extends NetEdge {

    public String getId() {
        return toString();
    }

    public FigEdge makePresentation(Layer lay) {

        FigEdge figEdge = new FigGraphEdgeLine();
        figEdge.setSourceArrowHead(new ArrowHeadTriangle());

        return figEdge;
    }
}
