
package Clases;

public class Seccion {

    private String nombre;
    private String codigo;
    private String jefe;
    private String laboratorio;

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getJefe() {
        return jefe;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setJefe(String jefe) {
        this.jefe = jefe;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    @Override
    public String toString() {
        return "Seccion{" + "nombre=" + nombre + ", codigo=" + codigo + ", jefe=" + jefe + ", laboratorio=" + laboratorio + '}';
    }

    
}
