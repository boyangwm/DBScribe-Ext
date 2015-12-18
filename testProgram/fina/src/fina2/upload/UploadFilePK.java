package fina2.upload;

import java.io.Serializable;

public class UploadFilePK implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	
	public UploadFilePK(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
        if (o instanceof UploadFilePK) {
            UploadFilePK otherKey = (UploadFilePK) o;
            return id == otherKey.getId();
        } else
            return false;
    }
}
