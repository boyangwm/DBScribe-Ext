

package Form;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;


public class ConsultaForm extends org.apache.struts.action.ActionForm{

    private String tipo;
    private String codlab;
    private String codsec;
    private Integer codprod;
    private String nombre;

    // Si es un insumo puede tener el siguiente atributo
    private String serial;

    // Si es un material/sustancia puede tener el siguiente atributo
    private String ubicacion;

    public String getTipo() {
        return tipo;
    }

    public String getCodlab() {
        return codlab;
    }

    public String getCodsec() {
        return codsec;
    }

    public Integer getCodprod() {
        return codprod;
    }

    public String getSerial() {
        return serial;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setCodlab(String codlab) {
        this.codlab = codlab;
    }

    public void setCodsec(String codsec) {
        this.codsec = codsec;
    }

    public void setCodprod(Integer codprod) {
        this.codprod = codprod;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "ConsultaForm{" + "tipo=" + tipo + ", codlab=" + codlab + ", codsec=" + codsec + ", codprod=" + codprod + ", nombre=" + nombre + ", serial=" + serial + ", ubicacion=" + ubicacion + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

}
