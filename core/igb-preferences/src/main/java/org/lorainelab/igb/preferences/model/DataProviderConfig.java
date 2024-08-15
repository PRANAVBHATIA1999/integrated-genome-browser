package org.lorainelab.igb.preferences.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
public class DataProviderConfig {

    @XmlValue
    protected String value;
    @Expose
    @XmlAttribute(name = "name")
    protected String name;
    @Expose
    @XmlAttribute(name = "factoryName")
    protected String factoryName;
    @Expose
    @XmlAttribute(name = "url")
    protected String url;
    @Expose
    @XmlAttribute(name = "loadPriority")
    protected Integer loadPriority;
    @SerializedName("default")
    @Expose
    @XmlAttribute(name = "default")
    protected String _default;
    @Expose
    @XmlAttribute(name = "mirror")
    protected String mirror;
    @Expose
    @XmlAttribute(name = "datasetLinkoutDomainUrl")
    protected String datasetLinkoutDomainUrl;
    /**
     * Default Data Providers should not be edited in the Data Sources table.
     * Only default data providers are read through this class, 
     * so editable should always be set to false.  Before IGBF-1206,
     * editable was consistently set to false essentially by accident. 
     * Now it is explicitly and deliberately the case. 
     */
    @Expose
    @XmlAttribute(name = "isEditable")
    protected boolean editable = false;
    @Expose
    @XmlAttribute(name = "status")
    protected String status;
    @Expose
    @XmlAttribute(name = "defaultDataProviderId")
    protected String defaultDataProviderId;
    
    public String getId() {
        return defaultDataProviderId;
    }

    public void setId(String id) {
        this.defaultDataProviderId = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) {
        this.url = value;
    }

    public Integer getLoadPriority() {
        return loadPriority;
    }

    public void setLoadPriority(Integer value) {
        this.loadPriority = value;
    }

    public String getDefault() {
        return _default;
    }

    public void setDefault(String value) {
        this._default = value;
    }

    public String getMirror() {
        return mirror;
    }

    public String getDatasetLinkoutDomainUrl() {
        return datasetLinkoutDomainUrl;
    }

    public void setDatasetLinkoutDomainUrl(String datasetLinkoutDomainUrl) {
        this.datasetLinkoutDomainUrl = datasetLinkoutDomainUrl;
    }

    public void setMirror(String value) {
        this.mirror = value;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(url).append(loadPriority).append(_default).append(mirror).append(editable).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DataProviderConfig) == false) {
            return false;
        }
        DataProviderConfig rhs = ((DataProviderConfig) other);
        return new EqualsBuilder().append(name, rhs.name).append(url, rhs.url).append(loadPriority, rhs.loadPriority).append(_default, rhs._default).append(mirror, rhs.mirror).append(editable, rhs.editable).isEquals();
    }
}
