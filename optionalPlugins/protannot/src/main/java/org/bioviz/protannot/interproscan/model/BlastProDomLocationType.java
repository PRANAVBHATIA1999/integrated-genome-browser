//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.29 at 04:56:15 PM EDT 
//


package org.bioviz.protannot.interproscan.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BlastProDomLocationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BlastProDomLocationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5}LocationType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="evalue" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="score" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BlastProDomLocationType")
public class BlastProDomLocationType
    extends LocationType
{

    @XmlAttribute(name = "evalue", required = true)
    protected double evalue;
    @XmlAttribute(name = "score", required = true)
    protected double score;

    /**
     * Gets the value of the evalue property.
     * 
     */
    public double getEvalue() {
        return evalue;
    }

    /**
     * Sets the value of the evalue property.
     * 
     */
    public void setEvalue(double value) {
        this.evalue = value;
    }

    /**
     * Gets the value of the score property.
     * 
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the value of the score property.
     * 
     */
    public void setScore(double value) {
        this.score = value;
    }

}
