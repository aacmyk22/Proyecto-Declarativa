// src/main/java/com/calculo/PanelGrafica.java
package com.calculo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.Arrays;

/**
 * Dibuja f(x) y sombrea el area entre a y b.
 * Arriba del eje x el area va en morado y abajo en rojo, para que se
 * vea por que la integral definida puede dar negativa o cero.
 */
public class PanelGrafica extends JPanel {

    private static final Color EJES      = new Color(148, 163, 184);
    private static final Color CUADRICULA = new Color(241, 245, 249);
    private static final Color CURVA     = new Color(79, 70, 229);
    private static final Color AREA_POS  = new Color(99, 102, 241, 70);
    private static final Color AREA_NEG  = new Color(244, 63, 94, 70);
    private static final Color TEXTO     = new Color(100, 116, 139);

    private static final int MARGEN = 36;

    private double[] ys;              // valores de f(x)
    private double xMin, xMax;        // rango del eje x
    private double yMin, yMax;        // rango del eje y
    private double a, b;              // limites de integracion
    private String fuente;

    private double progreso = 1;      // para la animacion del sombreado (0..1)
    private Timer animacion;
    private int indiceMouse = -1;     // punto donde esta el mouse

    public PanelGrafica(String fuente) {
        this.fuente = fuente;
        setOpaque(false);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (ys == null) return;
                double ancho = getWidth() - 2 * MARGEN;
                int i = (int) Math.round((e.getX() - MARGEN) / ancho * (ys.length - 1));
                indiceMouse = (i >= 0 && i < ys.length) ? i : -1;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                indiceMouse = -1;
                repaint();
            }
        };
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
    }

    /** Recibe los puntos ya calculados por Prolog y arranca la animacion. */
    public void mostrar(double[] ys, double xMin, double xMax, double a, double b) {
        this.ys = ys;
        this.xMin = xMin;
        this.xMax = xMax;
        this.a = Math.min(a, b);
        this.b = Math.max(a, b);
        calcularRangoY();

        progreso = 0;
        if (animacion != null) animacion.stop();
        animacion = new Timer(15, e -> {
            progreso += 0.04;
            if (progreso >= 1) {
                progreso = 1;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        animacion.start();
    }

    public void limpiar() {
        ys = null;
        repaint();
    }

    // El rango en y se saca ignorando los valores mas extremos (2% de cada
    // lado). Si no, funciones como 1/x cerca de 0 aplastan toda la grafica.
    private void calcularRangoY() {
        double[] validos = Arrays.stream(ys).filter(Double::isFinite).sorted().toArray();
        if (validos.length == 0) {
            yMin = -1;
            yMax = 1;
            return;
        }
        int corte = validos.length / 50;
        yMin = Math.min(0, validos[corte]);
        yMax = Math.max(0, validos[validos.length - 1 - corte]);
        if (yMax - yMin < 1e-9) {
            yMax += 1;
            yMin -= 1;
        }
        double extra = (yMax - yMin) * 0.1;
        yMin -= extra;
        yMax += extra;
    }

    private double xDe(int i) {
        return xMin + (xMax - xMin) * i / (ys.length - 1);
    }

    private int px(double x) {
        return (int) Math.round(MARGEN + (x - xMin) / (xMax - xMin) * (getWidth() - 2 * MARGEN));
    }

    private int py(double y) {
        return (int) Math.round(getHeight() - MARGEN - (y - yMin) / (yMax - yMin) * (getHeight() - 2 * MARGEN));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font(fuente, Font.PLAIN, 11));

        if (ys == null) {
            g2.setColor(TEXTO);
            g2.setFont(new Font(fuente, Font.PLAIN, 13));
            String msg = "Escribe una función y presiona \"Calcular área\"";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - w) / 2, getHeight() / 2);
            g2.dispose();
            return;
        }

        dibujarCuadricula(g2);
        dibujarArea(g2);
        dibujarCurva(g2);
        dibujarLimites(g2);
        dibujarPuntoMouse(g2);
        g2.dispose();
    }

    private void dibujarCuadricula(Graphics2D g2) {
        int lineas = 8;
        for (int i = 0; i <= lineas; i++) {
            double x = xMin + (xMax - xMin) * i / lineas;
            double y = yMin + (yMax - yMin) * i / lineas;

            g2.setColor(CUADRICULA);
            g2.drawLine(px(x), MARGEN, px(x), getHeight() - MARGEN);
            g2.drawLine(MARGEN, py(y), getWidth() - MARGEN, py(y));

            g2.setColor(TEXTO);
            String tx = formato(x);
            g2.drawString(tx, px(x) - g2.getFontMetrics().stringWidth(tx) / 2, getHeight() - MARGEN + 16);
            String ty = formato(y);
            g2.drawString(ty, MARGEN - 6 - g2.getFontMetrics().stringWidth(ty), py(y) + 4);
        }

        // ejes x e y (solo si se ven en pantalla)
        g2.setColor(EJES);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawLine(MARGEN, py(0), getWidth() - MARGEN, py(0));
        if (xMin < 0 && xMax > 0) {
            g2.drawLine(px(0), MARGEN, px(0), getHeight() - MARGEN);
        }
    }

    private void dibujarArea(Graphics2D g2) {
        double limite = a + (b - a) * progreso;   // hasta donde va la animacion
        int cero = py(0);

        for (int i = 0; i < ys.length - 1; i++) {
            double x1 = xDe(i), x2 = xDe(i + 1);
            double y1 = ys[i], y2 = ys[i + 1];
            if (x1 < a || x2 > limite) continue;
            if (!Double.isFinite(y1) || !Double.isFinite(y2)) continue;

            int[] xs = {px(x1), px(x1), px(x2), px(x2)};
            int[] yp = {cero, limitar(py(y1)), limitar(py(y2)), cero};
            g2.setColor((y1 + y2) / 2 >= 0 ? AREA_POS : AREA_NEG);
            g2.fillPolygon(xs, yp, 4);
        }
    }

    private void dibujarCurva(Graphics2D g2) {
        Path2D camino = new Path2D.Double();
        boolean levantado = true;
        double rango = yMax - yMin;

        for (int i = 0; i < ys.length; i++) {
            double y = ys[i];
            // se "levanta el lapiz" si no existe el punto o si hay un salto
            // muy grande (asintotas como en 1/x o tan(x))
            boolean salto = i > 0 && Double.isFinite(ys[i - 1]) && Math.abs(y - ys[i - 1]) > rango * 2;
            if (!Double.isFinite(y) || salto) {
                levantado = true;
                if (!Double.isFinite(y)) continue;
            }
            if (levantado) {
                camino.moveTo(px(xDe(i)), limitar(py(y)));
                levantado = false;
            } else {
                camino.lineTo(px(xDe(i)), limitar(py(y)));
            }
        }

        Shape antes = g2.getClip();
        g2.clipRect(MARGEN, MARGEN, getWidth() - 2 * MARGEN, getHeight() - 2 * MARGEN);
        g2.setColor(CURVA);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(camino);
        g2.setClip(antes);
    }

    private void dibujarLimites(Graphics2D g2) {
        g2.setColor(TEXTO);
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 4}, 0));
        g2.drawLine(px(a), MARGEN, px(a), getHeight() - MARGEN);
        g2.drawLine(px(b), MARGEN, px(b), getHeight() - MARGEN);

        g2.setFont(new Font(fuente, Font.BOLD, 12));
        g2.drawString("a", px(a) - 3, MARGEN - 6);
        g2.drawString("b", px(b) - 3, MARGEN - 6);
    }

    private void dibujarPuntoMouse(Graphics2D g2) {
        if (indiceMouse < 0 || !Double.isFinite(ys[indiceMouse])) return;

        double x = xDe(indiceMouse);
        double y = ys[indiceMouse];
        int cx = px(x), cy = limitar(py(y));

        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 6, cy - 6, 12, 12);
        g2.setColor(CURVA);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(cx - 6, cy - 6, 12, 12);

        String texto = "x = " + formato(x) + "   f(x) = " + formato(y);
        g2.setFont(new Font(fuente, Font.BOLD, 12));
        int w = g2.getFontMetrics().stringWidth(texto) + 16;
        int tx = Math.min(cx + 10, getWidth() - w - 4);
        int ty = Math.max(cy - 34, 4);
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fillRoundRect(tx, ty, w, 24, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawString(texto, tx + 8, ty + 16);
    }

    // evita coordenadas gigantes cuando la funcion se dispara
    private int limitar(int y) {
        return Math.max(-2000, Math.min(getHeight() + 2000, y));
    }

    private static String formato(double v) {
        if (Math.abs(v) < 1e-6) return "0";   // evita cosas como 7e-09
        if (Math.abs(v) >= 1000 || Math.abs(v) < 0.01) return String.format("%.1e", v);
        return String.format("%.2f", v).replaceAll("0+$", "").replaceAll("[.,]$", "");
    }
}
