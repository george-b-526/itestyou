//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b26-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.28 at 10:33:13 PM PDT 
//


package com.oy.tv.schema.core;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import com.oy.tv.schema.core.EEventState;


/**
 * <p>Java class for EEventState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EEventState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *     &lt;enumeration value="0"/>
 *     &lt;enumeration value="1"/>
 *     &lt;enumeration value="2"/>
 *     &lt;enumeration value="3"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum(Integer.class)
public enum EEventState {

    @XmlEnumValue("1")
    BUSY(1),
    @XmlEnumValue("3")
    FAILED(3),
    @XmlEnumValue("2")
    PROCESSED(2),
    @XmlEnumValue("0")
    QUEUED(0);
    private final int value;

    EEventState(int v) {
        value = v;
    }

    public int value() {
        return value;
    }

    public static EEventState fromValue(int v) {
        for (EEventState c: EEventState.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }

}
