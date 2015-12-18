
package fina2.upload.converter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fina2.upload.converter package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ConvertResponse_QNAME = new QName("http://converter.upload.fina2/", "convertResponse");
    private final static QName _ConverterMessage_QNAME = new QName("http://converter.upload.fina2/", "ConverterMessage");
    private final static QName _Convert_QNAME = new QName("http://converter.upload.fina2/", "convert");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fina2.upload.converter
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ConverterMessage }
     * 
     */
    public ConverterMessage createConverterMessage() {
        return new ConverterMessage();
    }

    /**
     * Create an instance of {@link Convert }
     * 
     */
    public Convert createConvert() {
        return new Convert();
    }

    /**
     * Create an instance of {@link ConvertResponse }
     * 
     */
    public ConvertResponse createConvertResponse() {
        return new ConvertResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://converter.upload.fina2/", name = "convertResponse")
    public JAXBElement<ConvertResponse> createConvertResponse(ConvertResponse value) {
        return new JAXBElement<ConvertResponse>(_ConvertResponse_QNAME, ConvertResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConverterMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://converter.upload.fina2/", name = "ConverterMessage")
    public JAXBElement<ConverterMessage> createConverterMessage(ConverterMessage value) {
        return new JAXBElement<ConverterMessage>(_ConverterMessage_QNAME, ConverterMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Convert }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://converter.upload.fina2/", name = "convert")
    public JAXBElement<Convert> createConvert(Convert value) {
        return new JAXBElement<Convert>(_Convert_QNAME, Convert.class, null, value);
    }

}
