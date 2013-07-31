package pl.psnc.dl.wf4ever.portal.model;

import java.net.URI;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Job status as JSON.
 * 
 * @author piotrekhol
 * 
 */
@XmlRootElement
public class JobStatus {

    /**
     * The job state.
     * 
     * @author piotrekhol
     * 
     */
    public enum State {
        /** The job has started and is running. */
        RUNNING,
        /** The job has finished succesfully. */
        DONE,
        /** The job has been cancelled by the user. */
        CANCELLED,
        /** The resource to be formated is invalid. */
        INVALID_RESOURCE,
        /** There has been an unexpected error during conversion. */
        RUNTIME_ERROR;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        };
    }


    /** workflow URI. */
    private URI resource;

    /** workflow format MIME type. */
    private String format;

    /** RO URI. */
    private URI ro;

    /** job state. */
    private State state;

    /** resources already uploaded. */
    private List<URI> added;

    /** reason for the status, i.e. exception message. */
    private String reason;


    /**
     * Default empty constructor.
     */
    public JobStatus() {

    }


    /**
     * Constructor.
     * 
     * @param resource
     *            workflow URI
     * @param format
     *            workflow format URI
     * @param ro
     *            RO URI
     * @param state
     *            job state
     * @param added
     *            resources added
     * @param reason
     *            reason for the status, i.e. exception message
     */
    public JobStatus(URI resource, String format, URI ro, State state, List<URI> added, String reason) {
        super();
        this.resource = resource;
        this.format = format;
        this.ro = ro;
        this.state = state;
        this.added = added;
        this.reason = reason;
    }


    public URI getResource() {
        return resource;
    }


    public void setResource(URI resource) {
        this.resource = resource;
    }


    public String getFormat() {
        return format;
    }


    public void setFormat(String format) {
        this.format = format;
    }


    public URI getRo() {
        return ro;
    }


    public void setRo(URI ro) {
        this.ro = ro;
    }


    public State getState() {
        return state;
    }


    public void setState(State state) {
        this.state = state;
    }


    public List<URI> getAdded() {
        return added;
    }


    public void setAdded(List<URI> added) {
        this.added = added;
    }


    public String getReason() {
        return reason;
    }


    public void setReason(String reason) {
        this.reason = reason;
    }

}
