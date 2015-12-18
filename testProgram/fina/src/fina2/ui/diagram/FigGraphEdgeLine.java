package fina2.ui.diagram;

import org.tigris.gef.presentation.FigEdgeLine;

public class FigGraphEdgeLine extends FigEdgeLine {

    public String toString() {

        StringBuffer buff = new StringBuffer();

        buff.append("[").append(getDestFigNode());
        buff.append("]-[").append(getSourceFigNode()).append("]");

        return buff.toString();
    }
}
