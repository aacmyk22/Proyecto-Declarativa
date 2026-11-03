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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Color ROJO           = new Color(225, 29, 72);

    // ====== Fuentes (con alternativas según el sistema operativo) ======
    private static final String FUENTE      = elegirFuente("SF Pro Text", "Helvetica Neue", "Segoe UI", "Inter", "SansSerif");
    private static final String FUENTE_MONO = elegirFuente("JetBrains Mono", "Menlo", "Consolas", "Monospaced");

    private static final String[] EJEMPLOS = {
            "x^2", "3*x^2 + 2*x", "sin(2*x)", "cos(x)",
            "exp(x)", "1/x", "sqrt(x)", "x^3 - 4*x"
    };

    private final MotorProlog motorProlog;
    private CampoTexto inputExpresion, inputLimInf, inputLimSup;
    private JTextArea textoIntegral, textoPasos;
    private JLabel labelAreaExacta, labelAreaReal;
    private PanelGrafica grafica;

    public InterfazGrafica() {
        motorProlog = new MotorProlog();

        setTitle("Sistema Experto de Integrales");
        setSize(1180, 780);
        setMinimumSize(new Dimension(1000, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);

        if (!motorProlog.estaCargado()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "No se pudo cargar integrales.pl. Revisa que el archivo exista.",
                    "Prolog", JOptionPane.WARNING_MESSAGE));
        }
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
        header.setBorder(new EmptyBorder(22, 30, 22, 30));

        JLabel titulo = new JLabel("∫  Calculadora de Integrales");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FUENTE, Font.BOLD, 26));

        JLabel subtitulo = new JLabel("Integra, explica cada paso y grafica el área bajo la curva");
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

        JLabel etiqueta = new JLabel("Sistema experto · Java + SWI-Prolog");
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setFont(new Font(FUENTE, Font.BOLD, 12));

        header.add(textos, BorderLayout.CENTER);
        header.add(etiqueta, BorderLayout.EAST);
        return header;
    }

    private JPanel crearContenido() {
        JPanel contenedor = new JPanel(new BorderLayout(20, 0));
        contenedor.setBackground(FONDO);
        contenedor.setBorder(new EmptyBorder(20, 24, 20, 24));

        contenedor.add(crearTarjetaDatos(), BorderLayout.WEST);
        contenedor.add(crearColumnaDerecha(), BorderLayout.CENTER);
        return contenedor;
    }

    // Columna izquierda: datos de entrada y resultados
    private JPanel crearTarjetaDatos() {
        PanelRedondeado tarjeta = new PanelRedondeado(20, TARJETA, BORDE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(22, 24, 22, 24));
        tarjeta.setPreferredSize(new Dimension(420, 100));

        // --- Expresión ---
        inputExpresion = new CampoTexto("Ej. x^2, 3*x + 1, sin(x)");
        tarjeta.add(crearCampo("Función f(x)", inputExpresion));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearEjemplos());
        tarjeta.add(Box.createVerticalStrut(16));

        // --- Límites ---
        inputLimInf = new CampoTexto("0");
        inputLimSup = new CampoTexto("1");
        JPanel limites = new JPanel(new GridLayout(1, 2, 14, 0));
        limites.setOpaque(false);
        limites.add(crearCampo("Límite inferior (a)", inputLimInf));
        limites.add(crearCampo("Límite superior (b)", inputLimSup));
        limites.setToolTipText("Puedes escribir números o cosas como pi, pi/2, 2*e");
        limites.setAlignmentX(Component.LEFT_ALIGNMENT);
        limites.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        tarjeta.add(limites);
        tarjeta.add(Box.createVerticalStrut(18));

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
        tarjeta.add(Box.createVerticalStrut(20));

        // --- Resultados ---
        textoIntegral = new JTextArea("—");
        textoIntegral.setEditable(false);
        textoIntegral.setOpaque(false);
        textoIntegral.setLineWrap(true);
        textoIntegral.setWrapStyleWord(true);
        textoIntegral.setBorder(null);
        textoIntegral.setForeground(TEXTO);
        textoIntegral.setFont(new Font(FUENTE, Font.BOLD, 16));

        labelAreaExacta = crearValor(PRIMARIO_HOVER);
        labelAreaReal = crearValor(EXITO);

        tarjeta.add(crearResultado("INTEGRAL INDEFINIDA", textoIntegral));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearResultado("INTEGRAL DEFINIDA  F(b) − F(a)", labelAreaExacta));
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(crearResultado("ÁREA REAL  ∫ |f(x)| dx", labelAreaReal));
        tarjeta.add(Box.createVerticalGlue());

        // --- Acciones ---
        btnCalcular.addActionListener(e -> calcular());
        btnLimpiar.addActionListener(e -> limpiar());
        inputExpresion.addActionListener(e -> calcular());
        inputLimInf.addActionListener(e -> calcular());
        inputLimSup.addActionListener(e -> calcular());

        return tarjeta;
    }

    // Columna derecha: gráfica arriba y procedimiento abajo
    private JPanel crearColumnaDerecha() {
        JPanel columna = new JPanel(new BorderLayout(0, 16));
        columna.setOpaque(false);

        grafica = new PanelGrafica(FUENTE);
        PanelRedondeado tarjetaGrafica = crearTarjetaConTitulo("Gráfica de f(x)", grafica);

        JLabel leyenda = new JLabel("<html><font color='#6366f1'>■</font> área positiva &nbsp;&nbsp;"
                + "<font color='#f43f5e'>■</font> área negativa &nbsp;&nbsp; (pasa el mouse sobre la curva)</html>");
        leyenda.setFont(new Font(FUENTE, Font.PLAIN, 12));
        leyenda.setForeground(TEXTO_SUAVE);
        tarjetaGrafica.add(leyenda, BorderLayout.SOUTH);

        textoPasos = new JTextArea("Aquí aparecen las reglas que usó Prolog para resolver la integral.");
        textoPasos.setEditable(false);
        textoPasos.setLineWrap(true);
        textoPasos.setWrapStyleWord(true);
        textoPasos.setForeground(TEXTO);
        textoPasos.setBackground(RESULTADO_BG);
        textoPasos.setFont(new Font(FUENTE, Font.PLAIN, 14));
        textoPasos.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(textoPasos);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        PanelRedondeado tarjetaPasos = crearTarjetaConTitulo("¿Cómo lo resolvió Prolog?", scroll);
        tarjetaPasos.setPreferredSize(new Dimension(100, 220));

        columna.add(tarjetaGrafica, BorderLayout.CENTER);
        columna.add(tarjetaPasos, BorderLayout.SOUTH);
        return columna;
    }

    private PanelRedondeado crearTarjetaConTitulo(String titulo, JComponent contenido) {
        PanelRedondeado tarjeta = new PanelRedondeado(20, TARJETA, BORDE);
        tarjeta.setLayout(new BorderLayout(0, 10));
        tarjeta.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font(FUENTE, Font.BOLD, 15));
        lbl.setForeground(TEXTO);

        tarjeta.add(lbl, BorderLayout.NORTH);
        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
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
        JLabel lbl = new JLabel("Ejemplos (clic para probar):");
        lbl.setFont(new Font(FUENTE, Font.PLAIN, 12));
        lbl.setForeground(TEXTO_SUAVE);

        JPanel chips = new JPanel(new GridLayout(2, 4, 6, 6));
        chips.setOpaque(false);
        for (String ejemplo : EJEMPLOS) {
            BotonRedondeado chip = new BotonRedondeado(ejemplo, CHIP_BG, CHIP_HOVER, PRIMARIO_HOVER, 14);
            chip.setFont(new Font(FUENTE_MONO, Font.PLAIN, 12));
            chip.setPreferredSize(new Dimension(80, 28));
            chip.addActionListener(e -> {
                inputExpresion.setText(ejemplo);
                if (inputLimInf.getText().isBlank()) inputLimInf.setText("0");
                if (inputLimSup.getText().isBlank()) inputLimSup.setText("2");
                calcular();
            });
            chips.add(chip);
        }

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(chips, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return panel;
    }

    private JLabel crearValor(Color color) {
        JLabel label = new JLabel("—");
        label.setFont(new Font(FUENTE, Font.BOLD, 22));
        label.setForeground(color);
        return label;
    }

    private JPanel crearResultado(String titulo, JComponent valor) {
        PanelRedondeado panel = new PanelRedondeado(14, RESULTADO_BG, BORDE);
        panel.setLayout(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(12, 18, 12, 18));

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
        String expr = normalizar(inputExpresion.getText());
        if (expr.isEmpty()) {
            error("Por favor, ingrese una expresión.");
            return;
        }

        Double a = leerLimite(inputLimInf.getText());
        Double b = leerLimite(inputLimSup.getText());
        if (a == null || b == null) {
            error("Por favor, ingrese números válidos en los límites (ej. 0, 2.5, pi, pi/2).");
            return;
        }

        MotorProlog.Resultado r;
        try {
            r = motorProlog.resolver(expr, a, b);
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage() + "\nRecuerda usar * para multiplicar, ej. 3*x^2.");
            return;
        }

        if (r == null) {
            textoIntegral.setText("No soportada");
            textoIntegral.setForeground(ROJO);
            labelAreaExacta.setText("—");
            labelAreaReal.setText("—");
            textoPasos.setText("El sistema no tiene una regla para integrar \"" + expr + "\".\n\n"
                    + "Reglas que conoce: constantes, x^n, suma y resta, constante por función,\n"
                    + "sin, cos, tan, exp, e^x, k^x, 1/x, sqrt(x), ln(x), sinh, cosh y 1/(1+x^2).\n"
                    + "(También funcionan con argumento a*x, por ejemplo sin(3*x).)");
            grafica.limpiar();
            return;
        }

        textoIntegral.setForeground(TEXTO);
        textoIntegral.setText("F(x) = " + bonito(r.integral()) + " + C");
        labelAreaExacta.setText(conUnidades(r.areaExacta()));
        labelAreaReal.setText(conUnidades(r.areaReal()));
        textoPasos.setText(numerarPasos(bonito(r.pasos())));
        textoPasos.setCaretPosition(0);

        // la grafica se ve un poco mas alla de los limites
        double ancho = Math.abs(b - a) == 0 ? 1 : Math.abs(b - a);
        double desde = Math.min(a, b) - ancho * 0.25;
        double hasta = Math.max(a, b) + ancho * 0.25;
        int n = 400;
        grafica.mostrar(motorProlog.puntos(expr, desde, hasta, n), desde, hasta, a, b);
    }

    private void limpiar() {
        inputExpresion.setText("");
        inputLimInf.setText("");
        inputLimSup.setText("");
        textoIntegral.setText("—");
        textoIntegral.setForeground(TEXTO);
        labelAreaExacta.setText("—");
        labelAreaReal.setText("—");
        textoPasos.setText("Aquí aparecen las reglas que usó Prolog para resolver la integral.");
        grafica.limpiar();
        inputExpresion.requestFocusInWindow();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Entrada", JOptionPane.ERROR_MESSAGE);
    }

    // Arregla cosas comunes al escribir: "sen" en vez de "sin", "3x" en vez
    // de "3*x", mayúsculas, log en vez de ln...
    private static String normalizar(String texto) {
        return texto.trim()
                .toLowerCase()
                .replace("sen(", "sin(")
                .replace("log(", "ln(")
                .replace("π", "pi")
                .replaceAll("(\\d)\\s*([a-z(])", "$1*$2");
    }

    private Double leerLimite(String texto) {
        String t = texto.trim().toLowerCase().replace(',', '.').replace("π", "pi");
        if (t.isEmpty()) return null;
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return motorProlog.evaluarNumero(t);   // "pi", "pi/2", "2*e"...
        }
    }

    private static String conUnidades(String valor) {
        return valor.startsWith("no") ? valor : valor + " u²";
    }

    private static String numerarPasos(String pasos) {
        String[] lineas = pasos.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lineas.length; i++) {
            boolean ultima = i == lineas.length - 1;
            sb.append(ultima ? "→  " : (i + 1) + ".  ").append(lineas[i]).append("\n");
        }
        return sb.toString().trim();
    }

    // Hace que la salida de Prolog se vea más como en el cuaderno
    private static String bonito(String s) {
        s = s.replace("*", "·")
             .replaceAll("(?<=[\\w)])([+-])(?=[\\w(])", " $1 ")   // x^3+x^2 -> x^3 + x^2
             .replaceAll("abs\\(([^()]*)\\)", "|$1|");

        // exponentes enteros como superindice: x^4 -> x⁴ (x^1.5 se queda igual)
        Matcher m = Pattern.compile("\\^(\\d+)(?![\\d.])").matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            StringBuilder sup = new StringBuilder();
            for (char c : m.group(1).toCharArray()) {
                sup.append("⁰¹²³⁴⁵⁶⁷⁸⁹".charAt(c - '0'));
            }
            m.appendReplacement(sb, sup.toString());
        }
        m.appendTail(sb);
        return sb.toString();
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
