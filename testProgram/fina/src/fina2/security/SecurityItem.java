package fina2.security;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The basic security item. Contains data about security options for users and
 * roles.
 */
public class SecurityItem implements Serializable {

    /**
     * Status for item 
     */
    public enum Status {
        YES, NO, YES_READONLY, PARTIAL
    }

    /** Item id */
    private int id = Integer.MIN_VALUE;

    /** Item description */
    private String text = null;

    /** Review permission */
    private Status review = null;

    /** Amend permission */
    private Status amend = null;

    /** Contains properties */
    HashMap<String, Object> properties = new HashMap<String, Object>();

    /** Creates the instance of class */
    public SecurityItem(int id, String text) {
        this(id, text, Status.NO);
    }

    /** Creates the instance of class */
    public SecurityItem(int id, String text, Status review) {
        this(id, text, review, Status.NO);
    }

    /** Creates the instance of class */
    public SecurityItem(int id, String text, Status review, Status amend) {

        this.id = id;
        this.text = text;

        setReview(review);
        setAmend(amend);
    }

    /** Returns id */
    public int getId() {
        return id;
    }

    /** Returns text */
    public String getText() {
        return text;
    }

    /** Returns review status */
    public Status getReview() {
        return review;
    }

    /** Returns amend status */
    public Status getAmend() {
        return amend;
    }

    /** Sets review status */
    public void setReview(boolean value) {
        Status status = (value == true) ? Status.YES : Status.NO;
        setReview(status);
    }

    /** Sets review status */
    public void setReview(Status value) {

        review = value;

        if (review == Status.NO) {
            /* If REVIEW is NO, then AMEND is also NO */
            amend = Status.NO;
        }
    }

    /** Sets amend status */
    public void setAmend(boolean value) {
        Status status = (value == true) ? Status.YES : Status.NO;
        setAmend(status);
    }

    /** Sets amend status */
    public void setAmend(Status value) {

        amend = value;

        /* Changing AMEND can affect REVIEW */
        if ((amend == Status.YES) && (review != Status.YES_READONLY)) {
            /* If AMEND is YES, then REVIEW is also YES */
            review = Status.YES;
        } else if (amend == Status.YES_READONLY) {
            /* If AMEND is YES_READONLY, then REVIEW is also YES_READONLY */
            review = Status.YES_READONLY;
        }
    }

    /** Sets a value of property with given name */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /** Returns a value of property with given name */
    public Object getProperty(String name) {
        return properties.get(name);
    }
}
