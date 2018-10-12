/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

/**
 * Basic Elements of Language
 */
public class Token
{
/** Possible types of tokens. */
public static enum Type
{
    /** Reserved word {@code integer}. */
    INTEGER,
    /** Reserved word {@code string}. */
    STRING,
    /** Reserved word {@code if}. */
    IF,
    /** Reserved word {@code else}. */
    ELSE,
    /** Reserved word {@code while}. */
    WHILE,
    /** Reserved word {@code end}. */
    END,
    /** Reserved word {@code print}. */
    PRINT,
    /** Identifiers (names of variables). */
    ID,
    /** Integer number. */
    INTVAL,
    /** Character string between double quotes. */
    STRVAL,
    /** Assignment operator: {@code =} */
    ASIGN,
    /** Operators: {@code +}, {@code -} */
    SUM,
    /** Operators: {@code *}, {@code /} */
    MUL,
    /** Relational operators.<br>
     *  {@code ==}, {@code !=},
     *  {@code <},  {@code <=},
     *  {@code >},  {@code >=} */
    REL,
    /** Operator: {@code !} */
    NEG,
    /** Operator: {@code ||} */
    OR,
    /** Operator: {@code &&} */
    AND,
    /** Left parenthesis: {@code (} */
    LPAR,
    /** Right parenthesis: {@code )} */
    RPAR,
    /** End of file. */
    EOF
}

/** Token type. */
public final Type type;
/** Token name. */
public final String name;
/** Row where the token is in the analyzed file. */
public final int row;
/** Column where the token is in the analyzed file. */
public final int column;

/**
 * Token constructor.
 * @param type   Token type
 * @param name   Token name
 * @param row    Row where the token is in the analyzed file
 * @param column Column where the token is in the analyzed file
 */
public Token(Type type, String name, int row, int column)
{
    this.type   = type;
    this.name   = name;
    this.row    = row;
    this.column = column;
}

/**
 * Returns a string representation of the token.
 * @return a string representation of the token
 */
@Override public String toString()
{
    StringBuilder sb = new StringBuilder();
    sb.append("Token ");
    sb.append(type);
    sb.append(": ");
    sb.append(name);
    return sb.toString();
}

} // Token
