package fina2.returns.cbn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRegistry
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CALLREPORT", propOrder = { "HEADER", "BODY", "FOOTER" })
@XmlRootElement(name = "CALLREPORT")
public class CallReport {
	protected HeaderType HEADER;
	protected BodyType BODY;
	protected FooterType FOOTER;

	public HeaderType getHEADER() {
		if (HEADER == null)
			HEADER = new HeaderType();
		return HEADER;
	}

	public BodyType getBODY() {
		if (BODY == null)
			BODY = new BodyType();
		return BODY;
	}

	public FooterType getFOOTER() {
		if (FOOTER == null)
			FOOTER = new FooterType();
		return FOOTER;
	}
}