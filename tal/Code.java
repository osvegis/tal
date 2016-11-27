/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.util.*;

/**
 * Esta clase se utiliza en <code>ASin</code> para la generación
 * de código.
 */
public class Code
{
private static enum TValor
{
    VOID, INTEGER, STRING, BOOLEAN
}

private static enum Accion
{
    DECLARACION, ASIGNACION, IMPRIMIR, IF, ELSE, WHILE,
    GOTO, END, VARIABLE, CONSTANTE, SUMAR, RESTAR, NEGAR,
    MULTIPLICAR, DIVIDIR, IGUAL, DISTINTO, MENOR, MENORIGUAL,
    MAYOR, MAYORIGUAL, NOT, OR, AND, NUM_SENTENCIAS
}

private static class Variable
{
    private String nombre;
    private TValor tipo;
    private Object valor;
}

private static class Codigo
{
    private int linea, fila, columna;
    private Accion accion;
    private TValor tipo;
    private Object valor;
    private Codigo next;
}

private static class Nodo
{
    private TValor tipo;
    private Object valor;
}

private interface RunCodigo
{
    Codigo run(Codigo c);
}

private final RunCodigo m_run[] = new RunCodigo[Accion.values().length];

// Datos de compilacion.
private Token m_token;

// Datos de compilacion y ejecucion
private final Map<String,Variable> m_variables = new HashMap<>();
private Codigo m_primero, m_ultimo;

// Pila para implementar los saltos en condiciones y bucles
private final LinkedList<Codigo> m_control = new LinkedList<>();

// Pila de ejecucion
private final LinkedList<Nodo> m_pila = new LinkedList<>();

private void add(Codigo nodo)
{
    nodo.linea   = m_ultimo==null ? 0 : m_ultimo.linea + 1;
    nodo.fila    = m_token.row;
    nodo.columna = m_token.column;

    if(m_primero == null)
    {
        assert m_ultimo == null;
        m_primero = m_ultimo = nodo;
    }
    else
    {
        assert m_ultimo.next == null;
        m_ultimo.next = nodo;
        m_ultimo = nodo;
    }
}

private void error(String mensaje)
{
    throw new RuntimeException(
        "Error ("+ m_token.row +":"+ m_token.column +
        "): "+ mensaje +" "+ m_token.name);
}

private Codigo newCodigo(Accion accion, TValor tipo, Object valor)
{
    Codigo c = new Codigo();
    c.fila    = m_token.row;
    c.columna = m_token.column;
    c.accion  = accion;
    c.tipo    = tipo;
    c.valor   = valor;
    return c;
}

private void declararVariable(TValor tipo)
{
    String nombre = m_token.name;

    if(m_variables.containsKey(nombre))
        error("Ya existe la variable");

    Variable v = new Variable();
    v.nombre   = nombre;
    v.tipo     = tipo;
    v.valor    = tipo == TValor.STRING ? "" : 0;
    m_variables.put(nombre, v);

    add(newCodigo(Accion.DECLARACION, tipo, v));
}

public void declararVariableInteger(Token token)
{
    m_token = token;
    declararVariable(TValor.INTEGER);
}

public void declararVariableString(Token token)
{
    m_token = token;
    declararVariable(TValor.STRING);
}

public void addAssignment(Token token)
{
    m_token = token;
    add(newCodigo(Accion.ASIGNACION, TValor.VOID, null));
}

public void addPrint(Token token)
{
    m_token = token;
    add(newCodigo(Accion.IMPRIMIR, TValor.VOID, null));
}

private void pushCtrl(Codigo n)
{
    m_control.addFirst(n);
}

private Codigo popCtrl()
{
    if(m_control.isEmpty())
        throw new RuntimeException("Pila vacia.");

    return m_control.removeFirst();
}

public void addIf(Token token)
{
    m_token = token;
    Codigo c = newCodigo(Accion.IF, TValor.VOID, null);
    add(c);
    pushCtrl(c);
}

public void addElse(Token token)
{
    m_token = token;
    Codigo gotoEnd = newCodigo(Accion.GOTO, TValor.VOID, null);
    add(gotoEnd);

    Codigo nodoElse = newCodigo(Accion.ELSE, TValor.VOID, null);
    add(nodoElse);

    popCtrl().valor = nodoElse;
    pushCtrl(gotoEnd);
}

public void addWhile(Token token)
{
    m_token = token;
    Codigo c = newCodigo(Accion.WHILE, TValor.VOID, null);
    add(c);
    pushCtrl(c);
}

public void addEnd(Token token)
{
    m_token = token;
    Codigo gotoInicio = null;

    boolean bucle = m_control.size() >= 2 &&
                    m_control.get(1).accion == Accion.WHILE;
    if(bucle)
    {
        gotoInicio = newCodigo(Accion.GOTO, TValor.VOID, null);
        add(gotoInicio);
    }

    Codigo fin = newCodigo(Accion.END, TValor.VOID, null);
    add(fin);

    // GOTO al final del bloque: cuando no se cumpla la condicion.
    popCtrl().valor = fin;

    if(bucle)
    {
        // GOTO al inicio del bucle.
        gotoInicio.valor = popCtrl();
    }
}

public void addVariableAssignment(Token token)
{
    m_token = token;
    Variable v = m_variables.get(token.name);

    if(v == null)
    {
        throw new RuntimeException(
            "No existe la variable: "+ token.name);
    }

    add(newCodigo(Accion.VARIABLE, TValor.VOID, v));
}

public void addVariableExpression(Token token)
{
    m_token = token;
    Variable v = m_variables.get(token.name);

    if(v == null)
    {
        throw new RuntimeException(
            "No existe la variable: "+ token.name);
    }

    add(newCodigo(Accion.VARIABLE, v.tipo, v));
}

public void addInteger(Token token)
{
    m_token = token;
    Object valor = Long.parseLong(token.name);
    add(newCodigo(Accion.CONSTANTE, TValor.INTEGER, valor));
}

public void addString(Token token)
{
    m_token = token;
    add(newCodigo(Accion.CONSTANTE, TValor.STRING, token.name));
}

public void addOperator(String operador)
{
    Accion sentencia;

    switch(operador)
    {
        case "+":  sentencia = Accion.SUMAR;       break;
        case "-":  sentencia = Accion.RESTAR;      break;
        case "-1": sentencia = Accion.NEGAR;       break;
        case "*":  sentencia = Accion.MULTIPLICAR; break;
        case "/":  sentencia = Accion.DIVIDIR;     break;
        case "==": sentencia = Accion.IGUAL;       break;
        case "<>": sentencia = Accion.DISTINTO;    break;
        case "<":  sentencia = Accion.MENOR;       break;
        case "<=": sentencia = Accion.MENORIGUAL;  break;
        case ">":  sentencia = Accion.MAYOR;       break;
        case ">=": sentencia = Accion.MAYORIGUAL;  break;
        case "!":  sentencia = Accion.NOT;         break;
        case "||": sentencia = Accion.OR;          break;
        case "&&": sentencia = Accion.AND;         break;
        default: throw new AssertionError();
    }

    add(newCodigo(sentencia, TValor.VOID, null));
}

@Override public String toString()
{
    StringBuilder s = new StringBuilder();
    Codigo n = m_primero;

    while(n != null)
    {
        s.append(String.format("%5d:  ", n.linea));

        TValor tipo = n.valor instanceof Variable ?
                      ((Variable)n.valor).tipo : n.tipo;
        switch(tipo)
        {
            case VOID:    s.append("     "); break;
            case INTEGER: s.append("int  "); break;
            case STRING:  s.append("str  "); break;
            case BOOLEAN: s.append("bool "); break;
            default: throw new AssertionError();
        }

        switch(n.accion)
        {
            case DECLARACION: s.append("decl  "); break;
            case ASIGNACION:  s.append(":=    "); break;
            case IMPRIMIR:    s.append("print "); break;
            case IF:          s.append("if    "); break;
            case ELSE:        s.append("else  "); break;
            case WHILE:       s.append("while "); break;
            case GOTO:        s.append("goto  "); break;
            case END:         s.append("end   "); break;
            case VARIABLE:    s.append("var   "); break;
            case CONSTANTE:   s.append("cte   "); break;
            case SUMAR:       s.append("+     "); break;
            case RESTAR:      s.append("-     "); break;
            case NEGAR:       s.append("-1    "); break;
            case MULTIPLICAR: s.append("*     "); break;
            case DIVIDIR:     s.append("/     "); break;
            case IGUAL:       s.append("==    "); break;
            case DISTINTO:    s.append("<>    "); break;
            case MENOR:       s.append("<     "); break;
            case MENORIGUAL:  s.append("<=    "); break;
            case MAYOR:       s.append(">     "); break;
            case MAYORIGUAL:  s.append("<=    "); break;
            case NOT:         s.append("!     "); break;
            case OR:          s.append("||    "); break;
            case AND:         s.append("&&    "); break;
            default: throw new AssertionError();
        }

        if(n.valor instanceof Variable)
            s.append(((Variable)n.valor).nombre);
        else if(n.accion == Accion.GOTO || n.accion == Accion.IF)
            s.append(((Codigo)n.valor).linea);
        else if(n.tipo == TValor.STRING)
            s.append("\""+ n.valor +"\"");
        else if(n.tipo == TValor.INTEGER)
            s.append(n.valor);

        s.append("\n");
        n = n.next;
    }

    return s.toString();
}

private void pilaPush(TValor tipo, Object valor)
{
    Nodo n  = new Nodo();
    n.tipo  = tipo;
    n.valor = valor;
    m_pila.addFirst(n);
}

private Nodo pilaPop()
{
    if(m_pila.isEmpty())
        throw new RuntimeException("Pila vacia.");

    return m_pila.removeFirst();
}

private void comprobarTipos(Codigo c, Nodo n1, TValor t2)
{
    if(n1.tipo != t2)
    {
        throw new RuntimeException(
            "Tipos incompatibles en "+ c.fila +"."+ c.columna);
    }
}

private Codigo runNext(Codigo c)
{
    return c.next;
}

private Codigo runAsignacion(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    if(n1.tipo != TValor.VOID)
        throw new RuntimeException("No es una variable de asignacion.");

    Variable v = (Variable)n1.valor;
    comprobarTipos(c, n2, v.tipo);
    v.valor = n2.valor;
    return c.next;
}

private Codigo runImprimir(Codigo c)
{
    System.out.println(pilaPop().valor);
    return c.next;
}

private Codigo runIf(Codigo c)
{
    Nodo n = pilaPop();
    comprobarTipos(c, n, TValor.BOOLEAN);
    return (Boolean)n.valor ? c.next : (Codigo)c.valor;
}

private Codigo runGoto(Codigo c)
{
    return (Codigo)c.valor;
}

private Codigo runVariable(Codigo c)
{
    Variable v = (Variable)c.valor;

    if(c.tipo == TValor.VOID)
    {
        // Variable de asignacion.
        pilaPush(c.tipo, c.valor);
    }
    else
    {
        // Variable de expresion.
        assert c.tipo == v.tipo;
        pilaPush(v.tipo, v.valor);
    }

    return c.next;
}

private Codigo runConstante(Codigo c)
{
    pilaPush(c.tipo, c.valor);
    return c.next;
}

private Codigo runSumar(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    if(n1.tipo == TValor.STRING || n2.tipo == TValor.STRING)
    {
        pilaPush(TValor.STRING, n1.valor +""+ n2.valor);
    }
    else
    {
        comprobarTipos(c, n1, TValor.INTEGER);
        comprobarTipos(c, n2, TValor.INTEGER);
        pilaPush(TValor.INTEGER, (Long)n1.valor + (Long)n2.valor);
    }

    return c.next;
}

private Codigo runRestar(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.INTEGER, (Long)n1.valor - (Long)n2.valor);
    return c.next;
}

private Codigo runNegar(Codigo c)
{
    Nodo n = pilaPop();
    comprobarTipos(c, n, TValor.INTEGER);
    pilaPush(TValor.INTEGER, -(Long)n.valor);
    return c.next;
}

private Codigo runMultiplicar(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.INTEGER, (Long)n1.valor * (Long)n2.valor);
    return c.next;
}

private Codigo runDividir(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.INTEGER, (Long)n1.valor / (Long)n2.valor);
    return c.next;
}

private Codigo runIgual(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.BOOLEAN, n1.valor.equals(n2.valor));
    return c.next;
}

private Codigo runDistinto(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.BOOLEAN, !n1.valor.equals(n2.valor));
    return c.next;
}

private Codigo runMenor(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.BOOLEAN, (Long)n1.valor < (Long)n2.valor);
    return c.next;
}

private Codigo runMenorIgual(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.BOOLEAN, (Long)n1.valor <= (Long)n2.valor);
    return c.next;
}

private Codigo runMayor(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.BOOLEAN, (Long)n1.valor > (Long)n2.valor);
    return c.next;
}

private Codigo runMayorIgual(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.INTEGER);
    comprobarTipos(c, n2, TValor.INTEGER);
    pilaPush(TValor.BOOLEAN, (Long)n1.valor >= (Long)n2.valor);
    return c.next;
}

private Codigo runNot(Codigo c)
{
    Nodo n = pilaPop();
    comprobarTipos(c, n, TValor.BOOLEAN);
    pilaPush(TValor.BOOLEAN, !(Boolean)n.valor);
    return c.next;
}

private Codigo runOr(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.BOOLEAN);
    comprobarTipos(c, n2, TValor.BOOLEAN);
    pilaPush(TValor.BOOLEAN, (Boolean)n1.valor || (Boolean)n2.valor);
    return c.next;
}

private Codigo runAnd(Codigo c)
{
    Nodo n2 = pilaPop(),
         n1 = pilaPop();

    comprobarTipos(c, n1, TValor.BOOLEAN);
    comprobarTipos(c, n2, TValor.BOOLEAN);
    pilaPush(TValor.BOOLEAN, (Boolean)n1.valor && (Boolean)n2.valor);
    return c.next;
}

private void inicializarRun()
{
    m_run[Accion.DECLARACION.ordinal()] = this::runNext;
    m_run[Accion.ASIGNACION .ordinal()] = this::runAsignacion;
    m_run[Accion.IMPRIMIR   .ordinal()] = this::runImprimir;
    m_run[Accion.IF         .ordinal()] = this::runIf;
    m_run[Accion.ELSE       .ordinal()] = this::runNext;
    m_run[Accion.WHILE      .ordinal()] = this::runNext;
    m_run[Accion.GOTO       .ordinal()] = this::runGoto;
    m_run[Accion.END        .ordinal()] = this::runNext;
    m_run[Accion.VARIABLE   .ordinal()] = this::runVariable;
    m_run[Accion.CONSTANTE  .ordinal()] = this::runConstante;
    m_run[Accion.SUMAR      .ordinal()] = this::runSumar;
    m_run[Accion.RESTAR     .ordinal()] = this::runRestar;
    m_run[Accion.NEGAR      .ordinal()] = this::runNegar;
    m_run[Accion.MULTIPLICAR.ordinal()] = this::runMultiplicar;
    m_run[Accion.DIVIDIR    .ordinal()] = this::runDividir;
    m_run[Accion.IGUAL      .ordinal()] = this::runIgual;
    m_run[Accion.DISTINTO   .ordinal()] = this::runDistinto;
    m_run[Accion.MENOR      .ordinal()] = this::runMenor;
    m_run[Accion.MENORIGUAL .ordinal()] = this::runMenorIgual;
    m_run[Accion.MAYOR      .ordinal()] = this::runMayor;
    m_run[Accion.MAYORIGUAL .ordinal()] = this::runMayorIgual;
    m_run[Accion.NOT        .ordinal()] = this::runNot;
    m_run[Accion.OR         .ordinal()] = this::runOr;
    m_run[Accion.AND        .ordinal()] = this::runAnd;
}

public void run()
{
    inicializarRun();
    Codigo c = m_primero;

    while(c != null)
        c = m_run[c.accion.ordinal()].run(c);
}

} // Code
