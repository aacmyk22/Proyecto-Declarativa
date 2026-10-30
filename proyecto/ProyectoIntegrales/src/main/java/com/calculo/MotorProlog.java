// src/main/java/com/calculo/MotorProlog.java
package com.calculo;

import org.jpl7.Query;
import org.jpl7.Term;
import java.util.Map;

public class MotorProlog {
    public MotorProlog() {
        // Cargar el archivo .pl
        String t1 = "consult('src/main/resources/integrales.pl')";
        Query q1 = new Query(t1);
        if (!q1.hasSolution()) {
            System.out.println("Error al cargar la base de conocimiento Prolog.");
        }
    }

    public String[] resolverIntegralYArea(String expresion, double limInf, double limSup) {
        // Formato de la consulta: calcular_area(Expresion, x, LimInf, LimSup, IntExpr, Area).
        String consulta = String.format("calcular_area(%s, x, %f, %f, IntExpr, Area)", expresion, limInf, limSup);
        Query query = new Query(consulta);

        if (query.hasSolution()) {
            Map<String, Term> solucion = query.oneSolution();
            String expresionIntegrada = solucion.get("IntExpr").toString();
            String area = solucion.get("Area").toString();
            return new String[]{expresionIntegrada, area};
        } else {
            return new String[]{"No soportada/Error de sintaxis", "N/A"};
        }
    }
}