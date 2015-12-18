package fina2.returns.cbn;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BODY")
public class BodyType {
	private List<ItemsInfoType> ITEMS_INFO;

	public List<ItemsInfoType> getITEMS_INFO() {
		if (ITEMS_INFO == null)
			ITEMS_INFO = new ArrayList<ItemsInfoType>();
		return ITEMS_INFO;
	}

	public void setITEMS_INFO(List<ItemsInfoType> iTEMSINFO) {
		ITEMS_INFO = iTEMSINFO;
	}
}