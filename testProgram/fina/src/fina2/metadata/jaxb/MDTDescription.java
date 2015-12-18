package fina2.metadata.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "MDT_DESCRIPION")
public class MDTDescription implements Serializable {
	private List<MDTNodeDescription> DESCRIPTION;

	public List<MDTNodeDescription> getDESCRIPTION() {
		if (DESCRIPTION == null)
			DESCRIPTION = new ArrayList<MDTNodeDescription>();
		return DESCRIPTION;
	}

	public void setDESCRIPTION(List<MDTNodeDescription> dESCRIPTION) {
		DESCRIPTION = dESCRIPTION;
	}

}
