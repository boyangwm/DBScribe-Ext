package Clases;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

public class ItemInsumo extends org.apache.struts.action.ActionForm {

    private Long id;
    private Long insumo;
    private Integer numero;
    private String serial;
    private String estado;
    private Boolean visible;
    private Boolean existe;
    private String observacion;
    private String fechaMod;
    private String fechaAgr;
    private String fechaEli;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInsumo() {
        return insumo;
    }

    public void setInsumo(Long insumo) {
        this.insumo = insumo;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean isExiste() {
        return existe;
    }

    public void setExiste(Boolean existe) {
        this.existe = existe;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getFechaMod() {
        return fechaMod;
    }

    public void setFechaMod(String fechaMod) {
        this.fechaMod = fechaMod;
    }

    public String getFechaAgr() {
        return fechaAgr;
    }

    public void setFechaAgr(String fechaAgr) {
        this.fechaAgr = fechaAgr;
    }

    public String getFechaEli() {
        return fechaEli;
    }

    public void setFechaEli(String fechaEli) {
        this.fechaEli = fechaEli;
    }

    @Override
    public String toString() {
        return "ItemInsumo{" + "insumo=" + insumo + ", numero=" + numero + ", serial=" + serial + ", estado=" + estado + ", visible=" + visible + ", existe=" + existe + ", observacion=" + observacion + ", fechaMod=" + fechaMod + ", fechaAgr=" + fechaAgr + ", fechaEli=" + fechaEli + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if ((this.numero < 1)){
            errors.add("numeroIns", new ActionMessage("error.numero.requerido"));
        }

        return errors;
    }
}
