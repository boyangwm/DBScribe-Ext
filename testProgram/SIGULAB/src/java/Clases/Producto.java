/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Clases;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/**
 *
 * @author Esteban
 */
public class Producto extends org.apache.struts.action.ActionForm {

    private Long codigoProd;
    private Long codigoSol;
    private Float cantidadSol;
    private Float cantidadExistente;
    private String unidad;
    private String tipo;

    public Long getCodigoProd() {
        return codigoProd;
    }

    public void setCodigoProd(Long codigoProd) {
        this.codigoProd = codigoProd;
    }

    public Long getCodigoSol() {
        return codigoSol;
    }

    public void setCodigoSol(Long codigoSol) {
        this.codigoSol = codigoSol;
    }

    public Float getCantidadSol() {
        return cantidadSol;
    }

    public void setCantidadSol(Float cantidadSol) {
        this.cantidadSol = cantidadSol;
    }

    public Float getCantidadExistente() {
        return cantidadExistente;
    }

    public void setCantidadExistente(Float cantidadExistente) {
        this.cantidadExistente = cantidadExistente;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }


    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (this.cantidadSol > this.cantidadExistente){
            errors.add("cantidadExcede", new ActionMessage("error.cantidad.excede"));
        }

        return errors;
    }

}
