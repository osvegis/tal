/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import static tal.Token.Type.*;

/**
 * Syntactic analyser implemented by a Descending-Recursive Analyser.
 * <p>The language grammar must be implemented in this class.
 */
public class DRA extends ASyn
{
/**
 * Build a Descending-Recursive Analyser.
 * @param lex Lexical analyser
 */
public DRA(ALex lex)
{
    super(lex);
}

/**
 * Initial symbol of grammar.
 */
public void program()
{
    declaration();
    block();
    tokenRead(EOF);
}

private void declaration()
{
    switch(tokenType())
    {
        case INTEGER:
            tokenRead(INTEGER);
            tokenRead(ID);
            declaration();
            break;

        case STRING:
            tokenRead(STRING);
            tokenRead(ID);
            declaration();
            break;
    }
}

private void block()
{
}

} // ADR
