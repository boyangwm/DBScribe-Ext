package fina2.security.users;

import javax.swing.JComponent;

import fina2.FinaTypeException;

/**
 * AbstractView
 */
public abstract class AbstractView<K> extends JComponent {

    /**
     * Working mode with data
     */
    public enum ModeType {
        CREATE, AMEND, REVIEW
    }

    /** Key data */
    private K key = null;

    /** The mode type of instance */
    private ModeType modeType = null;

    /**	Creates the instance of the class */
    protected AbstractView(ModeType modeType, K key) {
        checkArguments(modeType, key);

        this.modeType = modeType;
        this.key = key;
    }

    /**
     * Throws IllegalArgumentException if actionMode is AMEND/REVIEW,
     * and the key is null.
     */
    private void checkArguments(ModeType modeType, K key)
            throws IllegalArgumentException {

        if ((modeType != ModeType.CREATE) && (key == null)) {
            String error = "For AMEND and REVIEW actions the key must be specified";
            throw new IllegalArgumentException(error);
        }
    }

    /** Returns the mode type of the instance */
    public final ModeType getModeType() {
        return modeType;
    }

    /** Returns the key data */
    public final K getKey() {
        return key;
    }

    /** Sets the key data */
    public final void setKey(K key) {
        this.key = key;
    }

    /** Checks input data */
    public abstract void check() throws FinaTypeException;

    /** Saves the view data */
    public abstract void save() throws FinaTypeException, Exception;

}
