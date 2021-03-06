/**
 * 
 */
package pl.psnc.dl.wf4ever.portal.myexpimport.model;

import java.io.Serializable;
import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Represents complete metadata and data of a myExperiment pack/file/workflow.
 * 
 * For example http://www.myexperiment.org/workflow.xml?id=2648&all_elements=yes
 * 
 * @author Piotr Hołubowicz
 * 
 */
public abstract class BaseResource implements Serializable {

    /** id. */
    private static final long serialVersionUID = -9038815722609845400L;

    /** XML with metadata URI. */
    private URI uri;

    /** Format-agnostic resource URI. */
    private URI resource;

    /** Resource title. */
    private String title;

    /** Resource id in myExperiment. */
    private int id;


    /**
     * Default constructor.
     */
    public BaseResource() {

    }


    @XmlAttribute
    public URI getUri() {
        return uri;
    }


    public void setUri(URI uri) {
        this.uri = uri;
    }


    @XmlAttribute
    public URI getResource() {
        return resource;
    }


    public void setResource(URI resource) {
        this.resource = resource;
    }


    @XmlElement
    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    @XmlElement
    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }
}
