% src/main/resources/integrales.pl

% --- REGLAS DE INTEGRACIÓN SIMBÓLICA ---
% a) ∫ k dx
integral(K, X, K*X) :- number(K).
% b) ∫ x dx
integral(X, X, (X^2)/2) :- atom(X).
% c) ∫ (k * x) dx
integral(K*X, X, K*(X^2)/2) :- number(K), atom(X).
% d) ∫ x^n dx
integral(X^N, X, (X^N1)/N1) :- number(N), N \= -1, N1 is N + 1.
% k) ∫ sin(x) dx
integral(sin(X), X, -cos(X)).
% l) ∫ cos(x) dx
integral(cos(X), X, sin(X)).

% --- EVALUACIÓN Y SUSTITUCIÓN ---
% Sustituye la variable por el límite y evalúa aritméticamente
evaluar(Expr, Var, Val, Res) :-
    sustituir(Expr, Var, Val, ExprSust),
    Res is ExprSust.

% Lógica de sustitución en árboles de expresiones
sustituir(Var, Var, Val, Val) :- !.
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

% --- CÁLCULO DEL ÁREA BAJO LA CURVA ---
calcular_area(Expr, Var, LimInf, LimSup, IntExpr, Area) :-
    integral(Expr, Var, IntExpr),
    evaluar(IntExpr, Var, LimSup, ResSup),
    evaluar(IntExpr, Var, LimInf, ResInf),
    Area is ResSup - ResInf.