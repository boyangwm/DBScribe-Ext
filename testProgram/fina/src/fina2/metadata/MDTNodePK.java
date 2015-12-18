/*
 * MDTNodePK.java
 *
 * Created on October 19, 2001, 11:02 AM
 */

package fina2.metadata;

import java.io.Serializable;

/**
 * 
 * @author David Shalamberidze
 * @version
 */

public class MDTNodePK implements Serializable {

	private long id;

	public MDTNodePK(long id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		if (o instanceof MDTNodePK) {
			MDTNodePK otherKey = (MDTNodePK) o;
			return (id == otherKey.getId());
		} else
			return false;
	}

	public long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

}
