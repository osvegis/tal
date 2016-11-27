/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;
import static tal.Token.Type.*;

/**
 * Autómata Finito Determinista.
 */
public class AFD extends ALex
{
/**
 * Construye el autómata.
 * @param file Fichero de texto que se debe analizar.
 * @throws IOException
 */
public AFD(String file) throws IOException
{
    super(file);
    setStart(this::s_inicio);
}

private void s_inicio()
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

} // AFD
