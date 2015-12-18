package fina2.returns;

import java.io.Serializable;

public class ReturnVersion implements Serializable {

    int id;

    int sequence;

    String code;

    String description;

    int descStrId;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public int getDescStrId() {
        return descStrId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescStrId(int descStrId) {
        this.descStrId = descStrId;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
