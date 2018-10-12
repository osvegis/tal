/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;

/**
 * Base class to implement a syntactic analyser using the
 * descending-recursive method.
 */
public class ASyn
{
private final ALex m_lex;
private final Code m_code;
private Token m_token, m_previous;

/**
 * Build a syntactic analyser from a lexical analyser.
 * @param lex lexical analyser
 */
public ASyn(ALex lex)
{
    m_lex = lex;
    m_code = new Code();
    ASyn.this.tokenRead();
}

/**
 * Close the lexical analyzer.
 * @throws IOException
 */
public void close() throws IOException
{
    m_lex.close();
}

private void tokenRead()
{
    try
    {
        m_previous = m_token;
        m_token = m_lex.read();
    }
    catch(IOException ex)
    {
        RuntimeException rex = new RuntimeException(ex);
        rex.setStackTrace(ex.getStackTrace());
        throw rex;
    }
}

/**
 * Check if the next token is correct and read the next one.
 * If the token was not correct, throw a runtime exception.
 * @param t Type of the expected token
 */
public void tokenRead(Token.Type t)
{
    if(m_token.type != t)
    {
        throw new RuntimeException(
            "Error ("+ m_token.row +":"+ m_token.column +
            "): "+ m_token +". Expected: "+ t);
    }

    tokenRead();
}

/**
 * Get the type of the next token.
 * @return type of the next token
 */
public Token.Type tokenType()
{
    return m_token.type;
}

/**
 * Get the name of the next token.
 * @return name of the next token
 */
public String tokenName()
{
    return m_token.name;
}

/**
 * Generate code for the declaration of an integer variable.
 */
public void codeVariableInteger()
{
    m_code.addVariableInteger(m_previous);
}

/**
 * Generate code for the declaration of a string variable.
 */
public void codeVariableString()
{
    m_code.addVariableString(m_previous);
}

/**
 * Generate code to use a variable as the destination
 * of an assignment statement.
 */
public void codeVariableAssignment()
{
    m_code.addVariableAssignment(m_previous);
}

/**
 * Generate code to use a variable in an expression.
 */
public void codeVariableExpression()
{
    m_code.addVariableExpression(m_previous);
}

/**
 * Generate code to perform the assignment of a value to a variable.
 */
public void codeAssignment()
{
    m_code.addAssignment(m_previous);
}

/**
 * Generate code to print a value to screen.
 */
public void codePrint()
{
    m_code.addPrint(m_previous);
}

/**
 * Generate code for an 'if' statement.
 */
public void codeIf()
{
    m_code.addIf(m_previous);
}

/**
 * Generate code for the 'else' clause of an 'if' statement.
 */
public void codeElse()
{
    m_code.addElse(m_previous);
}

/**
 * Generate code to indicate the end of an 'if' statement
 * or a 'while' loop.
 */
public void codeEnd()
{
    m_code.addEnd(m_previous);
}

/**
 * Generate code for a 'while' loop.
 */
public void codeWhile()
{
    m_code.addWhile(m_previous);
}

/**
 * Generate code for an operator.
 * <br><tt>&nbsp; {@code +  }&nbsp; &nbsp;</tt> sum and concatenation
 * <br><tt>&nbsp; {@code -  }&nbsp; &nbsp;</tt> subtraction
 * <br><tt>&nbsp; {@code -1 }&nbsp;&nbsp;</tt>  negation
 * <br><tt>&nbsp; {@code *  }&nbsp; &nbsp;</tt> multiplication
 * <br><tt>&nbsp; {@code /  }&nbsp; &nbsp;</tt> division
 * <br><tt>&nbsp; {@code == }&nbsp;&nbsp;</tt>  equality
 * <br><tt>&nbsp; {@code != }&nbsp;&nbsp;</tt>  inequality
 * <br><tt>&nbsp; {@code <  }&nbsp; &nbsp;</tt> less than
 * <br><tt>&nbsp; {@code <= }&nbsp;&nbsp;</tt>  less than or equal to
 * <br><tt>&nbsp; {@code >  }&nbsp; &nbsp;</tt> greater than
 * <br><tt>&nbsp; {@code >= }&nbsp;&nbsp;</tt>  greater than or equal to
 * <br><tt>&nbsp; {@code !  }&nbsp; &nbsp;</tt> logical negation
 * <br><tt>&nbsp; {@code || }&nbsp;&nbsp;</tt>  disjunction
 * <br><tt>&nbsp; {@code && }&nbsp;&nbsp;</tt>  conjunction
 * @param operator name of the operator
 */
public void codeOperator(String operator)
{
    m_code.addOperator(operator);
}

/**
 * Generate code for an integer value.
 */
public void codeInteger()
{
    m_code.addInteger(m_previous);
}

/**
 * Generate code for a string value.
 */
public void codeString()
{
    m_code.addString(m_previous);
}

/**
 * Get a representation of the generated code.
 * @return generated code
 */
public String codeGet()
{
    return m_code.toString();
}

/**
 * Execute the generated code.
 */
public void codeRun()
{
    m_code.run();
}

} // ASyn
