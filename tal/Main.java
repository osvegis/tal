/*
 * Released under the MIT License.
 * Copyright 2016 Oscar Vega-Gisbert.
 */
package tal;

import java.io.*;

/**
 * Programa principal del compilador.
 */
public class Main
{
public static void main(String args[]) throws IOException
{
    if(args.length != 1)
    {
        System.out.println("Parámetros:  nombre_fichero");
        return;
    }

    getTokens(args[0]);
    compile(args[0]);
}

private static void getTokens(String file) throws IOException
{
    AFD afd = new AFD(file);
    Token t;

    while((t = afd.read()).type != Token.Type.EOF)
    {
        System.out.printf("%2d %8s  %s\n", t.type.ordinal(),
                          t.type, t.name);
    }

    afd.close();
}

private static void compile(String file) throws IOException
{
    AFD afd = new AFD(file);
    ADR adr = new ADR(afd);
    adr.programa();
    adr.close();

    System.out.println("\nCódigo ejecutable:\n");
    System.out.println(adr.codeGet());
    System.out.println("\nEjecución:\n");
    adr.codeRun();
}

} // Main
