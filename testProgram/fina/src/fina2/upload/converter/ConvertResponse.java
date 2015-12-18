
package fina2.upload.converter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for convertResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="convertResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ConverterMessage" type="{http://converter.upload.fina2/}converterMessage" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "convertResponse", propOrder = {
    "converterMessage"
})
public class ConvertResponse {

    @XmlElement(name = "ConverterMessage")
    protected ConverterMessage converterMessage;

    /**
     * Gets the value of the converterMessage property.
     * 
     * @return
     *     possible object is
     *     {@link ConverterMessage }
     *     
     */
    public ConverterMessage getConverterMessage() {
        return converterMessage;
    }

    /**
     * Sets the value of the converterMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConverterMessage }
     *     
     */
    public void setConverterMessage(ConverterMessage value) {
        this.converterMessage = value;
    }

}
