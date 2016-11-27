/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;

/**
 * Clase base para implementar analizadores sintácticos
 * mediante el método descendente recursivo.
 */
public class ASin
{
private final ALex m_lex;
private final Code m_code;
private Token m_token, m_previous;

/**
 * Construye un analizador sintáctico a partir de un analizador léxico.
 * @param lex analizador léxico.
 */
public ASin(ALex lex)
{
    m_lex = lex;
    m_code = new Code();
    ASin.this.tokenRead();
}

/**
 * Cierra el analizador léxico.
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
 * Comprueba si el siguiente token es el correcto y lee el siguiente.
 * Si el token no era correcto lanza una excepción.
 * @param t Tipo del token esperado.
 */
public void tokenRead(Token.Type t)
{
    if(m_token.type != t)
    {
        throw new RuntimeException(
            "Error ("+ m_token.row +":"+ m_token.column +
            "): "+ m_token +". Esperaba: "+ t);
    }

    tokenRead();
}

/**
 * Obtiene el tipo del siguiente token.
 * @return tipo del siguiente token.
 */
public Token.Type tokenType()
{
    return m_token.type;
}

/**
 * Obtiene el nombre del siguiente token.
 * @return nombre del siguiente token.
 */
public String tokenName()
{
    return m_token.name;
}

/**
 * Genera código para la declaración de una variable de tipo entero.
 */
public void codeVariableInteger()
{
    m_code.declararVariableInteger(m_previous);
}

/**
 * Genera código para la declaración de una variable de tipo cadena.
 */
public void codeVariableString()
{
    m_code.declararVariableString(m_previous);
}

/**
 * Genera código para utilizar una variable como destino
 * de una sentencia de asignación.
 */
public void codeVariableAssignment()
{
    m_code.addVariableAssignment(m_previous);
}

/**
 * Genera código para utilizar una variable en una expresión.
 */
public void codeVariableExpression()
{
    m_code.addVariableExpression(m_previous);
}

/**
 * Genera código para realizar la asignación de un valor a una variable.
 */
public void codeAssignment()
{
    m_code.addAssignment(m_previous);
}

/**
 * Genera código para imprimir un valor por pantalla.
 */
public void codePrint()
{
    m_code.addPrint(m_previous);
}

/**
 * Genera código para una sentencia 'if'.
 */
public void codeIf()
{
    m_code.addIf(m_previous);
}

/**
 * Genera código para la cláusula 'else' de una sentencia 'if'.
 */
public void codeElse()
{
    m_code.addElse(m_previous);
}

/**
 * Genera código para indicar el final de una sentencia 'if'
 * o de un bucle 'while'.
 */
public void codeEnd()
{
    m_code.addEnd(m_previous);
}

/**
 * Genera código para un bucle 'while'.
 */
public void codeWhile()
{
    m_code.addWhile(m_previous);
}

/**
 * Genera código para el operador indicado.
 * <br><tt>&nbsp; {@code +  } &nbsp; &nbsp;</tt> Sumar y concatenar
 * <br><tt>&nbsp; {@code -  } &nbsp; &nbsp;</tt> Restar números
 * <br><tt>&nbsp; {@code -1 } &nbsp;&nbsp;</tt>  Negación numérica
 * <br><tt>&nbsp; {@code *  } &nbsp; &nbsp;</tt> Multiplicar
 * <br><tt>&nbsp; {@code /  } &nbsp; &nbsp;</tt> Dividir
 * <br><tt>&nbsp; {@code == } &nbsp;&nbsp;</tt>  Igual
 * <br><tt>&nbsp; {@code <> } &nbsp;&nbsp;</tt>  Distinto
 * <br><tt>&nbsp; {@code <  } &nbsp; &nbsp;</tt> Menor
 * <br><tt>&nbsp; {@code <= } &nbsp;&nbsp;</tt>  Menor o igual
 * <br><tt>&nbsp; {@code >  } &nbsp; &nbsp;</tt> Mayor
 * <br><tt>&nbsp; {@code >= } &nbsp;&nbsp;</tt>  Mayor o igual
 * <br><tt>&nbsp; {@code !  } &nbsp; &nbsp;</tt> Negación lógica
 * <br><tt>&nbsp; {@code || } &nbsp;&nbsp;</tt>  Disyunción
 * <br><tt>&nbsp; {@code && } &nbsp;&nbsp;</tt>  Conjunción
 * @param operator nombre del operador.
 */
public void codeOperator(String operator)
{
    m_code.addOperator(operator);
}

/**
 * Genera código para un número entero.
 */
public void codeInteger()
{
    m_code.addInteger(m_previous);
}

/**
 * Genera código para una cadena de caracteres.
 */
public void codeString()
{
    m_code.addString(m_previous);
}

/**
 * Obtiene una representación del código generado.
 * @return código generado.
 */
public String codeGet()
{
    return m_code.toString();
}

/**
 * Ejecuta el código generado.
 */
public void codeRun()
{
    m_code.run();
}

} // ASin
