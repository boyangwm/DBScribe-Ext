package fina2.metadata.jaxb;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
	public ObjectFactory() {

	}
	
	public MDT createMDT(){
		return new MDT();
	}
}
