/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;
import static tal.Token.Type.*;

/**
 * Lexical analyser implemented by a Deterministic Finite Automaton.
 * <p>Each state of the automaton is implemented with a Runnable object.
 */
public class DFA extends ALex
{
/**
 * Build the automaton.
 * @param file Text file that must be analysed
 * @throws IOException
 */
public DFA(String file) throws IOException
{
    super(file);
    setStart(this::s_start);
}

private void s_start()
{
    if(isDigitChar())
        state(this::s_intval);
    else if(isSpaceChar())
        restart();
    else if(isEofChar())
        token(EOF);
    else
        error();
}

private void s_intval()
{
    if(isDigitChar())
        state(this::s_intval);
    else if(isIdChar())
        error();
    else
        token(INTVAL);
}

} // DFA
