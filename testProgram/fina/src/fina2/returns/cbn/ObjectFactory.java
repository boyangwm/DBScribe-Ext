package fina2.returns.cbn;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public ObjectFactory() {

	}

	public CallReport createCallReport() {
		return new CallReport();
	}
}
