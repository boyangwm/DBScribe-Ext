package Clases;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Solicitud extends org.apache.struts.action.ActionForm {

    private long codigo;
    private String solicitante;
    private String laboratorioA;
    private String seccionA;
    private String laboratorioB;
    private String seccionB;
    private String retirante;
    private String motivo;
    private String observacionA;
    private String observacionB;
    private boolean respuestaA;
    private boolean respuestaB;
    private boolean terminado;
    private boolean enviado;
    private String fechaEnvio;
    private String fechaRespA;
    private String fechaRespB;
    private ArrayList<Producto> productos;

    public long getCodigo() {
        return codigo;
    }

    public void setCodigo(long codigo) {
        this.codigo = codigo;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getLaboratorioA() {
        return laboratorioA;
    }

    public void setLaboratorioA(String laboratorioA) {
        this.laboratorioA = laboratorioA;
    }

    public String getSeccionA() {
        return seccionA;
    }

    public void setSeccionA(String seccionA) {
        this.seccionA = seccionA;
    }

    public String getLaboratorioB() {
        return laboratorioB;
    }

    public void setLaboratorioB(String laboratorioB) {
        this.laboratorioB = laboratorioB;
    }

    public String getSeccionB() {
        return seccionB;
    }

    public void setSeccionB(String seccionB) {
        this.seccionB = seccionB;
    }

    public String getRetirante() {
        return retirante;
    }

    public void setRetirante(String retirante) {
        this.retirante = retirante;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacionA() {
        return observacionA;
    }

    public void setObservacionA(String observacionA) {
        this.observacionA = observacionA;
    }

    public String getObservacionB() {
        return observacionB;
    }

    public void setObservacionB(String observacionB) {
        this.observacionB = observacionB;
    }

    public boolean isRespuestaA() {
        return respuestaA;
    }

    public void setRespuestaA(boolean respuestaA) {
        this.respuestaA = respuestaA;
    }

    public boolean isRespuestaB() {
        return respuestaB;
    }

    public void setRespuestaB(boolean respuestaB) {
        this.respuestaB = respuestaB;
    }

    public boolean isTerminado() {
        return terminado;
    }

    public void setTerminado(boolean terminado) {
        this.terminado = terminado;
    }

    public boolean isEnviado() {
        return enviado;
    }

    public void setEnviado(boolean enviado) {
        this.enviado = enviado;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getFechaRespA() {
        return fechaRespA;
    }

    public void setFechaRespA(String fechaRespA) {
        this.fechaRespA = fechaRespA;
    }

    public String getFechaRespB() {
        return fechaRespB;
    }

    public void setFechaRespB(String fechaRespB) {
        this.fechaRespB = fechaRespB;
    }

    public ArrayList<Producto> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<Producto> productos) {
        this.productos = productos;
    }

    @Override
    public String toString() {
        return "Solicitud{" + "codigo=" + codigo + ", solicitante=" + solicitante + ", laboratorioA=" + laboratorioA + ", seccionA=" + seccionA + ", laboratorioB=" + laboratorioB + ", seccionB=" + seccionB + ", retirante=" + retirante + ", motivo=" + motivo + ", observacionA=" + observacionA + ", observacionB=" + observacionB + ", respuestaA=" + respuestaA + ", respuestaB=" + respuestaB + ", terminado=" + terminado + ", enviado=" + enviado + ", fechaEnvio=" + fechaEnvio + ", fechaRespA=" + fechaRespA + ", fechaRespB=" + fechaRespB + ", productos=" + productos + '}';
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

}
