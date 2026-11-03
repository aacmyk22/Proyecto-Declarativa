// src/main/java/com/calculo/MotorProlog.java
package com.calculo;

import org.jpl7.Atom;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;

import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * Puente entre Java y Prolog (JPL).
 * Toda la "inteligencia" esta en integrales.pl, aqui solo se arman
 * las consultas y se leen los resultados.
 */
public class MotorProlog {

    /** Lo que devuelve Prolog despues de resolver una integral. */
    public record Resultado(String integral, String areaExacta, String areaReal, String pasos) {}

    private final boolean cargado;

    public MotorProlog() {
        String ruta = buscarArchivo();
        cargado = new Query("consult(?)", new Term[]{new Atom(ruta)}).hasSolution();
        if (!cargado) {
            System.out.println("Error al cargar la base de conocimiento Prolog: " + ruta);
        }
    }

    public boolean estaCargado() {
        return cargado;
    }

    // Primero busca el .pl en el classpath (target/classes) y si no lo
    // encuentra usa la ruta relativa de siempre.
    private String buscarArchivo() {
        URL url = getClass().getResource("/integrales.pl");
        if (url != null && "file".equals(url.getProtocol())) {
            try {
                return new File(url.toURI()).getAbsolutePath().replace('\\', '/');
            } catch (Exception ignored) {
                // si falla usamos la ruta de abajo
            }
        }
        return "src/main/resources/integrales.pl";
    }

    /**
     * Integra la expresion y calcula el area entre a y b.
     * Devuelve null si Prolog no tiene una regla para esa expresion.
     */
    public Resultado resolver(String expresion, double a, double b) {
        Term[] params = {new Atom(expresion), new org.jpl7.Float(a), new org.jpl7.Float(b)};
        try {
            Map<String, Term> sol = new Query("resolver(?, ?, ?, I, E, R, P)", params).oneSolution();
            if (sol == null) return null;
            return new Resultado(
                    sol.get("I").name(),
                    sol.get("E").name(),
                    sol.get("R").name(),
                    sol.get("P").name());
        } catch (PrologException e) {
            // casi siempre es un error de sintaxis al leer la expresion
            throw new IllegalArgumentException("La expresión tiene un error de sintaxis.");
        }
    }

    /** Valores de f(x) en n+1 puntos entre desde y hasta (NaN donde no existe). */
    public double[] puntos(String expresion, double desde, double hasta, int n) {
        double[] ys = new double[n + 1];
        java.util.Arrays.fill(ys, Double.NaN);

        Term[] params = {new Atom(expresion), new org.jpl7.Float(desde),
                new org.jpl7.Float(hasta), new org.jpl7.Integer(n)};
        try {
            Map<String, Term> sol = new Query("puntos(?, ?, ?, ?, Ys)", params).oneSolution();
            if (sol == null) return ys;

            Term[] lista = sol.get("Ys").listToTermArray();
            for (int i = 0; i < lista.length && i < ys.length; i++) {
                Term t = lista[i];
                if (t.isFloat() || t.isInteger()) ys[i] = t.doubleValue();
            }
        } catch (PrologException e) {
            // sin grafica, no pasa nada
        }
        return ys;
    }

    /**
     * Convierte un limite escrito por el usuario a numero usando Prolog,
     * asi se puede escribir "pi", "pi/2", "2*e", etc.
     */
    public Double evaluarNumero(String texto) {
        Term[] params = {new Atom(texto)};
        String consulta = "catch((term_string(T, ?), V is T), _, fail)";
        Map<String, Term> sol = new Query(consulta, params).oneSolution();
        if (sol == null) return null;
        return sol.get("V").doubleValue();
    }
}
