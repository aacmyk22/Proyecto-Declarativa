package com.calculo;

import org.jpl7.Atom;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;

import java.io.File;
import java.net.URL;
import java.util.Map;

public class MotorProlog {

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

    private String buscarArchivo() {
        URL url = getClass().getResource("/integrales.pl");
        if (url != null && "file".equals(url.getProtocol())) {
            try {
                return new File(url.toURI()).getAbsolutePath().replace('\\', '/');
            } catch (Exception ignored) {
            }
        }
        return "src/main/resources/integrales.pl";
    }

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
            throw new IllegalArgumentException("La expresión tiene un error de sintaxis.");
        }
    }

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
        }
        return ys;
    }

    public Double evaluarNumero(String texto) {
        Term[] params = {new Atom(texto)};
        String consulta = "catch((term_string(T, ?), V is T), _, fail)";
        Map<String, Term> sol = new Query(consulta, params).oneSolution();
        if (sol == null) return null;
        return sol.get("V").doubleValue();
    }
}
