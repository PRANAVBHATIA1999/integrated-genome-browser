//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.29 at 04:56:15 PM EDT 
//


package org.bioviz.protannot.interproscan.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProteinType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProteinType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sequence" type="{http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5}SequenceType" minOccurs="0"/>
 *         &lt;element name="xref" type="{http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5}ProteinXrefType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="super-match" type="{http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5}SuperMatchType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="matches" type="{http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5}matchesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProteinType", propOrder = {
    "sequence",
    "xref",
    "superMatch",
    "matches"
})
public class ProteinType {

    protected SequenceType sequence;
    protected List<ProteinXrefType> xref;
    @XmlElement(name = "super-match")
    protected List<SuperMatchType> superMatch;
    @XmlElement(required = true)
    protected MatchesType matches;

    /**
     * Gets the value of the sequence property.
     * 
     * @return
     *     possible object is
     *     {@link SequenceType }
     *     
     */
    public SequenceType getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link SequenceType }
     *     
     */
    public void setSequence(SequenceType value) {
        this.sequence = value;
    }

    /**
     * Gets the value of the xref property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xref property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXref().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProteinXrefType }
     * 
     * 
     */
    public List<ProteinXrefType> getXref() {
        if (xref == null) {
            xref = new ArrayList<ProteinXrefType>();
        }
        return this.xref;
    }

    /**
     * Gets the value of the superMatch property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the superMatch property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuperMatch().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SuperMatchType }
     * 
     * 
     */
    public List<SuperMatchType> getSuperMatch() {
        if (superMatch == null) {
            superMatch = new ArrayList<SuperMatchType>();
        }
        return this.superMatch;
    }

    /**
     * Gets the value of the matches property.
     * 
     * @return
     *     possible object is
     *     {@link MatchesType }
     *     
     */
    public MatchesType getMatches() {
        return matches;
    }

    /**
     * Sets the value of the matches property.
     * 
     * @param value
     *     allowed object is
     *     {@link MatchesType }
     *     
     */
    public void setMatches(MatchesType value) {
        this.matches = value;
    }

}
