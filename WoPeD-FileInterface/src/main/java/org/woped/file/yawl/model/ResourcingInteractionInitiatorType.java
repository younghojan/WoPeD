//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.18 at 08:57:29 AM CEST 
//


package org.woped.file.yawl.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResourcingInteractionInitiatorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResourcingInteractionInitiatorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="initiator" use="required" type="{http://www.yawlfoundation.org/yawlschema}ResourcingInitiatorType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourcingInteractionInitiatorType")
@XmlSeeAlso({
    ResourcingOfferFactsType.class,
    ResourcingAllocateFactsType.class
})
public class ResourcingInteractionInitiatorType {

    @XmlAttribute(name = "initiator", required = true)
    protected ResourcingInitiatorType initiator;

    /**
     * Gets the value of the initiator property.
     * 
     * @return
     *     possible object is
     *     {@link ResourcingInitiatorType }
     *     
     */
    public ResourcingInitiatorType getInitiator() {
        return initiator;
    }

    /**
     * Sets the value of the initiator property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResourcingInitiatorType }
     *     
     */
    public void setInitiator(ResourcingInitiatorType value) {
        this.initiator = value;
    }

}