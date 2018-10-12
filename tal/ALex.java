/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;

/**
 * Base class to implement a lexical analyser by means of a
 * deterministic finite automaton.
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
 * Build a lexical analyser.
 * @param file Text file that must be analysed
 * @throws IOException
 */
public ALex(String file) throws IOException
{
    m_is = new BufferedReader(new FileReader(file));
}

/**
 * Function to indicate which is the initial state of the automaton.
 * @param s initial state of the automaton
 */
public void setStart(Runnable s)
{
    if(m_start != null)
        throw new AssertionError("Ya se indicó el estado inicial.");

    m_start = s;
}

/**
 * It causes the automaton to return to the initial state by
 * discarding the read characters.
 * This function should be used to discard blank characters and
 * to finish reading comments.
 */
public void restart()
{
    state(m_start);
    m_name.setLength(0);
}

/**
 * Close the text file that has been analysed.
 * @throws IOException
 */
public void close() throws IOException
{
    m_is.close();
}

/**
 * Read the following token from the text file.
 * @return read token
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
 * Interrupt the compilation with an exception in case a
 * non-allowed character is read.
 */
public void error()
{
    throw new RuntimeException("Non-allowed character in "+
                               m_row +":"+ m_column +" : '"+ m_char +"'");
}

/**
 * When a state of the automaton finishes reading a token it
 * must call this function.
 * The states that call this function will be final.
 * @param t Read token type
 */
public void token(Token.Type t)
{
    m_token = new Token(t, m_name.toString(),
                        m_row, m_column - m_name.length());
    m_reading = false;
    m_charReaded = true;
}

/**
 * This function allows you to draw an arrow from the state that
 * calls this function to the state indicated as a parameter.
 * @param s Current state after reading the next character
 */
public void state(Runnable s)
{
    m_name.append(m_char);
    m_state = s;
}

/**
 * This function does the same thing as {@code state} but
 * does not add the read character to the name of the token.
 * It can be used to discard double quotes from character strings
 * and the comments.
 * @param s Current state after reading the next character
 * @see #state(Runnable)
 */
public void stateNoChar(Runnable s)
{
    m_state = s;
}

/**
 * Indicates if the next character matches the indicated one.
 * @param c Character to compare
 * @return {@code true} if the next character matches {@code c}
 */
public boolean isChar(char c)
{
    return m_char == c;
}

/**
 * Indicates whether the next character is valid for the
 * beginning of the name of an identifier.
 * @return {@code true} if the character is valid
 */
public boolean isIdCharStart()
{
    return Character.isLetter(m_char) ||
           m_char == '_';
}

/**
 * Indicates whether the next character is valid for an identifier.
 * To check if an identifier name can start with the next available
 * character, use the {@link #isIdCharStart isIdCharStart} function.
 * @return {@code true} if the character is valid
 */
public boolean isIdChar()
{
    return Character.isLetter(m_char) ||
           Character.isDigit(m_char)  ||
           m_char == '_';
}

/**
 * Indicates if the next character is a digit.
 * @return {@code true} if the next character is a digit
 */
public boolean isDigitChar()
{
    return Character.isDigit(m_char);
}

/**
 * Indicates if the next character is a space, tabulation, line break, ...
 * @return {@code true} if the character is a blank
 */
public boolean isSpaceChar()
{
    return Character.isWhitespace(m_char);
}

/**
 * Indicates if the end of the file has been reached.
 * @return {@code true} if the end of the file has been reached
 */
public boolean isEofChar()
{
    return m_char == Character.MAX_VALUE;
}

} // ALex
