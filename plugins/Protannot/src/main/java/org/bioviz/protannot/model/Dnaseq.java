//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.25 at 02:07:45 PM EDT 
//
package org.bioviz.protannot.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "mrnaAndAaseq",
    "residues"
})
@XmlRootElement(name = "dnaseq")
public class Dnaseq {

    @XmlElements({
        @XmlElement(name = "mRNA", required = true, type = Dnaseq.MRNA.class),
        @XmlElement(name = "aaseq", required = true, type = Dnaseq.Aaseq.class)
    })
    protected List<Object> mrnaAndAaseq;
    @XmlElement(required = true)
    protected Dnaseq.Residues residues;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "seq", required = true)
    protected String seq;

    /**
     * Gets the value of the mrnaAndAaseq property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the mrnaAndAaseq property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMRNAAndAaseq().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list null null     {@link Dnaseq.MRNA }
     * {@link Dnaseq.Aaseq }
     *
     *
     */
    public List<Object> getMRNAAndAaseq() {
        if (mrnaAndAaseq == null) {
            mrnaAndAaseq = new ArrayList<Object>();
        }
        return this.mrnaAndAaseq;
    }

    /**
     * Gets the value of the residues property.
     *
     * @return possible object is {@link Dnaseq.Residues }
     *
     */
    public Dnaseq.Residues getResidues() {
        return residues;
    }

    /**
     * Sets the value of the residues property.
     *
     * @param value allowed object is {@link Dnaseq.Residues }
     *
     */
    public void setResidues(Dnaseq.Residues value) {
        this.residues = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the seq property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSeq() {
        return seq;
    }

    /**
     * Sets the value of the seq property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setSeq(String value) {
        this.seq = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "simsearch"
    })
    public static class Aaseq {

        protected List<Dnaseq.Aaseq.Simsearch> simsearch;
        @XmlAttribute(name = "id", required = true)
        protected String id;

        /**
         * Gets the value of the simsearch property.
         *
         * <p>
         * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
         * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
         * method for the simsearch property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSimsearch().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list {@link Dnaseq.Aaseq.Simsearch }
         *
         *
         */
        public List<Dnaseq.Aaseq.Simsearch> getSimsearch() {
            if (simsearch == null) {
                simsearch = new ArrayList<Dnaseq.Aaseq.Simsearch>();
            }
            return this.simsearch;
        }

        /**
         * Gets the value of the id property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the value of the id property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setId(String value) {
            this.id = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "simhit"
        })
        public static class Simsearch {

            protected List<Dnaseq.Aaseq.Simsearch.Simhit> simhit;
            @XmlAttribute(name = "method", required = true)
            protected String method;

            /**
             * Gets the value of the simhit property.
             *
             * <p>
             * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
             * make to the returned list will be present inside the JAXB object. This is why there is not a
             * <CODE>set</CODE> method for the simhit property.
             *
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getSimhit().add(newItem);
             * </pre>
             *
             *
             * <p>
             * Objects of the following type(s) are allowed in the list {@link Dnaseq.Aaseq.Simsearch.Simhit }
             *
             *
             */
            public List<Dnaseq.Aaseq.Simsearch.Simhit> getSimhit() {
                if (simhit == null) {
                    simhit = new ArrayList<Dnaseq.Aaseq.Simsearch.Simhit>();
                }
                return this.simhit;
            }

            /**
             * Gets the value of the method property.
             *
             * @return possible object is {@link String }
             *
             */
            public String getMethod() {
                return method;
            }

            /**
             * Sets the value of the method property.
             *
             * @param value allowed object is {@link String }
             *
             */
            public void setMethod(String value) {
                this.method = value;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "descriptor",
                "simspan"
            })
            public static class Simhit {

                protected List<Dnaseq.Descriptor> descriptor;
                @XmlElement(required = true)
                protected List<Dnaseq.Aaseq.Simsearch.Simhit.Simspan> simspan;

                /**
                 * Gets the value of the descriptor property.
                 *
                 * <p>
                 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification
                 * you make to the returned list will be present inside the JAXB object. This is why there is not a
                 * <CODE>set</CODE> method for the descriptor property.
                 *
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getDescriptor().add(newItem);
                 * </pre>
                 *
                 *
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Dnaseq.Aaseq.Simsearch.Simhit.Descriptor }
                 *
                 *
                 */
                public List<Dnaseq.Descriptor> getDescriptor() {
                    if (descriptor == null) {
                        descriptor = new ArrayList<Dnaseq.Descriptor>();
                    }
                    return this.descriptor;
                }

                /**
                 * Gets the value of the simspan property.
                 *
                 * <p>
                 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification
                 * you make to the returned list will be present inside the JAXB object. This is why there is not a
                 * <CODE>set</CODE> method for the simspan property.
                 *
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getSimspan().add(newItem);
                 * </pre>
                 *
                 *
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link Dnaseq.Aaseq.Simsearch.Simhit.Simspan }
                 *
                 *
                 */
                public List<Dnaseq.Aaseq.Simsearch.Simhit.Simspan> getSimspan() {
                    if (simspan == null) {
                        simspan = new ArrayList<Dnaseq.Aaseq.Simsearch.Simhit.Simspan>();
                    }
                    return this.simspan;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "descriptor"
                })
                public static class Simspan {

                    protected List<Dnaseq.Descriptor> descriptor;
                    @XmlAttribute(name = "query_start", required = true)
                    @XmlSchemaType(name = "nonNegativeInteger")
                    protected BigInteger queryStart;
                    @XmlAttribute(name = "query_end", required = true)
                    @XmlSchemaType(name = "nonNegativeInteger")
                    protected BigInteger queryEnd;

                    /**
                     * Gets the value of the descriptor property.
                     *
                     * <p>
                     * This accessor method returns a reference to the live list, not a snapshot. Therefore any
                     * modification you make to the returned list will be present inside the JAXB object. This is why
                     * there is not a <CODE>set</CODE> method for the descriptor property.
                     *
                     * <p>
                     * For example, to add a new item, do as follows:
                     * <pre>
                     *    getDescriptor().add(newItem);
                     * </pre>
                     *
                     *
                     * <p>
                     * Objects of the following type(s) are allowed in the list
                     * {@link Dnaseq.Aaseq.Simsearch.Simhit.Simspan.Descriptor }
                     *
                     *
                     */
                    public List<Dnaseq.Descriptor> getDescriptor() {
                        if (descriptor == null) {
                            descriptor = new ArrayList<Dnaseq.Descriptor>();
                        }
                        return this.descriptor;
                    }

                    /**
                     * Gets the value of the queryStart property.
                     *
                     * @return possible object is {@link BigInteger }
                     *
                     */
                    public BigInteger getQueryStart() {
                        return queryStart;
                    }

                    /**
                     * Sets the value of the queryStart property.
                     *
                     * @param value allowed object is {@link BigInteger }
                     *
                     */
                    public void setQueryStart(BigInteger value) {
                        this.queryStart = value;
                    }

                    /**
                     * Gets the value of the queryEnd property.
                     *
                     * @return possible object is {@link BigInteger }
                     *
                     */
                    public BigInteger getQueryEnd() {
                        return queryEnd;
                    }

                    /**
                     * Sets the value of the queryEnd property.
                     *
                     * @param value allowed object is {@link BigInteger }
                     *
                     */
                    public void setQueryEnd(BigInteger value) {
                        this.queryEnd = value;
                    }

                }

            }

        }

    }

    /**
     * In ProtAnnot, the content of the descriptor tags will appear in the Property Sheet table when users click Glyphs
     * representing mRNAs, simhits, etc. Also, each mRNA needs a single descriptor element with type equal to
     * "protein_product_id". In addition, there should be one <aaseq> element in the file with "id" attribute equal to
     * that protein_product_id descriptor's value. This is how ProtAnnot links protein annotations (conserved motifs) to
     * the mRNA transcripts that encode them.
     *
     *
     * <p>
     * Java class for anonymous complex type.
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "descriptor",
        "exon",
        "cds"
    })
    public static class MRNA {

        @XmlElement(required = true)
        protected List<Dnaseq.Descriptor> descriptor;
        @XmlElement(required = true)
        protected List<Dnaseq.MRNA.Exon> exon;
        @XmlElement(required = true)
        protected Dnaseq.MRNA.Cds cds;
        @XmlAttribute(name = "start", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger start;
        @XmlAttribute(name = "end", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger end;
        @XmlAttribute(name = "strand", required = true)
        protected String strand;

        /**
         * Gets the value of the descriptor property.
         *
         * <p>
         * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
         * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
         * method for the descriptor property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDescriptor().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list {@link Dnaseq.MRNA.Descriptor }
         *
         *
         */
        public List<Dnaseq.Descriptor> getDescriptor() {
            if (descriptor == null) {
                descriptor = new ArrayList<Dnaseq.Descriptor>();
            }
            return this.descriptor;
        }

        public void addDescriptor(String type, String value) {
            Descriptor desc = new Descriptor();
            desc.setType(type);
            desc.setValue(value);
            getDescriptor().add(desc);
        }

        /**
         * Gets the value of the exon property.
         *
         * <p>
         * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
         * make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE>
         * method for the exon property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getExon().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list {@link Dnaseq.MRNA.Exon }
         *
         *
         */
        public List<Dnaseq.MRNA.Exon> getExon() {
            if (exon == null) {
                exon = new ArrayList<Dnaseq.MRNA.Exon>();
            }
            return this.exon;
        }

        /**
         * Gets the value of the cds property.
         *
         * @return possible object is {@link Dnaseq.MRNA.Cds }
         *
         */
        public Dnaseq.MRNA.Cds getCds() {
            return cds;
        }

        /**
         * Sets the value of the cds property.
         *
         * @param value allowed object is {@link Dnaseq.MRNA.Cds }
         *
         */
        public void setCds(Dnaseq.MRNA.Cds value) {
            this.cds = value;
        }

        /**
         * Gets the value of the start property.
         *
         * @return possible object is {@link BigInteger }
         *
         */
        public BigInteger getStart() {
            return start;
        }

        /**
         * Sets the value of the start property.
         *
         * @param value allowed object is {@link BigInteger }
         *
         */
        public void setStart(BigInteger value) {
            this.start = value;
        }

        /**
         * Gets the value of the end property.
         *
         * @return possible object is {@link BigInteger }
         *
         */
        public BigInteger getEnd() {
            return end;
        }

        /**
         * Sets the value of the end property.
         *
         * @param value allowed object is {@link BigInteger }
         *
         */
        public void setEnd(BigInteger value) {
            this.end = value;
        }

        /**
         * Gets the value of the strand property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getStrand() {
            return strand;
        }

        /**
         * Sets the value of the strand property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setStrand(String value) {
            this.strand = value;
        }

        /**
         * Represents the region bounded by the start codon (typically an ATG) and the termination codon (typically TAA,
         * TAG, or TGA) inclusive. Thus, for a plus strand feature, the CDS stop indicates the end of the termination
         * codon and the CDS start indicates the position of the first translated base, e.g., the A in the ATG initial
         * methionine. The reason we include the stop codon in the bounds of the CDS is mainly for convenience. Bed
         * files obtained from UCSC include the stop codon in the region bounded by the bed "thickStart" and "thickEnd"
         * fields.
         *
         *
         * <p>
         * Java class for anonymous complex type.
         *
         * <p>
         * The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
         *       &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Cds {

            @XmlAttribute(name = "start", required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger start;
            @XmlAttribute(name = "end", required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger end;
            @XmlAttribute(name = "transstart")
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger transstart;
            @XmlAttribute(name = "transstop")
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger transstop;

            /**
             * Gets the value of the start property.
             *
             * @return possible object is {@link BigInteger }
             *
             */
            public BigInteger getStart() {
                return start;
            }

            /**
             * Sets the value of the start property.
             *
             * @param value allowed object is {@link BigInteger }
             *
             */
            public void setStart(BigInteger value) {
                this.start = value;
            }

            /**
             * Gets the value of the end property.
             *
             * @return possible object is {@link BigInteger }
             *
             */
            public BigInteger getEnd() {
                return end;
            }

            /**
             * Sets the value of the end property.
             *
             * @param value allowed object is {@link BigInteger }
             *
             */
            public void setEnd(BigInteger value) {
                this.end = value;
            }

            /**
             * Gets the value of the transstart property.
             *
             * @return possible object is {@link BigInteger }
             *
             */
            public BigInteger getTransstart() {
                return transstart;
            }

            /**
             * Sets the value of the transstart property.
             *
             * @param value allowed object is {@link BigInteger }
             *
             */
            public void setTransstart(BigInteger value) {
                this.transstart = value;
            }

            /**
             * Gets the value of the transstop property.
             *
             * @return possible object is {@link BigInteger }
             *
             */
            public BigInteger getTransstop() {
                return transstop;
            }

            /**
             * Sets the value of the transstop property.
             *
             * @param value allowed object is {@link BigInteger }
             *
             */
            public void setTransstop(BigInteger value) {
                this.transstop = value;
            }

        }

        /**
         * <p>
         * Java class for anonymous complex type.
         *
         * <p>
         * The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="descriptor" maxOccurs="unbounded">
         *           &lt;complexType>
         *             &lt;simpleContent>
         *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
         *                 &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *               &lt;/extension>
         *             &lt;/simpleContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *       &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
         *       &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "descriptor"
        })
        public static class Exon {

            @XmlElement(required = true)
            protected List<Dnaseq.Descriptor> descriptor;
            @XmlAttribute(name = "start", required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger start;
            @XmlAttribute(name = "end", required = true)
            @XmlSchemaType(name = "nonNegativeInteger")
            protected BigInteger end;

            /**
             * Gets the value of the descriptor property.
             *
             * <p>
             * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you
             * make to the returned list will be present inside the JAXB object. This is why there is not a
             * <CODE>set</CODE> method for the descriptor property.
             *
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getDescriptor().add(newItem);
             * </pre>
             *
             *
             * <p>
             * Objects of the following type(s) are allowed in the list {@link Dnaseq.MRNA.Exon.Descriptor }
             *
             *
             */
            public List<Dnaseq.Descriptor> getDescriptor() {
                if (descriptor == null) {
                    descriptor = new ArrayList<Dnaseq.Descriptor>();
                }
                return this.descriptor;
            }

            public void addDescriptor(String type, String value) {
                Descriptor desc = new Descriptor();
                desc.setType(type);
                desc.setValue(value);
                getDescriptor().add(desc);
            }

            /**
             * Gets the value of the start property.
             *
             * @return possible object is {@link BigInteger }
             *
             */
            public BigInteger getStart() {
                return start;
            }

            /**
             * Sets the value of the start property.
             *
             * @param value allowed object is {@link BigInteger }
             *
             */
            public void setStart(BigInteger value) {
                this.start = value;
            }

            /**
             * Gets the value of the end property.
             *
             * @return possible object is {@link BigInteger }
             *
             */
            public BigInteger getEnd() {
                return end;
            }

            /**
             * Sets the value of the end property.
             *
             * @param value allowed object is {@link BigInteger }
             *
             */
            public void setEnd(BigInteger value) {
                this.end = value;
            }

        }

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *       &lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="0" />
     *       &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Residues {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "start")
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger start;
        @XmlAttribute(name = "end", required = true)
        @XmlSchemaType(name = "nonNegativeInteger")
        protected BigInteger end;

        /**
         * Gets the value of the value property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the start property.
         *
         * @return possible object is {@link BigInteger }
         *
         */
        public BigInteger getStart() {
            if (start == null) {
                return new BigInteger("0");
            } else {
                return start;
            }
        }

        /**
         * Sets the value of the start property.
         *
         * @param value allowed object is {@link BigInteger }
         *
         */
        public void setStart(BigInteger value) {
            this.start = value;
        }

        /**
         * Gets the value of the end property.
         *
         * @return possible object is {@link BigInteger }
         *
         */
        public BigInteger getEnd() {
            return end;
        }

        /**
         * Sets the value of the end property.
         *
         * @param value allowed object is {@link BigInteger }
         *
         */
        public void setEnd(BigInteger value) {
            this.end = value;
        }

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     *
     * <p>
     * The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Descriptor {

        @XmlValue
        protected String value;
        @XmlAttribute(name = "type", required = true)
        protected String type;

        /**
         * Gets the value of the value property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the type property.
         *
         * @return possible object is {@link String }
         *
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         *
         * @param value allowed object is {@link String }
         *
         */
        public void setType(String value) {
            this.type = value;
        }

    }

}
