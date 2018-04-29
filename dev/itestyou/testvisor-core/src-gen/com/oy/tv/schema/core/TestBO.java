//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b26-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.28 at 10:33:13 PM PDT 
//


package com.oy.tv.schema.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import com.oy.tv.schema.core.Adapter1;
import com.oy.tv.schema.core.ETestState;
import com.oy.tv.schema.core.TestBO;


/**
 * <p>Java class for TestBO complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TestBO">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ownerId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="bundleId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="startedOn" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="updatedOn" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="variationIds" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="answerIndexes" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="scoreSheet" type="{http://www.w3.org/2001/XMLSchema}boolean" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="attempted" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="completed" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="correct" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="incorrect" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="state" type="{}ETestState"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestBO", propOrder = {
    "id",
    "ownerId",
    "bundleId",
    "startedOn",
    "updatedOn",
    "variationIds",
    "answerIndexes",
    "scoreSheet",
    "attempted",
    "completed",
    "correct",
    "incorrect",
    "state"
})
public class TestBO
    implements Serializable
{

    private final static long serialVersionUID = 0L;
    @XmlElement(type = Integer.class)
    protected int id;
    @XmlElement(type = Integer.class)
    protected int ownerId;
    @XmlElement(type = Integer.class)
    protected int bundleId;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Calendar startedOn;
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    protected Calendar updatedOn;
    @XmlElement(type = Integer.class)
    protected List<Integer> variationIds;
    @XmlElement(type = Integer.class)
    protected List<Integer> answerIndexes;
    @XmlElement(type = Boolean.class)
    protected List<Boolean> scoreSheet;
    @XmlElement(type = Integer.class)
    protected int attempted;
    @XmlElement(type = Integer.class)
    protected int completed;
    @XmlElement(type = Integer.class)
    protected int correct;
    @XmlElement(type = Integer.class)
    protected int incorrect;
    protected ETestState state;

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
     * Gets the value of the ownerId property.
     * 
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * Sets the value of the ownerId property.
     * 
     */
    public void setOwnerId(int value) {
        this.ownerId = value;
    }

    /**
     * Gets the value of the bundleId property.
     * 
     */
    public int getBundleId() {
        return bundleId;
    }

    /**
     * Sets the value of the bundleId property.
     * 
     */
    public void setBundleId(int value) {
        this.bundleId = value;
    }

    /**
     * Gets the value of the startedOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getStartedOn() {
        return startedOn;
    }

    /**
     * Sets the value of the startedOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartedOn(Calendar value) {
        this.startedOn = value;
    }

    /**
     * Gets the value of the updatedOn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Sets the value of the updatedOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdatedOn(Calendar value) {
        this.updatedOn = value;
    }

    /**
     * Gets the value of the variationIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variationIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariationIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getVariationIds() {
        if (variationIds == null) {
            variationIds = new ArrayList<Integer>();
        }
        return this.variationIds;
    }

    /**
     * Gets the value of the answerIndexes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the answerIndexes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnswerIndexes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getAnswerIndexes() {
        if (answerIndexes == null) {
            answerIndexes = new ArrayList<Integer>();
        }
        return this.answerIndexes;
    }

    /**
     * Gets the value of the scoreSheet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the scoreSheet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getScoreSheet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Boolean }
     * 
     * 
     */
    public List<Boolean> getScoreSheet() {
        if (scoreSheet == null) {
            scoreSheet = new ArrayList<Boolean>();
        }
        return this.scoreSheet;
    }

    /**
     * Gets the value of the attempted property.
     * 
     */
    public int getAttempted() {
        return attempted;
    }

    /**
     * Sets the value of the attempted property.
     * 
     */
    public void setAttempted(int value) {
        this.attempted = value;
    }

    /**
     * Gets the value of the completed property.
     * 
     */
    public int getCompleted() {
        return completed;
    }

    /**
     * Sets the value of the completed property.
     * 
     */
    public void setCompleted(int value) {
        this.completed = value;
    }

    /**
     * Gets the value of the correct property.
     * 
     */
    public int getCorrect() {
        return correct;
    }

    /**
     * Sets the value of the correct property.
     * 
     */
    public void setCorrect(int value) {
        this.correct = value;
    }

    /**
     * Gets the value of the incorrect property.
     * 
     */
    public int getIncorrect() {
        return incorrect;
    }

    /**
     * Sets the value of the incorrect property.
     * 
     */
    public void setIncorrect(int value) {
        this.incorrect = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link ETestState }
     *     
     */
    public ETestState getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link ETestState }
     *     
     */
    public void setState(ETestState value) {
        this.state = value;
    }

}