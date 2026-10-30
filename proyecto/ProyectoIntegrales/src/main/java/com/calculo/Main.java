// src/main/java/com/calculo/Main.java
package com.calculo;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazGrafica app = new InterfazGrafica();
            app.setVisible(true);
        });
    }
}