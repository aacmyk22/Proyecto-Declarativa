:- encoding(utf8).

% --- Utilidades ---
depende(X, X) :- !.
depende(E, X) :-
    compound(E),
    E =.. [_|Args],
    member(A, Args),
    depende(A, X), !.

constante(E, X) :- \+ depende(E, X).

coeficiente(X, X, 1) :- !.
coeficiente(A*X, X, A) :- constante(A, X).
coeficiente(X*A, X, A) :- constante(A, X).

paso(Formato, Args, Texto) :- format(atom(Texto), Formato, Args).


% --- Tabla de integrales inmediatas ---
tabla(sin(U),       U, -cos(U),          'seno').
tabla(cos(U),       U, sin(U),           'coseno').
tabla(tan(U),       U, -ln(abs(cos(U))), 'tangente').
tabla(exp(U),       U, exp(U),           'exponencial').
tabla(e^U,          U, e^U,              'exponencial').
tabla(sinh(U),      U, cosh(U),          'seno hiperbólico').
tabla(cosh(U),      U, sinh(U),          'coseno hiperbólico').
tabla(1/U,          U, ln(abs(U)),       '1/u da ln|u|').
tabla(1/(1+U^2),    U, atan(U),          'arcotangente').
tabla(ln(U),        U, U*ln(U) - U,      'logaritmo, sale por partes').


% --- Reglas de integracion ---
integral(K, X, K*X, [P]) :-
    constante(K, X), !,
    paso('∫ ~w dx = ~w*~w   (integral de una constante)', [K, K, X], P).

integral(F + G, X, IF + IG, [P|Pasos]) :- !,
    paso('∫ (~w) dx se separa en ∫ ~w dx + ∫ ~w dx   (regla de la suma)', [F+G, F, G], P),
    integral(F, X, IF, P1),
    integral(G, X, IG, P2),
    append(P1, P2, Pasos).

integral(F - G, X, IF - IG, [P|Pasos]) :- !,
    paso('∫ (~w) dx se separa en ∫ ~w dx - ∫ ~w dx   (regla de la resta)', [F-G, F, G], P),
    integral(F, X, IF, P1),
    integral(G, X, IG, P2),
    append(P1, P2, Pasos).

integral(-F, X, -IF, [P|Pasos]) :- !,
    paso('El signo negativo sale de la integral: -∫ ~w dx', [F], P),
    integral(F, X, IF, Pasos).

integral(F, X, R, [P]) :-
    tabla(F, U, G, Nombre),
    coeficiente(U, X, A), !,
    simplificar(G / A, R),
    paso('∫ ~w dx = ~w   (inmediata: ~w)', [F, R, Nombre], P).

integral(K^U, X, R, [P]) :-
    constante(K, X), K \== e,
    coeficiente(U, X, A), !,
    simplificar(K^U / (A*ln(K)), R),
    paso('∫ ~w dx = ~w   (exponencial de base ~w)', [K^U, R, K], P).

integral(1/(X^N), X, R, [P|Pasos]) :-
    number(N), !,
    M is -N,
    paso('1/~w^~w se reescribe como ~w^(~w)', [X, N, X, M], P),
    integral(X^M, X, R, Pasos).

integral(K * F, X, K * IF, [P|Pasos]) :-
    constante(K, X), !,
    paso('La constante ~w sale de la integral: ~w * ∫ ~w dx', [K, K, F], P),
    integral(F, X, IF, Pasos).

integral(F * K, X, K * IF, [P|Pasos]) :-
    constante(K, X), !,
    paso('La constante ~w sale de la integral: ~w * ∫ ~w dx', [K, K, F], P),
    integral(F, X, IF, Pasos).

integral(F / K, X, IF / K, [P|Pasos]) :-
    constante(K, X), !,
    paso('Se divide entre la constante ~w: (1/~w) * ∫ ~w dx', [K, K, F], P),
    integral(F, X, IF, Pasos).

integral(K / F, X, K * IF, [P|Pasos]) :-
    constante(K, X), K \== 1, !,
    paso('La constante ~w sale de la integral: ~w * ∫ 1/~w dx', [K, K, F], P),
    integral(1/F, X, IF, Pasos).

integral(X, X, X^2/2, [P]) :- !,
    paso('∫ ~w dx = ~w^2/2   (potencia con n = 1)', [X, X], P).

integral(sqrt(X), X, R, [P|Pasos]) :- !,
    paso('sqrt(~w) se reescribe como ~w^0.5', [X, X], P),
    integral(X^0.5, X, R, Pasos).

integral(X^N, X, ln(abs(X)), [P]) :-
    number(N), N =:= -1, !,
    paso('∫ ~w^(-1) dx = ln|~w|   (caso especial n = -1)', [X, X], P).

integral(X^N, X, X^N1/N1, [P]) :-
    number(N), !,
    N1 is N + 1,
    simplificar(X^N1/N1, R),
    paso('∫ ~w^~w dx = ~w   (regla de la potencia)', [X, N, R], P).


% --- Simplificacion algebraica ---
simplificar(E, S) :-
    simp(E, E1),
    (   E1 == E
    ->  S = E
    ;   simplificar(E1, S)
    ).

simp(E, E) :- atomic(E), !.
simp(E, S) :-
    E =.. [Op|Args],
    maplist(simp, Args, Args1),
    E1 =.. [Op|Args1],
    (   regla(E1, S) -> true ; S = E1 ).

regla(A+B, C) :- number(A), number(B), C is A + B.
regla(A-B, C) :- number(A), number(B), C is A - B.
regla(A*B, C) :- number(A), number(B), C is A * B.
regla(A/B, C) :- integer(A), integer(B), B =\= 0, 0 is A mod B, C is A // B.

regla(0+A, A).
regla(A+0, A).
regla(A-0, A).
regla(0-A, -A).
regla(1*A, A).
regla(A*1, A).
regla(-1*A, -A).
regla(0*_, 0).
regla(_*0, 0).
regla(A/1, A).
regla(A^1, A).
regla(_^0, 1).

regla(-(-A), A).
regla(-N, M) :- number(N), M is -N.
regla(A+N, A-M) :- number(N), N < 0, M is -N.
regla(A-N, A+M) :- number(N), N < 0, M is -N.
regla(A + -B, A - B).
regla(A - -B, A + B).
regla(-(A/B), (-A)/B).
regla(A * -B, -(A*B)).
regla(A^N, 1/A^M) :- number(N), N < 0, M is -N.
regla(A/N, (-A)/M) :- number(N), N < 0, M is -N.
regla(K*(A/B), (K*A)/B) :- atomic(K), number(B).
regla((K*A)/B, C*A) :- integer(K), integer(B), B =\= 0, 0 is K mod B, C is K // B.
regla((A/B)/C, A/D) :- number(B), number(C), D is B * C.
regla(A*N, N*A) :- number(N), \+ number(A).


% --- Evaluacion y sustitucion ---
evaluar(Expr, Var, Val, Res) :-
    sustituir(Expr, Var, Val, ExprSust),
    Res is ExprSust.

f_en(F, X, V, Y) :-
    catch(evaluar(F, X, V, Y), _, fail),
    number(Y).

sustituir(Var, Var, Val, Val) :- !.
sustituir(ln(A), Var, Val, log(S)) :- !,
    sustituir(A, Var, Val, S).
sustituir(Term, Var, Val, TermSust) :-
    Term =.. [Op, Arg1, Arg2],
    sustituir(Arg1, Var, Val, S1),
    sustituir(Arg2, Var, Val, S2),
    TermSust =.. [Op, S1, S2], !.
sustituir(Term, Var, Val, TermSust) :-
    Term =.. [Op, Arg],
    sustituir(Arg, Var, Val, S),
    TermSust =.. [Op, S], !.
sustituir(X, _, _, X).


% --- Calculo del area ---
integral(Expr, Var, F) :-
    integral(Expr, Var, F0, _),
    simplificar(F0, F).

calcular_area(Expr, Var, LimInf, LimSup, IntExpr, Area) :-
    integral(Expr, Var, IntExpr),
    evaluar(IntExpr, Var, LimSup, ResSup),
    evaluar(IntExpr, Var, LimInf, ResInf),
    Area is ResSup - ResInf.

area_real(F, X, A, B, Area) :-
    N = 2000,
    H is (B - A) / N,
    findall(Y,
            ( between(0, N, I),
              Xi is A + I*H,
              f_en(F, X, Xi, Fx),
              Y is abs(Fx) ),
            Ys),
    length(Ys, Total),
    Total =:= N + 1,
    trapecio(Ys, H, Area).

trapecio(Ys, H, Area) :-
    sum_list(Ys, Suma),
    Ys = [Primero|_],
    last(Ys, Ultimo),
    Area is abs(H * (Suma - (Primero + Ultimo) / 2)).


% --- Predicados para la interfaz Java ---
resolver(Texto, A, B, Integral, Exacta, Real, Pasos) :-
    term_string(F, Texto),
    ground(F),
    integral(F, x, F0, ListaPasos),
    simplificar(F0, I),
    format(atom(Integral), '~w', [I]),
    (   area_real(F, x, A, B, R),
        catch(calcular_area(F, x, A, B, _, Ex), _, fail),
        number(Ex)
    ->  format(atom(Real), '~4f', [R]),
        format(atom(Exacta), '~4f', [Ex])
    ;   Real = 'no definida',
        Exacta = 'no definida'
    ),
    paso('Resultado: F(x) = ~w + C', [I], Final),
    append(ListaPasos, [Final], Todos),
    atomic_list_concat(Todos, '\n', Pasos).

puntos(Texto, Desde, Hasta, N, Ys) :-
    term_string(F, Texto),
    H is (Hasta - Desde) / N,
    findall(Y,
            ( between(0, N, I),
              Xi is Desde + I*H,
              (   f_en(F, x, Xi, Y0) -> Y = Y0 ; Y = nan )
            ),
            Ys).
