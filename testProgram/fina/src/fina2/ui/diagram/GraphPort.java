package fina2.ui.diagram;

import java.io.Serializable;

import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.presentation.NetNode;
import org.tigris.gef.graph.presentation.NetPort;

public class GraphPort extends NetPort implements Serializable {

    public GraphPort(NetNode parent) {
        super(parent);
    }

    protected Class defaultEdgeClass(NetPort otherPort) {

        return GraphEdge.class;
    }

    /** Add the constraint that SamplePort's can only be connected to
     * other ports of the same type. */
    public boolean canConnectTo(GraphModel gm, Object anotherPort) {
        return (super.canConnectTo(gm, anotherPort))
                && (anotherPort.getClass() == this.getClass());
    }
}
