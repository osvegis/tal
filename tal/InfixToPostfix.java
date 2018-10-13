/*
 * Released under the MIT License.
 * Copyright 2018 Oscar Vega-Gisbert.
 */
package tal;

import java.util.*;

/**
 * This class converts simple maths expressions in infix notation to
 * posfix notation using a Deterministic Finite Automaton and a grammar.
 *
 * <p>The expressions must be formed by numbers, parentheses and the
 * operators +, -, *, /.
 *
 * <p>For example, the expression "(2+3)*5" corresponds
 * to "2 3 + 5 *" in posfix notation
 */
public class InfixToPostfix
{
/**
 * Lexical analyser implemented by a Deterministic Finite Automaton.
 */
private static class Lex
{
private String   m_expression;
private int      m_offset, m_index;
private Runnable m_state;

/**
 * Initialize the lexical analyser with an expression.
 */
private Lex(String expression)
{
    m_expression = expression;
}

/**
 * Return the next token.
 * @return The next token or {@code null} if there are no more tokens.
 */
private String next()
{
    m_state = this::s_start;
    m_offset = m_index;
    int length = m_expression.length();

    while(m_state != null && m_index < length)
        m_state.run();

    return m_offset == m_index ? null :
           m_expression.substring(m_offset, m_index);
}

private void s_start()
{
    char c = getChar();

    if(Character.isSpaceChar(c))
        restart();
    else if(isOperator(c))
        state(this::s_operator);
    else if(Character.isLetter(c))
        state(this::s_id);
    else if(Character.isDigit(c))
        state(this::s_number);
    else
        error(c);
}

private void s_operator()
{
    token();
}

private void s_id()
{
    if(Character.isLetterOrDigit(getChar()))
        state(this::s_id);
    else
        token();
}

private void s_number()
{
    // In the numbers, we allow there to be commas and points.
    // Depending on the location, one will be considered as a
    // decimal point and the other will be ignored.

    char c = m_expression.charAt(m_index);

    if(Character.isDigit(c) || c == ',' || c == '.')
        state(this::s_number);
    else
        token();
}

private char getChar()
{
    return m_expression.charAt(m_index);
}

private boolean isOperator(char c)
{
    // The parentheses are not operators, but in the lexical analysis
    // we treat them as if they were to simplify the process.

    return c == '+' || c == '-' || c == '*' || c == '/' ||
           c == '(' || c == ')';
}

private void restart()
{
    state(this::s_start);
    m_offset = m_index;
}

private void state(Runnable r)
{
    m_index++;
    m_state = r;
}

private void token()
{
    m_state = null;
}

private void error(char c)
{
    throw new RuntimeException(
            "Character not allowed in "+ m_index +": "+ c);
}

} // Lex

/**
 * Syntactic analyser implemented by a Descending-Recursive Analyser.
 * 
 * Grammar to analyze mathematical expressions with addition,
 * subtraction, multiplication, division and parentheses.
 *
 *  expression -> vsum vsum1
 *              | SUM vsum vsum1
 *
 *  vsum  -> vmul vmul1
 *
 *  vsum1 -> SUM vsum vsum1
 *         | @
 *
 *  vmul  -> LPAR expression RPAR
 *         | VALUE
 *
 *  vmul1 -> MUL vmul vmul1
 *         | @
 */
private static class Syn
{
private Lex     m_lex;
private String m_token;
private ArrayList<String> m_postfix = new ArrayList<>();

/**
 * Initialize the syntactic analyser with an expression.
 */
private Syn(String expression)
{
    m_lex   = new Lex(expression);
    m_token = m_lex.next();
}

private String[] parse()
{
    expression();

    if(m_token != null)
        error("fin de la expresión");

    return m_postfix.toArray(new String[m_postfix.size()]);
}

private void expression()
{
    if(is_SUM())
    {
        String sum = nextToken();
        vsum();

        if("-".equals(sum))
            add("-1"); // Negation operator (unary).

        vsum1();
    }
    else
    {
        vsum();
        vsum1();
    }
}

private void vsum()
{
    vmul();
    vmul1();
}

private void vsum1()
{
    if(is_SUM())
    {
        String sum = nextToken();
        vsum();
        add(sum);
        vsum1();
    }
}

private void vmul()
{
    if(is_LPAR())
    {
        nextToken();
        expression();

        if(!is_RPAR())
            error(")");

        nextToken();
    }
    else
    {
        if(!is_VALUE())
            error("value or constant");

        add(nextToken());
    }
}

private void vmul1()
{
    if(is_MUL())
    {
        String mul = nextToken();
        vmul();
        add(mul);
        vmul1();
    }
}

private String nextToken()
{
    String t = m_token;
    m_token = m_lex.next();
    return t;
}

private boolean is_SUM()
{
    return "+".equals(m_token) || "-".equals(m_token);
}

private boolean is_MUL()
{
    return "*".equals(m_token) || "/".equals(m_token);
}

private boolean is_LPAR()
{
    return "(".equals(m_token);
}

private boolean is_RPAR()
{
    return ")".equals(m_token);
}

private boolean is_VALUE()
{
    return m_token != null &&
           !is_SUM() && !is_MUL() && !is_LPAR() && !is_RPAR();
}

private void add(String token)
{
    m_postfix.add(token);
}

private void error(String expected)
{
    if(m_token == null)
    {
        throw new RuntimeException(
            "Syntactic error in position "+ m_lex.m_index +
            ": expected '"+ expected +"'");
    }
    else
    {
        throw new RuntimeException(
            "Syntactic error in position "+ m_lex.m_index +
            ": expected '"+ expected +"' instead of '"+ m_token +"'");
    }
}

} // ASin

/**
 * Parse an expression in infix notation.
 * @param infixExpression expression in infix notation
 * @return Array that contains the tokens in postfix order.
 */
public static String[] parse(String infixExpression)
{
    return new Syn(infixExpression).parse();
}

/**
 * Computes the result using a stack.
 * @param stack Stack of values.
 * @param token Next token.
 */
private static void compute(LinkedList<Double> stack, String token)
{
    double a, b;

    switch(token)
    {
        case "+":
            b = stack.pop();
            a = stack.pop();
            stack.push(a + b);
            break;
        case "-":
            b = stack.pop();
            a = stack.pop();
            stack.push(a - b);
            break;
        case "-1":
            a = stack.pop();
            stack.push(-a);
            break;
        case "*":
            b = stack.pop();
            a = stack.pop();
            stack.push(a * b);
            break;
        case "/":
            b = stack.pop();
            a = stack.pop();
            stack.push(a / b);
            break;
        default:
            stack.push(Double.valueOf(token));
    }
}

/**
 * Main method.
 * @param args Only one parameter whith the math expression.
 */
public static void main(String[] args)
{
    if(args.length != 1)
    {
        System.out.println(
            "PARAMETERS: math expression between double quotes");

        System.exit(0);
    }

    Lex lex = new Lex(args[0]);
    String token;
    System.out.print("Tokens:\n ");

    while((token = lex.next()) != null)
        System.out.print(" "+ token);

    System.out.print("\nPostfix Notation:\n ");
    LinkedList<Double> stack = new LinkedList<>();

    for(String t : parse(args[0]))
    {
        System.out.print(" "+ t);
        compute(stack, t);
    }

    System.out.println("\nResult: "+ stack.pop());
}

} // InfixToPostfix
