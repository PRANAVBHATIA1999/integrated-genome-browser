//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.22 at 01:12:55 PM EDT 
//


package org.lorainelab.igb.das.model.gff;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gff"
})
@XmlRootElement(name = "DASGFF")
public class DasGff {

    @XmlElement(name = "GFF", required = true)
    protected Gff gff;

    /**
     * Gets the value of the gff property.
     * 
     * @return
     *     possible object is
     *     {@link Gff }
     *     
     */
    public Gff getGFF() {
        return gff;
    }

    /**
     * Sets the value of the gff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Gff }
     *     
     */
    public void setGFF(Gff value) {
        this.gff = value;
    }

}
