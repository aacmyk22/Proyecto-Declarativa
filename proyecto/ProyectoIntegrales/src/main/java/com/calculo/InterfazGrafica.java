// src/main/java/com/calculo/InterfazGrafica.java
package com.calculo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.Set;

public class InterfazGrafica extends JFrame {

    // ====== Paleta de colores ======
    private static final Color FONDO          = new Color(241, 245, 249);
    private static final Color TARJETA        = Color.WHITE;
    private static final Color BORDE          = new Color(226, 232, 240);
    private static final Color BORDE_FOCO     = new Color(99, 102, 241);
    private static final Color TEXTO          = new Color(15, 23, 42);
    private static final Color TEXTO_SUAVE    = new Color(100, 116, 139);
    private static final Color PRIMARIO       = new Color(99, 102, 241);
    private static final Color PRIMARIO_HOVER = new Color(79, 70, 229);
    private static final Color GRADIENTE_A    = new Color(79, 70, 229);
    private static final Color GRADIENTE_B    = new Color(139, 92, 246);
    private static final Color CHIP_BG        = new Color(238, 242, 255);
    private static final Color CHIP_HOVER     = new Color(224, 231, 255);
    private static final Color RESULTADO_BG   = new Color(248, 250, 252);
    private static final Color EXITO          = new Color(5, 150, 105);

    // ====== Fuentes (con alternativas según el sistema operativo) ======
    private static final String FUENTE      = elegirFuente("SF Pro Text", "Helvetica Neue", "Segoe UI", "Inter", "SansSerif");
    private static final String FUENTE_MONO = elegirFuente("JetBrains Mono", "Menlo", "Consolas", "Monospaced");

    private final MotorProlog motorProlog;
    private CampoTexto inputExpresion, inputLimInf, inputLimSup;
    private JTextArea textoIntegral;
    private JLabel labelResultadoArea;

    public InterfazGrafica() {
        motorProlog = new MotorProlog();

        setTitle("Sistema Experto de Integrales");
        setSize(620, 760);
        setMinimumSize(new Dimension(560, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);
    }

    // ================== CONSTRUCCIÓN DE LA INTERFAZ ==================

    private JPanel crearEncabezado() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, GRADIENTE_A, getWidth(), getHeight(), GRADIENTE_B));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(26, 30, 26, 30));

        JLabel titulo = new JLabel("∫  Calculadora de Integrales");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FUENTE, Font.BOLD, 26));

        JLabel subtitulo = new JLabel("Calcula la integral y el área bajo la curva de una función");
        subtitulo.setForeground(new Color(255, 255, 255, 200));
        subtitulo.setFont(new Font(FUENTE, Font.PLAIN, 13));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(6));
        textos.add(subtitulo);

        header.add(textos, BorderLayout.CENTER);
        return header;
    }

    private JPanel crearContenido() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO);
        contenedor.setBorder(new EmptyBorder(24, 28, 24, 28));

        PanelRedondeado tarjeta = new PanelRedondeado(20, TARJETA, BORDE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(26, 28, 26, 28));

        // --- Expresión ---
        inputExpresion = new CampoTexto("Ej. x^2, 5*x, sin(x)");
        tarjeta.add(crearCampo("Expresión de la función", inputExpresion));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearEjemplos());
        tarjeta.add(Box.createVerticalStrut(18));

        // --- Límites ---
        inputLimInf = new CampoTexto("0");
        inputLimSup = new CampoTexto("1");
        JPanel limites = new JPanel(new GridLayout(1, 2, 16, 0));
        limites.setOpaque(false);
        limites.add(crearCampo("Límite inferior (a)", inputLimInf));
        limites.add(crearCampo("Límite superior (b)", inputLimSup));
        limites.setAlignmentX(Component.LEFT_ALIGNMENT);
        limites.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        tarjeta.add(limites);
        tarjeta.add(Box.createVerticalStrut(22));

        // --- Botones ---
        BotonRedondeado btnCalcular = new BotonRedondeado("Calcular área", PRIMARIO, PRIMARIO_HOVER, Color.WHITE, 12);
        btnCalcular.setFont(new Font(FUENTE, Font.BOLD, 15));
        btnCalcular.setPreferredSize(new Dimension(100, 46));

        BotonRedondeado btnLimpiar = new BotonRedondeado("Limpiar", CHIP_BG, CHIP_HOVER, PRIMARIO_HOVER, 12);
        btnLimpiar.setFont(new Font(FUENTE, Font.BOLD, 14));
        btnLimpiar.setPreferredSize(new Dimension(110, 46));

        JPanel botones = new JPanel(new BorderLayout(12, 0));
        botones.setOpaque(false);
        botones.add(btnCalcular, BorderLayout.CENTER);
        botones.add(btnLimpiar, BorderLayout.EAST);
        botones.setAlignmentX(Component.LEFT_ALIGNMENT);
        botones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        tarjeta.add(botones);
        tarjeta.add(Box.createVerticalStrut(24));

        // --- Resultados ---
        textoIntegral = new JTextArea("—");
        textoIntegral.setEditable(false);
        textoIntegral.setOpaque(false);
        textoIntegral.setLineWrap(true);
        textoIntegral.setWrapStyleWord(true);
        textoIntegral.setBorder(null);
        textoIntegral.setForeground(TEXTO);
        textoIntegral.setFont(new Font(FUENTE_MONO, Font.BOLD, 16));

        labelResultadoArea = new JLabel("—");
        labelResultadoArea.setFont(new Font(FUENTE, Font.BOLD, 22));
        labelResultadoArea.setForeground(EXITO);

        tarjeta.add(crearResultado("INTEGRAL INDEFINIDA", textoIntegral));
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(crearResultado("ÁREA BAJO LA CURVA", labelResultadoArea));

        contenedor.add(tarjeta, BorderLayout.CENTER);

        // --- Acciones ---
        btnCalcular.addActionListener(e -> calcular());
        btnLimpiar.addActionListener(e -> limpiar());
        inputExpresion.addActionListener(e -> calcular());
        inputLimInf.addActionListener(e -> calcular());
        inputLimSup.addActionListener(e -> calcular());

        return contenedor;
    }

    private JPanel crearCampo(String etiqueta, CampoTexto campo) {
        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font(FUENTE, Font.BOLD, 13));
        label.setForeground(TEXTO);

        campo.setPreferredSize(new Dimension(100, 44));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(label, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        return panel;
    }

    private JPanel crearEjemplos() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fila.setOpaque(false);

        JLabel lbl = new JLabel("Ejemplos:");
        lbl.setFont(new Font(FUENTE, Font.PLAIN, 12));
        lbl.setForeground(TEXTO_SUAVE);
        fila.add(lbl);

        for (String ejemplo : new String[]{"x^2", "5*x", "sin(x)", "cos(x)"}) {
            BotonRedondeado chip = new BotonRedondeado(ejemplo, CHIP_BG, CHIP_HOVER, PRIMARIO_HOVER, 14);
            chip.setFont(new Font(FUENTE_MONO, Font.PLAIN, 12));
            chip.setPreferredSize(new Dimension(Math.max(58, ejemplo.length() * 10 + 24), 28));
            chip.addActionListener(e -> {
                inputExpresion.setText(ejemplo);
                inputExpresion.requestFocusInWindow();
            });
            fila.add(chip);
        }
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        // Alinear con el borde izquierdo de los campos
        fila.setBorder(new EmptyBorder(0, -8, 0, 0));
        return fila;
    }

    private JPanel crearResultado(String titulo, JComponent valor) {
        PanelRedondeado panel = new PanelRedondeado(14, RESULTADO_BG, BORDE);
        panel.setLayout(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font(FUENTE, Font.BOLD, 11));
        lbl.setForeground(TEXTO_SUAVE);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        return panel;
    }

    // ================== LÓGICA ==================

    private void calcular() {
        try {
            String expr = inputExpresion.getText().trim();
            if (expr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese una expresión.",
                        "Error de Entrada", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double limInf = Double.parseDouble(inputLimInf.getText().trim());
            double limSup = Double.parseDouble(inputLimSup.getText().trim());

            String[] resultados = motorProlog.resolverIntegralYArea(expr, limInf, limSup);

            textoIntegral.setText("F(x) = " + resultados[0]);
            labelResultadoArea.setText(resultados[1] + " u²");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese números válidos en los límites.",
                    "Error de Entrada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        inputExpresion.setText("");
        inputLimInf.setText("");
        inputLimSup.setText("");
        textoIntegral.setText("—");
        labelResultadoArea.setText("—");
        inputExpresion.requestFocusInWindow();
    }

    private static String elegirFuente(String... candidatas) {
        Set<String> disponibles = new HashSet<>();
        for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            disponibles.add(f);
        }
        for (String f : candidatas) {
            if (disponibles.contains(f)) return f;
        }
        return candidatas[candidatas.length - 1];
    }

    // ================== COMPONENTES PERSONALIZADOS ==================

    /** Panel con esquinas redondeadas y borde fino. */
    private static class PanelRedondeado extends JPanel {
        private final int arco;
        private final Color fondo, borde;

        PanelRedondeado(int arco, Color fondo, Color borde) {
            this.arco = arco;
            this.fondo = fondo;
            this.borde = borde;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arco, arco));
            g2.setColor(borde);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arco, arco));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Botón plano con esquinas redondeadas y efecto hover. */
    private static class BotonRedondeado extends JButton {
        private final Color normal, hover, textoColor;
        private final int arco;
        private boolean encima = false;

        BotonRedondeado(String texto, Color normal, Color hover, Color textoColor, int arco) {
            super(texto);
            this.normal = normal;
            this.hover = hover;
            this.textoColor = textoColor;
            this.arco = arco;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { encima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color c = getModel().isPressed() ? hover.darker() : (encima ? hover : normal);
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arco * 2, arco * 2));

            g2.setColor(textoColor);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /** Campo de texto redondeado con placeholder y resaltado al enfocar. */
    private static class CampoTexto extends JTextField {
        private final String placeholder;
        private boolean enfocado = false;

        CampoTexto(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setFont(new Font(FUENTE_MONO, Font.PLAIN, 15));
            setForeground(TEXTO);
            setCaretColor(PRIMARIO);
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { enfocado = true; repaint(); }
                @Override public void focusLost(FocusEvent e)   { enfocado = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.setColor(Color.WHITE);
            g2.fill(forma);
            g2.setColor(enfocado ? BORDE_FOCO : BORDE);
            g2.setStroke(new BasicStroke(enfocado ? 2f : 1.2f));
            g2.draw(forma);
            g2.dispose();

            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g3.setColor(new Color(148, 163, 184));
                g3.setFont(getFont());
                FontMetrics fm = g3.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g3.drawString(placeholder, getInsets().left, y);
                g3.dispose();
            }
        }
    }
}