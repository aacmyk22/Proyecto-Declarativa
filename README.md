# Proyecto-Declarativa

Sistema experto de integrales hecho con **Java (Swing) + SWI-Prolog (JPL)**
para la materia de Programación Declarativa.

Toda la parte "inteligente" (las reglas de integración, la simplificación y
el cálculo de áreas) está en Prolog, en
`proyecto/ProyectoIntegrales/src/main/resources/integrales.pl`.
Java solo arma la ventana, le manda la consulta a Prolog y muestra lo que
regresa.

## ¿Qué hace?

- Calcula la **integral indefinida** de la función.
- Explica **paso a paso** qué reglas usó Prolog (suma, constante, potencia, tabla...).
- Calcula la **integral definida** F(b) − F(a) y el **área real** ∫|f(x)| dx
  (regla del trapecio). Así se ve por qué ∫ sin(x) de 0 a 2π da 0 aunque sí
  hay área.
- **Grafica** la función y sombrea el área (morado arriba del eje, rojo abajo).
  Si pasas el mouse por la curva te dice el valor de f(x).

### Funciones que sabe integrar

| Tipo | Ejemplo |
|------|---------|
| Constantes | `7`, `pi` |
| Potencias | `x`, `x^3`, `1/x^2`, `sqrt(x)` |
| Suma / resta / signo | `3*x^2 + 2*x - 1`, `-x^2` |
| Constante por función | `5*x`, `x/2`, `3/x` |
| Trigonométricas | `sin(x)`, `cos(3*x)`, `tan(x)` |
| Exponenciales y log | `exp(x)`, `e^(2*x)`, `2^x`, `1/x`, `ln(x)` |
| Otras | `sinh(x)`, `cosh(x)`, `1/(1+x^2)` |

También acepta `sen(x)`, `log(x)` (como ln) y `3x` (se convierte a `3*x`).
Los límites pueden ser números o expresiones como `pi`, `pi/2`, `2*e`.

## Cómo correrlo

1. Instalar **SWI-Prolog** (64 bits) desde https://www.swi-prolog.org/download/stable
2. Tener **Java 21**.
3. Agregar la carpeta `bin` de SWI-Prolog al `PATH`
   (en Windows normalmente `C:\Program Files\swipl\bin`).
4. Abrir `proyecto/ProyectoIntegrales` en IntelliJ y correr `Main.java`
   con el directorio de trabajo en `proyecto/ProyectoIntegrales`.

El `pom.xml` ya apunta al `jpl.jar` que trae SWI-Prolog (Windows, Mac y
Linux). Si lo instalaron en otra carpeta, solo cambien la propiedad
`jpl.jar` del `pom.xml`.

## Probar las reglas desde la consola de Prolog

```prolog
?- consult('src/main/resources/integrales.pl').
?- integral(3*x^2 + sin(x), x, F).
F = x^3-cos(x).

?- calcular_area(x^2, x, 0, 3, F, Area).
F = x^3/3, Area = 9.

?- integral(4*cos(3*x), x, F, Pasos).
```

## Estructura

```
proyecto/ProyectoIntegrales/
├── pom.xml
└── src/main/
    ├── java/com/calculo/
    │   ├── Main.java            # arranca la app
    │   ├── InterfazGrafica.java # ventana principal
    │   ├── PanelGrafica.java    # dibuja la función y el área
    │   └── MotorProlog.java     # puente Java <-> Prolog (JPL)
    └── resources/
        └── integrales.pl        # base de conocimiento
```
