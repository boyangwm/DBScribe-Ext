package fina2.regions;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RegionStructureNodePK implements Serializable {
	private long id;

	public RegionStructureNodePK(long id) {
		this.id = id;
	}

	public boolean equals(Object o) {
		if (o instanceof RegionStructureNodePK) {
			RegionStructureNodePK otherKey = (RegionStructureNodePK) o;
			return (id == otherKey.getId());
		} else
			return false;
	}

	public int hashCode() {
		return (int) id;
	}

	public long getId() {
		return id;
	}

}
