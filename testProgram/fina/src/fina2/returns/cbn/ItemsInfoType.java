package fina2.returns.cbn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ITEMS_INFO")
public class ItemsInfoType {
	private int ITEM_CODE;
	private String ITEM_DESC;
	private float AMMOUNT;

	public int getITEM_CODE() {
		return ITEM_CODE;
	}

	public void setITEM_CODE(int iTEMCODE) {
		ITEM_CODE = iTEMCODE;
	}

	public String getITEM_DESC() {
		return ITEM_DESC;
	}

	public void setITEM_DESC(String iTEMDESC) {
		ITEM_DESC = iTEMDESC;
	}

	public float getAMMOUNT() {
		return AMMOUNT;
	}

	public void setAMMOUNT(float aMMOUNT) {
		AMMOUNT = aMMOUNT;
	}
}