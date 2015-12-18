/*
 * PeriodSession.java
 *
 * Created on October 30, 2001, 3:15 AM
 */
package fina2.period;

/**
 *
 * @author  vasop
 */
public class PeriodPK implements java.io.Serializable {

    private int id;

    public PeriodPK(int id) {
        this.id = id;
    }

   

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PeriodPK other = (PeriodPK) obj;
		if (id != other.id)
			return false;
		return true;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

    public int getId() {
        return id;
    }

}
