//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2015.04.06 at 11:25:31 AM EDT
//
package org.lorainelab.igb.quickload.model.annots;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "files"
})
@XmlRootElement(name = "files")
public class AnnotsRootElement {

    @XmlElements({
        @XmlElement(name = "file", type = QuickloadFile.class),})
    protected List<QuickloadFile> files;

    public List<QuickloadFile> getFiles() {
        if (files == null) {
            files = new ArrayList<>();
        }
        return this.files;
    }

}
