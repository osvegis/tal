/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;

/**
 * Clase base para implementar analizadores léxicos
 * mediante autómatas finitos deterministas.
 */
public class ALex
{
private final Reader m_is;
private char m_char;
private boolean m_charReaded, m_reading;
private int m_row = 1, m_column;
private final StringBuilder m_name = new StringBuilder();
private Token m_token;
private Runnable m_state, m_start;

/**
 * Construye un analizador léxico.
 * @param file Fichero de texto que se debe analizar.
 * @throws IOException
 */
public ALex(String file) throws IOException
{
    m_is = new BufferedReader(new FileReader(file));
}

/**
 * Función para indicar cuál es el estado inicial del autómata.
 * @param s Estado inicial del autómata.
 */
public void setStart(Runnable s)
{
    if(m_start != null)
        throw new AssertionError("Ya se indicó el estado inicial.");

    m_start = s;
}

public void restart()
{
    state(m_start);
    m_name.setLength(0);
}

/**
 * Cierra el fichero de texto que se ha analizado.
 * @throws IOException
 */
public void close() throws IOException
{
    m_is.close();
}

/**
 * Lee el siguiente token del fichero de texto.
 * @return Token leído.
 * @throws IOException
 */
public Token read() throws IOException
{
    m_reading = true;
    restart();

    while(m_reading)
    {
        readChar();
        m_state.run();
    }

    return m_token;
}

private void readChar() throws IOException
{
    if(m_charReaded)
    {
        m_charReaded = false;
    }
    else
    {
        m_char = (char)m_is.read();
        m_column++;

        if(m_char == '\n')
        {
            m_row++;
            m_column = 0;
        }
    }
}

/**
 * Interrumpe la compilación con una excepción en caso de que se lea un
 * carácter no permitido.
 */
public void error()
{
    throw new RuntimeException("Caracter no permitido en "+
                               m_row +":"+ m_column +" : '"+ m_char +"'");
}

/**
 * Cuando un estado del autómata termine de leer un token debe llamar
 * a esta función. Los estados que llamen a esta función serán finales.
 * @param t Tipo del token leído.
 */
public void token(Token.Type t)
{
    m_token = new Token(t, m_name.toString(),
                        m_row, m_column - m_name.length());
    m_reading = false;
    m_charReaded = true;
}

/**
 * Esta función permite dibujar una flecha desde el estado que
 * llama a esta función al estado indicado como parámetro.
 * @param s Estado actual tras leer el siguiente carácter.
 */
public void state(Runnable s)
{
    m_name.append(m_char);
    m_state = s;
}

/**
 * Esta función hace lo mismo que <code>state</code>
 * pero no añade el carácter leído al nombre del token.
 * <p>Se puede utilizar para descartar las dobles comillas de las
 * cadenas de caracteres y los comentarios.
 * @param s Estado actual tras leer el siguiente carácter.
 * @see #state(Runnable)
 */
public void stateNoChar(Runnable s)
{
    m_state = s;
}

/**
 * Indica si el siguiente carácter coincide con el indicado.
 * @param c Caracter a comparar.
 * @return true si el siguiente carácter coincide con c.
 */
public boolean isChar(char c)
{
    return m_char == c;
}

/**
 * Indica si el siguiente carácter es válido para el comienzo del
 * nombre de un identificador.
 * @return true si el carácter es válido.
 */
public boolean isIdCharStart()
{
    return Character.isLetter(m_char) ||
           m_char == '_';
}

/**
 * Indica si el siguiente carácter es válido para un identificador.
 * Para comprobar si un nombre de identificador puede comenzar con el
 * siguiente carácter disponible, hay que usar la función
 * {@link #isIdCharStart isIdCharStart}.
 * @return true si el carácter es válido.
 */
public boolean isIdChar()
{
    return Character.isLetter(m_char) ||
           Character.isDigit(m_char)  ||
           m_char == '_';
}

/**
 * Indica si el siguiente carácter es un dígito.
 * @return true si el carácter es un dígito.
 */
public boolean isDigitChar()
{
    return Character.isDigit(m_char);
}

/**
 * Indica si el siguiente carácter es un espacio, tabulación,
 * salto de línea...
 * @return true si el carácter es un espacio en blanco.
 */
public boolean isSpaceChar()
{
    return Character.isWhitespace(m_char);
}

/**
 * Indica si se ha llegado al final del fichero.
 * @return true si se ha llegado al final del fichero.
 */
public boolean isEofChar()
{
    return m_char == Character.MAX_VALUE;
}

} // ALex
