package fina2.bank;

public class BankCriterionPK implements java.io.Serializable {

    private int id;

    public BankCriterionPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof BankCriterionPK) {
            BankCriterionPK otherKey = (BankCriterionPK) o;
            return (id == otherKey.getId());
        } else
            return false;
    }

    public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

}
