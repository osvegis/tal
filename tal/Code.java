/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.util.*;

/**
 * This class is used in {@code ASyn} for code generation.
 */
public class Code
{
private static enum TValue
{
    VOID, INTEGER, STRING, BOOLEAN
}

private static enum Action
{
    DECLARE, ASSIGN, PRINT, IF, ELSE, WHILE,
    GOTO, END, VARIABLE, CONSTANT, SUM, SUBTRACT, NEGATE,
    MULTIPLY, DIVIDE, EQUAL, UNEQUAL, LESS, LESSEQ,
    GREATER, GREATEREQ, NOT, OR, AND
}

private static class Variable
{
    private String name;
    private TValue type;
    private Object value;
}

private static class Statement
{
    private int line, row, column;
    private Action action;
    private TValue type;
    private Object value;
    private Statement next;
}

private static class Node
{
    private TValue type;
    private Object value;
}

private interface RunStatement
{
    Statement run(Statement c);
}

private final RunStatement
    m_run[] = new RunStatement[Action.values().length];

// Compilation data.
private Token m_token;

// Compilation and execution data.
private final Map<String,Variable> m_variables = new HashMap<>();
private Statement m_first, m_last;

// Stack to implement the jumps in conditions and loops.
private final LinkedList<Statement> m_control = new LinkedList<>();

// Execution stack.
private final LinkedList<Node> m_pila = new LinkedList<>();

private void add(Statement nodo)
{
    nodo.line   = m_last==null ? 0 : m_last.line + 1;
    nodo.row    = m_token.row;
    nodo.column = m_token.column;

    if(m_first == null)
    {
        assert m_last == null;
        m_first = m_last = nodo;
    }
    else
    {
        assert m_last.next == null;
        m_last.next = nodo;
        m_last = nodo;
    }
}

private void error(String mensaje)
{
    throw new RuntimeException(
        "Error ("+ m_token.row +":"+ m_token.column +
        "): "+ mensaje +" "+ m_token.name);
}

private Statement newCodigo(Action accion, TValue tipo, Object valor)
{
    Statement c = new Statement();
    c.row    = m_token.row;
    c.column = m_token.column;
    c.action  = accion;
    c.type    = tipo;
    c.value   = valor;
    return c;
}

private void addVariable(TValue tipo)
{
    String nombre = m_token.name;

    if(m_variables.containsKey(nombre))
        error("The variable already exists");

    Variable v = new Variable();
    v.name   = nombre;
    v.type     = tipo;
    v.value    = tipo == TValue.STRING ? "" : 0;
    m_variables.put(nombre, v);

    add(newCodigo(Action.DECLARE, tipo, v));
}

public void addVariableInteger(Token token)
{
    m_token = token;
    addVariable(TValue.INTEGER);
}

public void addVariableString(Token token)
{
    m_token = token;
    addVariable(TValue.STRING);
}

public void addAssignment(Token token)
{
    m_token = token;
    add(newCodigo(Action.ASSIGN, TValue.VOID, null));
}

public void addPrint(Token token)
{
    m_token = token;
    add(newCodigo(Action.PRINT, TValue.VOID, null));
}

private void pushCtrl(Statement n)
{
    m_control.addFirst(n);
}

private Statement popCtrl()
{
    if(m_control.isEmpty())
        throw new RuntimeException("Empty stack");

    return m_control.removeFirst();
}

public void addIf(Token token)
{
    m_token = token;
    Statement c = newCodigo(Action.IF, TValue.VOID, null);
    add(c);
    pushCtrl(c);
}

public void addElse(Token token)
{
    m_token = token;
    Statement gotoEnd = newCodigo(Action.GOTO, TValue.VOID, null);
    add(gotoEnd);

    Statement nodoElse = newCodigo(Action.ELSE, TValue.VOID, null);
    add(nodoElse);

    popCtrl().value = nodoElse;
    pushCtrl(gotoEnd);
}

public void addWhile(Token token)
{
    m_token = token;
    Statement c = newCodigo(Action.WHILE, TValue.VOID, null);
    add(c);
    pushCtrl(c);
}

public void addEnd(Token token)
{
    m_token = token;
    Statement gotoInicio = null;

    boolean bucle = m_control.size() >= 2 &&
                    m_control.get(1).action == Action.WHILE;
    if(bucle)
    {
        gotoInicio = newCodigo(Action.GOTO, TValue.VOID, null);
        add(gotoInicio);
    }

    Statement fin = newCodigo(Action.END, TValue.VOID, null);
    add(fin);

    // GOTO al final del bloque: cuando no se cumpla la condicion.
    popCtrl().value = fin;

    if(bucle)
    {
        // GOTO al inicio del bucle.
        gotoInicio.value = popCtrl();
    }
}

public void addVariableAssignment(Token token)
{
    m_token = token;
    Variable v = m_variables.get(token.name);

    if(v == null)
        throw new RuntimeException("There is no variable: "+ token.name);

    add(newCodigo(Action.VARIABLE, TValue.VOID, v));
}

public void addVariableExpression(Token token)
{
    m_token = token;
    Variable v = m_variables.get(token.name);

    if(v == null)
        throw new RuntimeException("There is no variable: "+ token.name);

    add(newCodigo(Action.VARIABLE, v.type, v));
}

public void addInteger(Token token)
{
    m_token = token;
    Object valor = Long.parseLong(token.name);
    add(newCodigo(Action.CONSTANT, TValue.INTEGER, valor));
}

public void addString(Token token)
{
    m_token = token;
    add(newCodigo(Action.CONSTANT, TValue.STRING, token.name));
}

public void addOperator(String operador)
{
    Action sentencia;

    switch(operador)
    {
        case "+":  sentencia = Action.SUM;       break;
        case "-":  sentencia = Action.SUBTRACT;  break;
        case "-1": sentencia = Action.NEGATE;    break;
        case "*":  sentencia = Action.MULTIPLY;  break;
        case "/":  sentencia = Action.DIVIDE;    break;
        case "==": sentencia = Action.EQUAL;     break;
        case "!=": sentencia = Action.UNEQUAL;   break;
        case "<":  sentencia = Action.LESS;      break;
        case "<=": sentencia = Action.LESSEQ;    break;
        case ">":  sentencia = Action.GREATER;   break;
        case ">=": sentencia = Action.GREATEREQ; break;
        case "!":  sentencia = Action.NOT;       break;
        case "||": sentencia = Action.OR;        break;
        case "&&": sentencia = Action.AND;       break;
        default: throw new AssertionError();
    }

    add(newCodigo(sentencia, TValue.VOID, null));
}

@Override public String toString()
{
    StringBuilder s = new StringBuilder();
    Statement n = m_first;

    while(n != null)
    {
        s.append(String.format("%5d:  ", n.line));

        TValue tipo = n.value instanceof Variable ?
                      ((Variable)n.value).type : n.type;
        switch(tipo)
        {
            case VOID:    s.append("     "); break;
            case INTEGER: s.append("int  "); break;
            case STRING:  s.append("str  "); break;
            case BOOLEAN: s.append("bool "); break;
            default: throw new AssertionError();
        }

        switch(n.action)
        {
            case DECLARE:   s.append("decl  "); break;
            case ASSIGN:    s.append("=     "); break;
            case PRINT:     s.append("print "); break;
            case IF:        s.append("if    "); break;
            case ELSE:      s.append("else  "); break;
            case WHILE:     s.append("while "); break;
            case GOTO:      s.append("goto  "); break;
            case END:       s.append("end   "); break;
            case VARIABLE:  s.append("var   "); break;
            case CONSTANT:  s.append("cte   "); break;
            case SUM:       s.append("+     "); break;
            case SUBTRACT:  s.append("-     "); break;
            case NEGATE:    s.append("-1    "); break;
            case MULTIPLY:  s.append("*     "); break;
            case DIVIDE:    s.append("/     "); break;
            case EQUAL:     s.append("==    "); break;
            case UNEQUAL:   s.append("!=    "); break;
            case LESS:      s.append("<     "); break;
            case LESSEQ:    s.append("<=    "); break;
            case GREATER:   s.append(">     "); break;
            case GREATEREQ: s.append("<=    "); break;
            case NOT:       s.append("!     "); break;
            case OR:        s.append("||    "); break;
            case AND:       s.append("&&    "); break;
            default: throw new AssertionError();
        }

        if(n.value instanceof Variable)
            s.append(((Variable)n.value).name);
        else if(n.action == Action.GOTO || n.action == Action.IF)
            s.append(((Statement)n.value).line);
        else if(n.type == TValue.STRING)
            s.append("\""+ n.value +"\"");
        else if(n.type == TValue.INTEGER)
            s.append(n.value);

        s.append("\n");
        n = n.next;
    }

    return s.toString();
}

private void stackPush(TValue tipo, Object valor)
{
    Node n  = new Node();
    n.type  = tipo;
    n.value = valor;
    m_pila.addFirst(n);
}

private Node pilaPop()
{
    if(m_pila.isEmpty())
        throw new RuntimeException("Empty stack");

    return m_pila.removeFirst();
}

private void checkTypes(Statement c, Node n1, TValue t2)
{
    if(n1.type != t2)
    {
        throw new RuntimeException(
            "Incompatible types in "+ c.row +"."+ c.column);
    }
}

private Statement runNext(Statement c)
{
    return c.next;
}

private Statement runAsign(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    if(n1.type != TValue.VOID)
        throw new RuntimeException("It is not an assignment variable");

    Variable v = (Variable)n1.value;
    checkTypes(c, n2, v.type);
    v.value = n2.value;
    return c.next;
}

private Statement runPrint(Statement c)
{
    System.out.println(pilaPop().value);
    return c.next;
}

private Statement runIf(Statement c)
{
    Node n = pilaPop();
    checkTypes(c, n, TValue.BOOLEAN);
    return (Boolean)n.value ? c.next : (Statement)c.value;
}

private Statement runGoto(Statement c)
{
    return (Statement)c.value;
}

private Statement runVariable(Statement c)
{
    Variable v = (Variable)c.value;

    if(c.type == TValue.VOID)
    {
        // Variable de asignacion.
        stackPush(c.type, c.value);
    }
    else
    {
        // Variable de expresion.
        assert c.type == v.type;
        stackPush(v.type, v.value);
    }

    return c.next;
}

private Statement runConstant(Statement c)
{
    stackPush(c.type, c.value);
    return c.next;
}

private Statement runSum(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    if(n1.type == TValue.STRING || n2.type == TValue.STRING)
    {
        stackPush(TValue.STRING, n1.value +""+ n2.value);
    }
    else
    {
        checkTypes(c, n1, TValue.INTEGER);
        checkTypes(c, n2, TValue.INTEGER);
        stackPush(TValue.INTEGER, (Long)n1.value + (Long)n2.value);
    }

    return c.next;
}

private Statement runSubtract(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.INTEGER, (Long)n1.value - (Long)n2.value);
    return c.next;
}

private Statement runNegate(Statement c)
{
    Node n = pilaPop();
    checkTypes(c, n, TValue.INTEGER);
    stackPush(TValue.INTEGER, -(Long)n.value);
    return c.next;
}

private Statement runMultiply(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.INTEGER, (Long)n1.value * (Long)n2.value);
    return c.next;
}

private Statement runDivide(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.INTEGER, (Long)n1.value / (Long)n2.value);
    return c.next;
}

private Statement runEqual(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.BOOLEAN, n1.value.equals(n2.value));
    return c.next;
}

private Statement runUnequal(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.BOOLEAN, !n1.value.equals(n2.value));
    return c.next;
}

private Statement runLess(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.BOOLEAN, (Long)n1.value < (Long)n2.value);
    return c.next;
}

private Statement runLessEq(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.BOOLEAN, (Long)n1.value <= (Long)n2.value);
    return c.next;
}

private Statement runGreater(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.BOOLEAN, (Long)n1.value > (Long)n2.value);
    return c.next;
}

private Statement runGreaterEq(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.INTEGER);
    checkTypes(c, n2, TValue.INTEGER);
    stackPush(TValue.BOOLEAN, (Long)n1.value >= (Long)n2.value);
    return c.next;
}

private Statement runNot(Statement c)
{
    Node n = pilaPop();
    checkTypes(c, n, TValue.BOOLEAN);
    stackPush(TValue.BOOLEAN, !(Boolean)n.value);
    return c.next;
}

private Statement runOr(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.BOOLEAN);
    checkTypes(c, n2, TValue.BOOLEAN);
    stackPush(TValue.BOOLEAN, (Boolean)n1.value || (Boolean)n2.value);
    return c.next;
}

private Statement runAnd(Statement c)
{
    Node n2 = pilaPop(),
         n1 = pilaPop();

    checkTypes(c, n1, TValue.BOOLEAN);
    checkTypes(c, n2, TValue.BOOLEAN);
    stackPush(TValue.BOOLEAN, (Boolean)n1.value && (Boolean)n2.value);
    return c.next;
}

private void inicializarRun()
{
    m_run[Action.DECLARE  .ordinal()] = this::runNext;
    m_run[Action.ASSIGN   .ordinal()] = this::runAsign;
    m_run[Action.PRINT    .ordinal()] = this::runPrint;
    m_run[Action.IF       .ordinal()] = this::runIf;
    m_run[Action.ELSE     .ordinal()] = this::runNext;
    m_run[Action.WHILE    .ordinal()] = this::runNext;
    m_run[Action.GOTO     .ordinal()] = this::runGoto;
    m_run[Action.END      .ordinal()] = this::runNext;
    m_run[Action.VARIABLE .ordinal()] = this::runVariable;
    m_run[Action.CONSTANT .ordinal()] = this::runConstant;
    m_run[Action.SUM      .ordinal()] = this::runSum;
    m_run[Action.SUBTRACT .ordinal()] = this::runSubtract;
    m_run[Action.NEGATE   .ordinal()] = this::runNegate;
    m_run[Action.MULTIPLY .ordinal()] = this::runMultiply;
    m_run[Action.DIVIDE   .ordinal()] = this::runDivide;
    m_run[Action.EQUAL    .ordinal()] = this::runEqual;
    m_run[Action.UNEQUAL  .ordinal()] = this::runUnequal;
    m_run[Action.LESS     .ordinal()] = this::runLess;
    m_run[Action.LESSEQ   .ordinal()] = this::runLessEq;
    m_run[Action.GREATER  .ordinal()] = this::runGreater;
    m_run[Action.GREATEREQ.ordinal()] = this::runGreaterEq;
    m_run[Action.NOT      .ordinal()] = this::runNot;
    m_run[Action.OR       .ordinal()] = this::runOr;
    m_run[Action.AND      .ordinal()] = this::runAnd;
}

public void run()
{
    inicializarRun();
    Statement c = m_first;

    while(c != null)
        c = m_run[c.action.ordinal()].run(c);
}

} // Code
