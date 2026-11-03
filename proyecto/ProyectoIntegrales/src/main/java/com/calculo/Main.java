// src/main/java/com/calculo/Main.java
package com.calculo;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                InterfazGrafica app = new InterfazGrafica();
                app.setVisible(true);
            } catch (Throwable e) {
                // Lo mas comun: SWI-Prolog no esta instalado o no esta en el PATH
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "No se pudo iniciar Prolog.\n\n"
                                + "Revisa que SWI-Prolog esté instalado y que su carpeta bin\n"
                                + "(ej. C:\\Program Files\\swipl\\bin) esté en el PATH.\n\n"
                                + "Detalle: " + e,
                        "Error al iniciar", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
