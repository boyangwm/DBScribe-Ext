/*
 * BankAccesPermissions.java
 *
 * Created on August 7, 2002, 6:11 AM
 */

package fina2.bank;

import java.io.Serializable;
import java.util.Hashtable;

/**
 *
 * @author  vasop
 */
public class BankAccess implements Serializable {

    private Hashtable amendBanks;
    private Hashtable reviewBanks;

    /** Creates a new instance of BankAccesPermissions */
    public BankAccess() {
        amendBanks = new Hashtable();
        reviewBanks = new Hashtable();
    }

    public void initAmendBanks(Hashtable amendBanks) {
        this.amendBanks = amendBanks;
    }

    public void putAmendBanks(Integer key, Integer value) {
        amendBanks.put(key, value);
    }

    public Object getkAmendBanks(Integer key) {
        return amendBanks.get(key);
    }

    public boolean getAmendKey(int pk) {
        return amendBanks.containsKey(new Integer(pk));
    }

    public int getAmendSize() {
        return amendBanks.size();
    }

    public void removeObject(int pk) {
        amendBanks.remove(new Integer(pk));
        reviewBanks.remove(new Integer(pk));
    }

    public void initReviewBanks(Hashtable reviewBanks) {
        this.reviewBanks = reviewBanks;
    }

    public void putReviewBanks(Integer key, Integer value) {
        reviewBanks.put(key, value);
    }

    public Object getReviewBanks(Integer key) {
        return reviewBanks.get(key);
    }

    public boolean getReviewKey(int pk) {
        return reviewBanks.containsKey(new Integer(pk));
    }

    public int getReviewSize() {
        return reviewBanks.size();
    }

}
