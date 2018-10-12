/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;

/**
 * Main program of the compiler.
 */
public class Main
{
public static void main(String args[]) throws IOException
{
    if(args.length != 1)
    {
        System.out.println("Parameters:  filename");
        return;
    }

    readTokens(args[0]);
    //compile(args[0]);
}

private static void readTokens(String filename) throws IOException
{
    DFA afd = new DFA(filename);
    Token t;

    while((t = afd.read()).type != Token.Type.EOF)
    {
        System.out.printf("%2d %7s  %s\n", t.type.ordinal(),
                          t.type, t.name);
    }

    afd.close();
}

private static void compile(String filename) throws IOException
{
    DFA afd = new DFA(filename);
    DRA adr = new DRA(afd);
    adr.program();
    adr.close();

    System.out.println("\nExecutable code:\n");
    System.out.println(adr.codeGet());
    System.out.println("\nExecution:\n");
    adr.codeRun();
}

} // Main
