//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b26-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.28 at 10:33:13 PM PDT 
//


package com.oy.tv.schema.core;

import java.io.Serializable;
import java.util.Calendar;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.oy.tv.schema.core.Adapter1;
import com.oy.tv.schema.core.ResponseBO;


/**
 * <p>Java class for ResponseBO complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseBO">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="userId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unitId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="variationId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="submittedOn" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="receivedOn" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="passed" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="referrer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="clientAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="clientAgent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="activityId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseBO", propOrder = {
    "id",
    "userId",
    "unitId",
    "variationId",
    "submittedOn",
    "receivedOn",
    "passed",
    "referrer",
    "clientAddress",
    "clientAgent",
    "sessionId",
    "activityId"
})
public class ResponseBO
    implements Serializable
{

    private final static long serialVersionUID = 0L;
    @XmlElement(type = Integer.class)
    protected int id;
    @XmlElement(type = Integer.class)
    protected int userId;
    @XmlElement(type = Integer.class)
    protected int unitId;
    @XmlElement(type = Integer.class)
    protected int variationId;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Calendar submittedOn;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Calendar receivedOn;
    @XmlElement(type = Integer.class)
    protected int passed;
    protected String referrer;
    protected String clientAddress;
    protected String clientAgent;
    protected String sessionId;
    protected String activityId;

    /**
     * Gets the value of the id property.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the userId property.
     * 
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the value of the userId property.
     * 
     */
    public void setUserId(int value) {
        this.userId = value;
    }

    /**
     * Gets the value of the unitId property.
     * 
     */
    public int getUnitId() {
        return unitId;
    }

    /**
     * Sets the value of the unitId property.
     * 
     */
    public void setUnitId(int value) {
        this.unitId = value;
    }

    /**
     * Gets the value of the variationId property.
     * 
     */
    public int getVariationId() {
        return variationId;
    }

    /**
     * Sets the value of the variationId property.
     * 
     */
    public void setVariationId(int value) {
        this.variationId = value;
    }

    /**
     * Gets the value of the submittedOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getSubmittedOn() {
        return submittedOn;
    }

    /**
     * Sets the value of the submittedOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmittedOn(Calendar value) {
        this.submittedOn = value;
    }

    /**
     * Gets the value of the receivedOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getReceivedOn() {
        return receivedOn;
    }

    /**
     * Sets the value of the receivedOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceivedOn(Calendar value) {
        this.receivedOn = value;
    }

    /**
     * Gets the value of the passed property.
     * 
     */
    public int getPassed() {
        return passed;
    }

    /**
     * Sets the value of the passed property.
     * 
     */
    public void setPassed(int value) {
        this.passed = value;
    }

    /**
     * Gets the value of the referrer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferrer() {
        return referrer;
    }

    /**
     * Sets the value of the referrer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferrer(String value) {
        this.referrer = value;
    }

    /**
     * Gets the value of the clientAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientAddress() {
        return clientAddress;
    }

    /**
     * Sets the value of the clientAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientAddress(String value) {
        this.clientAddress = value;
    }

    /**
     * Gets the value of the clientAgent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientAgent() {
        return clientAgent;
    }

    /**
     * Sets the value of the clientAgent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientAgent(String value) {
        this.clientAgent = value;
    }

    /**
     * Gets the value of the sessionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the activityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityId() {
        return activityId;
    }

    /**
     * Sets the value of the activityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityId(String value) {
        this.activityId = value;
    }

}
