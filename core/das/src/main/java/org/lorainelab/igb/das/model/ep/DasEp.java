//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.22 at 09:55:37 AM EDT 
//


package org.lorainelab.igb.das.model.ep;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "entrypoints"
})
@XmlRootElement(name = "DASEP")
public class DasEp {

    @XmlElement(name = "ENTRY_POINTS", required = true)
    protected EntryPoints entrypoints;

    /**
     * Gets the value of the entrypoints property.
     * 
     * @return
     *     possible object is
     *     {@link EntryPoints }
     *     
     */
    public EntryPoints getENTRYPOINTS() {
        return entrypoints;
    }

    /**
     * Sets the value of the entrypoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link EntryPoints }
     *     
     */
    public void setENTRYPOINTS(EntryPoints value) {
        this.entrypoints = value;
    }

}
