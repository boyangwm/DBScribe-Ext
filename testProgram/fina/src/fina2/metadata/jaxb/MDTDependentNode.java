package fina2.metadata.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DEPENDENT_NODES")
public class MDTDependentNode implements Serializable {
	private List<Long> ID;

	public List<Long> getID() {
		if (ID == null)
			ID = new ArrayList<Long>();
		return ID;
	}

	public void setID(List<Long> iD) {
		ID = iD;
	}

}
