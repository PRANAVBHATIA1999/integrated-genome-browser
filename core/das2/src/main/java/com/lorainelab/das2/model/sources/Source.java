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
    "version"
})
public class Source {

    @XmlElement(name = "VERSION")
    protected List<Version> version;
    @XmlAttribute(name = "uri")
    protected String uri;
    @XmlAttribute(name = "title")
    protected String title;

    public List<Version> getVersion() {
        if (version == null) {
            version = new ArrayList<>();
        }
        return this.version;
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

}
