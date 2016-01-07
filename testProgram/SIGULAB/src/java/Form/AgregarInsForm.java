/*
    Documento   : -
    Creado por  : Acro Systems
                  Carla Barazarte
                  Samantha Campisi
                  Carlos Cruz
                  Alejandro Garbi
                  Ramón Marquez
                  Esteban Oliveros
 */
// Paquetes
package Form;

// Importaciones
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class AgregarInsForm extends org.apache.struts.action.ActionForm {

    private String nombre;
    private String serial;
    private String estado;
    private String codsec;
    private String codlab;

    public String getNombre() {
        return nombre;
    }

    public String getSerial() {
        return serial;
    }

    public String getEstado() {
        return estado;
    }

    public String getCodsec() {
        return codsec;
    }

    public String getCodlab() {
        return codlab;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setCodsec(String codsec) {
        this.codsec = codsec;
    }

    public void setCodlab(String codlab) {
        this.codlab = codlab;
    }

    @Override
    public String toString() {
        return "AgregarInsForm{" + "nombre=" + nombre + ", serial=" + serial + ", estado=" + estado + ", codsec=" + codsec + ", codlab=" + codlab + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if ((this.nombre == null) || (this.nombre.length() < 1)){
            errors.add("nombreIns", new ActionMessage("error.form.insumo.agregar.nombre"));
        }

        return errors;
    }
}