package fina2.metadata.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MDT")
public class MDT {
	private List<MDTNodeData> NODE;
	
	public List<MDTNodeData> getNODE() {
		if(NODE==null)
			NODE=new ArrayList<MDTNodeData>();
		return NODE;
	}

	public void setNODE(List<MDTNodeData> NODE) {
		this.NODE = NODE;
	}

	
}
