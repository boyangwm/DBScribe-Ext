package fina2.metadata.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "MDT_COMPARISON")
public class MDTComparison implements Serializable {

	private List<MDTNodeComparison> COMPARISON;

	public List<MDTNodeComparison> getCOMPARISON() {
		if (COMPARISON == null)
			COMPARISON = new ArrayList<MDTNodeComparison>();
		return COMPARISON;
	}

	public void setCOMPARISON(List<MDTNodeComparison> cOMPARISON) {
		COMPARISON = cOMPARISON;
	}

}