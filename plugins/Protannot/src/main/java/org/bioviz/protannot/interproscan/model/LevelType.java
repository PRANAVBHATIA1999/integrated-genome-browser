//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.29 at 04:56:15 PM EDT 
//


package org.bioviz.protannot.interproscan.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LevelType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LevelType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="STRONG"/>
 *     &lt;enumeration value="WEAK"/>
 *     &lt;enumeration value="NONE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LevelType")
@XmlEnum
public enum LevelType {

    STRONG,
    WEAK,
    NONE;

    public String value() {
        return name();
    }

    public static LevelType fromValue(String v) {
        return valueOf(v);
    }

}
