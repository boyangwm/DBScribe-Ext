
package Clases;

public class Laboratorio {

    private String nombre;
    private String codigo;
    private String jefe;

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getJefe() {
        return jefe;
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

    @Override
    public String toString() {
        return "Laboratorio{" + "nombre=" + nombre + ", codigo=" + codigo + ", jefe=" + jefe + '}';
    }

}
