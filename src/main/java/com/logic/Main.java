package com.logic;

import com.logic.input.PrefixArgs;
import com.logic.output.Console;
import com.logic.output.TruthTable;
import com.logic.parser.PrattParser;

/**
 * Main application class for the Propositional Parser.
 * This application allows users to input propositional logic expressions,
 * generate truth tables, and minimize expressions using the Quine-McCluskey algorithm.
 */
public class Main {
    /**
     * Entry point for the Propositional Parser application.
     * Provides an interactive command-line interface for parsing and evaluating
     * propositional logic expressions.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Console.println("┃ PROPOSITIONAL PARSER ┃ ENTER AN EXPRESSION OR ? FOR HELP ┃");
        while (true) {
            String input = Console.readLine(":: ");
            if (input.trim().isEmpty()) break;
            PrefixArgs prefix = new PrefixArgs(input);
            if (prefix.shouldShowHelp()) {
                Console.println("""
                        USAGE: (prefix) (expression)
                        PREFIXES: (can also be affixed) \t  ┃ OPERATORS:
                        \t?: show this message              ┃     NOT: ~
                        \t$: minify expression (QMC)        ┃     AND: &
                        \t                                  ┃      OR: |
                        \t                                  ┃ IMPLIES: >
                        \t                                  ┃  EQUALS: =
                        VARIABLES: a-z (case insensitive, will lowercase)
                        LITERALS: 0 (false), 1 (true)
                        ENTER EMPTY LINE TO EXIT""");
                continue;
            }
            input = prefix.getUnPrefixedInput();
            try {
                if (prefix.shouldSimplify()) {
                    input = new PrattParser(input).parseReduced().toString();
                    Console.println("MINIMIZED: " + input);
                }
                Console.println(new TruthTable(input).generate());
            } catch (Exception e) {
                Console.error(e.getMessage());
            }
        }
    }
}
