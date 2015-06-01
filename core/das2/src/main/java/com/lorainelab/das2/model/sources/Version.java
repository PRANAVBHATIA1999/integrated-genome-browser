//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2015.05.29 at 10:28:20 AM EDT
//
package com.lorainelab.das2.model.sources;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "coordinates",
    "capability"
})
public class Version {

    @XmlElement(name = "COORDINATES")
    protected Coordinates coordinates;
    @XmlElement(name = "CAPABILITY")
    protected List<Capability> capability;
    @XmlAttribute(name = "uri")
    protected String uri;
    @XmlAttribute(name = "title")
    protected String title;
    @XmlAttribute(name = "created")
    protected String created;

    /**
     * Gets the value of the coordinates property.
     *
     * @return
     * possible object is
     * {@link Coordinates }
     *
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the value of the coordinates property.
     *
     * @param value
     * allowed object is
     * {@link Coordinates }
     *
     */
    public void setCoordinates(Coordinates value) {
        this.coordinates = value;
    }

    /**
     * Gets the value of the capability property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the capability property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCAPABILITY().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Capability }
     *
     *
     */
    public List<Capability> getCapability() {
        if (capability == null) {
            capability = new ArrayList<Capability>();
        }
        return this.capability;
    }

    /**
     * Gets the value of the uri property.
     *
     * @return
     * possible object is
     * {@link String }
     *
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     *
     * @param value
     * allowed object is
     * {@link String }
     *
     */
    public void setUri(String value) {
        this.uri = value;
    }

    /**
     * Gets the value of the title property.
     *
     * @return
     * possible object is
     * {@link String }
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     *
     * @param value
     * allowed object is
     * {@link String }
     *
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the created property.
     *
     * @return
     * possible object is
     * {@link String }
     *
     */
    public String getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     *
     * @param value
     * allowed object is
     * {@link String }
     *
     */
    public void setCreated(String value) {
        this.created = value;
    }

}
